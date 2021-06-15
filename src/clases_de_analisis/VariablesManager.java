/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clases_de_analisis;

import java.util.ArrayList;

/**
 *
 * @author Angel
 * Esta clase ayuda al manejo de las variables inicializadas, guardando su tipo de dato, su identificador y su valor
 */
public class VariablesManager {
    private ArrayList<String> dataType;
    private ArrayList<String> dataName;
    private ArrayList<Object> dataValue;

    public VariablesManager() {
        this.dataType = new ArrayList<>();
        this.dataName = new ArrayList<>();
        this.dataValue = new ArrayList<>();
    }

    public void addVariable(String dataType, String dataName, Object dataValue){//Agregamos una nueva variable
        this.dataType.add(dataType);
        this.dataName.add(dataName);
        this.dataValue.add(dataValue);
    }
    
    public ArrayList<String> getAllDataType(){//Obtenemos todos los tipos de datos que están inicializados
        return this.dataType;
    }
    
    public String getDataTypeAt(int posicion){//Obtenemos 1 tipo de dato en la posicion 'posicion'
        return this.dataType.get(posicion);
    }
    
    public ArrayList<String> getAllDataName(){//Obtenemos todos los nombres de las variables que están inicializados
        return this.dataName;
    }
    
    public String getDataNameAt(int posicion){//Obtenemos el nombre de la variable en la posicion 'posicion'
        return this.dataName.get(posicion);
    }
    
    public ArrayList<Object> getAllDataValue(){//Obtenemos todos los valores de las variables que están inicializados
        return this.dataValue;
    }
    
    public Object getDataValueAt(int posicion){//Obtenemos el valor de la variable en la posicion 'posicion'
        return this.dataValue.get(posicion);
    }
    
    public void setVariable(Object newDataValue, int posicion){//Actualizamos el valor de la variable
        this.dataValue.set(posicion, newDataValue);
    }
}
