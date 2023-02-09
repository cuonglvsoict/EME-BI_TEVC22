package core;

import java.util.ArrayList;
import java.util.List;

import basic.Individual;
import basic.Params;
import util.Utils;

public class SearchOperators {

	public static Individual pbest1(Individual current, ArrayList<Individual> pop, List<Individual> best, double cr,
			double f) {
		Individual pbest, r1, r2;

		do {
			pbest = best.get(Params.rand.nextInt(best.size()));
		} while (pbest.getID() == current.getID());

		do {
			r1 = pop.get(Params.rand.nextInt(pop.size()));
		} while (pbest.getID() == r1.getID() || current.getID() == r1.getID());

		do {
			r2 = pop.get(Params.rand.nextInt(pop.size()));
		} while (pbest.getID() == r2.getID() || current.getID() == r2.getID() || r1.getID() == r2.getID());

		Individual child = new Individual(current.dim);
		int J = Params.rand.nextInt(current.dim);

		for (int i = 0; i < current.dim; i++) {
			if (Params.rand.nextDouble() <= cr || i == J) {
				child.chromosome[i] = pbest.chromosome[i] + f * (r1.chromosome[i] - r2.chromosome[i]);

				// bound constraints handling
				if (child.chromosome[i] > 1) {
					child.chromosome[i] = (current.chromosome[i] + 1.0) / 2.0;
				} else if (child.chromosome[i] < 0) {
					child.chromosome[i] = current.chromosome[i] / 2.0;
				}
			} else {
				child.chromosome[i] = current.chromosome[i];
			}
		}

		return child;
	}

	public static Individual currentTopBest1(Individual current, ArrayList<Individual> pop, List<Individual> best,
			double cr, double f) {
		Individual pbest, r1, r2;

		do {
			pbest = best.get(Params.rand.nextInt(best.size()));
		} while (pbest.getID() == current.getID());

		do {
			r1 = pop.get(Params.rand.nextInt(pop.size()));
		} while (pbest.getID() == r1.getID() || current.getID() == r1.getID());

		do {
			r2 = pop.get(Params.rand.nextInt(pop.size()));
		} while (pbest.getID() == r2.getID() || current.getID() == r2.getID() || r1.getID() == r2.getID());

		Individual child = new Individual(current.dim);
		int J = Params.rand.nextInt(current.dim);

		for (int i = 0; i < current.dim; i++) {
			if (Params.rand.nextDouble() <= cr || i == J) {
				child.chromosome[i] = current.chromosome[i]
						+ f * (pbest.chromosome[i] - current.chromosome[i] + r1.chromosome[i] - r2.chromosome[i]);

				// bound constraints handling
				if (child.chromosome[i] > 1) {
					child.chromosome[i] = (current.chromosome[i] + 1.0) / 2.0;
				} else if (child.chromosome[i] < 0) {
					child.chromosome[i] = current.chromosome[i] / 2.0;
				}
			} else {
				child.chromosome[i] = current.chromosome[i];
			}
		}

		return child;
	}

	public static Individual gaussMutation(Individual current) {
		Individual child = new Individual(current.dim);
		for (int i = 0; i < current.dim; i++) {
			if (Params.rand.nextDouble() <= 1.0 / current.dim) {
				child.chromosome[i] = Utils.gauss(current.chromosome[i], 0.01);

				if (child.chromosome[i] > 1) {
					child.chromosome[i] = (current.chromosome[i] + 1.0) / 2.0;
				} else if (child.chromosome[i] < 0) {
					child.chromosome[i] = current.chromosome[i] / 2.0;
				}
			} else {
				child.chromosome[i] = current.chromosome[i];
			}
		}
		return child;
	}

}
