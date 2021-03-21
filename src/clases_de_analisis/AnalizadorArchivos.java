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
    //Objetos para checar identificadores
    private final Pattern patron;
    

    public AnalizadorArchivos(File archivo) throws FileNotFoundException {
        this.archivo_analizar = archivo;
        this.lector = new FileReader(this.archivo_analizar);
        this.buffer = new BufferedReader(lector);
        this.valorCaracterActual = Caracter.ESPACIO_BLANCO.getValue();
        this.valorCaracterAnterior = Caracter.ESPACIO_BLANCO.getValue();
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
            }
            
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
    
}
