/**
*Clase que define la estructura de un protocolo.
*Contiene metodos get y set para actualizar los datos que lleva
*
*Implementa la clase serialzable, para que los objetos del tipo 'Protocolo' puedan enviarse a
*traves de sockets
*
*@author Diana Guerrero y Jorge Lázaro
*
*/
import java.io.Serializable;

public class Protocolo implements Serializable{

private int puertoFuente;
private int puertoDestino;
private int estadoMaquina;
private int codigoRespuesta;
private int idUsuario;
private String[] mensajeAplicacion;


/**
*Constructor
*ipFuente e ipDestino son equivalente a Puerto fuente y puerto destino respectivamente
*estadoMaquna -> Uno de muchos estados que puede presentar la maquina de estados finitos, este parámetro indica el estado actual
*codigoRespuesta -> Son los c&oacute;digos de mensajes del servidor, cliente y cliente - servidor
*mensajeAplicacion -> representa los mensajes que se envian entre el servidor y el cliente
*/

	public Protocolo(int puertoFuente, int puertoDestino, int estadoMaquina, int codigoRespuesta, int idUsuario, String[] mensajeAplicacion){
		this.puertoFuente = puertoFuente;
		this.puertoDestino = puertoDestino;
		this.estadoMaquina = estadoMaquina;
		this.codigoRespuesta = codigoRespuesta;
		this.idUsuario = idUsuario;
		this.mensajeAplicacion = mensajeAplicacion;
	}

	public Protocolo(){
		puertoFuente = -1;
		puertoDestino = -1;
		estadoMaquina = -1;
		codigoRespuesta = -1;
		idUsuario = -1;
		mensajeAplicacion = null;
	}


	public int obtenerPuertoFuente(){
		return puertoFuente;
	}

	public int obtenerPuertoDestino(){
		return puertoDestino;
	}

	public void modificarPuertoFuente(int sourcePort){
		puertoFuente = sourcePort;
	}

	public void modificarPuertoDestino(int destPort){
		puertoDestino = destPort;
	}

	/**
	*Devuelve el estado de la maquina
	*/

	public int obtenerEM(){
		return estadoMaquina;
	}

	/**
	*Modifica el estado actual de la maquina 
	*/
	public void modificarEM(int nuevoEM){
		estadoMaquina =	nuevoEM;
	}

	public int obtenerIdUsuario(){
		return idUsuario;
	}

	/**
	*Devuelve el c&oacute;digo de respuesta
	*/
	public int obtenerCR(){
		return codigoRespuesta;
	}

	/**
	*Modifica el codigo de respuesta 
	*/
	public void modificarCR(int nuevoCR){
		codigoRespuesta = nuevoCR;
	}


	/**
	*Devuelve el arreglo que representa el estado de la aplicací&oacute;n
	*@return El atributo mensajeAplicacion que representa los mensajes que se envian entre el servidor y el cliente
	*/
	public String[] obtenerMA(){
		return mensajeAplicacion;
	}

	/**
	*Obtiene la posici&oacute;n cero del arreglo 'mensajeAplicacion' que equivale al c&oacute;digo de los mensajes
	*entre el servidor y el cliente
	*@return El c&oacute;digo de los mensajes entre el servidor y el cliente
	*/
	public String obtenerCodigo(){
		return mensajeAplicacion[0];
	}

	public String printMA(String[] ma){
		String mensaje = " ";

		for(int i=0; i < ma.length; i++){
			mensaje += ma[i] + " | ";
		}
		return mensaje;
	}

	/**
	*imprime en la terminal la estructura del protocolo junto con los valores actuales que tiene almacenados 
	*/
	public void print(){
		System.out.println("|-----------------------------------------|");
		System.out.println("|		"+ puertoFuente + " 	|  	" + puertoDestino + "	|");
		System.out.println("|-----------------------------------------|");
		System.out.println("|  "+ estadoMaquina + "  |  " + codigoRespuesta + " | "+idUsuario +"  |  " + printMA(mensajeAplicacion));
		System.out.println("|-----------------------------------------|");
	}

	

	public static void main(String[] args){
		String[] m = new String[4];
		m[0] = "20";
		m[1] = "1";
		m[2] = "";
		m[3] = "";
		
		
		
	}

}
