package functions;

import model.KPData;
import model.KProblem;
import ec.util.Parameter;
import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

public class DoWhile extends GPNode {

	private static final long serialVersionUID = 4322751948463734062L;

	public String toString() {
		return "Do_While";
	}

	public void checkConstraints(final EvolutionState state, final int tree, final GPIndividual typicalIndividual,
			final Parameter individualBase) {

		super.checkConstraints(state, tree, typicalIndividual, individualBase);

		if (children.length != 2) {
			state.output.error("Incorrect number of children for node " + toStringForError() + " at " + individualBase);
		}
	}

	@Override
	public void eval(final EvolutionState state, final int thread, final GPData input, final ADFStack stack,
			final GPIndividual individual, final Problem problem) {

		boolean x, y;
		int i, n;
		double lastCost;
		KPData kpData = (KPData) input;
		KProblem kProblem = (KProblem) problem;

		children[0].eval(state, thread, kpData, stack, individual, kProblem);

		n = kpData.getInstance().getlistaDisponibles().size();
		x = kpData.getResult();
		// System.out.println("x result; " + x);
		i = 0;
		lastCost = kpData.getInstance().beneficioTotal();
		y = true;

		// Revisar
		while (x && y && i < n) {
			children[1].eval(state, thread, kpData, stack, individual, kProblem);
			if (kpData.getInstance().beneficioTotal() == lastCost) {
				y = false;
			} else {
				lastCost = kpData.getInstance().beneficioTotal();
				children[0].eval(state, thread, kpData, stack, individual, kProblem);
				x = kpData.getResult();
				i++;
			}
		}
		// Si el while iteró al menos una vez se considera resultado
		// verdaderos, caso contrario es falso
		if (i > 0)
			kpData.setResult(true);
		else
			kpData.setResult(false);
		// state.output.println("Resultado del \"WHILE(out)\":\t" +
		// mkpd.getResult(), MKProblem.LOG_FILE);
	}
}