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
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author josed
 */
public class GeneticoBasicoV2 {

    LeerFichero lf = new LeerFichero();
    ArrayList<Integer> ciudades;
    int[][] matrizCostes;
    int mejorCoste;
    ArrayList<Integer> mejorSolucion;
    Random aleatorio;
    ArrayList<ArrayList<Integer>> poblacion = new ArrayList<>();
    ArrayList<ArrayList<Integer>> grafico = new ArrayList<>();

    public GeneticoBasicoV2(String ruta, int semilla) {
        aleatorio = new Random(semilla);
        mejorCoste = 0;
        try {
            lf.leerfichero(ruta);
            this.ciudades = lf.getCiudades();
            this.matrizCostes = lf.getMatrizCostes();
        } catch (IOException ex) {
            Logger.getLogger(Greedy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int algoritmoGB() {

        ArrayList<Integer> costePoblacion;

        ArrayList<ArrayList<Integer>> padres;
        ArrayList<Integer> hijo1;
        ArrayList<Integer> hijo2;

        int iteraciongrafico = 0;

        int crucesSinMejorar = 0;
        boolean hayQueParar = false;

        //Variables del algoritmo
        int tam_poblacion = 40; //DE 30 A 100 INDIVIDUOS
        int k_torneo = (int) Math.round(0.1 * tam_poblacion); //se escogen k individuos aleatorios en el torneo
        int tam_cruce = (int) (ciudades.size() * 0.9);
        double prob_mutacion = 0.07; //probabilidad de que se haga la mutacion
        int tam_mutacion = (int) Math.round(0.1 * tam_poblacion);

        for (int i = 0; i < tam_poblacion; i++) { //generar población inicial aleatoria
            poblacion.add(generarSolucionAleatoria());
        }

        //Primera evaluacion
        costePoblacion = calcularCostesPoblacion(poblacion);
        mejorCoste = obtenerMejorPoblacion(costePoblacion);
        System.out.println("Mejor coste inicial: "+mejorCoste);
        int mediaPoblacion = calcularMediaPoblacion(costePoblacion);

        ArrayList<Integer> tupla = new ArrayList<>(); //tupla del grafico

        tupla.add(iteraciongrafico);
        tupla.add(mejorCoste);
        tupla.add(mediaPoblacion);

        grafico.add(tupla);

        while (!hayQueParar) {
            padres = operadorSeleccion(poblacion, k_torneo);
            hijo1 = Cruce(padres, tam_cruce); //GENERAR DOS HIJOS (Miguel Ángel me dijo que lo repitiera dos veces)
            hijo2 = Cruce(padres, tam_cruce);

            if (aleatorio.nextDouble() < prob_mutacion) { //mutacion
                hijo1 = Mutacion(hijo1, tam_mutacion);
                hijo2 = Mutacion(hijo2, tam_mutacion);
            }

            //reemplazo crowding determinístico
            poblacion = reemplazoDC(hijo1, poblacion);
            poblacion = reemplazoDC(hijo2, poblacion);

            //Criterio de parada (numero de veces sin mejorar)
            int costeCruce = calcularCosteSolucion(hijo1);
            int costeCruce2 = calcularCosteSolucion(hijo2);
            if (costeCruce >= mejorCoste || costeCruce2 >= mejorCoste) {
                crucesSinMejorar++;
            } else { //actualizar el mejor valor
                if (costeCruce<costeCruce2) {
                    mejorCoste = costeCruce;
                }else mejorCoste = costeCruce2;
                
                crucesSinMejorar = 0;
            }

            if (crucesSinMejorar == 5000) {
                hayQueParar = true;
            }

            costePoblacion = calcularCostesPoblacion(poblacion);
            mediaPoblacion = calcularMediaPoblacion(costePoblacion);

            if (iteraciongrafico % 800 == 0) { //para recoger valores en el grafico cada x generaciones
                tupla = new ArrayList<>();

                tupla.add(iteraciongrafico);
                tupla.add(mejorCoste);
                tupla.add(mediaPoblacion);

                grafico.add(tupla);

            }
            iteraciongrafico++;
        }

        escribirGrafico(grafico);
        return mejorCoste;
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

    private ArrayList<ArrayList<Integer>> operadorSeleccion(ArrayList<ArrayList<Integer>> poblacion, int k_torneo) { //torneo
        List<ArrayList<Integer>> pob_desord = (ArrayList<ArrayList<Integer>>) poblacion.clone();

        int posIni = aleatorio.nextInt(pob_desord.size() - k_torneo); //numero aleatorio entre 0 y n-n/4, ya que la lista no es cíclica
        int posFin = posIni + k_torneo;

        Collections.shuffle(pob_desord);

        pob_desord = pob_desord.subList(posIni, posFin);

        return elegirPadres(pob_desord); //elige los dos mejores candidatos de la lista

    }

    private ArrayList<ArrayList<Integer>> elegirPadres(List<ArrayList<Integer>> pob_desord) {

        ArrayList<ArrayList<Integer>> padres = new ArrayList<>();
        int min = Integer.MAX_VALUE;
        int posmin = -1;
        List<ArrayList<Integer>> pob_desord_aux = pob_desord;

        //Sacar primer padre
        for (int i = 0; i < pob_desord_aux.size(); i++) {
            ArrayList<Integer> ciudad = pob_desord_aux.get(i);
            int costeciudad = calcularCosteSolucion(ciudad);
            if (costeciudad < min) {
                min = costeciudad;
                posmin = i;
            }
        }

        padres.add(pob_desord_aux.get(posmin));
        pob_desord_aux.remove(posmin); //Quito el padre elegido para buscar al segundo mejor

        //Buscar al segundo padre mejor
        min = Integer.MAX_VALUE;
        posmin = -1;
        for (int i = 0; i < pob_desord_aux.size(); i++) {
            ArrayList<Integer> ciudad = pob_desord_aux.get(i);
            int costeciudad = calcularCosteSolucion(ciudad);
            if (costeciudad < min) {
                min = costeciudad;
                posmin = i;
            }
        }

        padres.add(pob_desord_aux.get(posmin));
        return padres;
    }

    private ArrayList<Integer> Cruce(ArrayList<ArrayList<Integer>> padres, int tam_cruce) {
        ArrayList<Integer> padre1 = (ArrayList<Integer>) padres.get(0).clone();
        ArrayList<Integer> padre2 = (ArrayList<Integer>) padres.get(1).clone();
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

    private ArrayList<Integer> Mutacion(ArrayList<Integer> S, int tam_mutacion) {

        int n = ciudades.size();
        int posIniSublista = aleatorio.nextInt(n - tam_mutacion); //numero aleatorio entre 0 y n-n/4, ya que la lista no es cíclica
        int posFinSublista = posIniSublista + tam_mutacion - 1; //se calcula el resto de la lista 
        List<Integer> sublista = S.subList(posIniSublista, posFinSublista + 1); //cogemos la sublista para barajarla

        Collections.shuffle(sublista); //se barajan aleatoriamente los elementos de la sublista

        //Se junta de nuevo la sublista con la primera parte y la última de la solución original
        List<Integer> primeraParte = S.subList(0, posIniSublista);
        List<Integer> ultimaParte = S.subList(posFinSublista + 1, n);

        ArrayList<Integer> solucionMutada = new ArrayList<Integer>(primeraParte);
        solucionMutada.addAll(sublista);
        solucionMutada.addAll(ultimaParte);

        return solucionMutada;

    }

    //No utilizo este reemplazo
    private ArrayList<ArrayList<Integer>> reemplazoRW(ArrayList<Integer> hijo, ArrayList<ArrayList<Integer>> poblacion1) {
        int max = Integer.MIN_VALUE;
        int posmax = -1;
        int i = 0;
        for (ArrayList<Integer> ciudades : poblacion1) {
            int costeCiudades = calcularCosteSolucion(ciudades);
            if (costeCiudades > max) {
                max = costeCiudades;
                posmax = i;
            }
            i++;
        }
        poblacion1.remove(poblacion1.get(posmax));
        poblacion1.add(hijo);

        return poblacion1;
    }

    //(otra opcion)->quedarme con los dos mejores de cuatro
    private ArrayList<ArrayList<Integer>> reemplazoDC(ArrayList<Integer> hijo, ArrayList<ArrayList<Integer>> poblacion1) { //crowding determinístico
        int min = Integer.MAX_VALUE;
        int posmin = -1;
        int i = 0;

        for (ArrayList<Integer> ciudad : poblacion1) {
            int distHamm = distanciaHamming(hijo, ciudad);
            if (distHamm < min) { 
                min = distHamm;
                posmin = i;
            }
            i++;
        }
        
        poblacion1.remove(poblacion1.get(posmin));
        poblacion1.add(hijo);

        return poblacion1;
    }

    private int distanciaHamming(ArrayList<Integer> hijo, ArrayList<Integer> ciudad) {
        int i = 0, count = 0;
        int n = hijo.size();
        int y = ciudad.size();

        while (i < n) {
            if ((int) ciudad.get(i) != (int) hijo.get(i)) { 
                count++;
            }
            i++;
        }
        return count;
    }

//    private int obtenerMejorPoblacion(ArrayList<ArrayList<Integer>> poblacion) {
//
//        int min = Integer.MAX_VALUE;
//        int posmin = -1;
//        int i = 0;
//        for (ArrayList<Integer> ciudad : poblacion) {
//            int menorDist = calcularCosteSolucion(ciudad);
//            if (menorDist < min) {
//                min = menorDist;
//                posmin = i;
//            }
//            i++;
//        }
//
//        return min;
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

    //Metodos para calcular graficos
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

        File f = new File("GraficoGeneticoBasico.csv");
        FileWriter fWriter;
        
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
            System.out.println("Escribir fichero LANZA UNA EXCEPCION");
        }
    }

}
