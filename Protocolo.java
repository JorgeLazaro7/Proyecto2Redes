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

int ipFuente;
int ipDestino;
int estadoMaquina;
int codigoRespuesta=-1;
int idUsuario;
String[] mensajeAplicacion;


/**
*Constructor
*ipFuente e ipDestino son equivalente a Puerto fuente y puerto destino respectivamente
*estadoMaquna -> Uno de muchos estados que puede presentar la maquina de estados finitos, este parámetro indica el estado actual
*codigoRespuesta -> Son los c&oacute;digos de mensajes del servidor, cliente y cliente - servidor
*mensajeAplicacion -> representa los mensajes que se envian entre el servidor y el cliente
*/

	public Protocolo(int ipFuente, int ipDestino, int estadoMaquina, int codigoRespuesta, int idUsuario, String[] mensajeAplicacion){
		this.ipFuente = ipFuente;
		this.ipDestino = ipDestino;
		this.estadoMaquina = estadoMaquina;
		this.codigoRespuesta = codigoRespuesta;
		this.idUsuario = idUsuario;
		this.mensajeAplicacion = mensajeAplicacion;
	}


	public int obtenerIpFuente(){
		return ipFuente;
	}

	public int obtenerIpDestino(){
		return ipDestino;
	}

	public void modificarIp(int ipSource, int ipDestiny){
		ipFuente = ipSource;
		ipDestino = ipDestiny;
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
	public int obtenerNuevoCR(){
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
		System.out.println("|		"+ ipFuente + " 	|  	" + ipDestino + "	|");
		System.out.println("|-----------------------------------------|");
		System.out.println("|  "+ estadoMaquina + "  |  " + codigoRespuesta + "  |  " + printMA(mensajeAplicacion));
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
