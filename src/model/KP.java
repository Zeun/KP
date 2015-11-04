package model;
public class KP {	

	/**************************
	 * 	TERMINALES
	 **************************/
	
	public static boolean agregarMasPesado (Instance ins) {
		return ins.agregarMasPesado();
	}
	
	public static boolean agregarMenosPesado (Instance ins) {
		return ins.agregarMenosPesado();
	}
	
	public static boolean agregarPrimeroDisponible (Instance ins) {
		return ins.agregarPrimeroDisponible();
	}
	
	public static boolean agregarMayorBeneficio (Instance ins) {
		return ins.agregarMayorBeneficio();
	}
	
	public static boolean agregarMayorGanancia (Instance ins) {
		return ins.agregarMayorGanancia();
	}
	
	public static boolean eliminarPeorGanancia (Instance ins) {
		return ins.eliminarPeorGanancia();
	}
	
	public static boolean eliminarPeorBeneficio (Instance ins) {
		return ins.eliminarPeorBeneficio();
	}
	
	public static boolean eliminarMasPesado (Instance ins) {
		return ins.eliminarMasPesado();
	}
	
	public static boolean isFull (Instance ins) {
		return ins.isFull();
	}
	
	public static boolean isTrue (Instance ins) {
		return ins.isTrue();
	}

}