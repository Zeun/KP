package model;


import java.util.ArrayList;

public class Instance {
	private String nombreInstancia;
	private int numeroElementos;
	private double capacidadMochila;
	private double beneficioOptimo;
	private ArrayList<ArrayList<Double>> listaDisponibles = new ArrayList<>();
	private ArrayList<ArrayList<Double>> listaIngresados = new ArrayList<>();

	
	public Instance(){}
	
	public Instance(
					String nombreInstancia,		
					int numeroElementos,
					double capacidadMochila,
					double beneficioOptimo,
					ArrayList<ArrayList<Double>> listaDisponibles,
					ArrayList<ArrayList<Double>> listaIngresados) 
	{
		this.nombreInstancia = nombreInstancia;
		this.numeroElementos = numeroElementos;
		this.capacidadMochila = capacidadMochila;
		this.beneficioOptimo = beneficioOptimo;
		this.listaDisponibles = listaDisponibles;
		this.listaIngresados = new ArrayList<>();		
	}
	
	@Override
	public Instance clone(){
		Instance clone = new Instance();
		clone.nombreInstancia = this.nombreInstancia;
		clone.numeroElementos = this.numeroElementos;
		clone.capacidadMochila = this.capacidadMochila;
		clone.beneficioOptimo = this.beneficioOptimo;

		ArrayList<ArrayList<Double>> listaDisponiblesTemporal = new ArrayList<>();
		listaDisponiblesTemporal.addAll(listaDisponibles);
		listaDisponiblesTemporal.addAll(listaIngresados);
		
		clone.listaDisponibles = listaDisponiblesTemporal;
		clone.listaIngresados = new ArrayList<>();
		
		/*
		for (Integer temp : this.LCT) {clone.LCT.add(temp);}
		for (Integer temp : this.LCA) {clone.LCA.add(temp);}
		for (Integer temp : this.ciudadesCercanas) {clone.ciudadesCercanas.add(temp);}
		for (Integer temp : this.ciudadesCentrales) {clone.ciudadesCentrales.add(temp);}
		for (Integer temp : this.ciudadesLejanas) {clone.ciudadesLejanas.add(temp);}
		*/
		return clone;
	}
	
	@Override
	public String toString(){
		String response ="N: "+capacidadMochila+"\n";
		for (ArrayList<Double> iterable_element : listaDisponibles) {
			response += iterable_element;
			response += "\n";
		}
		response += "]\n";
		return response;
	}
	
	//TERMINALES
	
	public boolean agregarMasPesado(){
		if (listaDisponibles.size() == 0) {
			return false;
		}
		else {
			if (listaDisponibles.size() == 1) {
				if (verificarIngreso(listaIngresados, listaDisponibles.get(0).get(1))){
					listaIngresados.add(listaDisponibles.get(0));
					listaDisponibles.remove(0);
					return true;
				}
				else {
					return false;
				}
			}
			
			int posicion = 0;
			double costo = listaDisponibles.get(posicion).get(1);
			
			/* Se busca el elemento con mayor coste */
			for (int i = 0; i < listaDisponibles.size(); i++) {
				if (listaDisponibles.get(i).get(1) > costo) {
					posicion = i;					
					costo = listaDisponibles.get(i).get(1);
				}
			}
			//Verifico si no me paso de la capacidad total y lo agrego, CC false
			if (verificarIngreso(listaIngresados, listaDisponibles.get(posicion).get(1))){
				listaIngresados.add(listaDisponibles.get(posicion));
				listaDisponibles.remove(posicion);
				return true;	
			}
			else {
				return false;
			}
		}
	}
	
	public boolean agregarMenosPesado() {
		if (listaDisponibles.size() == 0) {
			return false;
		}
		else {
			if (listaDisponibles.size() == 1) {
				if (verificarIngreso(listaIngresados, listaDisponibles.get(0).get(1))) {
					listaIngresados.add(listaDisponibles.get(0));
					listaDisponibles.remove(0);
					return true;
				} else {
					return false;
				}
			}

			int posicion = 0;
			double costo = listaDisponibles.get(posicion).get(1);

			/* Se busca el elemento con mayor coste */
			for (int i = 0; i < listaDisponibles.size(); i++) {
				if (listaDisponibles.get(i).get(1) < costo) {
					posicion = i;
					costo = listaDisponibles.get(i).get(1);
				}
			}
			//Verifico si no me paso de la capacidad total y lo agrego, CC false
			if (verificarIngreso(listaIngresados, listaDisponibles.get(posicion).get(1))) {
				listaIngresados.add(listaDisponibles.get(posicion));
				listaDisponibles.remove(posicion);
				return true;	
			}
			else {
				return false;
			}
		}
	}
	
	public boolean agregarPrimeroDisponible(){
		if (listaDisponibles.size() == 0) {
			return false;
		}
		else {
			if (verificarIngreso(listaIngresados, listaDisponibles.get(0).get(1))) {
				listaIngresados.add(listaDisponibles.get(0));
				listaDisponibles.remove(0);
				return true;
			}			
			return false;
		}
	}
		
	public boolean agregarMayorBeneficio() {
		
		if (listaDisponibles.size() == 0) {
			return false;
		} else {
			if (listaDisponibles.size() == 1) {
				if (verificarIngreso(listaIngresados, listaDisponibles.get(0).get(1))) {
					listaIngresados.add(listaDisponibles.get(0));
					listaDisponibles.remove(0);
					return true;
				} else {
					return false;
				}
			}
			int posicion = 0;
			double beneficio = listaDisponibles.get(posicion).get(2);
			
			// Se busca el elemento con mayor beneficio
			for (int i = 0; i < listaDisponibles.size(); i++) {
				if (listaDisponibles.get(i).get(2) > beneficio) {
					posicion = i;
					beneficio = listaDisponibles.get(i).get(2);					
				}
			}
			// Verifico si no me paso de la capacidad total y lo agrego, CC false
			if (verificarIngreso(listaIngresados, listaDisponibles.get(posicion).get(1))) {
				listaIngresados.add(listaDisponibles.get(posicion));
				listaDisponibles.remove(posicion);
				return true;	
			}
			else {
				return false;
			}
		}
	}
	
	public boolean agregarMayorGanancia() {
		if (listaDisponibles.size() == 0) {
			return false;
		}
		else {
			// ganancia = beneficio/peso
			if (listaDisponibles.size() == 1) {
				if (verificarIngreso(listaIngresados, listaDisponibles.get(0).get(1))) {
					listaIngresados.add(listaDisponibles.get(0));
					listaDisponibles.remove(0);
					return true;
				} else {
					return false;
				}
			}
			
			int posicion = 0;
			double ganancia = listaDisponibles.get(posicion).get(3);
			
			for (int i = 0; i < listaDisponibles.size(); i++) {
				if (listaDisponibles.get(i).get(3) > ganancia) {
					posicion = i;
					ganancia = listaDisponibles.get(i).get(3);
				}
			}
			//Verifico si no me paso de la capacidad total y lo agrego, CC false
			if (verificarIngreso(listaIngresados, listaDisponibles.get(posicion).get(1))) {
				listaIngresados.add(listaDisponibles.get(posicion));
				listaDisponibles.remove(posicion);
				return true;	
			}
			else {
				return false;
			}			
		}
	}
	
	public boolean eliminarPeorBeneficio() {
		
		if (listaIngresados.size() == 0) {
			return false;
		}
		else {
			if (listaIngresados.size() == 1) {
				listaDisponibles.add(listaIngresados.get(0));
				listaIngresados.remove(0);
				return true;
			}
			
			int posicion = 0;
			double beneficio = listaIngresados.get(posicion).get(2);
			
			/* Se busca el elemento con menor beneficio entre los que ya han ingresado*/
			for (int i = 0; i < listaIngresados.size(); i++) {
				if (listaIngresados.get(i).get(2) < beneficio) {
					posicion = i;
					beneficio = listaIngresados.get(i).get(2);					
				}
			}
			//Elimino el elemento con menor beneficio
			listaDisponibles.add(listaIngresados.get(posicion));
			listaIngresados.remove(posicion);
			return true;
		}
	}
	
	public boolean eliminarPeorGanancia() {
		if (listaIngresados.size() == 0) {
			return false;
		}
		else {
			
			if (listaIngresados.size() == 1) {
				listaDisponibles.add(listaIngresados.get(0));
				listaIngresados.remove(0);
				return true;
			}
			
			int posicion = 0;
			double ganancia = listaIngresados.get(posicion).get(3);
			
			/* Se busca el elemento con peor ganancia (beneficio/peso) entre los que ya han ingresado*/
			for (int i = 0; i < listaIngresados.size(); i++) {
				if (listaIngresados.get(i).get(3) < ganancia) {
					posicion = i;
					ganancia = listaIngresados.get(i).get(3);					
				}
			}
			//Elimino el elemento con mayor ganancia			
			listaDisponibles.add(listaIngresados.get(posicion));
			listaIngresados.remove(posicion);
			return true;
		}	
	}
	
	public boolean eliminarMasPesado() {
		if (listaIngresados.size() == 0) {
			return false;
		}
		else {
			
			if (listaIngresados.size() == 1) {
				listaDisponibles.add(listaIngresados.get(0));
				listaIngresados.remove(0);
				return true;
			}
			
			int posicion = 0;
			double peso = listaIngresados.get(posicion).get(1);
			
			/* Se busca el elemento con peor PESO entre los que ya han ingresado*/
			for (int i = 0; i < listaIngresados.size(); i++) {
				if (listaIngresados.get(i).get(1) > peso) {
					posicion = i;
					peso = listaIngresados.get(i).get(1);					
				}
			}
			//Elimino el elemento con mayor peso			
			listaDisponibles.add(listaIngresados.get(posicion));
			listaIngresados.remove(posicion);
			return true;
		}	
	}

	public boolean isFull(){
		if (listaDisponibles.size() == 0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean isTrue(){
		return true;
	}
	
	/* Metodos utilizados por los terminales */ 
	
	/**
	 * M�todo que verifica si un elemento puede ser agregado a la mochila.
	 * @param listaIngresados lista de elementos que se encuentran en la mochila
	 * @param costoAIngresar costo/peso del elemento que se desea ingresar
	 * @return verdadero si se puede ingresar ese el elemento a la mochila, falso cc.
	 */
	private boolean verificarIngreso(ArrayList<ArrayList<Double>> listaIngresados, double costoAIngresar) {
		double costoTotal = 0.0;
		if (listaIngresados.size() != 0) {
			ArrayList<ArrayList<Double>> listaIngresadosTemporal = new ArrayList<>(listaIngresados);
			for (int i = 0; i < listaIngresadosTemporal.size(); i++) {
				costoTotal += listaIngresadosTemporal.get(i).get(1);
			}
		}
		costoTotal += costoAIngresar;
		if (costoTotal <= capacidadMochila) {
			return true;
		}
		else {
			return false;
		}	
//		return true;
	}
	
	/**
	 * M�todo para obtener el costo (peso) total de la mochila
	 * @return peso de la mochila
	 */
	public double costoTotal() {	
		double costoTotal = 0.0;
		if (listaIngresados.size() > 0) {
			for (int i=0; i<listaIngresados.size();i++) {				
				costoTotal += listaIngresados.get(i).get(1);
			}
		}
//		else {
//			costoTotal = 400000000.0;
//		}
		return costoTotal;
	}
	
	/**
	 * M�todo para obtener el beneficio total de la mochila
	 * 
	 * @return beneficio de la mochila
	 */
	public double beneficioTotal() {
		double beneficioTotal = 0.0;
		if (listaIngresados.size() > 0) {
			for (int i = 0; i < listaIngresados.size(); i++) {
				beneficioTotal += listaIngresados.get(i).get(2);
			}
		}
		// else {
		// beneficioTotal = -400000000.0;
		// }
		return beneficioTotal;
	}
	
	/**
	 * M�todo utilizado para mostrar en pantalla los elementos actualmente
	 * agregados a la mochila. Adem�s se muestra el beneficio obtenido vs el �ptimo y 
	 * la capacidad de la mochila en el momento vs la capacidad de la mochila total
	 * @return estado de la mochila
	 */
	public String printResult() {
		ArrayList<Double> arrayList2 = new ArrayList<>();
		for (ArrayList<Double> arrayList : listaIngresados) {
			arrayList2.add(arrayList.get(0));
		}
		String response = "Lista Ingresados = " + arrayList2 + "\n";
		response += "Beneficio = " + this.beneficioTotal() + " / " + this.beneficioOptimo + "\n";
		response += "Costo = " + this.costoTotal() + " / " + this.capacidadMochila() + "\n";
		return response;
	}
	
	public boolean isNew(){
		if (listaIngresados.size() > 0) {
			return false;
		}
		else {
			return true;
		}
	}

	public double capacidadMochila() {
		return capacidadMochila;
	}
	
	public double beneficioOptimo() {
		return beneficioOptimo;
	}

	public ArrayList<ArrayList<Double>> getlistaDisponibles() {
		return listaDisponibles;
	}	
	
	public int numeroElementos() {
		return numeroElementos;
	}

	public ArrayList<ArrayList<Double>> getListadoIngresados() {
		return listaIngresados;
	}
	
	public String nombreInstancia(){
		return nombreInstancia;
	}	
}

