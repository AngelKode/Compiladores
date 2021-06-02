/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clases_de_analisis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.*;

/**
 *
 * @author depot
 */
public class AnalizadorArchivos {
    private final File archivo_analizar;
    private final FileReader lector;
    private final BufferedReader buffer;
    private int valorCaracterActual;
    private int valorCaracterAnterior;
    private boolean estado_lector;
    private boolean cadena_activada;
    private static final String[] simbolos = new String[]{"cte","float","int","string","char","bool","read",
                                                          "true","false","+","-","/","*","==","!=","<",">",
                                                          "<=",">=","&&","||","=",";",",","if","else if","else",
                                                          "for","print","(",")","{","}"};
//    private static final char[] letras = new char[]{'a','b','c','d','e','f','g','h','i','j','k','l','m','n','ñ','o', 
//                                                    'p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E',
//                                                    'F','G','H','I','J','K','L','M','N','Ñ','O','P','Q','R','S','T', 
//                                                    'U','V','W','X','Y','Z'};
//    private static final char[] numeros = new char[]{'0','1','2','3','4','5','6','7','8','9'};
    private static final String[] operadoresComparacion = new String[]{"==","!=",">",">=","<=","<"};
    private static final String[] operadoresOperacion = new String[]{"+","-","*","/"};
    private static final String[] follows_AUXCTE = new String[]{"var","process" , "read" ,"print" ,"for" , "if"};
    private static final String[] follows_AUXVAR = new String[]{"var","process" , "read" , "print" , "for" ,"if"};
    private static final String[] follows_Process = new String[]{"read" , "print" , "for" ,"if"};
    private static final String[] follows_AUX4 = new String[]{"*","+","/","-"};
    
    private static final String[] first = null;
    private String variableError;
    private String token_actual, dataTypeActual, dataTypeAnterior, token_anterior;
    private boolean addVariable, isDone, canRead;
    private ArrayList<String> variablesInicializadas;
    //Objetos para checar identificadores
    private final Pattern patron;
    

    public AnalizadorArchivos(File archivo) throws FileNotFoundException, IOException {
        this.archivo_analizar = archivo;
        this.lector = new FileReader(this.archivo_analizar);
        this.buffer = new BufferedReader(lector);
            this.buffer.mark(50);
            this.buffer.read();
        this.valorCaracterActual = Caracter.ESPACIO_BLANCO.getValue();
        this.valorCaracterAnterior = Caracter.ESPACIO_BLANCO.getValue();
        this.variablesInicializadas = new ArrayList<>();
        this.estado_lector = false;//falso si no se ha empezado a leer, y verdadero en caso contrario
        this.patron = Pattern.compile("^(int$)|(float$)|(char$)|(double$)|(string$)|(cte$)|(bool$)|(print$)|"
                                      +"(true$)|(false$)|(if$)|(else$)|(for$)");//expresion regular para identificadores
        this.token_actual = "";
        this.token_anterior = "";
        this.addVariable = false;
        this.isDone = false;
        this.canRead = true;
        this.dataTypeAnterior = "";
        this.dataTypeActual = "";
    }
    
    private char getCaracterActual() throws IOException{
        return (char)this.buffer.read(); 
    }
    
    private void imprimirSiguiente() throws IOException{
        System.out.println(getCaracterActual()+"");
    }
    
    private void cerrarBuffer() throws IOException{
        this.buffer.close();
    }
   
    public String obtenerCadena() throws IOException{
        
        //Marcamos la posicion actual
        this.buffer.reset();
        //Leemos el siguiente caracter
        this.valorCaracterActual = this.buffer.read();
        if((this.valorCaracterActual) == Caracter.FIN_DOCUMENTO.getValue()){
            this.isDone = true;
            return "";
        }
        //Guardamos la posicion actual
        this.buffer.mark(50);
        String palabra_retorno = "";//Aqui irá la palabra, numero o identificador obtenido
        
        if(!this.estado_lector){//Si aun no se habia empezado a leer el archivo
           //Nos posicionamos en la primer letra por ser la primer lectura
           if(this.valorCaracterActual == Caracter.ESPACIO_BLANCO.getValue()){
              while((this.valorCaracterActual = this.buffer.read()) == Caracter.ESPACIO_BLANCO.getValue()){
                  this.buffer.mark(50);
              }
           }
        }
        
        //Agregamos el caracter a la palabra
        palabra_retorno += (char) this.valorCaracterActual;
       
        //Checamos en donde entra el primer caracter
        if(Caracter.LETRA_MAY.isInRange(this.valorCaracterActual) || 
           Caracter.LETRA_MIN.isInRange(this.valorCaracterActual)){
            
            //Aqui verificamos si es un valor de variable valido
            palabra_retorno = obtenerVariableValido((char) this.valorCaracterActual);
            
            //Checamos si es identificador o no
            Matcher matcher = this.patron.matcher(palabra_retorno);
            if(matcher.find()){
                if(this.valorCaracterActual != Caracter.FIN_DOCUMENTO.getValue() &&
                   this.valorCaracterActual != Caracter.ESPACIO_BLANCO.getValue()){
                    if(palabra_retorno.equals("cte") || palabra_retorno.equals("var")){
                        this.addVariable = true;
                        if(this.dataTypeAnterior.equals(palabra_retorno) || this.dataTypeAnterior.equals("")){
                            this.dataTypeAnterior = palabra_retorno;
                        }else{
                            this.addVariable = false;
                        }
                    }
                }
            }else{
                if(this.addVariable){
                    this.variablesInicializadas.add(palabra_retorno);
                }
            }      
        }else if(Caracter.NUMERO.isInRange(this.valorCaracterActual)){
           //Aqui verificamos que sea un numero valido
           palabra_retorno =  obtenerNumeroValido((char)this.valorCaracterActual);
        }else if(Caracter.SIMBOLOS_PARENTESIS.isInRange(this.valorCaracterActual)){
            //Si son parentesis, unicamente se manda, y el que sigue
            palabra_retorno = (char) this.valorCaracterActual + "";
        }else if(Caracter.SIMBOLO_ABRIR.getValue() == this.valorCaracterActual ||
                 Caracter.SIMBOLO_CERRAR.getValue() == this.valorCaracterActual){
            //Si son simbolos de corchetes, unicamente se mandan
            palabra_retorno = (char) this.valorCaracterActual + "";
            this.buffer.mark(50);
            this.buffer.read();
        }else if(Caracter.PUNTO_COMA.getValue() == this.valorCaracterActual){
            palabra_retorno = (char) this.valorCaracterActual + ""; 
        }else if(Caracter.COMA.getValue() == this.valorCaracterActual){
            palabra_retorno = (char) this.valorCaracterActual + "";
        }else if(Caracter.IGUAL.getValue() == this.valorCaracterActual){
            //Checamos si el siguiente es el mismo
            palabra_retorno = (char) this.valorCaracterActual + "";
            this.buffer.mark(50);
            this.valorCaracterActual = this.buffer.read();
            
            if(Caracter.IGUAL.getValue() == this.valorCaracterActual){
                palabra_retorno += (char) this.valorCaracterActual + "";
                this.buffer.mark(50);
            }
        }else if(Caracter.EXCLAMACION.getValue() == this.valorCaracterActual){
            palabra_retorno = (char) this.valorCaracterActual + "";
            this.buffer.mark(50);
            this.valorCaracterActual = this.buffer.read();
            
            if(Caracter.IGUAL.getValue() == this.valorCaracterActual){
                palabra_retorno += (char) this.valorCaracterActual + "";
                this.buffer.mark(50);
            }      
        }else if(Caracter.MENOR_QUE.getValue() == this.valorCaracterActual){
            palabra_retorno = (char) this.valorCaracterActual + "";
            this.buffer.mark(50);
            this.valorCaracterActual = this.buffer.read();
            
            if(Caracter.IGUAL.getValue() == this.valorCaracterActual){
                palabra_retorno += (char) this.valorCaracterActual + "";
                this.buffer.mark(50);
            }      
        }else if(Caracter.MAYOR_QUE.getValue() == this.valorCaracterActual){
            palabra_retorno = (char) this.valorCaracterActual + "";
            this.buffer.mark(50);
            this.valorCaracterActual = this.buffer.read();
            
            if(Caracter.IGUAL.getValue() == this.valorCaracterActual){
                palabra_retorno += (char) this.valorCaracterActual + "";
                this.buffer.mark(50);
            }           
        }else if(Caracter.AMPERSON.getValue() == this.valorCaracterActual){
            palabra_retorno = (char) this.valorCaracterActual + "";
            this.buffer.mark(50);
            this.valorCaracterActual = this.buffer.read();
            
            if(Caracter.AMPERSON.getValue() == this.valorCaracterActual){
                palabra_retorno += (char) this.valorCaracterActual + "";
                this.buffer.mark(50);
            }      
        }else if(Caracter.LINEA_OR.getValue() == this.valorCaracterActual){
            palabra_retorno = (char) this.valorCaracterActual + "";
            this.buffer.mark(50);
            this.valorCaracterActual = this.buffer.read();
                  
            if(Caracter.LINEA_OR.getValue() == this.valorCaracterActual){
                palabra_retorno += (char) this.valorCaracterActual + "";
                this.buffer.mark(50);
            }
            
        }
        //Si son simbolos de retorno de carro, saltos de línea o espacios, se eliminan, no se guardan
        else if(Caracter.RETORNO_DE_CARRO.getValue() == this.valorCaracterActual){
            palabra_retorno = "";
            this.buffer.read();
            this.buffer.mark(50);
            palabra_retorno = obtenerCadena();
        }else if(Caracter.SALTO_LINEA.getValue() == this.valorCaracterActual){
            palabra_retorno = "";
            this.buffer.read();
            this.buffer.mark(50);
            palabra_retorno = obtenerCadena(); 
        }
        else if(Caracter.ESPACIO_BLANCO.getValue() == this.valorCaracterActual){
            palabra_retorno = "";
            //Nos movemos hasta una posicion antes de los espacios que haya
            while((this.valorCaracterActual = this.buffer.read()) == Caracter.ESPACIO_BLANCO.getValue()){
                this.buffer.mark(50);
            }
            palabra_retorno = obtenerCadena();
        }else if(this.valorCaracterActual == Caracter.COMILLAS.getValue()){
            //Cuando se encuentra con comillas, lee todo el contenido para regresarlo junto
            this.cadena_activada = true;
    
            while(this.cadena_activada){
                this.buffer.mark(50);
                this.valorCaracterActual = this.buffer.read();
                palabra_retorno += (char) this.valorCaracterActual + "";
                if(this.valorCaracterActual == Caracter.COMILLAS.getValue()){
                    this.valorCaracterActual = this.buffer.read();
                    this.buffer.mark(50);
                    this.cadena_activada = false;   
                }
            }
            
        }else{
            //Aqui entra si no es válida la primer letra para formar algo válido
            this.buffer.mark(50);
            this.valorCaracterActual = this.buffer.read();
            if(this.valorCaracterActual != Caracter.ESPACIO_BLANCO.getValue()){
                palabra_retorno += (char) this.valorCaracterActual;
                palabra_retorno = leerPalabraRestante(palabra_retorno);//Leemos lo restante, ya que no es válido
                this.buffer.mark(50);
            }
        }
        
        //Checamos si ya acabamos de leer todo el documento
        if(this.valorCaracterActual == Caracter.FIN_DOCUMENTO.getValue()){
            this.buffer.close();//Cerramos el flujo de datos
        }
            
        this.estado_lector = true;
        
        return palabra_retorno;
    }
    
    private String leerPalabraRestante(String inicio) throws IOException{
        String palabraRestante = "";
        palabraRestante += inicio;
       
        this.buffer.mark(50);
        this.valorCaracterActual = this.buffer.read();
        
        while(this.valorCaracterActual != Caracter.ESPACIO_BLANCO.getValue() &&
              this.valorCaracterActual != Caracter.FIN_DOCUMENTO.getValue() &&
              this.valorCaracterActual != Caracter.SALTO_LINEA.getValue() &&
              this.valorCaracterActual != Caracter.RETORNO_DE_CARRO.getValue()){
           palabraRestante += (char) this.valorCaracterActual;
           this.valorCaracterActual = this.buffer.read();
           this.buffer.mark(50);
        }
            
        return palabraRestante;
    }
    private String obtenerNumeroValido(char primerDigito) throws IOException{
        String numeroValido = "";
        int caracterSiguiente = (int) primerDigito;
        
        //Comparamos los primeros digitos
        if(Caracter.NUMERO.isInRange(caracterSiguiente)){
            numeroValido += (char) caracterSiguiente;
            this.buffer.mark(50);
            caracterSiguiente = this.buffer.read();
            while(Caracter.NUMERO.isInRange(caracterSiguiente)){
                numeroValido += (char) caracterSiguiente;//agregamos el numero
                this.buffer.mark(50);
                caracterSiguiente = this.buffer.read();
            }
        }
        
        //Si el caracter ya no es un numero, checamos si es un punto decimal o no
        if(caracterSiguiente == Caracter.SIMBOLO_PUNTO.getValue()){
            numeroValido += (char)Caracter.SIMBOLO_PUNTO.getValue();
            this.buffer.mark(50);
            caracterSiguiente = (char)this.buffer.read();//Leemos el siguiente caracter
            //Si es punto decimal, checamos los numeros del lado derecho
            while(Caracter.NUMERO.isInRange(caracterSiguiente)){
                numeroValido += (char) caracterSiguiente;//agregamos el numero
                this.buffer.mark(50);
                caracterSiguiente = this.buffer.read();
            }
            //Asignamos al valor del caracter actual donde se quedó el buffer
            //this.valorCaracterActual = caracterAnterior;
        }else{
            //Ya no seria un numero valido asi que regresamos lo que llevamos de numero
            return numeroValido;
        }
        
        return numeroValido;
    }

    private String obtenerVariableValido(char caracter) throws IOException {
        String variableValida = "";
        int caracterSiguiente = 0;
       
        //Checamos los caracteres
        if(Caracter.LETRA_MAY.isInRange((int) caracter) || Caracter.LETRA_MIN.isInRange((int) caracter)){
            variableValida += caracter;
            //Ciclo que obtiene lo que es una cadena valida
            this.buffer.mark(50);//marcamos la posicion
            caracterSiguiente = this.buffer.read();//leemos la siguiente letra
            while(caracterSiguiente != Caracter.ESPACIO_BLANCO.getValue() || 
                caracterSiguiente !=  Caracter.FIN_DOCUMENTO.getValue()){
                
                //Checamos si es minuscula, mayuscula o linea _
                if(Caracter.LETRA_MAY.isInRange(caracterSiguiente) ||
                    Caracter.LETRA_MIN.isInRange(caracterSiguiente) ||
                    Caracter.SIMBOLO_LINEA_BAJA.getValue() == caracterSiguiente){
                    variableValida += (char)caracterSiguiente;//agregamos el caracter
                    this.buffer.mark(50);//marcamos la posicion
                    caracterSiguiente = this.buffer.read();//avanzamos al siguiente
                }else{
                    //No seria valido, asi que finalizamos el ciclo
                    return variableValida;
                }
               
            }
        }else{
           //Si no cumple ninguna, no formaria un nombre valido
           return variableValida;
        }
        return variableValida;
    }
    
    private String isCadenaValida(String cadena){
        if(isSimbol(cadena) || isNumero(cadena) || isNombreVariable(cadena) || isCadena(cadena)){
            return "Es válido";
        }
        return "No es válido";
    }
    
    private boolean isCadena(String cadena){
        //Primero vemos si al inicio y al final hay comillas
        if(cadena.charAt(0) != Caracter.COMILLAS.getValue() || 
           cadena.charAt(cadena.length() - 1) != Caracter.COMILLAS.getValue()){
            return false;
        }else{
           for(int posicionInicial = 1; posicionInicial <= cadena.length() - 2;posicionInicial++){
               if(cadena.charAt(posicionInicial) == Caracter.COMILLAS.getValue()){
                   return false;
               }
           } 
        }
        
        return true;
    }
    private boolean isNombreVariable(String cadena){
        String aux = cadena;
        char caracter = cadena.charAt(0);
        
        for(int posicionCaracter = 0; posicionCaracter < cadena.length(); posicionCaracter++){
            if(posicionCaracter == 0){//El primer caracter tiene que ser letra, si no cumple, es un nombre de variable no valido
                int valor = (int) caracter;
                if(!Caracter.LETRA_MAY.isInRange((int) caracter) && 
                   !Caracter.LETRA_MIN.isInRange((int) caracter)){
                    return false;
                }
            }else{
                if(!Caracter.LETRA_MAY.isInRange((int) caracter) && 
                   !Caracter.LETRA_MIN.isInRange((int) caracter) &&
                    Caracter.SIMBOLO_LINEA_BAJA.getValue() != caracter){
                    return false;
                }
            }     
        }
        
        return true;
    }
    
    private boolean isNumero(String cadena){
          
        String aux = cadena;
        char caracter = ' ';
        int posicion = 0;
        
        for(int i=0;i<cadena.length();i++){
            caracter = cadena.charAt(i);
            posicion = i;
            if(caracter == '.'){
                break;
            }else if(!Caracter.NUMERO.isInRange((int) caracter)){
                return false;
            }
        }
        
        //Evaluamos que todos sean numeros, si no, no es un numero valido
        for(int i=posicion + 1;i<cadena.length();i++){
            caracter = cadena.charAt(i);
                
            if(!Caracter.NUMERO.isInRange((int) caracter)){
                return false;
            }
            
        }
        
        return true;
    }
    private boolean isSimbol(String cadena){
        
        for (String simbolo : AnalizadorArchivos.simbolos) {
            if(cadena.equals(simbolo)){
                return true;
            }
        }
            
        return false;
    }
    
    public void regla_Programa() throws IOException{
        //Siguiente regla del bloque del programa
        regla_Bloque();
        //Leemos el siguiente token
        this.token_actual = obtenerCadena();
        //Comparamos ya que es un no terminal
        if(this.token_actual.equals("")){
            System.out.println("Programa compilado exitósamente");
        }else{
            getError(CompilerError.ERROR_BLOQUE.getValue());
        }
    }
    
    private void regla_Bloque() throws IOException{
        //Obtenemos la siguiente palabra
        this.token_actual = obtenerCadena();
        
        if(!this.token_actual.equals("")){
            //Ponemos las funciones para cada bloque
            regla_AUXCTE();
            regla_AUXVAR();
            regla_Process();
            regla_Proposicion();
            //Sin condiciones ya que no es terminal
        }
    }
    
    private void regla_AUXCTE() throws IOException{
        //Verificamos si hay llamada recursiva y ya terminamos
        if(!this.isDone){
            //Verificamos que el primer token sea distinto a 'cte'
            if(!this.token_actual.equals("cte")){
                //Verificamos los follows
                boolean isFollow = false;
                int posicionSiguienteRegla = 0;
                for(String follows : AnalizadorArchivos.follows_AUXCTE){
                    if(follows.equals(this.token_actual)){
                        //Encontramos un follow
                        isFollow = true;
                        break;
                    }
                    posicionSiguienteRegla++;
                }
                //Checamos si es follow
                if(isFollow){
                   switch(posicionSiguienteRegla){
                       case 0:{
                           regla_AUXVAR();
                           break;
                       }
                       case 1:{
                           regla_Process();
                           break;
                       }
                       default:{
                           regla_Proposicion();
                           break;
                       }
                   }
                }else{
                    //Terminamos ya que ninguno es
                    this.isDone = true;
                }
            }else{
                
                //Obtenemos el nombre de la variable
                this.token_actual = obtenerCadena();
                
                if(isNombreVariable(this.token_actual)){
                    //Obtenemos el simbolo de asignacion
                    this.token_actual = obtenerCadena();

                    if(this.token_actual.equals("=")){
                        //Checamos que el siguiente sea un numero
                        this.token_actual = obtenerCadena();
                        if(isNumero(this.token_actual)){
                            this.token_actual = obtenerCadena();
                            regla_AUX1();
                        }else{
                            getError(CompilerError.ERROR_NUMEROCTE.getValue());
                        }
                    }else{
                        getError(CompilerError.ERROR_SIGNO_IGUAL.getValue());
                    }
                }else{
                    getError(CompilerError.ERROR_IDENTIFICADOR.getValue());
                } 
            }
        }
    }
    
    private void regla_AUX1() throws IOException{
        //Tiene que ser un ; o una , el token 
        if(this.token_actual.equals(",")){
            //Obtenemos el nombre del identificador
            this.token_actual = obtenerCadena();

            if(isNombreVariable(this.token_actual)){
                this.token_actual = obtenerCadena();

                if(this.token_actual.equals("=")){
                    //Checamos que el siguiente sea un numero
                    this.token_actual = obtenerCadena();
                    if(isNumero(this.token_actual)){
                        //obtenemos el siguiente token
                        this.token_actual = obtenerCadena();
                        regla_AUX1();
                    }else{
                        getError(CompilerError.ERROR_NUMEROCTE.getValue());
                    }
                }else{
                    getError(CompilerError.ERROR_SIGNO_IGUAL.getValue());
                }
            }else{
                getError(CompilerError.ERROR_IDENTIFICADOR.getValue());
            } 
        }else if(!this.token_actual.equals(";")){
            //Si es diferente a ';' está mal
            getError(CompilerError.ERROR_FINAL_CTE.getValue());
        }else{
            regla_Bloque();
        }
    }
    
    
    private void regla_AUXVAR() throws IOException{
        //Checamos la llamada recursiva
        if(!this.isDone){
            if(!this.token_actual.equals("var")){
                //Checamos si la palabra entra dentro de los follows
                boolean isFollow = false;
                int posicionSiguienteRegla = 0;
                for(String follows : AnalizadorArchivos.follows_AUXVAR){
                    if(follows.equals(this.token_actual)){
                        //Encontramos un follow
                        isFollow = true;
                        break;
                    }
                    posicionSiguienteRegla++;
                }
                
                //Checamos si es follow o no
                if(isFollow){
                    switch(posicionSiguienteRegla){
                       case 0:{
                           regla_Process();
                           break;
                       }
                       default:{
                           regla_Proposicion();
                           break;
                       }
                   }
                }else{
                    this.isDone = true;
                }
            }else{
                //Obtenemos el nombre del identificador
                this.token_actual = obtenerCadena();

                if(isNombreVariable(this.token_actual)){
                    regla_AUXVAR2();
                }else{
                    getError(CompilerError.ERROR_IDENTIFICADOR.getValue());
                }
            }
        }
    }
    
    private void regla_AUXVAR2() throws IOException{
        //Checamos que sea , o ;
        this.token_actual = obtenerCadena();
        
        if(this.token_actual.equals(",")){
            //Obtenemos el nombre del identificador
            this.token_actual = obtenerCadena();
            
            if(isNombreVariable(this.token_actual)){
                regla_AUXVAR2();
            }else{
                getError(CompilerError.ERROR_IDENTIFICADOR.getValue());
            }
        }else if(!this.token_actual.equals(";")){
            getError(CompilerError.ERROR_FINAL_CTE.getValue());
        }else{
            regla_Bloque();
        }
    }
    
    private void regla_Process() throws IOException{
        //Verificamos la llamada recursiva
        if(!this.isDone){
            if(this.token_actual.equals("process")){
                //Obtenemos el nombre del proceso
                this.token_actual = obtenerCadena();
                if(isNombreVariable(this.token_actual)){
                    //Ahora checamos el =
                    this.token_actual = obtenerCadena();
                    if(this.token_actual.equals("=")){
                            this.token_actual = obtenerCadena();
                            if(this.token_actual.equals("{")){
                                regla_Bloque();
                                //Ahora obtenemos la llave de cierre
                                this.token_actual = obtenerCadena();
                                if(!this.token_actual.equals("}")){
                                    getError(CompilerError.ERROR_FIN_PROCESS.getValue());
                                }else{
                                   //Leemos el siguiente token
                                   this.token_actual = obtenerCadena();
                                   regla_Bloque();
                                }
                            }else{
                               getError(CompilerError.ERROR_INICIO_PROCESS.getValue());
                            }
                    }else{
                        getError(CompilerError.ERROR_SIGNO_IGUAL.getValue());
                    }
                }else{
                    getError(CompilerError.ERROR_IDENTIFICADOR.getValue());
                }
            }else{
                //Checamos si la palabra entra dentro de los follows
                boolean isFollow = false;
                for(String follows : AnalizadorArchivos.follows_Process){
                    if(follows.equals(this.token_actual)){
                        //Encontramos un follow
                        isFollow = true;
                        break;
                    }
                }

                if(isFollow){
                    regla_Proposicion();
                }else{
                    this.isDone = true;
                }
            }
        }
    }
    
    private void regla_Proposicion() throws IOException{
        //Checamos la llamada recursiva
        if(!this.isDone){
            //Dependiendo la palabra reservada, tomamos un camino
            switch(this.token_actual){
                //En cada caso, excepto el ultimo, obtenemos el siguiente token que va a ser evaluado
                case "read":{
                    this.token_actual = obtenerCadena();
                    regla_Read();
                    break;
                }
                case "print":{
                    this.token_actual = obtenerCadena();
                    regla_Imprimir();
                    break;
                }
                case "for":{
                    this.token_actual = obtenerCadena();
                    regla_CicloFor();
                    break;
                }
                case "if":{
                    this.token_actual = obtenerCadena();
                    regla_if();
                    break;
                }
                default:{
                    //Si no es ninguna, checamos que sea una variable inicializada
                    boolean isVariable = false;
                    for(String variable : this.variablesInicializadas){
                        if(variable.equals(this.token_actual)){
                            isVariable = true;
                        }
                    }

                    //Checamos si era variable o no
                    if(!isVariable){
                        if(!this.token_actual.equals("}")){
                            this.variableError = this.token_actual;
                            getError(CompilerError.ERROR_VARIABLE_NO_INICIALIZADA.getValue());
                        }
                    }else{
                        regla_Asignacion();
                    }
                    break;
                }
            }
        }
    }
    
    private void regla_Asignacion() throws IOException{
        regla_CaminoIdent();
    }
    
    private void regla_CaminoIdent() throws IOException{
        //Checamos que camino sigue
        this.token_actual = obtenerCadena();
        
        //Y verificamos que signo sigue
        if(this.token_actual.equals("=")){
            regla_Expresion();
            
            //Checamos que tenga un ;
            if(!this.token_actual.equals(";")){
                getError(CompilerError.ERROR_FIN_SENTENCIA.getValue());
            }
        }else{
            getError(1);
        }
    }
    
    private void regla_Read() throws IOException{
        boolean isVariable = false;
        //Verificamos que esté dentro de las variables inicializadas
        for(String variable : this.variablesInicializadas){
            //Es válido si ya habia sido declarada anteriormente
            if(variable.equals(this.token_actual)){
                isVariable = true;
                break;
            }
        }
        //Validamos que sea una variable ya declarada
        if(!isVariable){
            getError(CompilerError.ERROR_IDENTIFICADOR.getValue());
        }else{
            //Checamos el ;
            this.token_actual = obtenerCadena();
            if(!this.token_actual.equals(";")){
                getError(CompilerError.ERROR_FIN_SENTENCIA.getValue());
            }
        }
    }
    
    private void regla_Imprimir() throws IOException{
        //Checamos si es una variable inicializada
        boolean isVariable = false;
        for(String variable : this.variablesInicializadas){
            if(variable.equals(this.token_actual)){
                isVariable = true;
                break;
            }
        }
        
        //Si no fue variable, checamos que sea numero o cadena
        if(!isVariable){
            if(!(isNumero(this.token_actual) || isCadena(this.token_actual))){
                getError(1);
            }
        }else{
            //Checamos el ;
            this.token_actual = obtenerCadena();
            if(!this.token_actual.equals(";")){
                getError(CompilerError.ERROR_FIN_SENTENCIA.getValue());
            }
        }
    }
    
    private void regla_CicloFor() throws IOException{
        
            if(this.token_actual.equals("(")){
                //Ahora checamos que la siguiente palabra sea el identificador
                try {
                    this.token_actual = obtenerCadena();
                } catch (IOException e) {}
                boolean isVariable = false;
                for(String variable : this.variablesInicializadas){
                    if(variable.equals(this.token_actual)){
                        isVariable = true;
                        break;
                    }
                }
                //Evaluamos que sea un identificador
                if(isVariable){
                    //Verificamos que la siguiente palabra sea '='
                    try {
                        this.token_actual = obtenerCadena();
                    } catch (IOException e) {}
                    
                    if(this.token_actual.equals("=")){
                        //Ahora checamos que lo siguiente sea una expresion
                        try {
                            regla_Expresion();
                        } catch (IOException e) {}
                        //Y obtenemos lo siguiente, que sería ';'
                        
                        if(this.token_actual.equals(";")){
                            //Checamos que sea correcta la condicion
                            try {
                                regla_Condicion();
                            } catch (IOException e) {}
                             //Y obtenemos lo siguiente, que sería ';'

                            if(this.token_actual.equals(";")){
                                //Obtenemos la variable
                                try {
                                    this.token_actual =  obtenerCadena();
                                } catch (IOException e) {}
                                isVariable = false;
                                for(String variable : this.variablesInicializadas){
                                    if(variable.equals(this.token_actual)){
                                        isVariable = true;
                                        break;
                                    }
                                }
                                //Verificamos que sea una variable inicializada
                                if(isVariable){
                                    //Por ultimo, checamos como van a ser los incrementos/decrementos
                                    try {
                                        regla_CaminoFor();
                                    } catch (IOException e) {}
                                    //Obtenemos el parentesis de cierre
                                    try {
                                        this.token_actual = obtenerCadena();
                                    } catch (IOException e) {}
                                    
                                    if(this.token_actual.equals(")")){
                                        //Obtenemos la llave de apertura
                                        try {
                                            this.token_actual = obtenerCadena();
                                        } catch (IOException e) {}
                                        
                                        if(this.token_actual.equals("{")){
                                            //Seguimos con la regla que cumplirá el ciclo
                                            try {
                                                this.token_actual = obtenerCadena();
                                                regla_Proposicion();   
                                            } catch (IOException e) {}
                                            //Al final, checamos que se haya cerrado el ciclo
                                            try {
                                                this.token_actual = obtenerCadena();
                                            } catch (IOException e) {}
                                            
                                            //Aqui checamos si la ultima instruccion tenía ;
                                            if(!this.token_actual.equals("}")){
                                                 getError(CompilerError.ERROR_FIN_SENTENCIA.getValue());
                                            }
                                        }else{
                                            getError(1);
                                        }
                                    }else{
                                        getError(1);
                                    }
                                }else{
                                    getError(1);
                                }
                            }else{
                                getError(1);
                            }
                        }else{
                            getError(1);
                        }
                    }else{
                        getError(1);
                    }
                }else{
                    getError(1);
                }
            }else{
                getError(1);
            }
    }
    
    private void regla_CaminoFor() throws IOException{
        //Obtenemos como va a actuar la variable de control
        this.token_actual = obtenerCadena();
        
        if(!(this.token_actual.equals("SUMAR") || this.token_actual.equals("RESTAR"))){
            getError(1);
        }
    }
    
    private void regla_if() throws IOException{
            //Checamos el parentesis de apertura
        
            if(this.token_actual.equals("(")){
                //Checamos la condicion que tiene que cumplir
                try {
                    regla_Condicion();
                } catch (IOException e) {}
                
                //Obtenemos el parentesis de cierre
                if(this.token_actual.equals(")")){
                    //Checamos la llave de apertura
                    try {
                        this.token_actual = obtenerCadena();
                    } catch (IOException e) {}
                    
                    if(this.token_actual.equals("{")){
                        //Evaluamos la regla de proposion adentro de la condicion
                        //Obtenemos la siguiente palabra
                        this.token_actual = obtenerCadena();
                        try {
                            regla_Proposicion();
                        } catch (IOException e) {}
                        
                        //Verificamos que tenga llave de cierre
                        this.token_actual = obtenerCadena();
                        if(this.token_actual.equals("}")){
                            //Verificamos que camino toma
                            try {
                                regla_CaminoIf();
                            } catch (IOException e) {}                          
                        }else{
                            getError(CompilerError.ERROR_CIERRE_LLAVES.getValue());
                        }
                    }else{
                        getError(1);
                    }
                }else{
                    getError(1);
                }
            }else{
                getError(1);
            }
    }
    
    private void regla_Condicion()throws IOException{
        //Primero checamos la expresion
        regla_Expresion();//x
        
        //Checamos que lo siguiente esté dentro de los simbolos de comparacion
        boolean isOperador = false;
 
        for(String opAux : AnalizadorArchivos.operadoresComparacion){
            if(opAux.equals(this.token_actual)){
                isOperador = true;
                break;
            }
        }
        
        if(isOperador){
            //Volvemos a checar la expresion
            regla_Expresion();
            
            if(this.token_actual.equals("&&") || this.token_actual.equals("||")){
                regla_Condicion();
            }
        }else{
            getError(1);
        }
        
    }
    
    private void regla_CaminoIf() throws IOException{
        //Verificamos cual de los 2 caminos tomó
        this.token_actual = obtenerCadena();
        if(this.token_actual.equals("else")){
            //Checamos si es el ultimo condicional
            try{
                this.token_actual = obtenerCadena();
            }catch(IOException e){}

            if(this.token_actual.equals("if")){
                //Checamos el parentesis de apertura
                try {
                    this.token_actual = obtenerCadena();
                } catch (IOException e) {}

                if(this.token_actual.equals("(")){
                    //Checamos la condicion que tiene que cumplir
                    regla_CondicionParentesis();

                    //Obtenemos el parentesis de cierre
                    try {
                        this.token_actual = obtenerCadena();
                    } catch (IOException e) {}

                    if(this.token_actual.equals(")")){
                        //Checamos la llave de apertura
                        try {
                            this.token_actual = obtenerCadena();
                        } catch (IOException e) {}

                        if(this.token_actual.equals("{")){
                            //Evaluamos la regla de proposion adentro de la condicion
                            try {
                                regla_Proposicion();
                            } catch (IOException e) {}

                            //Verificamos que tenga llave de cierre
                            try {
                                this.token_actual = obtenerCadena();
                            } catch (IOException e) {}

                            if(this.token_actual.equals("}")){
                                //Verificamos que camino toma
                                try {
                                    regla_CaminoIf();
                                } catch (IOException e) {}                          
                            }else{
                                getError(1);
                            }
                        }else{
                            getError(1);
                        }
                    }else{
                        getError(1);
                    }
                }else{
                    getError(1);
                }
            }else if(this.token_actual.equals("{")){
                //Evaluamos la regla de proposion adentro de la condicion
                try {
                    regla_Proposicion();
                } catch (IOException e) {}
                        
                //Verificamos que tenga llave de cierre
                try {
                    this.token_actual = obtenerCadena();
                } catch (IOException e) {}
                        
                if(!this.token_actual.equals("}")){
                     getError(1);
                }
            }else{
                getError(1);
            }
            //Checamos la llave de apertura
            String llaveApertura = "";
            try {
                llaveApertura = obtenerCadena();
            } catch (IOException e) {}
        }
    }
    
    private void regla_CondicionParentesis() throws IOException{
        regla_Expresion();
    }
    
    private void regla_Expresion() throws IOException{
        regla_Contenido();
        regla_Aux4();
    }
    
    private void regla_Contenido() throws IOException{
        //Obtenemos la siguiente palabra
        this.token_actual = obtenerCadena();
        
        if(!regla_Identificador()){
            if(!regla_Numero()){
                if(!regla_Cadena()){
                    if(!regla_CrearExpresion()){
                        getError(CompilerError.ERROR_CONTENIDO.getValue());
                    }
                }
            }
        }   
    }
    
    private boolean regla_Identificador() throws IOException{
        boolean isVariable = false;
        
        //Checamos que sea variable valida, ya que puede que sea un numero o una cadena o una expresion
        if(isNombreVariable(this.token_actual)){
            //Verificamos que esté dentro de las variables inicializadas
            for(String variable : this.variablesInicializadas){
                if(variable.equals(this.token_actual)){
                   isVariable = true;
                }
            }
            
            if(!isVariable){
                getError(CompilerError.ERROR_VARIABLE_NO_INICIALIZADA.getValue());
                System.exit(1);
            }
        }
        
        return true;
    }
    
    private boolean regla_Numero() throws IOException{
        //Verificamos que sea un numero
        if(isNumero(this.token_actual)){
           return true;
        }
        return false;
    }
    
    private boolean regla_Cadena() throws IOException{
       //No se hace nada ya que ya verificamos anteriormente si era una variable valida
       //Solo verificamos que sea una cadena
       //Obtenemos la siguiente palabra
        
        //Verificamos que sea un numero
        if(isCadena(this.token_actual)){
           return true; 
        }
        
        return false;
    }

    private boolean regla_CrearExpresion() throws IOException{
        //Primero checamos la apertura del parentesis
        
        if(this.token_actual.equals("(")){
            regla_Expresion();
            //Despues de la expresion, checamos que haya cerrado
            this.token_actual = obtenerCadena();
            
            if(!this.token_actual.equals(")")){
                getError(CompilerError.ERROR_CIERRE_EXPRESION.getValue());
                return false;
            }
        }else{
            getError(CompilerError.ERROR_APERTURA_EXPRESION.getValue());
            return false;
        }
        
        return true;
    }
 
    private void regla_Aux4() throws IOException{
        
        //Comparamos los follows
        boolean isFollow = false;
        this.token_actual = obtenerCadena();
        for(String follow : AnalizadorArchivos.follows_AUX4){
            if(follow.equals(this.token_actual)){
                isFollow = true;
                break;
            }
        }
        
        if(!isFollow){
            //Checamos que sea un operador
            boolean isOperador = false;

            for(String op : AnalizadorArchivos.operadoresOperacion){
                if(op.equals(this.token_actual)){
                    isOperador = true;
                    break;
                }
            }
            
            if(isOperador){
                //Nos volvemos a regresar
                regla_Expresion();
            }
        }
    }
    
    private void getError(int errorType){
        System.out.println(this.token_actual);
        switch(errorType){
            case 1:{
                throw new Error("No se esperaba ningún caracter");
            }
            case 2:{
                throw new Error("Se esperaba una variable o una palabra reservada");
            }
            case 3:{
                throw new Error("Se esperaba una variable o una palabra reservada");
            }
            case 4:{
                throw new Error("Se esperaba el siguiente identificador: process");
            }
            case 15:{
                throw new Error("La variable " + this.token_actual + " no está inicializada");
            }
            case 17:{
               throw new Error("Se esperaba la palabra reservada 'cte'"); 
            }
            case 19:{
               throw new Error("Condición inválida"); 
            }
            default:{
                break;
            }
        }
        //System.exit(1);
    }

    
    
}
