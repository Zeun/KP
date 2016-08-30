package model;

import java.awt.Toolkit;
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
	public static double BETA = 1 - ALFA;
	public static long startGenerationTime;
	public static long endGenerationTime;
	public static String semillas;
	public static int elites;
	public static final double IND_MAX_REL_ERR = 0.01;
	public static final double IND_MAX_NODES = 15.0;
	public static int JOBS;
	public static int SUBPOPS;
	public static int cross_validation_number = 5;
	public static int survival_individuals = 5;

	ArrayList<KPData> data_island1;
	ArrayList<KPData> data_island2;
	ArrayList<ArrayList<KPData>> data;
	Boolean flag = true;
	int contador = 0;

	@Override
	public KProblem clone() {
		KProblem kp = (KProblem) super.clone();
		return kp;
	}

	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		JOB_NUMBER = ((Integer) (state.job[0])).intValue();
		super.setup(state, base);

		if (!(input instanceof KPData)) {
			state.output.fatal("Obteniendo instancias de prueba desde archivo");
		}
		JOBS = state.parameters.getInt(new ec.util.Parameter("jobs"), null);
		SUBPOPS = state.parameters.getInt(new ec.util.Parameter("pop.subpops"), null);
		// elites = state.parameters.getInt(new
		// ec.util.Parameter("breed.elite.0"), null);
		semillas = state.parameters.getString(new ec.util.Parameter("seed.0"), null);

		data_island1 = new ArrayList<KPData>();
		data_island2 = new ArrayList<KPData>();
		data = new ArrayList<ArrayList<KPData>>();

		try {
			(new File("out/results/evolution" + JOB_NUMBER)).mkdirs();
//			 LOG_FILE = FileIO.newLog(state.output, "out/results/evolution" +
//			 JOB_NUMBER + "/KPLog.out");
//			 RESULTS_FILE = FileIO.newLog(state.output, "out/results/evolution" + JOB_NUMBER + "/KPResults.txt");
//			 state.output.print("Generacion" + ", ", RESULTS_FILE);
//			 state.output.print("N° Gen" + ", ", RESULTS_FILE);
//			 state.output.print("N° Islas" + ", ", RESULTS_FILE);
//			 state.output.print("Isla" + ", ", RESULTS_FILE);
//			 state.output.print("Tiempo(ms)" + ", ", RESULTS_FILE);
//			 state.output.print("Individuo" + ", ", RESULTS_FILE);
//			 state.output.print("Obtenido" + ", ", RESULTS_FILE);
//			 state.output.print("Óptimo" + ", ", RESULTS_FILE);
//			 state.output.print("Capacidad utilizada" + ", ", RESULTS_FILE);
//			 state.output.print("Capacidad máxima" + ", ", RESULTS_FILE);
//			 state.output.print("Error relativo tamaño árbol" + ", ", RESULTS_FILE);
//			 state.output.print("Profundidad árbol" + ", ", RESULTS_FILE);
//			 state.output.print("Tamaño árbol" + ", ", RESULTS_FILE);
//			 state.output.println("Nombre Instancia", RESULTS_FILE);

			// DOT_FILE = FileIO.newLog(state.output, "out/results/evolution" +
			// JOB_NUMBER + "/job." + JOB_NUMBER + ".BestIndividual.dot");
			DOT_FILE = FileIO.newLog(state.output, "out/results/evolution" + JOB_NUMBER + "/BestIndividual.dot");
			final File folder_island1;
			// Si tengo más de una población, uso 2 grupos de instancias
			if (SUBPOPS > 1) {
				folder_island1 = new File("data/Evol_isla1_GX");
				final File folder_island2 = new File("data/Evol_isla2_GX");
				FileIO.readInstances(data_island2, folder_island2);
				data.add(data_island2);
			} else {
				// folder_island1 = new File("data/Evaluación/Eval G15");
				folder_island1 = new File("data/test");
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
	public void evaluate(final EvolutionState state, final Individual individual, final int subpopulation,
			final int threadnum) {

		if (!individual.evaluated) {

			GPIndividual gpind = (GPIndividual) individual;

			// state.output.println("Generacion: " + state.generation +
			// "\nSubpopulation: " + subpopulation + "\nKProblem: evaluando el
			// individuo [" + gpind.toString() + "]\n", LOG_FILE);
			// gpind.printIndividualForHumans(state, LOG_FILE);

			int hits = 0;
			double errRelativoAcumulado = 0.0, errorRelativoTamañoArbol = 0.0, errorRelativo, errorRelativoPeso,
					pobSize;

			// Si el tamaño del árbol es mayor al permitido se calcula el error
			// relativo de éste
			if (gpind.size() > IND_MAX_NODES) {
				errorRelativoTamañoArbol = Math.abs(IND_MAX_NODES - gpind.size()) / IND_MAX_NODES;
			}
			// state.output.println("\n---- Iniciando evaluacion ---\nNum de
			// Nodos:" + gpind.size(), LOG_FILE);

			ArrayList<KPData> data_temp = new ArrayList<KPData>();
			
//			if (state.generation < cross_validation_number) {
//				contador = 0;
//			} else {
//				
//			}
			int contador2;	
			if (state.generation % cross_validation_number == 0 && state.generation != 0) {
				contador = ((((state.generation) / (cross_validation_number) % data.get(0).size()) - 1) * 10) % data.get(0).size();
				if ((data.get(0).size()*0.9 + contador) == 100) {
					contador2 = 100;
				} else {
					contador2 = (int) ((data.get(0).size()*0.9 + contador)%data.get(0).size());
				}	
				for (int i = 0; i < data.get(0).size(); i++) {
					if (contador < contador2) {
						if (i < contador || i >= contador2) {
							
							KPData kp = data.get(0).get(i);
	//						System.out.println("agrego " + kp.getInstance().nombreInstancia());
							data_temp.add(kp);
						}
					}	else {
						if (i < contador && i >= contador2) {
							
							KPData kp = data.get(0).get(i);
	//						System.out.println("agrego " + kp.getInstance().nombreInstancia());
							data_temp.add(kp);
						}
						
					}
				}
				if (flag) {
					System.out.println("Voy en el cross validation " + (state.generation) / (cross_validation_number));
//					if (contador < contador2) {
//						System.out.println("el rango es de todas las que no esten dentro de " + contador + " hasta " + contador2 + " con " + data_temp.size() + "instancias");
//					} else {
//						System.out.println("el rango es de todas las que esten dentro de " + contador2 + " hasta " + contador + " con " + data_temp.size() + "instancias");
//					}					
					flag = false;
				}
			} else {
				contador = ((((state.generation) / (cross_validation_number) % data.get(0).size())) * 10) % data.get(0).size();
				if ((data.get(0).size()*0.9 + contador) == 100) {
					contador2 = 100;
				} else {
					contador2 = (int) ((data.get(0).size()*0.9 + contador)%data.get(0).size());
				}	
				for (int i = 0; i < data.get(0).size(); i++) {
					if (contador < contador2) {
						if (i >= contador && i < contador2) {
							KPData kp = data.get(0).get(i);
							data_temp.add(kp);						
						}
					} else {
						if (i >= contador || i < contador2) {
							KPData kp = data.get(0).get(i);
							data_temp.add(kp);						
						}
						
					}
				}
				if (flag) {
//					System.out.println("en otros casos tengo " + data_temp.size() + " instancias");
//					if (contador < contador2) {
//						System.out.println("el rango es de todas las que esten dentro de " + contador + " hasta " + contador2 + " con " + data_temp.size() + "instancias");
//					} else {
//						System.out.println("el rango es de todas las que no esten dentro de " + contador2 + " hasta " + contador + " con " + data_temp.size() + "instancias");
//					}
					flag = false;
				}
			}
			pobSize = data_temp.size();
			String output = "";
			// El individuo es evaluado en cada una de las instancias
			for (int i = 0; i < pobSize; i++) {
				// Carga de datos de la instancia a evaluar
				Instance auxData = new Instance();
				auxData = data_temp.get(i).getInstance().clone();
				KPData aux = new KPData();
				aux.instance = auxData;

				// Escribir individuos en formato dot
				gpind.trees[0].printStyle = GPTree.PRINT_STYLE_DOT;

				// Variables cronómetro
				long timeInit, timeEnd;
				timeInit = System.nanoTime();

				// Evaluar el individuo gpind para la instancia actual
				gpind.trees[0].child.eval(state, threadnum, aux, stack, gpind, this);

				// Fin del tiempo de evaluación
				timeEnd = System.nanoTime();

				// Error relativo de la diferencia entre el resultado obtenido y
				// el óptimo
				errorRelativo = (auxData.beneficioOptimo() - auxData.beneficioTotal()) / (auxData.beneficioOptimo());

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
					// Descomentar para ver circuito en pantalla
					// System.out.println(auxData.printResult());
				}

				// Log de resultados por instancia
				// state.output.print(state.generation + ", ", RESULTS_FILE);
				// state.output.print(state.numGenerations + ", ",
				// RESULTS_FILE);
				// state.output.print(SUBPOPS + ", ", RESULTS_FILE);
				// state.output.print(subpopulation + ", ", RESULTS_FILE);
				// state.output.print((timeEnd - timeInit) / 1000000 + ", ",
				// RESULTS_FILE);
				// state.output.print(gpind.toString() + ", ", RESULTS_FILE);
				// state.output.print((int)auxData.beneficioTotal() + ", ",
				// RESULTS_FILE);
				// state.output.print((int)auxData.beneficioOptimo() + ", ",
				// RESULTS_FILE);
				// state.output.print((int)auxData.costoTotal() + ", ",
				// RESULTS_FILE);
				// state.output.print((int)auxData.capacidadMochila() + ", ",
				// RESULTS_FILE);
				// state.output.print(errorRelativoTamañoArbol + ", ",
				// RESULTS_FILE);
				// state.output.print(gpind.trees[0].child.depth() + ", ",
				// RESULTS_FILE);
				// state.output.print(gpind.size() + ", ", RESULTS_FILE);
				// state.output.println(auxData.nombreInstancia() + " ",
				// RESULTS_FILE);
//				output += (state.generation + ", ");
//				output += (state.numGenerations + ", ");
//				output += (SUBPOPS + ", ");
//				output += (subpopulation + ", ");
//				output += ((timeEnd - timeInit) / 1000000 + ", ");
//				output += (gpind.toString() + ", ");
//				output += ((int) auxData.beneficioTotal() + ", ");
//				output += ((int) auxData.beneficioOptimo() + ", ");
//				output += ((int) auxData.costoTotal() + ", ");
//				output += ((int) auxData.capacidadMochila() + ", ");
//				output += (errorRelativoTamañoArbol + ", ");
//				output += (gpind.trees[0].child.depth() + ", ");
//				output += (gpind.size() + ", ");
//				output += (auxData.nombreInstancia() + "\n");

				errRelativoAcumulado += errorRelativo;
			}

//			state.output.print(output, RESULTS_FILE);
			output = "";

			Runtime garbage = Runtime.getRuntime();
			garbage.gc();

			// state.output.println("---- Evaluacion terminada ----", LOG_FILE);

			/*
			 * Funciones objetivo
			 */
			double profitResult, hitsPromedio = Math.abs(hits - pobSize) / pobSize,
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
				// Función objetivo combinada para caso sin co-evolución
				profitResult = ALFA * errRelativoPromedio + BETA * hitsPromedio;
			}

			// state.output.println("Fitness = " + (ALFA*profitResult +
			// BETA*errorRelativoTamañoArbol), LOG_FILE);
			// state.output.println(" ===================================== \n",
			// LOG_FILE);
			KozaFitness f = ((KozaFitness) gpind.fitness);

			float fitness = (float) (profitResult * ALFA + BETA * errorRelativoTamañoArbol);
			f.setStandardizedFitness(state, fitness);
			f.hits = hits;
			gpind.evaluated = false;
		}
	}

	@Override
	public void describe(final EvolutionState state, final Individual individual, final int subpopulation,
			final int threadnum, final int log) {

		endGenerationTime = System.nanoTime(); // fin cronometro evoluciÃ³n
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

		individual.evaluated = false;
		((GPIndividual) individual).printIndividual(state, dataOutput);

		dataOutput.println("\nJob: " + JOB_NUMBER);
		dataOutput.println("Isla: " + subpopulation);
		dataOutput.println("Generacion: " + state.generation);
		dataOutput.println("Semilla: " + semillas);
		dataOutput.println(message_time);
		Toolkit.getDefaultToolkit().beep();
		dataOutput.close();

		GPIndividual gpind = (GPIndividual) individual;
		gpind.trees[0].printStyle = GPTree.PRINT_STYLE_DOT;
		// System.out.println("PRINTSTYLE: " + gpind.trees[0].printStyle);
		String indid = gpind.toString().substring(19);
		state.output.println("label=\"Individual=" + indid + " Fitness="
				+ ((KozaFitness) gpind.fitness).standardizedFitness() + " Hits=" + ((KozaFitness) gpind.fitness).hits
				+ " Size=" + gpind.size() + " Depth=" + gpind.trees[0].child.depth() + "\";", DOT_FILE);
		gpind.printIndividualForHumans(state, DOT_FILE);
		System.out.println("estoy imprimiendo a los individuos en .dot del job: " + JOB_NUMBER + " de la poblacion "
				+ subpopulation);
		try {
			FileIO.repairDot(JOB_NUMBER, JOBS, subpopulation);
			FileIO.dot_a_png(JOB_NUMBER, subpopulation);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public int cross_validation_number() {
		return cross_validation_number;
	}
	
	public int survival_individuals() {
		return survival_individuals;
	}

}
