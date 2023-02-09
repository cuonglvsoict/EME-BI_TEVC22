package core;

import java.util.Random;

import basic.Params;
import basic.Problem;

public abstract class Solver {
	protected Problem problem;
	protected int randomSeed;

	public Solver(Problem problem, int seed) {
		this.problem = problem;
		this.randomSeed = seed;
		Params.rand = new Random(seed);
	}

	public abstract void run();
	public abstract String getName();
}
