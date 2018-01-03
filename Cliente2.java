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
                    System.out.println("Protocolo recibido");
                    entrada.print();

	    			//espera = true;
	    			leerPaquete(entrada);
	    		} catch (Exception ex) {
	            	Logger.getLogger(Cliente2.class.getName()).log(Level.SEVERE, null, ex);
	            	System.out.println("no se recibió el paquete");
	            	//conectado=false;
                    cerrar = true;
                    System.out.println("Ocurrió un error, la aplicacion finalizará");
	        	}
	        //}
    	}

    	desconectar();

    }

    public void leerPaquete(Protocolo entrada){

    	int codigoRespuesta = entrada.obtenerCR(); //Obtenemos codigo de respuesta y con base en el crearemos el paquete que se va a responder
    	Scanner in = new Scanner(System.in);
        boolean valido = true;

        switch(codigoRespuesta){
            case 24: 
                // Inicio de sesion exitoso
            	System.out.println("Inicio de sesión correcto\n\nBienvenido/a " + nickname);

                prot.modificarIdUsuario(entrada.obtenerIdUsuario());

                menuInicial(); // menu, que deseas hacer? capturar pokemon, ver pokedex, salir

            break;
            case 25: //inicio de sesion fallido
            	System.out.println("Nickname o password inválidos\nVuelva a intentarlo");
            	iniciarSesion();
            	//ejecutar();
            	//cerrar = true;
            break;
            case 20:
            	System.out.println("\n\n¡Encontraste un pokemon!\n");
            	System.out.println(entrada.obtenerMA()[3] + "\n");
            	System.out.println(entrada.obtenerMA()[2] + "\n\n");
            	System.out.println("¿Quieres capturarlo? s/n \nTienes tres intentos");
                valido = true;
                do{
                    try{
                        String opcion2 = in.nextLine();
                        switch(opcion2){
                            case "s"://si
                                System.out.println("Elegiste capturarlo");
                                // método aceptar pasandole como parámetro solo el protocolo recibido, de ahi sacamos el estado de la máquina
                                aceptar(entrada);
                            break;
                            case "n"://no
                                System.out.println("Elegiste no capturar el pokemon \n\n");
                                menuInicial();
                        }
                    }catch (java.util.InputMismatchException e){
                        System.out.println("Opcion incorrecta");
                        valido = false;
                        in.nextLine();
                    }
                }while(!valido);
            break;
            case 21:
                System.out.println("\n\n¡Escapó! ¿Quieres intentar capturarlo de nuevo? s/n");
                valido = true;
                do{
                    try{
                        String opcion2 = in.nextLine();
                        switch(opcion2){
                            case "s"://si
                                System.out.println("Elegiste intentar de nuevo");
                                // método aceptar pasandole como parámetro solo el protocolo recibido, de ahi sacamos el estado de la máquina
                                aceptar(entrada);
                            break;
                            case "n"://no
                                System.out.println("Elegiste no capturar el pokemon \n\n");
                                menuInicial();

                        }
                    }catch (java.util.InputMismatchException e){
                        System.out.println("Opcion incorrecta");
                        valido = false;
                        in.nextLine();
                    }
                }while(!valido);
            break;
            case 22:
                System.out.println("\n\nFelicidades! capturaste un "+ entrada.obtenerMA()[3] + "\n\n");
                System.out.println(entrada.obtenerMA()[2]);
                System.out.println("\n\nPuedes verlo en tu pokedex \n \n \n");
                menuInicial();
            break;
            case 11:
                //recibe la pokedex
                System.out.println("Esta es tu pokedex");
                System.out.println(entrada.obtenerMA()[2] + "\n\n");
                menuInicial();
            break;
            default:
                System.out.println("Error en el protocolo: Mensaje " + codigoRespuesta + " desconocido");
                desconectar();
            break;
        }
    }

    public void menuInicial(){
        // Muestra menú (Ver pokedex, solicitar pokemon, salir)
        Scanner in = new Scanner(System.in);
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
                        prot.modificarEM(1);
                        prot.modificarCR(10);
                        cambiaMA("10", null, null, null);
                        prot.modificarMA(mensajeAplicacion);
                        enviarPaquete(prot);
                        //ejecutar();
                        //espera= false;
                        //cerrar= true;
                    break;
                    case 2:
                        // Se envia código para ver pokedex
                        System.out.println("\nPediste ver tu pokedex");
                        prot.modificarEM(8);
                        prot.modificarCR(11);
                        cambiaMA("11", null, null, null);
                        prot.modificarMA(mensajeAplicacion);
                        enviarPaquete(prot);
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
    }

    public void aceptar(Protocolo p){
        int estadoM = p.obtenerEM();
        prot.modificarCR(30);
        cambiaMA("30", null, null, null);
        prot.modificarMA(mensajeAplicacion);
        switch(estadoM){
            case 2: 
                System.out.println("Elegiste capturar el pokemon");
                prot.modificarEM(3);
                enviarPaquete(prot);
            break;
            case 4: 
                System.out.println("Elegiste volver a intentarlo");
                prot.modificarEM(4);
                enviarPaquete(prot);
            break;
            default:
                System.out.println("Error en el protocolo: Estado " + estadoM + " desconocido");
                desconectar();
            break;
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
            System.out.println("Protocolo que se va a enviar");
            prot.print();
            System.out.println("Mensaje de aplicacion que se va a enviar");
            System.out.println(Arrays.toString(prot.obtenerMA()));
    		dos.reset();
    		dos.writeObject(p);
    	}catch (IOException ex) {
            Logger.getLogger(Cliente2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void desconectar(){
    	try {
            sk.close();
            cerrar = true;
        } catch (IOException ex) {
            Logger.getLogger(Cliente2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {

        System.out.println("\n\n\n                                  ,'\\ \n" 
+"    _.----.        ____         ,'  _\\   ___    ___     ____\n"
+"_,-'       `.     |    |  /`.   \\,-'    |   \\  /   |   |    \\  |`.\n"
+"\\      __    \\    '-.  | /   `.  ___    |    \\/    |   '-.   \\ |  |\n"
+" \\.    \\ \\   |  __  |  |/    ,','_  `.  |          | __  |    \\|  |\n"
+"   \\    \\/   /,' _`.|      ,' / / / /   |          ,' _`.|     |  |\n"
+"    \\     ,-'/  /   \\    ,'   | \\/ / ,`.|         /  /   \\  |     |\n"
+"     \\    \\ |   \\_/  |   `-.  \\    `'  /|  |    ||   \\_/  | |\\    |\n"
+"      \\    \\ \\      /       `-.`.___,-' |  |\\  /| \\      /  | |   |\n"
+"       \\    \\ `.__,'|  |`-._    `|      |__| \\/ |  `.__,'|  | |   |\n"
+"        \\_.-'       |__|    `-._ |              '-.|     '-.| |   |\n"
+"                                `'                            '-._|\n");

    	System.out.println("¡Bienvenido/a!\nPara comenzar inicia sesión");
    	
    	Cliente2 clientecito = new Cliente2(args[0]);

    	clientecito.iniciarSesion();

    	clientecito.ejecutar();
    }

}