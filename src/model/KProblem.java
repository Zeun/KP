package model;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import ec.*;
import ec.gp.*;
import ec.gp.koza.KozaFitness;
import ec.simple.SimpleProblemForm;
import ec.util.*;

public class KProblem extends GPProblem implements SimpleProblemForm {

	private static final long serialVersionUID = -8430160211244271537L;
	
	public static int LOG_FILE;
	public static int RESULTS_FILE;
	public static int HEURISTICS_FILE;
	public static int DOT_FILE;
	public static int JOB_NUMBER;
	public static double ALFA = 0.95;
	public static double BETA = 1- ALFA;
	public static long startGenerationTime;
	public static long endGenerationTime;
	public static String semillas;
	public static int elites;
	public static final double IND_MAX_REL_ERR = 0.01;
	public static final double IND_MAX_NODES = 15.0;
	public static int JOBS;
	public static int SUBPOPS;
	// Numero de generaciones: state.numGenerations
	ArrayList<KPData> data_island1;
	ArrayList<KPData> data_island2;
	ArrayList<ArrayList<KPData>> data;
	
	@Override
	public KProblem clone() {
		KProblem kp = (KProblem) super.clone();
		return kp;
	}
	
	@Override
	public void setup(final EvolutionState state, final Parameter base) {	
		JOB_NUMBER = ((Integer)(state.job[0])).intValue();
		super.setup(state, base);
		
		if (!(input instanceof KPData)){
			state.output.fatal("Obteniendo instancias de prueba desde archivo");
		}
		JOBS =  state.parameters.getInt(new ec.util.Parameter("jobs"), null);	
		SUBPOPS =  state.parameters.getInt(new ec.util.Parameter("pop.subpops"), null);
		elites =  state.parameters.getInt(new ec.util.Parameter("breed.elite.0"), null);	
		semillas =  state.parameters.getString(new ec.util.Parameter("seed.0"), null);
		data_island1 = new ArrayList<KPData>();
		data_island2 = new ArrayList<KPData>();
		data = new ArrayList<ArrayList<KPData>>();
		
		try {
			LOG_FILE = FileIO.newLog(state.output, "out/KPLog.out");
			(new File("out/results/evolution" + JOB_NUMBER)).mkdirs();
			RESULTS_FILE = FileIO.newLog(state.output, "out/results/evolution" + JOB_NUMBER + "/KPResults.out");
			state.output.print("Generacion" + ", ", RESULTS_FILE);
			state.output.print("N° Gen" + ", ", RESULTS_FILE);
			state.output.print("Isla" + ", ", RESULTS_FILE);
			state.output.print("N° Islas" + ", ", RESULTS_FILE);
			state.output.print("Tiempo(ms)" + ", ", RESULTS_FILE);
			state.output.print("Individuo" + ", ", RESULTS_FILE);
			state.output.print("Obtenido" + ", ", RESULTS_FILE);
			state.output.print("Óptimo" + ", ", RESULTS_FILE);
			// state.output.print("N° Elementos" + ", ", RESULTS_FILE);
			state.output.print("Error relativo" + ", ", RESULTS_FILE);
			state.output.print("Hits" + ", ", RESULTS_FILE);
			state.output.print("Error relativo tamaño árbol" + ", ", RESULTS_FILE);
			state.output.print("Profundidad árbol" + ", ", RESULTS_FILE);
			state.output.println("Tamaño árbol" + ", ", RESULTS_FILE);
			state.output.println("Nombre Instancia", RESULTS_FILE);
			
			// DOT_FILE = FileIO.newLog(state.output, "out/results/evolution" + JOB_NUMBER + "/job." + JOB_NUMBER + ".BestIndividual.dot");
			DOT_FILE = FileIO.newLog(state.output, "out/results/evolution" + JOB_NUMBER + "/BestIndividual.dot");
			//System.out.println("Archivo de salida: " + DOT_FILE);
			final File folder_island1;
			// Si tengo más de una población, uso 2 grupos de instancias
			if (SUBPOPS > 1){
				folder_island1 = new File("data/evaluacion_island1");
				final File folder_island2 = new File("data/evaluacion_island2");
				FileIO.readInstances(data_island2, folder_island2);
				data.add(data_island2);
			} else {
				folder_island1 = new File("data/evaluacion_canonico");
			}
			FileIO.readInstances(data_island1, folder_island1);

			data.add(data_island1);
			
			System.out.println("Lectura desde archivo terminada con Exito!");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("KProblem: Evolucionando...");
		startGenerationTime = System.nanoTime();	
	}

	@Override
	public void evaluate(
			final EvolutionState state,
	        final Individual individual,
	        final int subpopulation,
	        final int threadnum) {
		
			if (!individual.evaluated) {
			
			//KPData auxData;
			
			GPIndividual gpind = (GPIndividual) individual;
			
			state.output.println("Generacion: " + state.generation + "\nSubpopulation: " + subpopulation + "\nKProblem: evaluando el individuo [" + gpind.toString() + "]\n", LOG_FILE);
			gpind.printIndividualForHumans(state, LOG_FILE);
			
			int hits = 0;
			double errRelativoAcumulado = 0.0,
					errorRelativoTamañoArbol = 0.0,
					errorRelativo,
					errorRelativoPeso,
					pobSize = data.get(subpopulation % 2).size();
			
			// Si el tamaño del árbol es mayor al permitido se calcula el error relativo de éste
			if (gpind.size() > IND_MAX_NODES) {
				errorRelativoTamañoArbol = Math.abs(IND_MAX_NODES - gpind.size() ) / IND_MAX_NODES;
			}
			state.output.println("\n---- Iniciando evaluacion ---\nNum de Nodos:" + gpind.size(), LOG_FILE);
			
			// El individuo es evaluado en cada una de las instancias
			for (int i = 0; i < pobSize; i++) {
				// Carga de datos de la instancia a evaluar
				Instance auxData = new Instance();
				auxData = data.get(subpopulation%2).get(i).getInstance().clone();
				KPData aux = new KPData();
				aux.instance = auxData;
				// Escribir individuos en formato dot
				gpind.trees[0].printStyle = GPTree.PRINT_STYLE_DOT;				
				
				// Variables cronómetro
				long timeInit, timeEnd;
				timeInit = System.nanoTime();
				
				// Evaluar el individuo gpind para la instancia actual
				gpind.trees[0].child.eval(state, threadnum, aux, stack, gpind, this);	//evaluar el individuo gpind para la instancia i
				
				// Fin del tiempo de evaluación
				timeEnd = System.nanoTime();
				
				// Error relativo de la diferencia entre el resultado obtenido y el óptimo
				errorRelativo = Math.abs( auxData.beneficioTotal() - auxData.beneficioOptimo())/(auxData.beneficioOptimo());
				
				// Error de peso en caso de que me pase (penalizacion)
				if (auxData.costoTotal() > auxData.capacidadMochila()) {
					errorRelativoPeso = auxData.costoTotal() - auxData.capacidadMochila();
					errorRelativoPeso /= auxData.capacidadMochila();
				} else {
					errorRelativoPeso = 0.0;
				}
				
				// Número de hits (sólo cuentan si es solución factible y tiene
				// error menor a un porcentaje determinado
				if (errorRelativo <= IND_MAX_REL_ERR && errorRelativoPeso == 0.0) {
					hits++;
					// System.out.println(auxData.printResult());
				}
				
				// Descomentar para ver circuito en pantalla
//				if (errorRelativo < 0.2 && errorRelativoPeso == 0.0) {
//					System.out.println(auxData.printResult());
//				}

				// Log de resultados por instancia
				state.output.print(state.generation + ", ", RESULTS_FILE);
				state.output.print(state.numGenerations + ", ", RESULTS_FILE);
				state.output.print(subpopulation + ", ", RESULTS_FILE);
				state.output.print(SUBPOPS + ", ", RESULTS_FILE);
				state.output.print((timeEnd - timeInit) / 1000000 + ", ", RESULTS_FILE);
				state.output.print(gpind.toString() + ", ", RESULTS_FILE);
				state.output.print(auxData.beneficioTotal() + ", ", RESULTS_FILE);
				state.output.print(auxData.beneficioOptimo() + ", ", RESULTS_FILE);
				// state.output.print(auxData.numeroElementos() + ", ", RESULTS_FILE);
				state.output.print(errorRelativo + ", ", RESULTS_FILE);
				state.output.print(hits + ", ", RESULTS_FILE);
				state.output.print(errorRelativoTamañoArbol + ", ", RESULTS_FILE);
				state.output.print(gpind.trees[0].child.depth() + ", ", RESULTS_FILE);
				state.output.print(gpind.size() + ", ", RESULTS_FILE);
				state.output.println(auxData.nombreInstancia() + " ", RESULTS_FILE);

				errRelativoAcumulado += errorRelativo;
			}
			
			Runtime garbage = Runtime.getRuntime();
			garbage.gc();
			
			state.output.println("---- Evaluacion terminada ----", LOG_FILE);
			
			
			/*
			 * Funciones objetivo 
			 */
			double profitResult,
				hitsPromedio = Math.abs(hits - pobSize) / pobSize,
				errRelativoPromedio = errRelativoAcumulado / pobSize;
			
			// Función objetivo para cada isla
			// Si tengo más de una población, uso islas... cc uso f obj estándar
			if (SUBPOPS > 1) {
				// Las primeras 2 islas se evaluan con fitness 1 y fitness 2
				// respectivamente
				if (subpopulation < 2) {
					if (subpopulation % 2 == 0) {
						// Función objetivo por número de hits
						profitResult = hitsPromedio;
						// state.output.println("Fitness Hits ", LOG_FILE);
					} else {
						// Funcion objetivo con el error relativo
						profitResult = errRelativoPromedio;
						// state.output.println("Fitness error relativo ",
						// LOG_FILE);
					}
					// Las islas 3 y 4 se evaluan con fitness 2 y fitness 1
					// respectivamente
				} else {
					if (subpopulation % 2 != 0) {
						// Función objetivo por número de hits
						profitResult = hitsPromedio;
						// state.output.println("Fitness Hits ", LOG_FILE);
					} else {
						// Funcion objetivo con el error relativo
						profitResult = errRelativoPromedio;
						// state.output.println("Fitness error relativo ",
						// LOG_FILE);
					}
				}
			} else {
				// Función objetivo combinada para caso canónico
				profitResult = ALFA * errRelativoPromedio + BETA * hitsPromedio;
			}		
			
			//System.out.println("El resultado del profit esta generación es: " + profitResult + " para el ind: " + gpind.trees.hashCode());
			state.output.println("Fitness = " + (ALFA*profitResult + BETA*errorRelativoTamañoArbol), LOG_FILE);
			state.output.println(" ===================================== \n", LOG_FILE);
			KozaFitness f = ((KozaFitness) gpind.fitness);
			
			float fitness = (float)(profitResult*ALFA + BETA*errorRelativoTamañoArbol);
			f.setStandardizedFitness(state, fitness);
			f.hits = hits;
//			if (state.numGenerations == 1 && subpopulation == 0) {
//				System.out.println("Instancia evaluada...");
//				gpind.evaluated = false;
//			} else {
//				gpind.evaluated = true;
//			}
			gpind.evaluated = true;
		}
	}
	
	@Override
	public void describe(final EvolutionState state,
			final Individual individual,
			final int subpopulation,
			final int threadnum,
			final int log) {
		
		endGenerationTime = System.nanoTime();	//fin cronometro evoluciÃ³n
		String message_time = "Evolution duration: " + (endGenerationTime - startGenerationTime) / 1000000 + " ms";	
		state.output.message(message_time);
		PrintWriter dataOutput = null;
		Charset charset = Charset.forName("UTF-8");
		try {			
			dataOutput = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(
							"out/results/job." + JOB_NUMBER + ".subpop" + subpopulation + ".BestIndividual.in"),
					charset)));

		} catch (Exception e) {
			e.printStackTrace();
		}
		dataOutput.println(Population.NUM_SUBPOPS_PREAMBLE + Code.encode(1));
		dataOutput.println(Population.SUBPOP_INDEX_PREAMBLE + Code.encode(0));
		dataOutput.println(Subpopulation.NUM_INDIVIDUALS_PREAMBLE + Code.encode(1));
		dataOutput.println(Subpopulation.INDIVIDUAL_INDEX_PREAMBLE + Code.encode(0));

//		individual.evaluated = false;
		((GPIndividual)individual).printIndividual(state, dataOutput);
		
		dataOutput.println("\nJob: " + JOB_NUMBER);
		dataOutput.println("Isla: " + subpopulation);
		dataOutput.println("Generacion: " + state.generation);
		
		
		dataOutput.println(message_time);
		
		dataOutput.close();

		GPIndividual gpind = (GPIndividual) individual;
		gpind.trees[0].printStyle = GPTree.PRINT_STYLE_DOT;
		// System.out.println("PRINTSTYLE: " + gpind.trees[0].printStyle);
		String indid = gpind.toString().substring(19);
		state.output.println("label=\"Individual=" + indid + " Fitness=" + ((KozaFitness) gpind.fitness).standardizedFitness() + " Hits=" + ((KozaFitness) gpind.fitness).hits + " Size=" + gpind.size() + " Depth=" + gpind.trees[0].child.depth() + "\";", DOT_FILE);
		gpind.printIndividualForHumans(state, DOT_FILE);
		System.out.println("estoy imprimiendo a los individuos en .dot del job: " + JOB_NUMBER + " de la poblacion " + subpopulation);
		try {
			FileIO.repairDot(JOB_NUMBER, JOBS, subpopulation);
			FileIO.dot_a_png(KProblem.JOB_NUMBER, subpopulation);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
