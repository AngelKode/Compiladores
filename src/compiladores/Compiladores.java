/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiladores;

import clases_de_analisis.AnalizadorArchivos;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JFileChooser;

/**
 *
 * @author depot
 */
public class Compiladores {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        // TODO code application logic here

        AnalizadorArchivos analizador = new AnalizadorArchivos(new File("C:\\Users\\depot\\Desktop\\Codigo.txt"));
        analizador.regla_Programa();
    }
    
}
