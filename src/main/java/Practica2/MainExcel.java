package Practica2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author josed
 */
public class MainExcel {

     
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        GeneticoBasicoV2 gb ;
        CHC chc;
        MultimodalSecuencial ms;
        
        //ficheros
        ArrayList<String> ficheros = new ArrayList<>();
        ficheros.add("./st70.tsp");
        ficheros.add("./ch130.tsp");
        ficheros.add("./a280.tsp");
        
        //semillas
        Random aleatorio = new Random();
        int[] semillas = new int[5];
        for (int i = 0; i < 5; i++) {
            semillas[i] = aleatorio.nextInt(999999)+1;
        }
        
        File f = new File("Resultados.csv");
        FileWriter fWriter;
        try {
            fWriter = new FileWriter(f);
            
            //Genetico Basico
            System.out.println("Geneto Basico-----------");
            fWriter.write("GeneticoBasico;ST70;ST70;CH130;CH130;A280;A280;");
            fWriter.write("\n");
            fWriter.write("N Ejecucion;Semilla;Coste;Semilla;Coste;Semilla;Coste;");
            
            for (int j = 0; j < semillas.length; j++) {
                System.out.println(j+" Semilla: "+semillas[j]);
                fWriter.write("\n");
                fWriter.write(j+";");
                for (String fichero : ficheros) {
                    System.out.println("FICHERO: "+fichero);
                    fWriter.write(semillas[j]+";");
                    gb = new GeneticoBasicoV2(fichero, semillas[j]);
                    int coste = gb.algoritmoGB();
                    fWriter.write(coste+";");
                    
                }
                fWriter.flush();
            }
            System.out.println("FIN GREEDY***********");
            fWriter.write(";;;;;;;;;;;;;;;;;;");
            fWriter.write(";;;;;;;;;;;;;;;;;;");
            
            
            //CHC
            System.out.println("CHC-----------");
            fWriter.write("\n");
            fWriter.write("CHC;ST70;ST70;CH130;CH130;A280;A280;");
            fWriter.write("\n");
            fWriter.write("N Ejecucion;Semilla;Coste;Semilla;Coste;Semilla;Coste;");
            
            for (int j = 0; j < semillas.length; j++) {
                System.out.println(j+" Semilla: "+semillas[j]);
                fWriter.write("\n");
                fWriter.write(j+";");
                for (String fichero : ficheros) {
                    System.out.println("FICHERO: "+fichero);
                    fWriter.write(semillas[j]+";");
                    chc = new CHC(fichero, semillas[j]);
                    int coste = chc.algoritmoCHC();
                    fWriter.write(coste+";");
                    
                }
                fWriter.flush();
            }
            System.out.println("FIN CHC***********");
            fWriter.write(";;;;;;;;;;;;;;;;;;");
            fWriter.write(";;;;;;;;;;;;;;;;;;");
            
            //Multimodal
            System.out.println("Multimodal-----------");
            fWriter.write("\n");
            fWriter.write("Multimodal;ST70;ST70;CH130;CH130;A280;A280;");
            fWriter.write("\n");
            fWriter.write("N Ejecucion;Semilla;Coste;Semilla;Coste;Semilla;Coste;");
            
            for (int j = 0; j < semillas.length; j++) {
                System.out.println(j+" Semilla: "+semillas[j]);
                fWriter.write("\n");
                fWriter.write(j+";");
                for (String fichero : ficheros) {
                    System.out.println("FICHERO: "+fichero);
                    fWriter.write(semillas[j]+";");
                    ms = new MultimodalSecuencial(fichero, semillas[j]);
                    ArrayList<Integer> costes = ms.multimodal();
                    int min = Integer.MAX_VALUE;
                    int mejorCoste = -1;
                    for (Integer coste : costes) {
                        if (min>coste) {
                            mejorCoste = coste;
                        }
                    }
                    fWriter.write(mejorCoste+";");
                    
                }
                fWriter.flush();
            }
            System.out.println("Multimodal***********");
            fWriter.write(";;;;;;;;;;;;;;;;;;");
            fWriter.write(";;;;;;;;;;;;;;;;;;");
            
            

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

}
