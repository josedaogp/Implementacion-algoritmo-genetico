/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Practica2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author josed
 */
public class CHC {

    LeerFichero lf = new LeerFichero();
    ArrayList<Integer> ciudades;
    int[][] matrizCostes;
    int mejorCosteTotal;
    ArrayList<Integer> mejorSolucion;
    Random aleatorio;
    ArrayList<ArrayList<Integer>> poblacion = new ArrayList<>();

    public CHC(String ruta, int semilla) {
        aleatorio = new Random(semilla);
        mejorCosteTotal = 0;
        try {
            lf.leerfichero(ruta);
            this.ciudades = lf.getCiudades();
            // System.out.println("Ciudades.size: "+this.ciudades.size());
            this.matrizCostes = lf.getMatrizCostes();
        } catch (IOException ex) {
            Logger.getLogger(Greedy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int algoritmoCHC() {

        int NumerodeReinicios = 0;
        int d = ciudades.size() / 4;
        int tam_poblacion = 40; //DE 30 A 100 INDIVIDUOS
        int tam_cruce = (int) (ciudades.size() * 0.9);
        boolean hayQueParar = false;
            int numReinicios = 1000;
        //int M = 2; //Número de copias del mejor elemento en el reinicio del diverge

        ArrayList<ArrayList<Integer>> Ct = new ArrayList<>(); //C(t)
        ArrayList<ArrayList<Integer>> CtPrima = new ArrayList<>(); //C'(t)
        ArrayList<ArrayList<Integer>> Pt1 = new ArrayList<>(); //P(t)

        ArrayList<Integer> costePoblacion = new ArrayList<>();

        for (int i = 0; i < tam_poblacion; i++) { //generar población inicial aleatoria
            poblacion.add(generarSolucionAleatoria());
        }

        costePoblacion = calcularCostesPoblacion(poblacion);
        mejorCosteTotal = obtenerMejorPoblacion(costePoblacion);
        System.out.println("El mejor coste inicial es: " + mejorCosteTotal);

        while (!hayQueParar) {

            Ct = selectR(poblacion); //copia en C(t), "poblacion" en orden aleatorio
            CtPrima = recombine(Ct, d, tam_cruce); //genera una nueva poblacion C'(t) segun distancia Hamming

            Pt1 = selectS(poblacion, CtPrima); //Forma P(t), "Pt1", desde P(t-1), "poblacion", reemplazando los mejores de C'(t) en los peores de P(t-1)

            if (igualesPoblacion(poblacion, Pt1)) { //si la poblacion generada es igual a la anterior, decrementamos d
                d--;
            }

            if (d < 0) { //reinicio cuando d llega a cero
                //El diverge genera una poblacion aleatoria exceptuando los x elementos mejores de P(t)
                poblacion = diverge(Pt1, tam_poblacion);
                d = ciudades.size() / 4; //reseteamos d
                NumerodeReinicios++; //para el criterio de parada

            } else { //actualizar poblacion
                poblacion = Pt1;
            }
            System.out.println("Numero Reinicios= " + NumerodeReinicios);
            //CRITERIO DE PARADA
            if (NumerodeReinicios == numReinicios) {
                hayQueParar = true;
            }
        }

        costePoblacion = calcularCostesPoblacion(poblacion);
        mejorCosteTotal = obtenerMejorPoblacion(costePoblacion);
        return mejorCosteTotal;
    }

    public ArrayList<ArrayList<Integer>> selectR(ArrayList<ArrayList<Integer>> poblac) { //baraja la población

        ArrayList<ArrayList<Integer>> poblaciondevolver = (ArrayList<ArrayList<Integer>>) poblac.clone();
        Collections.shuffle(poblaciondevolver);
        return poblaciondevolver;

    }

    private ArrayList<ArrayList<Integer>> recombine(ArrayList<ArrayList<Integer>> Ct, int d, int tam_cruce) {
        ArrayList<ArrayList<Integer>> aDevolver = new ArrayList<>();
        int n = Ct.size();

        for (int i = 0; i < Ct.size(); i += 2) { //para cada par de poblac
            ArrayList<Integer> hijo1 = Ct.get(i);
            ArrayList<Integer> hijo2 = Ct.get(i + 1);
            int distanciaHamming = distanciaHamming(Ct.get(i), Ct.get(i + 1));
            if (distanciaHamming / 2 > d) {
                hijo1 = Cruce(Ct.get(i), Ct.get(i + 1), tam_cruce);
                hijo2 = Cruce(Ct.get(i), Ct.get(i + 1), tam_cruce);

                aDevolver.add(hijo1);
                aDevolver.add(hijo2);
            }
//            else { //si no, remueve el par de Ct (creo que no hace falta)
//                Ct.remove((Object) Ct.get(i));
//                Ct.remove((Object) Ct.get(i + 1));
//            }
        }
        return aDevolver;
    }

    public ArrayList<ArrayList<Integer>> selectS(ArrayList<ArrayList<Integer>> Ptmenos1, ArrayList<ArrayList<Integer>> CtPrima) {

        ArrayList<ArrayList<Integer>> Pt = (ArrayList<ArrayList<Integer>>) Ptmenos1.clone();
        ArrayList<ArrayList<Integer>> Ptmenos1aux = (ArrayList<ArrayList<Integer>>) Ptmenos1.clone();
        ArrayList<ArrayList<Integer>> CtPrimaAux = (ArrayList<ArrayList<Integer>>) CtPrima.clone();
        boolean queden = true;

        while (queden) {   //mientras queden mejores         
            int posicionPeor = obtenerPeor(Ptmenos1aux)[0]; //obtener el peor de P(t-1)
            int costePeor = obtenerPeor(Ptmenos1aux)[1]; //y el coste del peor de P(t-1)
            int posicionMejor = obtenerMejor(CtPrimaAux)[0]; //obtener el Mejor de C'(t)
            int costeMejor = obtenerMejor(CtPrimaAux)[1]; //y su coste

            if (costePeor < costeMejor) { //si ya no hay ninguno mejor en C'(t) que en P(t-1)
                queden = false;
            } else {
                Pt.set(posicionPeor, CtPrimaAux.get(posicionMejor)); //reemplazar el peor por el mejor
                CtPrimaAux.remove(posicionMejor); //para que no se vuelva a buscar el mejor de antes
                Ptmenos1aux.remove(posicionPeor); //para que no se vuelva a buscar el peor de antes
            }
        }

        return Pt;
    }

    private int distanciaHamming(ArrayList<Integer> hijo, ArrayList<Integer> ciudad) {
        int i = 0, count = 0;
        int n = hijo.size();
        int y = ciudad.size();

        while (i < n) {
            if ((int) ciudad.get(i) != (int) hijo.get(i)) { //cambiar el object.equals para comparar posición a posición
                count++;
            }
            i++;
        }
        return count;
    }

    private ArrayList<Integer> Cruce(ArrayList<Integer> padre1, ArrayList<Integer> padre2, int tam_cruce) {
        List<Integer> sublista = (List<Integer>) padre1.clone();
        List<Integer> sublista2 = (List<Integer>) padre2.clone();
        List<Integer> sublista2aux = (List<Integer>) padre2.clone();
        ArrayList<Integer> hijo = new ArrayList<>();

        int posIni = aleatorio.nextInt(padre1.size() - tam_cruce); //para controlar que la lista no es cíclica
        int posFin = posIni + tam_cruce;

        sublista = sublista.subList(posIni, posFin);
        hijo.addAll(sublista);

        int tamaniosublista2 = sublista2.size();
        for (int i = 0; i < tamaniosublista2; i++) { //quitar elementos repetidos de la sublista 2
            int ciudadsl2 = sublista2.get(i); //ciudad sublista 2
            if (hijo.contains((Object) ciudadsl2)) {
                sublista2aux.remove((Object) ciudadsl2);
            }
//            if (!hijo.contains((Object)ciudadsl2)) { //Otra forma de hacerlo
//                hijo.add(ciudadsl2);
//                nhijos++;
//            }
        }

        hijo.addAll(sublista2aux);

        return hijo;
    }

    private ArrayList<Integer> generarSolucionAleatoria() {

        int pos; //posición que se generara aleatoriamente

        ArrayList<Integer> conjuntoNodos = (ArrayList<Integer>) this.ciudades.clone(); //nodos candidatos 
        ArrayList<Integer> vecino = new ArrayList<>(); //vecino que se generará

        while (!conjuntoNodos.isEmpty()) {
            pos = aleatorio.nextInt(conjuntoNodos.size()); //se genera una nueva posición
            vecino.add(conjuntoNodos.get(pos)); //se añade a la solución la ciudad correspondiente
            conjuntoNodos.remove(pos);  //se elimina de los candidatos
        }  //se repite mientras queden candidatos por elegir

        return vecino;
    }

    private int[] obtenerMejor(ArrayList<ArrayList<Integer>> poblacion) {
        int min = Integer.MAX_VALUE;
        int posmin = -1;
        int[] devolver = new int[2];
        //Sacar primer padre
        for (int i = 0; i < poblacion.size(); i++) {
            ArrayList<Integer> ciudad = poblacion.get(i);
            int costeciudad = calcularCosteSolucion(ciudad);
            if (costeciudad < min) {
                min = costeciudad;
                posmin = i;
            }
        }

        devolver[0] = posmin;
        devolver[1] = min;
        return devolver;
    }

    private int[] obtenerPeor(ArrayList<ArrayList<Integer>> CtPrima) {
        int max = Integer.MIN_VALUE;
        int posmax = -1;
        int[] devolver = new int[2];

        //Sacar primer padre
        for (int i = 0; i < CtPrima.size(); i++) {
            ArrayList<Integer> ciudad = CtPrima.get(i);
            int costeciudad = calcularCosteSolucion(ciudad);
            if (costeciudad > max) {
                max = costeciudad;
                posmax = i;
            }
        }

        devolver[0] = posmax;
        devolver[1] = max;
        return devolver;
    }

    private int calcularCosteSolucion(ArrayList<Integer> solucion) {
        //evaluaciones++;
        int costeDevolver = 0;
        for (int i = 0; i < solucion.size() - 1; i++) {
            int ciudad1 = solucion.get(i);
            int ciudad2 = solucion.get(i + 1);
            costeDevolver += matrizCostes[ciudad1 - 1][ciudad2 - 1];
        }
        return costeDevolver;
    }

    private boolean igualesPoblacion(ArrayList<ArrayList<Integer>> poblacion, ArrayList<ArrayList<Integer>> Pt1) {
        int i = 0;
        int n = poblacion.size();
        boolean encontrado = false;

        while (i < n && !encontrado) {
            ArrayList<Integer> ciudad1 = poblacion.get(i);
            ArrayList<Integer> ciudad2 = Pt1.get(i);
            encontrado = igualesCiudades(ciudad1, ciudad2); //si las ciudades a comparar son iguales
            i++;
        }
        return encontrado;
    }

    private boolean igualesCiudades(ArrayList<Integer> ciudad1, ArrayList<Integer> ciudad2) {//si dos soluciones son iguales
        int i = 0;
        int n = poblacion.size();
        boolean encontrado = false;

        while (i < n && !encontrado) {
            int valorciudad1 = ciudad1.get(i);
            int valorciudad2 = ciudad2.get(i);
            if (valorciudad1 != valorciudad2) {
                encontrado = true;
            }
            i++;
        }
        return !encontrado;
    }

    private ArrayList<ArrayList<Integer>> diverge(ArrayList<ArrayList<Integer>> Pt1, int tam_poblacion) {
        ArrayList<ArrayList<Integer>> devolver = new ArrayList<>();
        ArrayList<ArrayList<Integer>> poblacionaux = (ArrayList<ArrayList<Integer>>) Pt1.clone();

        /*El siguiente código comentado es por si se quiere meter no solo el primer mejor de Pt1, si no un porcentaje*/
//        int pos = 0;
//        for (int i = 0; i < (ciudades.size() * 0.2); i++) {//meter n mejores (un porcentaje de la poblacion) en vez de uno solo (1/3 o un 1/4 de la poblacion)
//            int posicionMejor = obtenerMejor(poblacionaux)[0];
//            if (posicionMejor == -1) {
//                break;
//            } else {
//                devolver.add(poblacionaux.get(posicionMejor));
//                poblacionaux.remove(posicionMejor);
//                pos++;
//            }
//        }
//
//        //
//        for (int i = pos+1; i < tam_poblacion; i++) { //generar el resto de la población inicial aleatoria
//            devolver.add(generarSolucionAleatoria());
//        }

        for (int i = 0; i < 1; i++) {//meter el mejor
            int posicionMejor = obtenerMejor(poblacionaux)[0];
                devolver.add(poblacionaux.get(posicionMejor));
                poblacionaux.remove(posicionMejor);
            
        }

        //
        for (int i = 1; i < tam_poblacion; i++) { //generar el resto de la población inicial aleatoria
            devolver.add(generarSolucionAleatoria());
        }

        return devolver;
    }

//    private ArrayList<ArrayList<Integer>> obtenerMejorPoblacion(ArrayList<ArrayList<Integer>> poblacion) {
//
//        int min = Integer.MAX_VALUE;
//        int posmin = -1;
//        int i = 0;
//        ArrayList<ArrayList<Integer>> aDevolver = new ArrayList<>();
//        ArrayList<Integer> solucionMejor = new ArrayList<>();
//        
//        for (ArrayList<Integer> ciudad : poblacion) {
//            int menorDist = calcularCosteSolucion(ciudad);
//            if (menorDist < min) {
//                min = menorDist;
//                posmin = i;
//                solucionMejor = ciudad;
//            }
//            i++;
//        }
//
//        aDevolver.add(new ArrayList<Integer>(min));
//        aDevolver.add(solucionMejor);
//        return aDevolver;
//    }
    private int obtenerMejorPoblacion(ArrayList<Integer> costesPoblacion) {

        int min = Integer.MAX_VALUE;
        int posmin = -1;
        int i = 0;
        for (Integer ciudad : costesPoblacion) {
            if (ciudad < min) {
                min = ciudad;
                posmin = i;
            }
            i++;
        }

        return min;
    }

    private ArrayList<Integer> calcularCostesPoblacion(ArrayList<ArrayList<Integer>> poblacion) {
        ArrayList<Integer> costes = new ArrayList<>();
        for (ArrayList<Integer> ciudad : poblacion) {
            int coste = calcularCosteSolucion(ciudad);
            costes.add(coste);
        }
        return costes;
    }

    private int calcularMediaPoblacion(ArrayList<Integer> costePoblacion) {
        int media = 0;
        int total = 0;
        int numeroCiudades = costePoblacion.size();

        for (Integer costeCiudad : costePoblacion) {
            total += costeCiudad;
        }
        media = total / numeroCiudades;
        return media;
    }

    private void escribirGrafico(ArrayList<ArrayList<Integer>> grafico) {

        File f = new File("GraficoCHC.csv");
        FileWriter fWriter;
        System.out.println("Entra en el escribir grafico y el tamaño de grafico es: " + grafico.size());

        try {
            fWriter = new FileWriter(f);
            fWriter.write("i;mejor;media");
            fWriter.write("\n");
            for (ArrayList<Integer> tupla : grafico) {
                fWriter.write(tupla.get(0) + ";" + tupla.get(1) + ";" + tupla.get(2));
                fWriter.write("\n");
            }

            fWriter.flush();
        } catch (IOException ex) {
            System.out.println("LANZA UNA EXCEPCION");
        }
    }
}
