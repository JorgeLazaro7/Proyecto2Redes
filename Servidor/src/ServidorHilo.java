/**
*Los canales de entrada y salida reciben y envian objetos
*Cada hilo que despache a un cliente, debe crear su propio socket para la consulta a la base de datos
*de esta forma garantizamos la integridad de la lectura y escritura de cada uno
*/

import java.io.*;
import java.net.*;
import java.util.logging.*;
import java.sql.*;



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
            

            leerPaquete(paquetito);
            
        } catch (Exception ex) {
            Logger.getLogger(ServidorHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
        desconnectar();
    }

/**
*Recibe un paquete
*Consulta el campo "Codigo de respuesta" para conocer el tipo de mensaje
*Una vez que se obtiene el codigoRespuesta, se elige la opci&oacute;n a ejecutar en un switch-case
*/
    public void leerPaquete(Protocolo paquetin){
        int codigoRespuesta; //Indica el tipo de mensaje que se está recibiendo
        int estadoMaquina;
        codigoRespuesta = paquetin.obtenerCR();
        estadoMaquina = paquetin.obtenerEM();

        //Case, dependiendo del estado de la maquina, se realiza una accion
        switch (codigoRespuesta){    
            case 10:
                //El cliente solicita un pokemon a capturar
                enviarPokemonAleatorio(paquetin);
                break;                
            case 11:
                verPokedex(paquetin);
                break;
            case 12:    
                iniciarSesion(paquetin);
                break;
            case 22:    
                System.out.println("22");
                break;
            case 23:    
                System.out.println("23");
                break;
            case 30:    
                aceptar(paquetin, estadoMaquina);//aplica cuando el cliente acepta atrapar un pokemon o intentar atraparlo de nuevo
                break;
            case 31:    
                System.out.println("31");
                break;
            case 32:    
                System.out.println("32");
                break;

            default:
                System.out.println("Error en protocolo PKP: Mensaje "+ codigoRespuesta +" desconocido");
                break;


        }

    }

    /**
*Env&iacute; un objeto del tipo 'Protocolo' al servidor a trav&eacute;s del canal de salida
*/
    public void enviarPaquete(Protocolo paquetin){
        try {

            /**
            *Envia el paquete al cliente a traves del canal de salida utilizando writeObject
            *de la clase java.io.OutputStream
            */
            dos.writeObject(paquetin); 

        } catch (IOException ex) {
            Logger.getLogger(ServidorHilo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


/**
*Obtiene de los argumentos del paquete "MensajeDeAplicacion" el nickname del usuario y su contraseña
*Envía a traves del socket un paquete de respuesta.
*
*Envía al cliente un paquete con un código de respuesta 24 y el id del usuario si el inicio de sesión fué exitoso
*o envía un paquete con código de respuesta 25 y un id = -1 si el inicio de sesión fué rechazado
*/
    public boolean iniciarSesion(Protocolo paquetin){

        String[] ma = paquetin.obtenerMA(); //obtiene el arreglo que representa a la seccion 'mensajeAplicacion'
        String user = ma[1]; //El nombre de usuario
        String pass = ma[2]; //La contraseña del usuario
        int id;
        ResultSet rs;

        Connection conexion = conectar(); //lo utilizaremos para la consulta a la BD


        //consulta a la base de datos
        try{
            Statement sentencia = conexion.createStatement();
            String query = "SELECT idUsuario FROM Usuario where nickname = '"+user+"' and password = SHA1('"+ pass+"')";
            
            rs = sentencia.executeQuery(query);//ejecuta la sentencia en la BD
     

            if(rs.next()){
                id = rs.getInt("idUsuario");//obtenemos el resultado de la BD
               
                conexion.close(); //se cierra la conexion con la BD
                System.out.println("Inicio de sesión exitoso"); 
                //Construimos el paquete de respuesta para enviar al cliente:
                Protocolo respuesta = new Protocolo(9999,1111,12,24,id,ma);
                enviarPaquete(respuesta);

                return true;
            }

            System.out.println("No existe el usuario/o la contraseña es incorrecta"); 
            conexion.close(); //se cierra la conexion con la BD

            //Construimos el paquete de respuesta para enviar al cliente
            Protocolo respuesta = new Protocolo(9999,1111,12,25,-1,ma);
            enviarPaquete(respuesta);

            return false;
            
            
            
        }catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
    *1- Consulta a la base cuantos registros de pokemon hay.
    *2- Elige un pokemon aleatorio para enviarlo al cliente
    *3- Envía al cliente un  paquete con el código de respuesta = 20 (¿capturar al pokemon x?)
    */
    public boolean enviarPokemonAleatorio(Protocolo paquetin){
        int np=-1; //El numero de pokemon que hay en la BD
        int aleatorio; //un numero aleatorio que representa el ID de un pokemon
        ResultSet rs;
        Connection conexion = conectar(); //lo utilizaremos para la consulta a la BD
        String query;
        Statement sentencia;
        String imagen="";//Imagen del pokemon
        String nombre=""; //nombre del pokemon

            //1-Consulta a la base cuantos registros de pokemon hay
        try{
            sentencia = conexion.createStatement();//creamos la conexion con la BD
            query = "select count(*) from Pokemon";

            rs = sentencia.executeQuery(query);//ejecuta la sentencia en la BD
     

            while(rs.next()){
                np = rs.getInt("count(*)");//obtenemos el resultado de la BD
            }




            //2-SOLICITAMOS UN POKEMON ALEATORIO
            aleatorio = (int) (Math.random() * np) + 1; //Selecciona un número aleatorio entre 1 y np

            query = "select * from Pokemon where idPokemon = "+aleatorio; //solicitamos un pokemon aleatorio a la BD

            rs = sentencia.executeQuery(query);//ejecuta la sentencia en la BD
            while(rs.next()){
                nombre = rs.getString("nombre");//obtenemos el resultado de la BD
                imagen = rs.getString("imagen");//obtenemos el resultado de la BD
            }

            conexion.close(); //se cierra la conexion con la BD

            //Construimos el paquete de respuesta para enviar al cliente:
            String[] m = new String[4];
            m[0] = "20";
            m[1] = Integer.toString(aleatorio);
            m[2] = imagen;
            m[3] = nombre;
            
            Protocolo respuesta = new Protocolo(9999,1111,2,20,paquetin.obtenerIdUsuario(),m);
            enviarPaquete(respuesta);
            respuesta.print();

                return true;

            
        }catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    public void verPokedex(Protocolo paquetin){

        ResultSet rs;
        
        String query="";
        Statement sentencia;
        String imagen="";//Imagen del pokemon
        String nombre=""; //nombre del pokemon
        int id= paquetin.obtenerIdUsuario();
        int prueba=-1;

        try{

            Connection conexion = conectar(); //lo utilizaremos para la consulta a la BD
            sentencia = conexion.createStatement();//creamos la conexion con la BD
            query = "SELECT nombre, imagen FROM (SELECT Usuario.idUsuario, idPokemon FROM Usuario JOIN Usuario_Pokemon ON Usuario.idUsuario=Usuario_Pokemon.idUsuario) AS T1 JOIN Pokemon ON T1.idPokemon=Pokemon.idPokemon WHERE idUsuario='"+id+"'";
            //query = "SELECT nombre, imagen FROM (SELECT Usuario.idUsuario, idPokemon FROM Usuario JOIN Usuario_Pokemon ON Usuario.idUsuario=Usuario_Pokemon.idUsuario) AS T1 JOIN Pokemon ON T1.idPokemon=Pokemon.idPokemon WHERE idUsuario='1'";
           
            rs = sentencia.executeQuery(query);//ejecuta la sentencia en la BD
            
            if(rs.wasNull())
              System.out.println("consulta vacía");  

            while(rs.next()){
                
                nombre = rs.getString(1);//obtenemos el resultado de la BD
                imagen += rs.getString(2);//obtenemos el resultado de la BD
                
            }

            conexion.close(); //se cierra la conexion con la BD

            //Construimos el paquete de respuesta para enviar al cliente:
            String[] m = new String[4]; //contruimos el "MensajeDeAplicacion"
            m[0] = "11";
            m[1] = Integer.toString(id);
            m[2] = imagen;
            
            
            Protocolo respuesta = new Protocolo(9999,1111,2,11,paquetin.obtenerIdUsuario(),m);
            enviarPaquete(respuesta);
            respuesta.print();


        }catch (SQLException e) {
            e.printStackTrace(); 
        }catch (Exception e) {
            System.out.println("Error " + e);
        }
    }

/**
*Dos posibles acciones
*Si el estado actual de la maquina es 2 (Desea capturar el pokemon ofrecido), envia al cliente un paquete con el número de intentos
*que tiene para capturarlo.
*
*
*Si el estado actual de la maquina es 4 (Desea reintentar capturar al pokemon) envia al cliente un paquete con el número
*de intentos que le restan para capturarlo
*/
public boolean aceptar(Protocolo paquetin, int estadoMaquina){
    if(estadoMaquina == 2){
        //Construimos el paquete de respuesta para enviar al cliente:
            String[] m = paquetin.obtenerMA(); //contruimos el "MensajeDeAplicacion"
            m[0] = "26";
            m[1] = m[1]; //se mantiene intacto, ya que es el ID del pokemon que se presentó previamente al usuario
            m[2] = "3"; //

        Protocolo respuesta = new Protocolo(9999,1111,2,26,paquetin.obtenerIdUsuario(),m);
        enviarPaquete(respuesta);
        respuesta.print();
        return true;
    }

    //Si el estado actual de la maquina es 4 
    //Construimos el paquete de respuesta para enviar al cliente:
        String[] m = paquetin.obtenerMA(); //contruimos el "MensajeDeAplicacion"
        m[0] = "21";
        m[1] = m[1]; //se mantiene intacto, ya que es el ID del pokemon que se presentó previamente al usuario
        int intentos = Integer.parseInt(m[2]);
        intentos -= 1;

        m[2]= Integer.toString(intentos);//convertimos de nuevo a String y almacenamos

        Protocolo respuesta = new Protocolo(9999,1111,4,21,paquetin.obtenerIdUsuario(),m);
        enviarPaquete(respuesta);
        respuesta.print();
        return true;

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
    *Añadimos useSSL=false para permitir correr al servidor sin un certificado SSL
    */
    public Connection conectar(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            String BaseDeDatos = "jdbc:mysql://localhost/appPokemon?user=root&password=Bull3tproof#!&useSSL=false";
            setConexion(DriverManager.getConnection(BaseDeDatos)); 


            //Verificamos la conexion con la BD
            if (getConexion() != null) {
                System.out.println("Conexion exitosa a la BD!");
            } else {
                System.out.println("Conexion fallida! a la BD");
            }

        }catch (Exception e) {
                e.printStackTrace();
        }

        return conexion;

    }



}