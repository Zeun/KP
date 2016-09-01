package model;

import java.awt.Toolkit;
import java.io.*;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
	public static final double IND_MAX_NODES = 20.0;
	public static int JOBS;
	public static int SUBPOPS;
	// NÚMERO DE GENERACIONES EN QUE SE HACE LA CROSS VALIDATION
	public static int cross_validation_number = 99;
	// NÚMERO DE INVIDIVUOS QUE SOBREVIVEN
	public static int survival_individuals = 5;
	// DIRECTORIO DONDE SE ENCUENTRAN LAS INSTANCIAS
	public static String directory_data = "data/cross_validation_grupo_SS";

	ArrayList<KPData> data_temp;
	ArrayList<KPData> data_evaluation;
	// ArrayList<ArrayList<KPData>> data;
	Boolean flag = true;
	int contador = 0;
	
	DateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy_HH.mm.ss");
	Date date = new Date();
	String name = "results_" + dateformat.format(date);

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

		data_temp = new ArrayList<KPData>();
		// data_evaluation = new ArrayList<KPData>();
		// data = new ArrayList<ArrayList<KPData>>();
		
		try {
			(new File("out/" + name)).mkdirs();
			(new File("out/" + name + "/evolution" + JOB_NUMBER)).mkdirs();
			 LOG_FILE = FileIO.newLog(state.output, "out/" + name + "/evolution" +
			 JOB_NUMBER + "/KPLog.out");
			 RESULTS_FILE = FileIO.newLog(state.output, "out/" + name + "/evolution" + JOB_NUMBER + "/KPResults.txt");
			 state.output.print("Generacion" + ", ", RESULTS_FILE);
			 state.output.print("Tiempo(ms)" + ", ", RESULTS_FILE);
			 state.output.print("Individuo" + ", ", RESULTS_FILE);
			 state.output.print("Obtenido" + ", ", RESULTS_FILE);
			 state.output.print("Óptimo" + ", ", RESULTS_FILE);
			 state.output.print("Capacidad utilizada" + ", ", RESULTS_FILE);
			 state.output.print("Capacidad máxima" + ", ", RESULTS_FILE);
			 state.output.print("Error relativo tamaño árbol" + ", ", RESULTS_FILE);
			 state.output.print("Profundidad árbol" + ", ", RESULTS_FILE);
			 state.output.print("Tamaño árbol" + ", ", RESULTS_FILE);
			 state.output.println("Nombre Instancia", RESULTS_FILE);

			// DOT_FILE = FileIO.newLog(state.output, "out/results/evolution" +
			// JOB_NUMBER + "/job." + JOB_NUMBER + ".BestIndividual.dot");
			DOT_FILE = FileIO.newLog(state.output, "out/" + name + "/evolution" + JOB_NUMBER + "/BestIndividual.dot");
			final File folder_data_training;
			// Si tengo más de una población, uso 2 grupos de instancias
//			if (SUBPOPS > 1) {
//				folder_data_training = new File("data/Evol_isla1_GX");
//				final File folder_data_evaluation = new File("data/Evol_isla2_GX");
//				FileIO.readInstances(data_evaluation, folder_data_evaluation);
				// data.add(data_evaluation);
//			} else {
				// folder_island1 = new File("data/Evaluación/Eval G15");
				folder_data_training = new File(directory_data);

//			}
			FileIO.readInstances(data_temp, folder_data_training);

			// data.add(data_training);
			// data.add(data_evaluation);

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

			int hits = 0;
			double errRelativoAcumulado = 0.0, errorRelativoTamañoArbol = 0.0, errorRelativo, errorRelativoPeso,
					instanceNumber;

			if (gpind.size() > IND_MAX_NODES) {
				errorRelativoTamañoArbol = Math.abs(IND_MAX_NODES - gpind.size()) / IND_MAX_NODES;
			}

			// ArrayList<KPData> data_temp = new ArrayList<KPData>();

			int contador2;
			/*
			if (state.generation == (state.numGenerations - 1)) {
				data_temp = new ArrayList<KPData>();
				final File folder_island2 = new File(directory_data + "/eval");
				try {
					FileIO.readInstances(data_temp, folder_island2);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (flag) {
					flag = false;
				}
			} else {
				if (state.generation % cross_validation_number == 0 && state.generation != 0) {
					contador = ((((state.generation) / (cross_validation_number) % data_training.size()) - 1) * 10)
							% data_training.size();
					if ((data_training.size() * 0.9 + contador) == 100) {
						contador2 = 100;
					} else {
						contador2 = (int) ((data_training.size() * 0.9 + contador) % data_training.size());
					}
					for (int i = 0; i < data_training.size(); i++) {
						if (contador < contador2) {
							if (i < contador || i >= contador2) {
	
								KPData kp = data_training.get(i);
								data_temp.add(kp);
							}
						} else {
							if (i < contador && i >= contador2) {
	
								KPData kp = data_training.get(i);
								data_temp.add(kp);
							}
	
						}
					}
					if (flag) {
						System.out.println("Voy en el cross validation " + (state.generation) / (cross_validation_number));

						flag = false;
					}
				} else {
					contador = ((((state.generation) / (cross_validation_number) % data_training.size())) * 10)
							% data_training.size();
					if ((data_training.size() * 0.9 + contador) == 100) {
						contador2 = 100;
					} else {
						contador2 = (int) ((data_training.size() * 0.9 + contador) % data_training.size());
					}
					for (int i = 0; i < data_training.size(); i++) {
						if (contador < contador2) {
							if (i >= contador && i < contador2) {
								KPData kp = data_training.get(i);
								data_temp.add(kp);
							}
						} else {
							if (i >= contador || i < contador2) {
								KPData kp = data_training.get(i);
								data_temp.add(kp);
							}
	
						}
					}
					if (flag) {
						flag = false;
					}
				}
			}
			*/
			instanceNumber = data_temp.size();
			System.out.println(data_temp.size());
			String output = "";
			// El individuo es evaluado en cada una de las instancias
			for (int i = 0; i < instanceNumber; i++) {
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
				 output += (state.generation + ", ");
				 output += ((timeEnd - timeInit) / 1000000 + ", ");
				 output += (gpind.toString() + ", ");
				 output += ((int) auxData.beneficioTotal() + ", ");
				 output += ((int) auxData.beneficioOptimo() + ", ");
				 output += ((int) auxData.costoTotal() + ", ");
				 output += ((int) auxData.capacidadMochila() + ", ");
				 output += (errorRelativoTamañoArbol + ", ");
				 output += (gpind.trees[0].child.depth() + ", ");
				 output += (gpind.size() + ", ");
				 output += (auxData.nombreInstancia() + "\n");

				errRelativoAcumulado += errorRelativo;
			}
			state.output.print(output, RESULTS_FILE);

			output = "";

			Runtime garbage = Runtime.getRuntime();
			garbage.gc();

			/*
			 * Función objetivo
			 */
			double profitResult, noHitsPromedio = Math.abs(hits - instanceNumber) / instanceNumber,
					errRelativoPromedio = errRelativoAcumulado / instanceNumber;

			// profitResult = 0.8 * errRelativoPromedio + 0.2 * noHitsPromedio;

			KozaFitness f = ((KozaFitness) gpind.fitness);

			float fitness = (float) (0.90 * errRelativoPromedio + 0.05 * noHitsPromedio + 0.05 * errorRelativoTamañoArbol);
			f.setStandardizedFitness(state, fitness);
			f.hits = hits;
			gpind.evaluated = false;
		}
	}

	@Override
	public void describe(final EvolutionState state, final Individual individual, final int subpopulation,
			final int threadnum, final int log) {

		endGenerationTime = System.nanoTime(); // fin cronometro evolución
		String message_time = "Evolution duration: " + (endGenerationTime - startGenerationTime) / 1000000 + " ms";
		state.output.message(message_time);
		PrintWriter dataOutput = null;
		Charset charset = Charset.forName("UTF-8");
		try {
			dataOutput = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(
							"out/" + name + "/job." + JOB_NUMBER + ".subpop" + subpopulation + ".BestIndividual.in"),
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
			FileIO.repairDot(JOB_NUMBER, JOBS, subpopulation, name);
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
