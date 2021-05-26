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
    
    private static final String[] first = null;
    private ArrayList<String> variablesInicializadas;
    //Objetos para checar identificadores
    private final Pattern patron;
    

    public AnalizadorArchivos(File archivo) throws FileNotFoundException {
        this.archivo_analizar = archivo;
        this.lector = new FileReader(this.archivo_analizar);
        this.buffer = new BufferedReader(lector);
        this.valorCaracterActual = Caracter.ESPACIO_BLANCO.getValue();
        this.valorCaracterAnterior = Caracter.ESPACIO_BLANCO.getValue();
        this.variablesInicializadas = new ArrayList<>();
        this.estado_lector = false;//falso si no se ha empezado a leer, y verdadero en caso contrario
        this.patron = Pattern.compile("^(int$)|(float$)|(char$)|(double$)|(string$)|(cte$)|(bool$)|(print$)|"
                                      +"(true$)|(false$)|(if$)|(else$)|(for$)");//expresion regular para identificadores
    }
    
    public char getCaracterActual() throws IOException{
        return (char)this.buffer.read(); 
    }
    
    public void imprimirSiguiente() throws IOException{
        System.out.println(getCaracterActual()+"");
    }
    
    public void cerrarBuffer() throws IOException{
        this.buffer.close();
    }
    
     public String obtenerCadena() throws IOException{
        
        if(this.valorCaracterAnterior == Caracter.FIN_DOCUMENTO.getValue()){
            return "Fin del archivo";
        }
             
        this.valorCaracterActual = 0;//Para obtener el caracter actual
        String palabra_retorno = "";//Aqui irá la palabra, numero o identificador obtenido
        
        if(!this.estado_lector){//Si aun no se habia empezado a leer el archivo
           this.valorCaracterAnterior = this.buffer.read();
           //Nos posicionamos en la primer letra por ser la primer lectura
           if(this.valorCaracterAnterior == Caracter.ESPACIO_BLANCO.getValue())
           {
              while((this.valorCaracterAnterior = this.buffer.read()) != Caracter.ESPACIO_BLANCO.getValue())
              {
                  
              }
           }
        }
    
        //Agregamos la letra que ya se leyó
        palabra_retorno += (char) this.valorCaracterAnterior;
        
        //Checamos en donde entra el primer caracter
        if(Caracter.LETRA_MAY.isInRange(this.valorCaracterAnterior) || 
           Caracter.LETRA_MIN.isInRange(this.valorCaracterAnterior)){
            
            //Aqui verificamos si es un valor de variable valido
            palabra_retorno = obtenerVariableValido((char) this.valorCaracterAnterior);
            
            //Checamos si es identificador o no
            Matcher matcher = this.patron.matcher(palabra_retorno);
            if(matcher.find()){
                System.out.print("Hay un identificador: ");
                if(this.valorCaracterActual != Caracter.FIN_DOCUMENTO.getValue() &&
                   this.valorCaracterActual == Caracter.ESPACIO_BLANCO.getValue()){
                    //Checamos si la palabra es un else
                    if(palabra_retorno.equals("else")){
                        this.buffer.mark(1000);//Marcamos la posicion del buffer
                        this.valorCaracterActual = this.buffer.read();//Leemos el siguiente caracter
                        String aux = obtenerVariableValido((char) this.valorCaracterActual);//Y obtenemos la cadena que se forma

                        //Si la cadena que se formó es un if
                        if(aux.equals("if")){
                            palabra_retorno += " " + aux;
                        }else{
                            //Si no es un if, reseteamos a la posicion en donde empezamos a leer
                            this.buffer.reset();
                        }
                        
                    }
                }
            }else{
                System.out.print("No hay identificador: ");
                //Agregamos la variable ya que no es un identificador
                this.variablesInicializadas.add(palabra_retorno);
            }
          
            //Actualizamos el valor de el caracter anterior
            this.valorCaracterAnterior = this.valorCaracterActual;
            
        }else if(Caracter.NUMERO.isInRange(this.valorCaracterAnterior)){
           //Aqui verificamos que sea un numero valido
           palabra_retorno =  obtenerNumeroValido((char)this.valorCaracterAnterior);
           this.valorCaracterAnterior = this.valorCaracterActual;
            
        }else if(Caracter.SIMBOLOS_PARENTESIS.isInRange(this.valorCaracterAnterior)){
            //Si son parentesis, unicamente se manda, y el que sigue
            palabra_retorno = (char) this.valorCaracterAnterior + "";
            this.valorCaracterAnterior = this.buffer.read();
            
        }else if(Caracter.SIMBOLO_ABRIR.getValue() == this.valorCaracterAnterior ||
                 Caracter.SIMBOLO_CERRAR.getValue() == this.valorCaracterAnterior){
            //Si son simbolos de corchetes, unicamente se mandan
            palabra_retorno = (char) this.valorCaracterAnterior + "";
            this.valorCaracterAnterior = this.buffer.read();
            
        }else if(Caracter.PUNTO_COMA.getValue() == this.valorCaracterAnterior){
            palabra_retorno = (char) this.valorCaracterAnterior + "";
            this.valorCaracterAnterior = this.buffer.read();
            
        }else if(Caracter.IGUAL.getValue() == this.valorCaracterAnterior){
            //Checamos si el siguiente es el mismo
            palabra_retorno = (char) this.valorCaracterAnterior + "";
            this.valorCaracterAnterior = this.buffer.read();
            
            if(Caracter.IGUAL.getValue() == this.valorCaracterAnterior){
                palabra_retorno += (char) this.valorCaracterAnterior + "";
                this.valorCaracterAnterior = this.buffer.read();
            }
        }else if(Caracter.EXCLAMACION.getValue() == this.valorCaracterAnterior){
            palabra_retorno = (char) this.valorCaracterAnterior + "";
            this.valorCaracterAnterior = this.buffer.read();
            
            if(Caracter.IGUAL.getValue() == this.valorCaracterAnterior){
                palabra_retorno += (char) this.valorCaracterAnterior + "";
                this.valorCaracterAnterior = this.buffer.read();
            }
        }else if(Caracter.MENOR_QUE.getValue() == this.valorCaracterAnterior){
            palabra_retorno = (char) this.valorCaracterAnterior + "";
            this.valorCaracterAnterior = this.buffer.read();
            
            if(Caracter.IGUAL.getValue() == this.valorCaracterAnterior){
                palabra_retorno += (char) this.valorCaracterAnterior + "";
                this.valorCaracterAnterior = this.buffer.read();
            }
            
        }else if(Caracter.MAYOR_QUE.getValue() == this.valorCaracterAnterior){
            palabra_retorno = (char) this.valorCaracterAnterior + "";
            this.valorCaracterAnterior = this.buffer.read();
            
            if(Caracter.IGUAL.getValue() == this.valorCaracterAnterior){
                palabra_retorno += (char) this.valorCaracterAnterior + "";
                this.valorCaracterAnterior = this.buffer.read();
            }
            
        }else if(Caracter.AMPERSON.getValue() == this.valorCaracterAnterior){
            palabra_retorno = (char) this.valorCaracterAnterior + "";
            this.valorCaracterAnterior = this.buffer.read();
            
            if(Caracter.AMPERSON.getValue() == this.valorCaracterAnterior){
                palabra_retorno += (char) this.valorCaracterAnterior + "";
                this.valorCaracterAnterior = this.buffer.read();
            }
            
        }else if(Caracter.LINEA_OR.getValue() == this.valorCaracterAnterior){
            palabra_retorno = (char) this.valorCaracterAnterior + "";
            this.valorCaracterAnterior = this.buffer.read();
            
            if(Caracter.LINEA_OR.getValue() == this.valorCaracterAnterior){
                palabra_retorno += (char) this.valorCaracterAnterior + "";
                this.valorCaracterAnterior = this.buffer.read();
            }
            
        }
        //Si son simbolos de retorno de carro, saltos de línea o espacios, se eliminan, no se guardan
        else if(Caracter.RETORNO_DE_CARRO.getValue() == this.valorCaracterAnterior){
            this.valorCaracterAnterior = this.buffer.read();
            palabra_retorno = obtenerCadena();
            
        }else if(Caracter.SALTO_LINEA.getValue() == this.valorCaracterAnterior){
            this.valorCaracterAnterior = this.buffer.read();
            palabra_retorno = obtenerCadena();
            
        }
        else if(Caracter.ESPACIO_BLANCO.getValue() == this.valorCaracterAnterior){
            this.valorCaracterAnterior = this.buffer.read();
            palabra_retorno = obtenerCadena();
            
        }else if(this.valorCaracterAnterior == Caracter.COMILLAS.getValue()){
            //Cuando se encuentra con comillas, lee todo el contenido para regresarlo junto
            this.cadena_activada = true;
            
                
            while(this.cadena_activada){
                this.valorCaracterAnterior = this.buffer.read();
                palabra_retorno += (char) this.valorCaracterAnterior + "";
                if(this.valorCaracterAnterior == Caracter.COMILLAS.getValue()){
                    this.valorCaracterAnterior = this.buffer.read();
                    this.cadena_activada = false;
                    
                }
            }
            
        }else{
            //Aqui entra si no es válida la primer letra para formar algo válido
            this.valorCaracterAnterior = this.buffer.read();
            if(this.valorCaracterAnterior != Caracter.ESPACIO_BLANCO.getValue()){
                palabra_retorno += (char) this.valorCaracterAnterior;
                palabra_retorno = leerPalabraRestante(palabra_retorno);//Leemos lo restante, ya que no es válido
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
       
        this.valorCaracterAnterior = this.buffer.read();
        
        while(this.valorCaracterAnterior != Caracter.ESPACIO_BLANCO.getValue() &&
              this.valorCaracterAnterior != Caracter.FIN_DOCUMENTO.getValue() &&
              this.valorCaracterActual != Caracter.SALTO_LINEA.getValue() &&
              this.valorCaracterActual != Caracter.RETORNO_DE_CARRO.getValue()){
           palabraRestante += (char) this.valorCaracterAnterior;
           this.valorCaracterAnterior = this.buffer.read();
        }
            
        return palabraRestante;
    }
    private String obtenerNumeroValido(char primerDigito) throws IOException{
        String numeroValido = primerDigito + "";//Aqui guardamos el string resultante
        
        //Aqui guardamos el caracter anterior,
        //esto en caso de que ya hayamos leido y tener el caracter
        //que se leyó en algún lado
        int caracterAnterior = this.buffer.read();
        
        //Comparamos los primeros digitos
        while(Caracter.NUMERO.isInRange(caracterAnterior)){
           numeroValido += (char) caracterAnterior;//agregamos el numero
           caracterAnterior = this.buffer.read();
        }
        
        //Si el caracter ya no es un numero, checamos si es un punto decimal o no
        if(caracterAnterior == Caracter.SIMBOLO_PUNTO.getValue()){
            numeroValido += (char)Caracter.SIMBOLO_PUNTO.getValue();
            caracterAnterior = this.buffer.read();//Leemos el siguiente caracter
            //Si es punto decimal, checamos los numeros del lado derecho
            while(Caracter.NUMERO.isInRange(caracterAnterior)){
                numeroValido += (char) caracterAnterior;//agregamos el numero
                caracterAnterior = this.buffer.read();
            }
            //Asignamos al valor del caracter actual donde se quedó el buffer
            this.valorCaracterActual = caracterAnterior;
        }else{
            //Ya no seria un numero valido asi que regresamos lo que llevamos de numero
            this.valorCaracterActual = caracterAnterior;//Avanzamos al siguiente numero
            return numeroValido;
        }
        
        return numeroValido;
    }

    private String obtenerVariableValido(char caracter) throws IOException {
        String variableValida = "";
        int caracterAnterior = this.buffer.read();
       
        //Checamos los caracteres
        if(Caracter.LETRA_MAY.isInRange((int) caracter)){
            variableValida += caracter;
            //Ciclo que obtiene lo que es una cadena valida
            while(caracterAnterior != Caracter.ESPACIO_BLANCO.getValue() || 
                caracterAnterior !=  Caracter.FIN_DOCUMENTO.getValue()){
               
                //Checamos si es minuscula, mayuscula o linea _
                if(Caracter.LETRA_MAY.isInRange(caracterAnterior) ||
                    Caracter.LETRA_MIN.isInRange(caracterAnterior) ||
                    Caracter.SIMBOLO_LINEA_BAJA.getValue() == caracterAnterior){
                    variableValida += (char)caracterAnterior;//agregamos el caracter
                    caracterAnterior = this.buffer.read();//avanzamos al siguiente
                }else{
                    //No seria valido, asi que finalizamos el ciclo
                    this.valorCaracterActual = caracterAnterior;
                    return variableValida;
                }
               
            }
        }else if(Caracter.LETRA_MIN.isInRange((int) caracter)){
            variableValida += caracter;
            
            //Ciclo que obtiene lo que es una cadena valida
            while(caracterAnterior != Caracter.ESPACIO_BLANCO.getValue() || 
                caracterAnterior !=  Caracter.FIN_DOCUMENTO.getValue()){
                //Checamos si es minuscula, mayuscula o linea _
                if(Caracter.LETRA_MAY.isInRange(caracterAnterior) ||
                    Caracter.LETRA_MIN.isInRange(caracterAnterior) ||
                    Caracter.SIMBOLO_LINEA_BAJA.getValue() == caracterAnterior){
                    variableValida += (char)caracterAnterior;//agregamos el caracter
                    caracterAnterior = this.buffer.read();//avanzamos al siguiente
                }else{
                    //No seria valido, asi que finalizamos el ciclo
                    this.valorCaracterActual = caracterAnterior;
                    return variableValida;
                }
            }
        }else{
           //Si no cumple ninguna, no formaria un nombre valido
           this.valorCaracterActual = caracterAnterior; //Asignamos al valor del caracter actual donde se quedó el buffer
           return variableValida;
        }
        return variableValida;
    }
    
    public String isCadenaValida(String cadena){
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
            }
            
            if(!Caracter.LETRA_MAY.isInRange((int) caracter) && 
               !Caracter.LETRA_MIN.isInRange((int) caracter) &&
                Caracter.SIMBOLO_LINEA_BAJA.getValue() != caracter){
                return false;
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
    
    private void regla_Programa() throws IOException{
        //Siguiente regla del bloque del programa
        regla_Bloque();
        //Obtenemos la siguiente cadena
        String palabra = obtenerCadena();
        //Comparamos ya que es un no terminal
        if(palabra.equals("")){
            System.out.println("Programa compilado exitósamente");
        }else{
            getError(CompilerError.ERROR_BLOQUE.getValue());
        }
    }
    
    private void regla_Bloque() throws IOException{
        //Ponemos las funciones para cada bloque
        regla_AUXCTE();
        regla_AUXVAR();
        regla_Process();
        regla_Proposicion();
        //Sin condiciones ya que no es terminal
    }
    
    private void regla_AUXCTE() throws IOException{
        //Obtenemos la siguiente palabra
        String palabra = obtenerCadena();
                
        //Checamos los first
        if(!palabra.equals("cte")){
            getError(CompilerError.ERROR_AUXCONST.getValue());
        }else{
           //Obtenemos el nombre del identificador
            String identificador = obtenerCadena();

            if(isCadena(identificador)){
                String signoIgual = obtenerCadena();

                if(signoIgual.equals("=")){
                    //Checamos que el siguiente sea un numero
                    String numero = obtenerCadena();
                    if(isNumero(numero)){
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
    
    private void regla_AUX1() throws IOException{
        //Obtenemos y tiene que ser un ; o una ,
        String signo = obtenerCadena();
        
        if(signo.equals(",")){
            //Obtenemos el nombre del identificador
            String identificador = obtenerCadena();

            if(isNombreVariable(identificador)){
                String signoIgual = obtenerCadena();

                if(signoIgual.equals("=")){
                    //Checamos que el siguiente sea un numero
                    String numero = obtenerCadena();
                    if(isNumero(numero)){
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
        }else if(!signo.equals(";")){
            //Si es diferente a ';' está mal
            getError(CompilerError.ERROR_FINAL_CTE.getValue());
        }
    }
    
    
    private void regla_AUXVAR() throws IOException{
        //Obtenemos la siguiente palabra
        String palabra = obtenerCadena();

        if(!palabra.equals("var")){
            getError(CompilerError.ERROR_AUXVAR.getValue());
        }else{
            //Obtenemos el nombre del identificador
            String identificador = obtenerCadena();
            
            if(isNombreVariable(identificador)){
                regla_AUXVAR2();
            }else{
                getError(CompilerError.ERROR_IDENTIFICADOR.getValue());
            }
        }
    }
    
    private void regla_AUXVAR2() throws IOException{
        //Checamos que sea , o ;
        String signo = obtenerCadena();
        
        if(signo.equals(",")){
            //Obtenemos el nombre del identificador
            String identificador = obtenerCadena();
            
            if(isNombreVariable(identificador)){
                regla_AUXVAR2();
            }else{
                getError(CompilerError.ERROR_IDENTIFICADOR.getValue());
            }
        }else if(!signo.equals(";")){
            getError(CompilerError.ERROR_FINAL_CTE.getValue());
        }
    }
    
    private void regla_Process() throws IOException{
        //Obtenemos la siguiente palabra
        String palabra = obtenerCadena();
        
        if(palabra.equals("process")){
            //Ahora checamos el =
            String igual = obtenerCadena();
            if(igual.equals("=")){
                //Ahora checamos el identificador
                String identificador = obtenerCadena();
                if(isNombreVariable(identificador)){
                    //Ahora checamos la apertura del proceso
                    String llaveInicio = obtenerCadena();
                    if(llaveInicio.equals("{")){
                        regla_Bloque();
                        //Ahora obtenemos la llave de cierre
                        String llaveCierre = obtenerCadena();
                        if(!llaveCierre.equals("}")){
                            getError(CompilerError.ERROR_FIN_PROCESS.getValue());
                        }else{
                           regla_Process();
                        }
                    }else{
                       getError(CompilerError.ERROR_INICIO_PROCESS.getValue());
                    }
                }else{
                    getError(CompilerError.ERROR_IDENTIFICADOR.getValue());
                }
            }else{
                getError(CompilerError.ERROR_SIGNO_IGUAL.getValue());
            }
        }else{
            getError(CompilerError.ERROR_PROCESS.getValue());
        }
    }
    
    private void regla_Proposicion() throws IOException{
        //Obtenemos la palabra reservada
        String palabra_reservada = obtenerCadena();
        
        //Dependiendo la palabra reservada, tomamos un camino
        switch(palabra_reservada){
            case "read":{
                regla_Read();
                break;
            }
            case "print":{
                regla_Imprimir();
                break;
            }
            case "for":{
                regla_CicloFor();
                break;
            }
            case "if":{
                regla_if();
                break;
            }
            default:{
                //Si no es ninguna, checamos que sea una variable inicializada
                boolean isVariable = false;
                for(String variable : this.variablesInicializadas){
                    if(variable.equals(palabra_reservada)){
                        isVariable = true;
                        regla_Asignacion();
                    }
                }
                
                //Checamos si era variable o no
                if(!isVariable){
                    getError(1);
                }
                break;
            }
        }
    }
    
    private void regla_Asignacion() throws IOException{
        //Obtenemos la siguiente palabra
        String variableInicial = obtenerCadena();
        boolean isDeclared = false;
        //Verificamos que la variable ya haya sido declarada
        for(String variable : this.variablesInicializadas){
            if(variable.equals(variableInicial)){
                isDeclared = true;
                break;
            }
        }
        
        //Checamos si está declarada o no
        if(isDeclared){
            regla_CaminoIdent();
        }else{
            getError(CompilerError.ERROR_VARIABLE_NO_INICIALIZADA.getValue());
        }
    }
    
    private void regla_CaminoIdent() throws IOException{
        //Checamos que camino sigue
        String signoIgual = obtenerCadena();
        
        //Y verificamos que signo sigue
        if(signoIgual.equals("=")){
            regla_Expresion();
        }else{
            regla_Expresion();
        }
    }
    
    private void regla_Read(){
        //Obtenemos la siguiente palabra
        String variableLeer = "";
        try{
            variableLeer = obtenerCadena();
        }catch(IOException e){}
        boolean isVariable = false;
        //Verificamos que esté dentro de las variables inicializadas
        for(String variable : this.variablesInicializadas){
            //Es válido si ya habia sido declarada anteriormente
            if(variable.equals(variableLeer)){
                isVariable = true;
                break;
            }
        }
        //Validamos que sea una variable ya declarada
        if(!isVariable){
            getError(CompilerError.ERROR_IDENTIFICADOR.getValue());
        }
    }
    
    private void regla_Imprimir(){
        //Obtenemos lo que se quiere imprimir
        String palabraAImprimir = "";
        try {
            palabraAImprimir = obtenerCadena();
        } catch (IOException e) {}
        
        //Checamos si es una variable inicializada
        boolean isVariable = false;
        for(String variable : this.variablesInicializadas){
            if(variable.equals(palabraAImprimir)){
                isVariable = true;
                break;
            }
        }
        
        //Si no fue variable, checamos que sea numero o cadena
        if(!isVariable){
            if(!(isNumero(palabraAImprimir) || isCadena(palabraAImprimir))){
                getError(1);
            }
        }
    }
    
    private void regla_CicloFor(){
            //Evaluamos que tenga el parentesis
            String parentesis_inicio = "";
            try {
                parentesis_inicio = obtenerCadena();
            } catch (IOException e) {}
            
            if(parentesis_inicio.equals("(")){
                //Ahora checamos que la siguiente palabra sea el identificador
                String identificador = "";
                try {
                    identificador = obtenerCadena();
                } catch (IOException e) {}
                boolean isVariable = false;
                for(String variable : this.variablesInicializadas){
                    if(variable.equals(identificador)){
                        isVariable = true;
                        break;
                    }
                }
                //Evaluamos que sea un identificador
                if(!isVariable){
                    //Verificamos que la siguiente palabra sea '='
                    String signoIgual = "";
                    try {
                        signoIgual = obtenerCadena();
                    } catch (IOException e) {}
                    
                    if(signoIgual.equals("=")){
                        //Ahora checamos que lo siguiente sea una expresion
                        try {
                            regla_Expresion();
                        } catch (IOException e) {}
                        //Y obtenemos lo siguiente, que sería ';'
                        String finInicializacion = "";
                        try {
                            finInicializacion = obtenerCadena();
                        } catch (IOException e) {}
                        
                        if(finInicializacion.equals(";")){
                            //Checamos que sea correcta la condicion
                            try {
                                regla_Condicion();
                            } catch (IOException e) {}
                             //Y obtenemos lo siguiente, que sería ';'
                            finInicializacion = "";
                            try {
                                finInicializacion = obtenerCadena();
                            } catch (IOException e) {}
                            
                            if(finInicializacion.equals(";")){
                                //Obtenemos la variable
                                String variableControl = "";
                                try {
                                    variableControl =  obtenerCadena();
                                } catch (IOException e) {}
                                isVariable = false;
                                for(String variable : this.variablesInicializadas){
                                    if(variable.equals(variableControl)){
                                        isVariable = true;
                                        break;
                                    }
                                }
                                //Verificamos que sea una variable inicializada
                                if(!isVariable){
                                    //Por ultimo, checamos como van a ser los incrementos/decrementos
                                    try {
                                        regla_CaminoFor();
                                    } catch (IOException e) {}
                                    //Obtenemos el parentesis de cierre
                                    String parentesisCierre = "";
                                    try {
                                        parentesisCierre = obtenerCadena();
                                    } catch (IOException e) {}
                                    
                                    if(parentesisCierre.equals(")")){
                                        //Obtenemos la llave de apertura
                                        String llaveApertura = "";
                                        try {
                                            llaveApertura = obtenerCadena();
                                        } catch (IOException e) {}
                                        
                                        if(llaveApertura.equals("{")){
                                            //Seguimos con la regla que cumplirá el ciclo
                                            try {
                                                regla_Proposicion();   
                                            } catch (IOException e) {}
                                            //Al final, checamos que se haya cerrado el ciclo
                                            String llaveCierre = "";
                                            try {
                                                llaveCierre = obtenerCadena();
                                            } catch (IOException e) {}
                                            
                                            if(!llaveCierre.equals("}")){
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
            }else{
                getError(1);
            }
    }
    
    private void regla_CaminoFor() throws IOException{
        //Obtenemos como va a actuar la variable de control
        String comportamiento = obtenerCadena();
        
        if(!(comportamiento.equals("SUMAR") || comportamiento.equals("RESTAR"))){
            getError(1);
        }
    }
    
    private void regla_if(){
            //Checamos el parentesis de apertura
            String parentesisApertura = "";
            try {
                parentesisApertura = obtenerCadena();
            } catch (IOException e) {}
            
            if(parentesisApertura.equals("(")){
                //Checamos la condicion que tiene que cumplir
                try {
                    regla_Condicion();
                } catch (IOException e) {}
                
                //Obtenemos el parentesis de cierre
                String parentesisCierre = "";
                try {
                    parentesisCierre = obtenerCadena();
                } catch (IOException e) {}
                
                if(parentesisCierre.equals(")")){
                    //Checamos la llave de apertura
                    String llaveApertura = "";
                    try {
                        llaveApertura = obtenerCadena();
                    } catch (IOException e) {}
                    
                    if(llaveApertura.equals("{")){
                        //Evaluamos la regla de proposion adentro de la condicion
                        try {
                            regla_Proposicion();
                        } catch (IOException e) {}
                        
                        //Verificamos que tenga llave de cierre
                        String llaveCierre = "";
                        try {
                            llaveCierre = obtenerCadena();
                        } catch (IOException e) {}
                        
                        if(llaveCierre.equals("}")){
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
    }
    
    private void regla_Condicion()throws IOException{
        //Primero checamos la expresion
        regla_Expresion();
        
        //Checamos que lo siguiente esté dentro de los simbolos de comparacion
        String operador = obtenerCadena();
        boolean isOperador = false;
        
        for(String opAux : AnalizadorArchivos.operadoresComparacion){
            if(opAux.equals(operador)){
                isOperador = true;
                break;
            }
        }
        
        if(isOperador){
            //Volvemos a checar la expresion
            regla_Expresion();
            
            //Si el siguiente simbolo es && o ||, nos regresamos, si no, continuamos
            String siguiente = obtenerCadena();
            
            if(siguiente.equals("&&") || siguiente.equals("&&")){
                regla_Condicion();
            }
        }else{
            getError(1);
        }
        
    }
    
    private void regla_CaminoIf() throws IOException{
        //Verificamos cual de los 2 caminos tomó
        String palabra = obtenerCadena();
        if(palabra.equals("else")){
            //Checamos si es el ultimo condicional
            String siguientePalabraReservada = "";
            try{
                siguientePalabraReservada = obtenerCadena();
            }catch(IOException e){}
           
            if(siguientePalabraReservada.equals("if")){
                //Checamos el parentesis de apertura
                String parentesisApertura = "";
                try {
                    parentesisApertura = obtenerCadena();
                } catch (IOException e) {}

                if(parentesisApertura.equals("(")){
                    //Checamos la condicion que tiene que cumplir
                    regla_CondicionParentesis();

                    //Obtenemos el parentesis de cierre
                    String parentesisCierre = "";
                    try {
                        parentesisCierre = obtenerCadena();
                    } catch (IOException e) {}

                    if(parentesisCierre.equals(")")){
                        //Checamos la llave de apertura
                        String llaveApertura = "";
                        try {
                            llaveApertura = obtenerCadena();
                        } catch (IOException e) {}

                        if(llaveApertura.equals("{")){
                            //Evaluamos la regla de proposion adentro de la condicion
                            try {
                                regla_Proposicion();
                            } catch (IOException e) {}

                            //Verificamos que tenga llave de cierre
                            String llaveCierre = "";
                            try {
                                llaveCierre = obtenerCadena();
                            } catch (IOException e) {}

                            if(llaveCierre.equals("}")){
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
            }else if(siguientePalabraReservada.equals("{")){
                //Evaluamos la regla de proposion adentro de la condicion
                try {
                    regla_Proposicion();
                } catch (IOException e) {}
                        
                //Verificamos que tenga llave de cierre
                String llaveCierre = "";
                try {
                    llaveCierre = obtenerCadena();
                } catch (IOException e) {}
                        
                if(llaveCierre.equals("}")){
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
            //Checamos la llave de apertura
            String llaveApertura = "";
            try {
                llaveApertura = obtenerCadena();
            } catch (IOException e) {}
        }else{
            getError(1);
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
        regla_Identificador();
        regla_Numero();
        regla_Cadena();
        regla_CrearExpresion();
    }
    
    private void regla_Identificador() throws IOException{
        //Obtenemos la siguiente palabra
        String palabra = obtenerCadena();
        boolean isVariable = false;
        
        //Verificamos que esté dentro de las variables inicializadas
        for(String variable : this.variablesInicializadas){
            if(variable.equals(palabra)){
               isVariable = true;
            }
        }
    }
    
    private void regla_Numero() throws IOException{
        //Obtenemos la siguiente palabra
        String palabra = obtenerCadena();
        
        //Verificamos que sea un numero
        if(!isNumero(palabra)){
           getError(1); 
        }
    }
    
    private void regla_Cadena() throws IOException{
       //No se hace nada ya que ya verificamos anteriormente si era una variable valida
       //Solo verificamos que sea una cadena
       //Obtenemos la siguiente palabra
        String palabra = obtenerCadena();
        
        //Verificamos que sea un numero
        if(!isCadena(palabra)){
           getError(1); 
        }
    }

    private void regla_CrearExpresion() throws IOException{
        //Primero checamos la apertura del parentesis
        String apertura = obtenerCadena();
        
        if(apertura.equals("(")){
            regla_Expresion();
            //Despues de la expresion, checamos que haya cerrado
            String cierre = obtenerCadena();
            
            if(!cierre.equals(")")){
                getError(1);
            }
        }else{
            getError(CompilerError.ERROR_APERTURA_EXPRESION.getValue());
        }
    }
 
    private void regla_Aux4() throws IOException{
        //Checamos que sea un operador
        String operador = obtenerCadena();
        boolean isOperador = false;
        
        for(String op : AnalizadorArchivos.operadoresOperacion){
            if(op.equals(operador)){
                isOperador = true;
                break;
            }
        }
        
        if(isOperador){
            //Nos volvemos a regresar
            regla_Expresion();
        }
    }
    
    private void getError(int errorType){
        switch(errorType){
            case 1:{
                throw new Error("Se esperaba un .");
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
            default:{
                break;
            }
        }
    }

    
    
}
