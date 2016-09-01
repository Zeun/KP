package model;

import java.util.Arrays;
import java.util.Random;

import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.simple.SimpleProblemForm;
import ec.simple.SimpleStatistics;
import ec.steadystate.SteadyStateStatisticsForm;
import model.KProblem;

public class KPStatistic extends SimpleStatistics implements SteadyStateStatisticsForm {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5773305748714967534L;
	
	@Override
	public void postEvaluationStatistics(final EvolutionState state) {
//		super.postEvaluationStatistics(state);

		// for now we just print the best fitness per subpopulation.
		Individual[] best_i = new Individual[state.population.subpops.length];
		Individual[] best_hits = new Individual[state.population.subpops.length];

		Individual[] list_individuals = new Individual[state.population.subpops[0].individuals.length];
		System.arraycopy( state.population.subpops[0].individuals, 0, list_individuals, 0, state.population.subpops[0].individuals.length );

		Arrays.sort(list_individuals);

		for (int x = 0; x < state.population.subpops.length; x++) {
			best_i[x] = state.population.subpops[x].individuals[0];
			best_hits[x] = state.population.subpops[x].individuals[0];
			for (int y = 1; y < state.population.subpops[x].individuals.length; y++){
				if (state.population.subpops[x].individuals[y].fitness.betterThan(best_i[x].fitness)){
					best_i[x] = state.population.subpops[x].individuals[y];
				}					
				if (bestPerHits(
						state.population.subpops[x].individuals[y].fitness,
						best_hits[x].fitness
					)) {
					best_hits[x] = state.population.subpops[x].individuals[y];
				}
			}
					

			// now test to see if it's the new best_of_run
			if (best_of_run[x] == null || best_i[x].fitness.betterThan(best_of_run[x].fitness))
				best_of_run[x] = (Individual) (best_i[x].clone());
		}

		// print the best-of-generation individual
		if (doGeneration)
			state.output.println("\nGeneration: " + state.generation, statisticslog);
		if (doGeneration)
			state.output.println("Best Individual:", statisticslog);
		for (int x = 0; x < state.population.subpops.length; x++) {
			if (doGeneration)
				state.output.println("Subpopulation " + x + ":", statisticslog);
			if (doGeneration)
				best_i[x].printIndividualForHumans(state, statisticslog);
			if (doMessage && !silentPrint)
				if (state.generation % KProblem.cross_validation_number == 0 && state.generation != 0) {
					state.output.message("Subpop " + x + " best " + KProblem.survival_individuals + " fitness of generation");
					for (int i = 0; i < KProblem.survival_individuals; i++) {
						state.output.message("\t" + i + ": " + list_individuals[i].fitness.fitnessToStringForHumans());
					}

				} else {
					state.output.message("Subpop " + x + " best fitness of generation"
							+ best_i[x].fitness.fitnessToStringForHumans());
					state.output.message("Subpop " + x + " best hits of generation"
							+ best_hits[x].fitness.fitnessToStringForHumans());
				}

			// describe the winner if there is a description
			if (doGeneration && doPerGenerationDescription) {
				if (state.evaluator.p_problem instanceof SimpleProblemForm)
					((SimpleProblemForm) (state.evaluator.p_problem.clone())).describe(state, best_i[x], x, 0,
							statisticslog);
			}
		}
//		state.population.subpops[0].individuals = null;
//		for(int i = 5 ; i < state.population.subpops[0].individuals.length; i++)
//			state.population.subpops[0].individuals[i] = null;

	}

	private boolean bestPerHits(Fitness fitness, Fitness fitness2) {
		// TODO Auto-generated method stub
		String[] list1 = fitness.fitnessToStringForHumans().split(" ");
		String[] hits1 = list1[list1.length-1].split("=");
		String[] list2 = fitness2.fitnessToStringForHumans().split(" ");
		String[] hits2 = list2[list2.length-1].split("=");
		
		if (Integer.valueOf(hits1[hits1.length-1]) > Integer.valueOf(hits2[hits2.length-1])){
			return true;
		}
		return false;
	}
}
