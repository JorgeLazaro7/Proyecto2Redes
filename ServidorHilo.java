/**
*Los canales de entrada y salida reciben y envian objetos
*Cada hilo que despache a un cliente, debe crear su propio socket para la consulta a la base de datos
*de esta forma garantizamos la integridad de la lectura y escritura de cada uno
*/

import java.io.*;
import java.net.*;
import java.util.logging.*;
import java.sql.*;
import mysql-connector-java-5.1.45-bin.org.java;
import mysql-connector-java-5.1.45-bin.com.java;


public class ServidorHilo extends Thread {
    private Socket socket; //socket del cliente 
    private ObjectOutputStream dos;//Canal de salida de datos hacia el cliente (envia datos al cliente)
    private ObjectInputStream dis;//Canal de entrada de datos desde el cliente (recibe datos del cliente)
    private Connection conexion; //Conexion a la BD

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
    }//constructor

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


    /**
    *Recibe un paquete (objeto del tipo protocolo)
    *Después envía el paquete recibido al metodo leerPaquete para determinar la acci&oacute;n a tomar
    */
    public void run() {
        
        try {
            //Recibe el paquete del cliente
            Protocolo paquetito = (Protocolo) dis.readObject();
            paquetito.print();//Imprimimos en pantalla el contenido del paquete

            leerPaquete(paquetito);
            
        } catch (Exception ex) {
            Logger.getLogger(ServidorHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
        desconnectar();
    }

/**
*Recibe un paquete
*Consulta el campo "Estado actual de la maquina" para conocer el tipo de mensaje
*Una vez que se obtiene el edoMaquina, se elige la opci&oacute;n a ejecutar en un switch-case
*/
    public void leerPaquete(Protocolo paquetin){
        int edoMaquina; //Indica el tipo de mensaje que se está recibiendo
        edoMaquina = paquetin.obtenerEM();

        //Case, dependiendo del estado de la maquina, se realiza una accion
        switch (edoMaquina){    
            case 10:
                System.out.println("opcion 10");
                break;                
            case 11:
                System.out.println("opcion 11");
                break;
            case 12:    
                System.out.println("Haz elegido iniciar sesión");
                iniciarSesion(paquetin);
                break;
            case 20:    
                System.out.println("20");
                break;
            case 21:    
                System.out.println("21");
                break;
            case 22:    
                System.out.println("22");
                break;
            case 23:    
                System.out.println("23");
                break;
            case 30:    
                System.out.println("30");
                break;
            case 31:    
                System.out.println("31");
                break;
            case 32:    
                System.out.println("32");
                break;

            default:
                System.out.println("Error en protocolo PKP: Mensaje "+ edoMaquina +" desconocido");
                break;


        }

    }


    public void iniciarSesion(Protocolo paquetin){

        String[] ma = paquetin.obtenerMA(); //obtiene el arreglo que representa a la seccion 'mensajeAplicacion'
        String user = ma[1]; //El nombre de usuario
        String pass = ma[2]; //La contraseña del usuario

        System.out.println("Usuario: "+user);
        System.out.println("pass: "+pass);

        conectar();
       


        

    }

    public Connection getConexion() {
        return conexion;
    }   

    public void setConexion(Connection conexion) {
        this.conexion = conexion;
    } 

    /**
    *Conecta con la base de datos
    *Es necesario descargar un Driver e instalarlo para lograr la conexion a la BD
    */
    public Connection conectar(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            String BaseDeDatos = "jdbc:mysql://localhost/appPokemon?user=root&password=Bull3tproof#!";
            setConexion(DriverManager.getConnection(BaseDeDatos)); 


            //Verificamos la conexion con la BD
            if (getConexion() != null) {
                System.out.println("Conexion exitosa!");
            } else {
                System.out.println("Conexion fallida!");
            }

        }catch (Exception e) {
                e.printStackTrace();
        }

        return conexion;

    }



}