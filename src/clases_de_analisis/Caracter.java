/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clases_de_analisis;

/**
 *
 * @author depot
 * Clase que nos ayuda a tener mejor visualizacion sobre el estado del caracter leido
 */
public enum Caracter{
    
    FIN_DOCUMENTO(-1),
    SALTO_LINEA(10),
    RETORNO_DE_CARRO(13),
    ESPACIO_BLANCO(32),
    EXCLAMACION(33),
    AMPERSON(38),
    SIMBOLOS_PARENTESIS(40,41),
    SIMBOLO_PUNTO(46),
    NUMERO(48,57),
    PUNTO_COMA(59),
    MENOR_QUE(60),
    IGUAL(61),
    MAYOR_QUE(62),
    LETRA_MAY(65,90),
    LETRA_MIN(97,122),
    SIMBOLO_ABRIR(123),
    LINEA_OR(124),
    SIMBOLO_LINEA_BAJA(95),
    SIMBOLO_CERRAR(125),
    COMILLAS(34),
    COMA(44);
    
    private int valor_ascii;
    private int valor_inicio_ascii;
    private int valor_final_ascii;
    
    private Caracter(int valor){
        this.valor_ascii = valor;
    }
    
    private Caracter(int rango_inicio, int rango_final){
        this.valor_inicio_ascii = rango_inicio;
        this.valor_final_ascii = rango_final;
    }
    
    public boolean isInRange(int valor_comparar){
        return (valor_comparar >= this.valor_inicio_ascii) && (valor_comparar <= this.valor_final_ascii);
    }
    
    public int getValue(){
        return this.valor_ascii;
    }
    
    public void setValue(int valor){
        this.valor_ascii = valor;
    }
    
}
