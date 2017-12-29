public class Pokemon{
	
	private int id;
	private String nombre; 
	private String imagen;

	public int obtenerId(){
		return id;
	}

	public String obtenerNombre(){
		return nombre;
	}

	public int obtenerImagen(){
		return id;
	}

	public void modificarId(int nId){
		id = nId;
	}

	public void modificarNombre(String nNombre){
		nombre = nNombre;
	}

	public void modificarImagen(String nImagen){
		imagen=nImagen;
	}
}