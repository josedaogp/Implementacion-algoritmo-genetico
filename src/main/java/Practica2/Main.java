package Practica2;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

//        GeneticoBasicoV2 gb = new GeneticoBasicoV2("./ch130.tsp", 53154);
//        System.out.println("Coste GB: "+gb.algoritmoGB());

//        CHC chc = new CHC("./a280.tsp", 53154);
//        int solucionchc = chc.algoritmoCHC();
//        System.out.println("CHC: "+solucionchc);
        
        MultimodalSecuencial ms = new MultimodalSecuencial("./ch130.tsp", 53154);
        ArrayList<Integer> solucion = ms.multimodal();
        System.out.println("Multimodal\n "+solucion);
        
    }

}
