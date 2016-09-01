package model;

import java.io.*;
import java.util.*;

import ec.util.*;
//import ec.util.Output;

public class FileIO {
	public static int newLog(Output output, String filename) throws IOException {
		// System.out.println(filename);
		// FileWriter fw = new FileWriter(filename, false);
		// fw.write("");

		File file = new File(filename);
		// fw.close();
		return output.addLog(file, true);
	}

	public static void readInstances(ArrayList<KPData> data, final File folder) throws IOException {
		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				readInstances(data, fileEntry);
			} else {
				System.out.println("Leyendo: " + fileEntry.getName());
				KPData kp = new KPData();
				kp.setInstance(readFile(fileEntry.getPath()));
				data.add(kp);
			}
		}
	}

	private static Instance readFile(String filename) throws IOException {
		/*
		 * Considerando que el formato de los archivos de entrada es el
		 * siguiente:
		 * numeroElementos capacidadMochila beneficioOptimo
		 * numElemento1	costoElemento1 beneficioElemento1 
		 * numElemento2 costoElemento2 beneficioElemento2
		 * ...
		 * numElementoN costoElementoN beneficioElementoN
		 */

		File file = new File(filename);

		Scanner archivoEntrada = new Scanner(file);
		// Nombre instancia
		String nombreInstancia = file.getName();
		// Num elementos mochila
		int numeroElementos = archivoEntrada.nextInt();
		// Capacidad max de la mochila
		double capacidadMochila = (double) archivoEntrada.nextInt();
		// Optimo de la mochila
		double beneficioOptimo = (double) archivoEntrada.nextInt();
		// Lista de elementos disponibles para agregar a la mochila
		ArrayList<ArrayList<Double>> listaDisponibles = new ArrayList<>();
		ArrayList<Double> listTemp;
		// Lista de elementos ingresados en la mochila
		ArrayList<ArrayList<Double>> listaIngresados = new ArrayList<>();
		
		for (int i = 0; i < numeroElementos; i++) {
			listTemp = new ArrayList<>();
			for (int j = 0; j < 3; j++) { // Agregando num, profit, weight
				listTemp.add((double) archivoEntrada.nextInt());
			}
			listTemp.add(listTemp.get(1) / (double) listTemp.get(2)); // Agregando
																		// profit/weight
			listaDisponibles.add(listTemp);
		}

		archivoEntrada.close();
		return new Instance(nombreInstancia ,numeroElementos, capacidadMochila, beneficioOptimo, listaDisponibles, listaIngresados);

	}

	public static void repairDot(int JOB_NUMBER, int JOBS, int subpopulation, String filename) throws IOException {
		File file;
		int jobs = JOBS;
		System.out.println(jobs);
		if (jobs == 1) {
			file = new File("out/" + filename + "/evolution" + JOB_NUMBER + "/BestIndividual.dot");
		} else {
			file = new File("out/" + filename + "/evolution" + JOB_NUMBER + "/job." + JOB_NUMBER + ".BestIndividual.dot");
		}
		Scanner s = new Scanner(file);
		StringBuilder buffer = new StringBuilder();
		int i = 1;
		String label = "";
		String txt;
		while (s.hasNextLine()) {
			txt = s.nextLine();
			if (!txt.isEmpty()) {
				if (i == 1) {
					// label = s.nextLine();
					label = txt;
				} else if (i > 4) {
					// txt = s.nextLine();
					buffer.append(txt + "\n");
					if (i == 5)
						buffer.append(label + "\n");
				}
				i++;
				
			} else {
				writeFile(buffer.toString(),
						"out/" + filename +"/evolution" + JOB_NUMBER + "/job"+ JOB_NUMBER + ".subpop" + subpopulation + ".BestIndividual.dot");
				buffer.setLength(0);
				i = 1;
				label = "";
			}
		}
		s.close();
	}

	public static void writeFile(String line, String filename) throws IOException {
		File file = new File(filename);
		// System.out.println("filename : " + filename);
		// System.out.println("file name write : " + file);
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		// System.out.println("file name write : " + file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(line);
		bw.close();
		fw.close();
	}

	public static void dot_a_png(int job_number, int subpopulation, String name) {
		try {
			System.out.println("[dot_a_png]");
			String dotPath = "C:/Program Files (x86)/Graphviz2.38/bin/dot.exe";
			
			String fileInputPath = "out/" + name + "/evolution" + job_number + "/job"+ job_number + ".subpop" + subpopulation + ".BestIndividual.dot";
			String fileOutputPath = "out/" + name + "/evolution" + job_number + "/job"+ job_number + ".subpop" + subpopulation + ".BestIndividual.png";
			// System.out.println(dotPath);
			 System.out.println(fileInputPath);
			// System.out.println(fileOutputPath);

			Runtime rt = Runtime.getRuntime();
			rt.exec(dotPath + " -Tpng " + fileInputPath + " -o " + fileOutputPath);

		} catch (IOException ioe) {
			System.out.println(ioe);
		} finally {
		}

	}
}
