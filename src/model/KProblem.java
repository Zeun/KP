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
		KProblem mkpp = (KProblem) super.clone();
		return mkpp;
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
			state.output.print("Fitness Error Relativo" + ", ", RESULTS_FILE);
			state.output.print("Hits" + ", ", RESULTS_FILE);
			state.output.print("Profundidad árbol" + ", ", RESULTS_FILE);
			state.output.println("Tamaño árbol" + ", ", RESULTS_FILE);
			// DOT_FILE = FileIO.newLog(state.output, "out/results/evolution" + JOB_NUMBER + "/job." + JOB_NUMBER + ".BestIndividual.dot");
			DOT_FILE = FileIO.newLog(state.output, "out/results/evolution" + JOB_NUMBER + "/BestIndividual.dot");
			//System.out.println("Archivo de salida: " + DOT_FILE);
			final File folder_island1 = new File("data/evaluacion_island1");
			final File folder_island2 = new File("data/evaluacion_island2");
			
			FileIO.readInstances(data_island1, folder_island1);
			FileIO.readInstances(data_island2, folder_island2);
			data.add(data_island1);
			data.add(data_island2);
			
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
			
			state.output.println("\n\nGeneracion:" + state.generation + "\nSOProblem: evaluando el individuo [" + gpind.toString() + "]\n", LOG_FILE);
			gpind.printIndividualForHumans(state, LOG_FILE);
			
			int hits = 0;
			double relErrAcum = 0.0;
			double nodesResult = 0.0;
			double instanceRelErr, err, wRelErr;
			
			if (gpind.size() > IND_MAX_NODES) {
				nodesResult = Math.abs(IND_MAX_NODES - gpind.size() ) / IND_MAX_NODES;
			}
			state.output.println("\n---- Iniciando evaluacion ---\nNum de Nodos:" + gpind.size(), LOG_FILE);
//			for(int i = 0; i < data.size(); i++) {
//				if(!data.get(i).instance.isNew()){
//					System.out.println(data.get(i).printResult());
//				}
//			}
//			// considerar que siempre tendran que tener el mismo numero de elementos las islas
			for (int i = 0; i < data_island1.size(); i++) {
				//KPData auxData = new KPData();
				Instance auxData = new Instance();
				// System.out.println(subpopulation%2 + " subpop " + subpopulation);
				auxData = data.get(subpopulation%2).get(i).getInstance().clone();
				// System.out.println(auxData.instance.beneficioTotal() + " /");
				KPData aux = new KPData();
				aux.instance = auxData;
				gpind.trees[0].printStyle = GPTree.PRINT_STYLE_DOT;	//escribir individuos en formato dot				
				long timeInit, timeEnd;
				timeInit = System.nanoTime();	//inicio cronometro
				gpind.trees[0].child.eval(state, threadnum, aux, stack, gpind, this);	//evaluar el individuo gpind para la instancia i
				timeEnd = System.nanoTime();	//fin cronometro
				
				//Diferencia entre el resultado obtenido y el óptimo
				err = Math.abs( auxData.beneficioTotal() - auxData.beneficioOptimo());
				//Error relativo entre la diferencia entre el resultado obtenido y el óptimo
				instanceRelErr = err/(auxData.beneficioOptimo());
				
				// Error de peso en caso de que me pase (penalizacion)
				if (auxData.costoTotal() > auxData.capacidadMochila()) {
					wRelErr = auxData.costoTotal() - auxData.capacidadMochila();
					wRelErr /= auxData.capacidadMochila();
					System.out.println(auxData.costoTotal() + " /" + auxData.capacidadMochila());
				} else {
					wRelErr = 0.0;
				}
				
				if (instanceRelErr < IND_MAX_REL_ERR && wRelErr == 0.0) {
				//if (instanceRelErr == 0 && wRelErr == 0.0) {
					hits++;
				}
				
				//*log result*/
				// KPResults.out
				state.output.print(state.generation + ", ", RESULTS_FILE);
				state.output.print(state.numGenerations + ", ", RESULTS_FILE);
				state.output.print(subpopulation + ", ", RESULTS_FILE);
				state.output.print(SUBPOPS + ", ", RESULTS_FILE);
				state.output.print((timeEnd - timeInit) + ", ", RESULTS_FILE);
				state.output.print(gpind.toString() + ", ", RESULTS_FILE);
				state.output.print(auxData.beneficioTotal() + ", ", RESULTS_FILE);
				state.output.print(auxData.beneficioOptimo() + ", ", RESULTS_FILE);
				// state.output.print(auxData.numeroElementos() + ", ", RESULTS_FILE);
				state.output.print(instanceRelErr + ", ", RESULTS_FILE);
				state.output.print((BETA*nodesResult + ALFA*instanceRelErr + ", "), RESULTS_FILE);
				state.output.print(hits + ", ", RESULTS_FILE);
				state.output.print(gpind.trees[0].child.depth() + ", ", RESULTS_FILE);
				state.output.println(gpind.size() + ", ", RESULTS_FILE);
				
				//state.output.println(nodesResult + " ", RESULTS_FILE);	
				//state.output.println(auxData.get(i).printResult() +" ", RESULTS_FILE);	

				
				relErrAcum += instanceRelErr;
				// relErrAcum += wRelErr;
				state.output.print("Time: [init= " + timeInit + "], [end= " + timeEnd + "], [dif= " + (timeEnd - timeInit) + "]", LOG_FILE);
			}
			
			Runtime garbage = Runtime.getRuntime();
			garbage.gc();
			
			state.output.println("---- Evaluacion terminada ----", LOG_FILE);
			
			
			/*
			 * Funciones objetivo 
			 */
			double profitResult;
			// Las primeras 2 islas se evaluan con f1 y f2  
			if (subpopulation < 2){
				// Si la isla es par, funcion obj con hit
				if (subpopulation % 2 == 0){
					// Funcion objetivo considerando el numero de hits
					profitResult = Math.abs(hits-data.get(subpopulation%2).size())/(double)data.get(subpopulation%2).size();
				} else { // Si la isla es par, funcion obj con err relativo
					// Funcion objetivo tradicional con el error relativo
					profitResult = relErrAcum / data.get(subpopulation%2).size();
				}
			} else { // Las ultimas 2 islas se evaluan con f2 y f1 
				// Si la isla es par, funcion obj con hit
				if (subpopulation % 2 != 0){
					// Funcion objetivo considerando el numero de hits
					profitResult = Math.abs(hits-data.get(subpopulation%2).size())/(double)data.get(subpopulation%2).size();
				} else { // Si la isla es par, funcion obj con err relativo
					// Funcion objetivo tradicional con el error relativo
					profitResult = relErrAcum / data.get(subpopulation%2).size();
				}
			}
			 
			
			
			
			
			//System.out.println("El resultado del profit esta generación es: " + profitResult + " para el ind: " + gpind.trees.hashCode());
			state.output.println(" Error relativo de la cantidad de nodos = " + nodesResult, LOG_FILE);
			state.output.println(" Error relativo del profit = " + profitResult, LOG_FILE);
			KozaFitness f = ((KozaFitness) gpind.fitness);
			
			float fitness = (float)(profitResult*ALFA + BETA*nodesResult);
			f.setStandardizedFitness(state, fitness);
			f.hits = hits;
			if (state.numGenerations == 1 && subpopulation == 0) {
				System.out.println("Instancia evaluada...");
				gpind.evaluated = false;
			} else {
				gpind.evaluated = true;
			}
			
		}
	}
	
	@Override
	public void describe(final EvolutionState state,
			final Individual individual,
			final int subpopulation,
			final int threadnum,
			final int log) {
		
		endGenerationTime = System.nanoTime();	//fin cronometro evoluciÃ³n
		state.output.message("Evolution duration: " + (endGenerationTime - startGenerationTime) / 1000000 + " ms");	//duraciÃ³n evoluciÃ³n en ms
		PrintWriter dataOutput = null;
		Charset charset = Charset.forName("UTF-8");
		try {			
			dataOutput = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream("out/results/job." + JOB_NUMBER + ".BestIndividual.in"), charset)));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// ACÁ IMPRIMO AL INDIVIDUO EN EL .IN
//		dataOutput.println(Population.NUM_SUBPOPS_PREAMBLE + Code.encode(1));
//		dataOutput.println(Population.SUBPOP_INDEX_PREAMBLE + Code.encode(0));
//		dataOutput.println(Subpopulation.NUM_INDIVIDUALS_PREAMBLE + Code.encode(1));
//		dataOutput.println(Subpopulation.INDIVIDUAL_INDEX_PREAMBLE + Code.encode(0));

		dataOutput.println("Job: " + JOB_NUMBER);
		dataOutput.println("Isla: " + subpopulation);
		dataOutput.println("Generacion: " + state.generation);
		//individual.evaluated = false;
		((GPIndividual)individual).printIndividual(state, dataOutput);
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
