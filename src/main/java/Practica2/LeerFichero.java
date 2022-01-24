package Practica2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.Math.rint;
import static java.lang.Math.sqrt;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author josed
 */
public class LeerFichero {

    ArrayList<float[]> datos;
    int[][] matrizCostes;
    ArrayList<Integer> ciudades;
    int i=1;

    public LeerFichero() {
        this.datos = new ArrayList<>();
        this.ciudades = new ArrayList<>();
    }

    public void leerfichero(String rutafich) throws IOException {
        String cadena;
        FileReader fichero = new FileReader(rutafich);
        try (BufferedReader bufferFichero = new BufferedReader(fichero)) {
            bufferFichero.readLine();
            bufferFichero.readLine();
            bufferFichero.readLine();
            bufferFichero.readLine();
            bufferFichero.readLine();
            bufferFichero.readLine();
            while ((cadena = bufferFichero.readLine()) != null) {
                String[] filaString = cadena.split(" ");
                if (filaString.length > 1) {
                    float[] filanumero = new float[2];
                    filanumero[0] = Float.parseFloat(filaString[1]);
                    filanumero[1] = Float.parseFloat(filaString[2]);
                    datos.add(filanumero);
                    i++;
                } else {
                    break;
                }

            }
        }catch(Exception e){
            System.out.println("La lectura del fichero se ha quedado en la fila numero "+i);
        }
        
        for (Integer i = 1; i <= datos.size(); i++) {
            ciudades.add(i);
        }
        calcularMCostes();
    }

    public void calcularMCostes() {
        int n = datos.size();
        matrizCostes = new int[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrizCostes[i][j] = calcularDistancia(datos.get(i), datos.get(j));
            }
        }
    }

    private int calcularDistancia(float[] x, float[] y) {
        float xd = x[0] - y[0];
        float yd = x[1] - y[1];
        int dij = (int) Math.round(sqrt(xd * xd + yd * yd));
        return dij;
    }

    public ArrayList<float[]> getdatos() {
        return this.datos;
    }
    
    public ArrayList<Integer> getCiudades() {
        return this.ciudades;
    }
    
    public int[][] getMatrizCostes() {
        return this.matrizCostes;
    }

}


