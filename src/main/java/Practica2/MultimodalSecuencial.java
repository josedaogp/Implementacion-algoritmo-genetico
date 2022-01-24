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
public class MultimodalSecuencial {

    LeerFichero lf = new LeerFichero();
    ArrayList<Integer> ciudades;
    int[][] matrizCostes;
    int mejorCoste;
    ArrayList<Integer> mejorSolucion;
    Random aleatorio;
    ArrayList<ArrayList<Integer>> poblacion = new ArrayList<>();
    ArrayList<ArrayList<Integer>> grafico = new ArrayList<>();
    //ArrayList<Integer> eliminadosClearing = new ArrayList<>();
    int radioSecuencial;
    int tam_poblacion;
    //int[][] DistanciasHamming;

    public MultimodalSecuencial(String ruta, int semilla) {
        aleatorio = new Random(semilla);
        mejorCoste = 0;
        try {
            lf.leerfichero(ruta);
            this.ciudades = lf.getCiudades();
            // System.out.println("Ciudades.size: "+this.ciudades.size());
            this.matrizCostes = lf.getMatrizCostes();
        } catch (IOException ex) {
            Logger.getLogger(Greedy.class.getName()).log(Level.SEVERE, null, ex);
        }

        tam_poblacion = 40; //DE 30 A 100 INDIVIDUOS
        radioSecuencial = (int) (0.2 * tam_poblacion);
    }

    public ArrayList<Integer> multimodal() {

        int nichos = 5; //numero de nichos
        ArrayList<ArrayList<Integer>> solucionesNichos = new ArrayList<>(); //guarda las mejores soluciones de cada nicho
        ArrayList<Integer> mejorSolucionBucle;
        ArrayList<Integer> costesNichos = new ArrayList<>(); //guarda el coste de cada solucion de cada nicho

        while (solucionesNichos.size() < nichos) { //hasta que no se consigan las 5 soluciones      
            mejorSolucionBucle = algoritmoGB(solucionesNichos); //generamos una solucion con genetico basico
            costesNichos.add(calcularCosteSolucion(mejorSolucionBucle));
            solucionesNichos.add(mejorSolucionBucle); //añadir la solucion
        }

        //ver la distancia hamming entre las soluciones:
        ArrayList<Integer> solucion1;
        ArrayList<Integer> solucion2;
        for (int i = 0; i < 5; i++) {
            solucion1 = solucionesNichos.get(i);
            for (int j = i + 1; j < 5; j++) {
                solucion2 = solucionesNichos.get(j);
                System.out.println("Distancia Hamming entre ciudad "+i+" y ciudad "+j+"= "+distanciaHamming(solucion1, solucion2));
            }

        }
        return costesNichos;
    }

    public ArrayList<Integer> algoritmoGB(ArrayList<ArrayList<Integer>> solucionesNichos) { //en la clase geneticobasicov2 se añaden mas comentarios explicativos de este metodo

        ArrayList<Integer> costePoblacion = new ArrayList<>();

        ArrayList<ArrayList<Integer>> padres = new ArrayList<>();
        ArrayList<Integer> hijo1 = new ArrayList<>();
        ArrayList<Integer> hijo2 = new ArrayList<>();
        ArrayList<Integer> mejorSolucionGB = new ArrayList<>();

        int iteraciongrafico = 0;

        int crucesSinMejorar = 0;
        boolean hayQueParar = false;

        //Variables del algoritmo
        //int tam_poblacion = 40; //DE 30 A 100 INDIVIDUOS -> ESTÁ EN EL CONSTRUCTOR JUNTO AL RADIO
        int k_torneo = (int) Math.round(0.1 * tam_poblacion); //se escogen k individuos aleatorios en el torneo
        int tam_cruce = (int) (ciudades.size() * 0.9);
        double prob_mutacion = 0.07;
        int tam_mutacion = (int) Math.round(0.1 * tam_poblacion);

        for (int i = 0; i < tam_poblacion; i++) { //generar población inicial aleatoria
            poblacion.add(generarSolucionAleatoria());
        }

        costePoblacion = calcularCostesPoblacion(poblacion, solucionesNichos);
        mejorCoste = obtenerMejorPoblacion(costePoblacion);
        int mediaPoblacion = calcularMediaPoblacion(costePoblacion);

        ArrayList<Integer> tupla = new ArrayList<>();

        tupla.add(iteraciongrafico);
        tupla.add(mejorCoste);
        tupla.add(mediaPoblacion);

        grafico.add(tupla);

        while (!hayQueParar) {
            padres = operadorSeleccion(poblacion, k_torneo);
            hijo1 = Cruce(padres, tam_cruce); //GENERAR DOS HIJOS
            hijo2 = Cruce(padres, tam_cruce);

            if (aleatorio.nextDouble() < prob_mutacion) {
                hijo1 = Mutacion(hijo1, tam_mutacion);
                hijo2 = Mutacion(hijo2, tam_mutacion);
            }

            poblacion = reemplazoDC(hijo1, poblacion);
            poblacion = reemplazoDC(hijo2, poblacion);

            //Criterio de parada
            int costeCruce = calcularCosteSolucionSecuencial(hijo1, solucionesNichos);
            int costeCruce2 = calcularCosteSolucionSecuencial(hijo2, solucionesNichos);
            if (costeCruce >= mejorCoste || costeCruce2 >= mejorCoste) {
                crucesSinMejorar++;
            } else {
                if (costeCruce < costeCruce2) {
                    mejorCoste = costeCruce;
                    mejorSolucion = (ArrayList<Integer>) hijo1.clone();
                } else {
                    mejorCoste = costeCruce2;
                    mejorSolucion = (ArrayList<Integer>) hijo2.clone();
                }

                crucesSinMejorar = 0;
            }

            if (crucesSinMejorar == 5000) {
                hayQueParar = true;
            }

            costePoblacion = calcularCostesPoblacion(poblacion, solucionesNichos);
            mediaPoblacion = calcularMediaPoblacion(costePoblacion);

            if (iteraciongrafico % 500 == 0) {
                tupla = new ArrayList<>();

                tupla.add(iteraciongrafico);
                tupla.add(mejorCoste);
                tupla.add(mediaPoblacion);

                grafico.add(tupla);

            }
            iteraciongrafico++;
        }

        //escribirGrafico(grafico);
        return mejorSolucion;
    }

//     private ArrayList<ArrayList<Integer>> clearing(ArrayList<ArrayList<Integer>> poblacion, int[][] DistanciasHamming, int alpha, int kappa) {
//         ArrayList<ArrayList<Integer>> pobClearing = (ArrayList<ArrayList<Integer>>) poblacion.clone();
//         int numGanadores = 0;
//         int tamaniopob = poblacion.size();
//         
//         for (int i = 0; i < tamaniopob; i++) {
//             if (eliminadosClearing.get(i)>0) {
//                 numGanadores=1;
//                 for (int j = i+1; j < tamaniopob; j++) {
//                     if (eliminadosClearing.get(j)>0 && DistanciasHamming[i][j]<alpha) {
//                         if (numGanadores<kappa) {
//                             numGanadores++;
//                         }else {
//                             eliminadosClearing.set(j, 0);
//                             pobClearing.remove(j);
//                         }
//                     }
//                 }
//             }
//         }
//         return pobClearing;
//    }
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

    private int calcularCosteSolucionSecuencial(ArrayList<Integer> solucion, ArrayList<ArrayList<Integer>> solucionesNichos) {
        //evaluaciones++;
        int costeDevolver = 0;
        for (int i = 0; i < solucion.size() - 1; i++) {
            int ciudad1 = solucion.get(i);
            int ciudad2 = solucion.get(i + 1);
            costeDevolver += matrizCostes[ciudad1 - 1][ciudad2 - 1];
        }
        //En costeDevolver estará el coste real

        for (ArrayList<Integer> nicho : solucionesNichos) {//para penalizar la solucion si esta cerca de otro nicho
            int distHamming = distanciaHamming(nicho, solucion);
            if (this.radioSecuencial > distHamming) { //si el radio contiene a la solucion se le penaliza
                costeDevolver *= (radioSecuencial / (distHamming + 0.01)); //se le suma eso por si es cero
                break;
            }
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

    private ArrayList<ArrayList<Integer>> elegirPadres(List<ArrayList<Integer>> pob_desord) { //POR COMPROBAR----------------

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

    //quedarme con los dos mejores de cuatro.
    private ArrayList<ArrayList<Integer>> reemplazoDC(ArrayList<Integer> hijo, ArrayList<ArrayList<Integer>> poblacion1) { //crowding determinístico
        int min = Integer.MAX_VALUE;
        int posmin = -1;
        int i = 0;

        //System.out.println("Empieza el for de reemplazo");
        for (ArrayList<Integer> ciudad : poblacion1) {
            int distHamm = distanciaHamming(hijo, ciudad);
            if (distHamm < min) {
                min = distHamm;
                posmin = i;
            }
            i++;
        }
        //System.out.println("Termina el for de reemplazo");
        poblacion1.remove(poblacion1.get(posmin));
        poblacion1.add(hijo);

        return poblacion1;
    }

    private int distanciaHamming(ArrayList<Integer> hijo, ArrayList<Integer> ciudad) {
        int i = 0, count = 0;
        int n = hijo.size();
        int y = ciudad.size();

        while (i < n) {
            int valorciudad = (int) ciudad.get(i);
            int valorhijo = (int) hijo.get(i);
            if ((int) ciudad.get(i) != (int) hijo.get(i)) { //cambiar el object.equals para comparar posición a posición
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
        mejorSolucion = poblacion.get(posmin);
        return min;
    }

    private ArrayList<Integer> calcularCostesPoblacion(ArrayList<ArrayList<Integer>> poblacion, ArrayList<ArrayList<Integer>> solucionesNichos) {
        ArrayList<Integer> costes = new ArrayList<>();
        for (ArrayList<Integer> ciudad : poblacion) {
            int coste = calcularCosteSolucionSecuencial(ciudad, solucionesNichos);
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

        File f = new File("Grafico.csv");
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
