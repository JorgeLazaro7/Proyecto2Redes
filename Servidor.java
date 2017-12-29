/**
*Esta clase solo acepta conexiones de clientes
*Los metodos para inicio de sesion etc se gestionan desde la clase ServidorHilo
*
*/

import java.io.*;
import java.net.*;
import java.util.logging.*;
public class Servidor {
    public static void main(String args[]) throws IOException {
        ServerSocket ss; //Socket del servidor
        System.out.print("Inicializando servidor... ");
        try {
            ss = new ServerSocket(9999);//Inicializa el socket en el puerto 9999
            System.out.println("\t[OK]");

           
            //Espera indefinidamente a un cliente
            while (true) {
                Socket socket; //socket del cliente
                socket = ss.accept();//acepta la conexión con el cliente
                System.out.println("Nueva conexión entrante: "+socket);
                //comienza a atender al hilo del cliente
                ((ServidorHilo) new ServidorHilo(socket)).start();
               
            }
        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}