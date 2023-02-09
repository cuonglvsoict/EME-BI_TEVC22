package util.crossover;

import java.util.ArrayList;

import basic.Params;

public class UniformCrossover {

	public static ArrayList<double[]> generateOffspring(double[] p1, double[] p2) {
		ArrayList<double[]> offspring = new ArrayList<double[]>();
		double[] c1 = p1.clone();
		double[] c2 = p2.clone();

		double tmp;
		for (int i = 0; i < c1.length; i++) {
			if (Params.rand.nextBoolean()) {
				tmp = c1[i];
				c1[i] = c2[i];
				c2[i] = tmp;
			}
		}

		offspring.add(c1);
		offspring.add(c2);
		return offspring;
	}

}
