package model;

import java.util.Arrays;
import java.util.Random;

import ec.EvolutionState;
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
		Individual[] best_i = new Individual[state.population.subpops.length]; // quiets
																				// compiler
																				// complaints
		Individual[] list_individuals = new Individual[state.population.subpops[0].individuals.length];
		System.arraycopy( state.population.subpops[0].individuals, 0, list_individuals, 0, state.population.subpops[0].individuals.length );
//		System.out.println(list_individuals[0].fitness.fitnessToStringForHumans() + " a " + list_individuals[1].fitness.fitnessToStringForHumans());
//		System.out.println(list_individuals[0].fitness);
		Arrays.sort(list_individuals);
//	    System.out.println(Arrays.asList(list_individuals));
//	    System.out.println(list_individuals[0].fitness.fitnessToStringForHumans() + " a " + list_individuals[1].fitness.fitnessToStringForHumans());
		for (int x = 0; x < state.population.subpops.length; x++) {
			best_i[x] = state.population.subpops[x].individuals[0];
			for (int y = 1; y < state.population.subpops[x].individuals.length; y++)
				if (state.population.subpops[x].individuals[y].fitness.betterThan(best_i[x].fitness))
					best_i[x] = state.population.subpops[x].individuals[y];

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
					Random ran = new Random();
					int range = (int) (list_individuals.length - list_individuals.length*0.9);
					int random_range = (int) (ran.nextInt(range) + list_individuals.length*0.9);
					for (int i = KProblem.survival_individuals + 1; i < list_individuals.length; i++) {
						state.population.subpops[0].individuals[i] = list_individuals[random_range];
					}
				} else {
					state.output.message("Subpop " + x + " best fitness of generation"
							+ best_i[x].fitness.fitnessToStringForHumans());
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
}
