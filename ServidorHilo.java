/**
*Los canales de entrada y salida reciben y envian objetos
*/

import java.io.*;
import java.net.*;
import java.util.logging.*;


public class ServidorHilo extends Thread {
    private Socket socket; //socket del cliente 
    private ObjectOutputStream dos;//Canal de salida de datos hacia el cliente (envia datos al cliente)
    private ObjectInputStream dis;//Canal de entrada de datos desde el cliente (recibe datos del cliente)
    

    /**
    *CONSTRUCTOR
    *Recibe un socket del cliente
    *Asigna un hilo del servidor para que atienda a ese cliente
    */
    public ServidorHilo(Socket socket) {
        this.socket = socket;
        
        try {
            //inicializa los canales de comunicacion
            dos = new ObjectOutputStream(socket.getOutputStream());
            dis = new ObjectInputStream(socket.getInputStream()); 

        } catch (IOException ex) {
            Logger.getLogger(ServidorHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

/**
*Termina la conexi&oacute;n 
*/
    public void desconnectar() {
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ServidorHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    
    public void run() {
        
        try {
            //Recibe el paquete del cliente
            Protocolo paquetito = (Protocolo) dis.readObject();
            paquetito.print();//Imprimimos en pantalla el contenido del paquete

            //dos.writeUTF("adios");//Escribe un mensaje al cliente
            
        } catch (Exception ex) {
            Logger.getLogger(ServidorHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
        desconnectar();
    }
}