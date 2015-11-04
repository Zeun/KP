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
	
	ArrayList<KPData> data;
	
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
		
		elites =  state.parameters.getInt(new ec.util.Parameter("breed.elite.0"),null);	
		semillas =  state.parameters.getString(new ec.util.Parameter("seed.0"),null);
		data = new ArrayList<KPData>();
		
		try {
			LOG_FILE = FileIO.newLog(state.output, "out/KPLog.out");
			(new File("out/results/evolution" + (JOB_NUMBER))).mkdirs();
			RESULTS_FILE = FileIO.newLog(state.output, "out/results/evolution" + (JOB_NUMBER) + "/KPResults.out");
			DOT_FILE = FileIO.newLog(state.output, "out/results/evolution" + (JOB_NUMBER) + "/job." + (JOB_NUMBER) + ".BestIndividual.dot");
			//System.out.println("Archivo de salida: " + DOT_FILE);
			final File folder = new File("data/evaluacion");
			
			FileIO.readInstances(data, folder);
			
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
//					System.out.println(data.get(i).getInstance().printResult());
//				}
//			}
			
			for(int i = 0; i < data.size(); i++) {
				KPData auxData = new KPData();
				
				auxData = data.get(i).clone();	//nuevo data (vaciar mochila)	
				//System.out.println(auxData.instance.beneficioTotal() + " /");
				//gpind.trees[0].printStyle = GPTree.PRINT_STYLE_DOT;	//escribir individuos en formato dot				
				long timeInit, timeEnd;
				timeInit = System.nanoTime();	//inicio cronometro
				gpind.trees[0].child.eval(state, threadnum, auxData, stack, gpind, this);	//evaluar el individuo gpind para la instancia i
				timeEnd = System.nanoTime();	//fin cronometro
				
				//Diferencia entre el resultado obtenido y el �ptimo
				err = Math.abs( auxData.getInstance().beneficioTotal() - auxData.getInstance().beneficioOptimo());
				//Error relativo entre la diferencia entre el resultado obtenido y el �ptimo
				instanceRelErr = err/(auxData.getInstance().beneficioOptimo());
				
				// Error de peso en caso de que me pase (penalizacion)
				if (auxData.getInstance().costoTotal() > auxData.getInstance().capacidadMochila()) {
					wRelErr = auxData.getInstance().costoTotal() - auxData.getInstance().capacidadMochila();
					wRelErr /= auxData.getInstance().capacidadMochila();
					System.out.println(auxData.getInstance().costoTotal() + " /" + auxData.getInstance().capacidadMochila());
				} else {
					wRelErr = 0.0;
				}
				//Hits
//				if(err == 0 && size == 0) {
//					hits++;
//				}
				if (instanceRelErr < IND_MAX_REL_ERR && wRelErr == 0.0) {
					hits++;
				}
				//System.out.println(auxData.get(i).getInstance().printResult());
				
				//*log result*/
				// KPResults.out
				state.output.print(state.generation + " ", RESULTS_FILE);
				state.output.print(state.numGenerations + " ", RESULTS_FILE);
				state.output.print((timeEnd - timeInit) + " ", RESULTS_FILE);
				state.output.print(gpind.toString() + " ", RESULTS_FILE);
				state.output.print(auxData.getInstance().beneficioTotal() + " ", RESULTS_FILE);
				state.output.print(auxData.getInstance().beneficioOptimo() + " ", RESULTS_FILE);
				state.output.print(auxData.getInstance().numeroElementos() + " ", RESULTS_FILE);
				state.output.print(instanceRelErr + " ", RESULTS_FILE);
				state.output.print((BETA*nodesResult + ALFA*instanceRelErr + " "), RESULTS_FILE);
				state.output.print(gpind.trees[0].child.depth() + " ", RESULTS_FILE);
				state.output.print(gpind.size() + " ", RESULTS_FILE);
				state.output.print(hits + " ", RESULTS_FILE);
				//state.output.println(nodesResult + " ", RESULTS_FILE);	
				//state.output.println(auxData.get(i).getInstance().printResult() +" ", RESULTS_FILE);	

				
				relErrAcum += instanceRelErr;
				// relErrAcum += wRelErr;
				state.output.print("Time: [init= " + timeInit + "], [end= " + timeEnd + "], [dif= " + (timeEnd - timeInit) + "]", LOG_FILE);
			}
			
			Runtime garbage = Runtime.getRuntime();
			garbage.gc();
			
			state.output.println("---- Evaluacion terminada ----", LOG_FILE);
			
			double profitResult = relErrAcum / data.size();
			//System.out.println("El resultado del profit esta generaci�n es: " + profitResult + " para el ind: " + gpind.trees.hashCode());
			state.output.println(" Error relativo de la cantidad de nodos = " + nodesResult, LOG_FILE);
			state.output.println(" Error relativo del profit = " + profitResult, LOG_FILE);
			KozaFitness f = ((KozaFitness) gpind.fitness);
			
			float fitness = (float)(profitResult);//*ALFA + BETA*nodesResult);
			f.setStandardizedFitness(state, fitness);
			f.hits = hits;
			//gpind.evaluated = true;
		}
	}
	
	@Override
	public void describe(final EvolutionState state,
			final Individual individual,
			final int subpopulation,
			final int threadnum,
			final int log) {
		
		endGenerationTime = System.nanoTime();	//fin cronometro evolución
		state.output.message("Evolution duration: " + (endGenerationTime - startGenerationTime) / 1000000 + " ms");	//duración evolución en ms
		PrintWriter dataOutput = null;
		Charset charset = Charset.forName("UTF-8");
		try {			
			dataOutput = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream("out/results/job."+JOB_NUMBER+".BestIndividual.in"), charset)));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		dataOutput.println(Population.NUM_SUBPOPS_PREAMBLE + Code.encode(1));
		dataOutput.println(Population.SUBPOP_INDEX_PREAMBLE + Code.encode(0));
		dataOutput.println(Subpopulation.NUM_INDIVIDUALS_PREAMBLE + Code.encode(1));
		dataOutput.println(Subpopulation.INDIVIDUAL_INDEX_PREAMBLE + Code.encode(0));
		
		individual.evaluated = false;
		((GPIndividual)individual).printIndividual(state, dataOutput);
		dataOutput.close();

		GPIndividual gpind = (GPIndividual) individual;
		gpind.trees[0].printStyle = GPTree.PRINT_STYLE_DOT;
		String indid = gpind.toString().substring(19);
		System.out.println("dotfile" + DOT_FILE);
		state.output.println("label=\"Individual=" + indid + " Fitness=" + ((KozaFitness) gpind.fitness).standardizedFitness() + " Hits=" + ((KozaFitness) gpind.fitness).hits + " Size=" + gpind.size() + " Depth=" + gpind.trees[0].child.depth() + "\";", DOT_FILE);
		gpind.printIndividualForHumans(state, DOT_FILE);
		
		try {
			FileIO.repairDot(JOB_NUMBER);
			FileIO.dot_a_png(KProblem.JOB_NUMBER);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}