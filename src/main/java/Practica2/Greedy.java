/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Practica2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author josed
 */
public class Greedy {

    LeerFichero lf = new LeerFichero();
    ArrayList<Integer> ciudades;
    int[][] matrizCostes;
    int costeTotal;
    int evaluaciones;

    public Greedy(String ruta) {
        evaluaciones = 0;
        costeTotal = 0;
        try {
            lf.leerfichero(ruta);
            this.ciudades = lf.getCiudades();
            // System.out.println("Ciudades.size: "+this.ciudades.size());
            this.matrizCostes = lf.getMatrizCostes();
        } catch (IOException ex) {
            Logger.getLogger(Greedy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Greedy(ArrayList<Integer> solucionActual, String ruta) {
        evaluaciones = 0;
        costeTotal = 0;
        try {
            lf.leerfichero(ruta);
            this.matrizCostes = lf.getMatrizCostes();
        } catch (IOException ex) {
            Logger.getLogger(Greedy.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.ciudades = (ArrayList<Integer>) solucionActual.clone();
    }

    public ArrayList<Integer> greedy() {
        // System.out.println("Ciudades dentro: "+ this.ciudades.size());

        Integer ciuInicial = this.ciudades.get(0); //ciudad origen la primera (Integer en vez de int para tratarlo como objeto y no como número)
        ArrayList<Integer> solucionFinal = new ArrayList<>(); //creamos array solucion

        /*Primera actualización*/
        ArrayList<Integer> conjuntoNodos = ciudades; //obtenemos en principio todas las ciudades
        solucionFinal.add(ciuInicial); //añadimos la primera ciudad a la solucion
        conjuntoNodos.remove((Object) ciuInicial); //quitamos la primera ciudad del conjunto de nodos (quitamos ese objeto, no confundir con el índice que indicaría el número de la ciudad)
        int ultimaCiudad = ciuInicial;

        evaluaciones = 0;
        while (!conjuntoNodos.isEmpty()) { //mientras queden ciudades por coger...
            int siguienteCiudad = calcularCercana(ultimaCiudad, conjuntoNodos); //calculamos la siguiente ciudad y sumamos su coste
            ultimaCiudad = siguienteCiudad; //actualizar la última ciudad que se ha cogido
            solucionFinal.add(siguienteCiudad); //añadimos esa ciudad a la solucion
            conjuntoNodos.remove((Object) siguienteCiudad); //quitamos esa ciudad de las disponibles
            evaluaciones++;
        }

        solucionFinal.add(ciuInicial); //añadimos la primera para cerrar el ciclo
        int ciuFinal = solucionFinal.get(solucionFinal.size() - 1);
        costeTotal += matrizCostes[ciuInicial - 1][ciuFinal]; //y añadimos el coste de esa ultima
        return solucionFinal;
    }

    public Integer calcularCercana(int ciuPartir, ArrayList<Integer> conjuntoNodos) {

        int min = Integer.MAX_VALUE;
        Integer aBuscar = -1;

        for (int ciudad : conjuntoNodos) { //para que coja cada ciudad como objeto
            if (matrizCostes[ciuPartir - 1][ciudad - 1] < min) {
                min = matrizCostes[ciuPartir - 1][ciudad - 1];
                aBuscar = ciudad;
            }
        }

        costeTotal += min;
        return aBuscar;
    }

    public int getCoste() {
        return this.costeTotal;
    }
    
    public int getEvaluaciones() {
        return this.evaluaciones;
    }
}
