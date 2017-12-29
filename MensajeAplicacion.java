
/**
*Clase que define la estructura de cada uno de los tres posibles mensajes de 
*la maquina de estados finita
*a) C&oacute;digo 20 -> C&oacute;digo pokemon aleatorio
*b) C&oacute;digo 21 ->  Captura pokemon
*c) C&oacute;digo 22 -> Código Pokemón cliente
*
*@author Diana Guerrero y Jorge Lázaro
*
*/
public class MensajeAplicacion{
	String[] mensajeAplicacion;

	public MensajeAplicacion(String[] mensaje){
		this.mensajeAplicacion = mensaje;
	}

/**
*Obtiene el codigo de uno de los tres mensajes diferentes
*/
	public String obtenerCodigo(){
		return mensajeAplicacion[0];
	}

/**
*Modifica el código de respuesta del mensaje
*/
	public void modificarCodigo(String c){
		mensajeAplicacion[0] = c;
	}	

/**
*Obtiene el ID de un pokemon en el mensaje
*/
	public String obtenerId(){
		return mensajeAplicacion[1];
	}

/**
*Obtiene el numero de intentos para capturar a un pokemon
*/
	public String obtenerNumAttemp(){
		return mensajeAplicacion[2];
	}


	public void modificarNumAttemp(String n){
		mensajeAplicacion[2] = n;
	}

/**
*Obtiene el tamaño de la imagen de un pokemon
*/
	public String obtenerImageSize(){
		return mensajeAplicacion[2];
	}

/**
*Obtiene la imágen de un pokemon
*/
	public String obtenerImage(){
		return mensajeAplicacion[3];
	}



	/**
	*imprime en la terminal la estructura del mensaje de aplicaci&oacute;n junto con los valores actuales que tiene almacenados 
	*/
	public String printMA(){
		String cadena="";
		for(int i=0; i< mensajeAplicacion.length; i++){
			cadena += "|"+ mensajeAplicacion[i];
		}
		return cadena;
	}
}