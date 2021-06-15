/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clases_de_analisis;

/**
 *
 * @author depot
 */
public enum CompilerError{
    
    ERROR_BLOQUE(1),
    ERROR_AUXCONST(2),
    ERROR_AUXVAR(3),
    ERROR_PROCESS(4),
    ERROR_ASIGNACION(5),
    ERROR_SIGNO_IGUAL(6),
    ERROR_IDENTIFICADOR(7),
    ERROR_NUMEROCTE(8),
    ERROR_FINAL_CTE(9),
    ERROR_INICIO_PROCESS(11),
    ERROR_FIN_PROCESS(12),
    ERROR_APERTURA_EXPRESION(13),
    ERROR_CONTENIDO(19),
    ERROR_CIERRE_EXPRESION(14),
    ERROR_VARIABLE_NO_INICIALIZADA(15),
    ERROR_READ(16),
    ERROR_FIN_AUXCONST(17),
    ERROR_FIN_AUXVAR(18),
    ERROR_FIN_SENTENCIA(19),
    ERROR_CIERRE_LLAVES(20),
    ERROR_TIPO_DATO(21),
    ERROR_ASIGNACION_DATO(22),
    ERROR_CAMBIO_CTE(23),
    ERROR_CICLO_FOR(24);
           
    private int valorError;

    private CompilerError(int errorType) {
        this.valorError = errorType;
    }
    
    public int getValue(){
        return this.valorError;
    }
    
    public void setValue(int newErrorType){
        this.valorError = newErrorType;
    } 
    
}
