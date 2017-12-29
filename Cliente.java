/**
*El cliente debe iniciar siempre la conexi&oacute;n al servidor con el mensaje 12 (Inicio de sesi&oacute;n)
*
*Implementa la clase serializable para enviar objetos a traves de los sockets
*/

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.logging.*;
import java.util.Scanner;
import java.io.OutputStream;

class Cliente extends Thread implements Serializable{
    protected Socket sk; //Socket del cliente
    protected ObjectOutputStream dos; //Canal de salida de datos hacia el servidor
    protected ObjectInputStream dis; //Canal de entrada de datos desde el servidor
   

    /**
    *Solicita iniciar esión a través del protocolo PKP con el mensaje 12
    *Importa y Utiliza la clase Scanner de Java para leer datos desde el teclado
    *
    *Al solicitar el inicio de sesi&oacute;n, no conocemos el id de usuario, por lo que en el 
    *Paquete, se env&iacute; -1
    */
    public void iniciarSesion(){
        Scanner in = new Scanner(System.in);
        String nn;//Nickname
        String pass; //Password


        System.out.println("Bienvenido! \n"+"Para inciar ingresa tu Nickname: ");
        nn = in.nextLine();
        System.out.println("Contraseña:");
        pass= in.nextLine();

        //Construye el paquete "MensajeDeAplicacion"
        String[] m = new String[3];
        m[0] = "12";
        m[1] = nn;
        m[2] = pass;

        //Construye el paquete (protocolo PKP) e incluye el paquete "MensajeDeAplicacion"

        Protocolo doce = new Protocolo(1111,9999,12,12,-1,m); 
        enviarPaquete(doce);

        //while (true){}

            
    }

/**
*Env&iacute; un objeto del tipo 'Protocolo' al servidor a trav&eacute;s del canal de salida
*/
    public void enviarPaquete(Protocolo paquetin){
        try {

            //Inicializa el socket del cliente
            sk = new Socket("localhost", 9999); 

            //Canal de salida de datos hacia el servidor
            dos = new ObjectOutputStream(sk.getOutputStream()); 

            /**
            *Envia el paquete al servidor a traves del canal de salida utilizando writeObject
            *de la clase java.io.OutputStream
            */
            dos.writeObject(paquetin); 

            dos.close();//cierra el canal de datos hacia el servidor
            sk.close(); //cierra el socket
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

/*
    public void run() {
       
        try {
            sk = new Socket("127.0.0.1", 9999);
            dos = new DataOutputStream(sk.getOutputStream()); //Canal de salida de datos hacia el servidor
            dis = new DataInputStream(sk.getInputStream()); //Canal de entrada de datos desde el servidor
            //System.out.println(id + " Solicitando inicio de sesión al ervidor");

            iniciarSesion();        
            dos.writeUTF("hola"); //envia un mensaje al servidor a traves del canal de salida

            String respuesta="";
            respuesta = dis.readUTF(); //Lee la respuesta del servidor desde el canal de entrada 
            System.out.println(id + " Servidor devuelve saludo: " + respuesta);

            while (true){

            }

            //dis.close(); //Cierra el canal de entrada
            //dos.close(); //Cierra el canal de salida
            //sk.close(); //cierra el socket
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    */


    public static void main(String[] args) {
        Cliente clientecito = new Cliente();

         
        clientecito.iniciarSesion();
    
    
    }
}
