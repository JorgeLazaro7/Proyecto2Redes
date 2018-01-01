import java.io.*;
import java.net.Socket;
import java.util.*;
import java.io.Console;
import java.util.logging.*;
import java.util.Arrays;

public class Cliente2 implements Serializable{
	protected Socket sk; //Socket del cliente
    protected ObjectOutputStream dos; //Canal de salida de datos hacia el servidor
    protected ObjectInputStream dis; //Canal de entrada de datos desde el servidor
    //private String ipDestino; // direccion ip del servidor
    private Protocolo prot;//Protocolo
    private boolean cerrar;
    String nickname;
    String[] mensajeAplicacion;
    boolean espera;

    //Constructor inicializamos el socket y un protocolo con valores por default
    public Cliente2(String ipDest){

    	try {
            //Inicializa el socket del cliente
            sk = new Socket(ipDest, 9999); 
            //Canales de comunicacion con el servidor entrada y salida
            dos = new ObjectOutputStream(sk.getOutputStream()); 
            dis = new ObjectInputStream(sk.getInputStream());

            prot = new Protocolo();
        	cerrar = false;

        	espera = false;

        	mensajeAplicacion = new String[4];

        } catch (IOException ex) {
            Logger.getLogger(Cliente2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void ejecutar(){
    	//Para empezar iniciamos sesion
    	//iniciarSesion();
    	//
    	boolean conectado = true;
    	while(!cerrar && conectado){
    		//if (!espera){
	    		try{
	    			Protocolo entrada = (Protocolo) dis.readObject();
	    			//espera = true;
	    			leerPaquete(entrada);
	    		} catch (Exception ex) {
	            	Logger.getLogger(Cliente2.class.getName()).log(Level.SEVERE, null, ex);
	            	System.out.println("no se recibió el paquete");
	            	//conectado=false;
	        	}
	        //}
    	}

    	desconectar();

    }

    public void leerPaquete(Protocolo entrada){

    	int codigoRespuesta = entrada.obtenerCR(); //Obtenemos codigo de respuesta y con base en el crearemos el paquete que se va a responder
    	Scanner in = new Scanner(System.in);

        switch(codigoRespuesta){
            case 24: 
                // Inicio de sesion exitoso
            	System.out.println("Inicio de sesión correcto\n\nBienvenido/a " + nickname);

                prot.modificarIdUsuario(entrada.obtenerIdUsuario());

                // Muestra menú (Ver pokedex, solicitar pokemon, salir)
            	System.out.println("Elige una opción:");
            	System.out.println("1: Buscar Pokemon");
            	System.out.println("2: Ver pokedex");
            	System.out.println("3: Salir\n");
            	System.out.println("Opción: -> ");
            	boolean valido = true;
            	do{
            		try {
	            	int opcion = in.nextInt();
	            	
			            switch(opcion){
			            	case 1:
			            		// Se envia código de solicitar pokemon aleatorio
			            		System.out.println("\nPediste un pokemon aleatorio");
			            		prot.modificarEM(2);
			            		prot.modificarCR(10);
			            		cambiaMA("10", null, null, null);
			            		prot.modificarMA(mensajeAplicacion);
			            		System.out.println("Protocolo que se va a enviar");
			            		prot.print();
			            		System.out.println("Mensaje de aplicacion que se va a enviar");
			            		System.out.println(Arrays.toString(prot.obtenerMA()));
			            		enviarPaquete(prot);
			            		//ejecutar();
			            		//espera= false;
			           			//cerrar= true;
			           		break;
			           		case 2:
			           			// Se envia código para ver pokedex
			           			System.out.println("\nPediste ver tu pokedex");
			           			//cerrar= true;
			           		break;
			           		case 3:
			           			System.out.println("\nAdios");
			           			cerrar = true;
			           		break;
			           		default:
		            			System.out.println("Opcion incorrecta");
		            			valido = false;
		            		break;
			            }
		            }catch (java.util.InputMismatchException e){
		            	System.out.println("Opcion incorrecta");
		            	valido = false;
            			in.nextLine();
        			}
	            } while(!valido); 

            break;
            case 25: //inicio de sesion fallido
            	System.out.println("Nickname o password inválidos\nVuelva a intentarlo");
            	iniciarSesion();
            	//ejecutar();
            	//cerrar = true;
            break;
            case 20:
            	System.out.println("¡Encontraste un pokemon!\n");
            	System.out.println(entrada.obtenerMA()[3] + "\n");
            	System.out.println(entrada.obtenerMA()[2] + "\n\n");
            	System.out.println("¿Quieres capturarlo?  s/n");

        }
    }

    //Construye el mensaje de respuesta con los parámetros que recibe (Si un mensaje no necesita tantos paráetros, estos serán null)
    public void cambiaMA(String codigo, String a, String b, String c){
    	mensajeAplicacion[0]=codigo;
    	mensajeAplicacion[1]=a;
    	mensajeAplicacion[2]=b;
    	mensajeAplicacion[3]=c;
    }


    public void iniciarSesion(){
    	Console c = System.console();
        
        nickname = new String (c.readLine("Nickname: "));
        String pass = new String (c.readPassword("Password: "));

        //Cambiamos los datos del protocolo para enviarlo con las credenciales
        prot.modificarPuertoFuente(sk.getLocalPort());
        prot.modificarPuertoDestino(9999);
        prot.modificarEM(0);
        prot.modificarCR(12);

        cambiaMA("12", nickname, pass, null);
        prot.modificarMA(mensajeAplicacion);
        /*
        //Creamos el mensaje de respuesta
        String[] m = new String[4];
        m[0] = "12";
        m[1] = nickname;
        m[2] = pass;
        m[3] = "";

        prot.modificarMA(m);
		*/

        enviarPaquete(prot);
    }

    public void enviarPaquete(Protocolo p){
    	try{
    		dos.reset();
    		dos.writeObject(p);
    	}catch (IOException ex) {
            Logger.getLogger(Cliente2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void desconectar(){
    	try {
            sk.close();
        } catch (IOException ex) {
            Logger.getLogger(Cliente2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {

    	System.out.println("¡Bienvenido/a!\nPara comenzar inicia sesión");
    	
    	Cliente2 clientecito = new Cliente2(args[0]);

    	clientecito.iniciarSesion();

    	clientecito.ejecutar();
    }

}