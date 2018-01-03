/**
*Los canales de entrada y salida reciben y envian objetos
*Cada hilo que despache a un cliente, debe crear su propio socket para la consulta a la base de datos
*de esta forma garantizamos la integridad de la lectura y escritura de cada uno
*
*El servidor crea un objeto Protocolo cada vez que inicia una conexión con el servidor
* lo iremos modificando cada vez que nuestro mensaje de respuesta cambie en algo), esto para no estar creando un nuevo objeto protocolo por cada 
*mensaje que se envía y ademas por que siempre serán pocos 
*cambios de una respuesta a otra
*@author Diana Guerrero y Jorge L&aacute;zaro
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

    private Protocolo prot; //El protocolo que se va a enviar 
    //(lo voy a inicializar en el constructor sin parámetros 
    //(el constructor por default) 
    int idPokemon;

    String imagenPokemon;
    String nombrePokemon;

    /**
    *CONSTRUCTOR
    *Recibe un socket del cliente
    *Asigna un hilo del servidor para que atienda a ese cliente
    */
    public ServidorHilo(Socket socket) {
        this.socket = socket;
        
        try {

            prot = new Protocolo();
            prot.modificarPuertoFuente(socket.getLocalPort());
            prot.modificarPuertoDestino(socket.getPort());

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
        boolean cerrar = false;
        while(!cerrar){
            try {
                //Recibe el paquete del cliente
                Protocolo paquetito = (Protocolo) dis.readObject();
                System.out.println("Protocolo recibido");
                paquetito.print();

                leerPaquete(paquetito);
                
            } catch (Exception ex) {
                Logger.getLogger(ServidorHilo.class.getName()).log(Level.SEVERE, null, ex);
                cerrar = true;
                System.out.println("Error desconocido");
            }
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
        int id;//El id del usuario que tiene asignado en la BD
        codigoRespuesta = paquetin.obtenerCR();
        estadoMaquina = paquetin.obtenerEM();

        //Case, dependiendo del estado de la maquina, se realiza una accion
        switch (codigoRespuesta){    
            case 10:
                //El cliente solicita un pokemon a capturar
                enviarPokemonAleatorio(paquetin);
                System.out.println("pidió un pokemon aleatorio");
                break;                
            case 11:
                verPokedex(paquetin);
                break;
            case 12:    
                iniciarSesion(paquetin);
                break;
            //case 21:
                //¿Intentar captura de nuevo? Quedan k intentos.
                //capturarDeNuevo(paquetin);
                //break;
            case 22:
                //Envía pokemon capturado    
                pokemonCapturado(paquetin);
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
                //El cliente cerró sesión  
                id = paquetin.obtenerIdUsuario();
                System.out.println("El cliente "+id +"cerró sesión");
                desconnectar();
                break;

            default:
                error(paquetin);

                break;


        }

    }

/**
*Envía un error 401 = El mensaje de codigoRespuesta que recibió el servidor es desconocido
*/

    public void error(Protocolo paquetin){

        System.out.println("SERVIDOR: Error en protocolo PKP: Mensaje "+ paquetin.obtenerCR() +" desconocido");
        paquetin.modificarCR(401);
        enviarPaquete(paquetin);
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
            dos.reset();
            dos.writeObject(paquetin); 

            System.out.println("Protocolo enviado");
            paquetin.print();

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
        int id=-1;
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
                //Se modifica el protocolo que ya existe desde un inicio con los datos que debo enviar, 
                //como puerto origen y destno siempre van a ser iguales, a esos no le muevo nada
                prot.modificarEM(0);
                prot.modificarCR(24);
                prot.modificarIdUsuario(id);

                ma[0] = "24";
                prot.modificarMA(ma);

                enviarPaquete(prot);

                return true;
            }

            System.out.println("No existe el usuario o la contraseña es incorrecta"); 
            conexion.close(); //se cierra la conexion con la BD

            //Construimos el paquete de respuesta para enviar al cliente
            prot.modificarEM(0);
            prot.modificarCR(25);
            prot.modificarIdUsuario(id);

            ma[0] = "25";
            prot.modificarMA(ma);

            enviarPaquete(prot);

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
            idPokemon = aleatorio;

            query = "select * from Pokemon where idPokemon = "+aleatorio; //solicitamos un pokemon aleatorio a la BD

            rs = sentencia.executeQuery(query);//ejecuta la sentencia en la BD
            while(rs.next()){
                nombre = rs.getString("nombre");//obtenemos el resultado de la BD
                imagen = rs.getString("imagen");//obtenemos el resultado de la BD
            }

            conexion.close(); //se cierra la conexion con la BD

            imagenPokemon = imagen;
            nombrePokemon = nombre;

            //Construimos el paquete de respuesta para enviar al cliente:
            String[] m = new String[4];
            m[0] = "20";
            m[1] = Integer.toString(aleatorio);
            m[2] = imagen;
            m[3] = nombre;

            // Igual que en iniciar sesion, cambio aqui para que en vez de crear un nuevo protocolo, modifico el que ya tengo.
            prot.modificarEM(2);
            prot.modificarCR(20);
            prot.modificarMA(m);
        

            enviarPaquete(prot);
            

                return true;

            
        }catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

/**
*Consulta en la Base de datos los pokemones que pertenecen al id de un usuario espec&iacute;fico
*Ya que las imágenes de los pokemones están representadas por un String, se concatenan las im&aacute;genes
*y desṕués se env&iacute;n a través de un paquete
*
*/
    public void verPokedex(Protocolo paquetin){

        ResultSet rs;
        
        String query="";
        Statement sentencia;
        String imagen="";//Imagen del pokemon
        String nombre=""; //nombre del pokemon
        int id= paquetin.obtenerIdUsuario();

        try{

            Connection conexion = conectar(); //lo utilizaremos para la consulta a la BD
            sentencia = conexion.createStatement();//creamos la conexion con la BD
            query = "SELECT nombre, imagen FROM (SELECT Usuario.idUsuario, idPokemon FROM Usuario JOIN Usuario_Pokemon ON Usuario.idUsuario=Usuario_Pokemon.idUsuario) AS T1 JOIN Pokemon ON T1.idPokemon=Pokemon.idPokemon WHERE idUsuario='"+id+"'";
           
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
            
            prot.modificarEM(8);
            prot.modificarCR(11);
            prot.modificarMA(m);
            //Protocolo respuesta = new Protocolo(9999,1111,2,11,paquetin.obtenerIdUsuario(),m);

            enviarPaquete(prot);
            prot.print();


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
    *
    *Verifica que el cliente a&uacute;n tenga intentos restantes, Si se agotaron los intentos, responde al cliente con el c&oacute;digo de respuesta 23 (Número de intentos de captura agotados.)
    *
    *Si a&uacute;n le restan intentos, envía al cliente el c&oacute;digo de respuesta 21 (¿Intentar captura de nuevo? Quedan k intentos.)
    *
    */
    public void aceptar(Protocolo paquetin, int estadoMaquina){
        if(estadoMaquina == 3){

            //Se le dice al servidor que si se quiere capturar al pokemon, 
            //(ya le dijimos al usuario que tiene hasta tres intentos). 
            //Aleatoriamente (puede ser 50/50) se decide si la captura tuvo exito o no
            //Si tiene exito se envia un CR 22 EM 5 y el pokemon que se capturó.
            //Si no tuvo exito se envía el protocolo con EM 4 CR 21 y en la segunda 
                //posicion del arreglo mensaje el número de intentos restantes (dos)

            captura(3); // El parámetro 3 corresponde al numero de intentos restantes

            
        }else if(estadoMaquina == 4){

            //Si el estado actual de la maquina es 4 Quiere decir que ya hubo al menos un intento 
            //fallido de captura y que el cliente pideió reintentar

            int intentosRestantes = Integer.parseInt(prot.obtenerMA()[2]); // Obtenemos los intentos restantes en el protocolo anterior

            captura(intentosRestantes);
        }

    }

    public void captura(int intentosR){
        double captura = Math.random(); // Generamos un número aleatorio entre 0 y 1 
                                            //para decidir si la captura fué exitosa o no

        if (captura < 0.5){
            // la captura fué exitosa registra la captura en la base de datos (insert) regresa protocolo con EM 5 CR 22 
            ////////////////////////////////////////////////////////////////////////
            
            // Se hace el insert a la base de datos Esto falta

            ////////////////////////////////////////////////////////////////////////
                
            prot.modificarEM(5);
            prot.modificarCR(22);
            String[] m = {"22", Integer.toString(idPokemon), imagenPokemon, nombrePokemon};
            prot.modificarMA(m);
            enviarPaquete(prot);

        }else{

            if(intentosR >=1) {
                // La captura falló se envia protocolo con mensaje 21 "¿Intentar capturar de nuevo?" 
                prot.modificarEM(4);
                prot.modificarCR(21);
                String[] m = {"21", Integer.toString(idPokemon), Integer.toString(intentosR - 1) , null};
                prot.modificarMA(m);
                enviarPaquete(prot);
            } else {
                prot.modificarEM(0);
                prot.modificarCR(23);
                String[] m = {"23", null, null, null};
            }
        }
    }


/**
*Devuelve al cliente la imágen del pokemón que se captur&oacute;
*/
    public void pokemonCapturado(Protocolo paquetin){
        ResultSet rs;
        String query;
        String imagen="";
        Statement sentencia=null;
        String[] m = paquetin.obtenerMA(); //obtenemos el arreglo "MensajeAplicacion"
        int id = Integer.parseInt(m[1]); //id del pokemon

        try{

            query = "select * from Pokemon where idPokemon = "+id; //solicitamos un pokemon aleatorio a la BD

            rs = sentencia.executeQuery(query);//ejecuta la sentencia en la BD
            while(rs.next()){
                imagen = rs.getString("imagen");//obtenemos el resultado de la BD
            }

                conexion.close(); //se cierra la conexion con la BD
        }catch (SQLException e) {
            e.printStackTrace();
        }

        
        m[0] = "22";
        m[1] = m[1];//se mantiene intacto, ya que es el ID del pokemon que se presentó previamente al usuario
        m[2] = "200";//tamaño de la imagen
        m[3] = imagen;

        prot.modificarEM(5);
        prot.modificarCR(22);
        prot.modificarMA(m);

        enviarPaquete(prot);
        prot.print();//imprmimos el protocolo

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
            String BaseDeDatos = "jdbc:mysql://localhost/appPokemon?user=root&password=password&useSSL=false";
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