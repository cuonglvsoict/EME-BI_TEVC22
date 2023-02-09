package main;

import java.io.IOException;

import basic.Params;
import basic.ProblemManager;
import benchmark.ProblemConstructor;
import core.EMEdPS;
import core.Solver;

public class Main {

	public static void main(String[] args) throws IOException {		
		
//		Params.BENCHMARKS_PATH = "data/" + Params.BENCHMARKS_PATH;
		
		// for the benchmark of 10 tasks
		ProblemManager pm = new ProblemManager(ProblemConstructor.get10TasksBenchmark());
		
		// for the benchmark of 50 tasks 
		// ProblemManager pm = new ProblemManager(ProblemConstructor.get10TasksBenchmark());

		for (int i = 0; i < pm.numberOfProblems(); i++) {
			int taskNum = pm.getProblem(i).TASKS_NUM;
			System.out.println("Problem " + (i + 1));
			
			// cpu time of each run
			Params.memRuntime = new long[Params.REPEAT];
			
			// mean of all runs
			Params.meanFitness = new double[taskNum];

			// 30 independent runs
			for (int seed = 0; seed < Params.REPEAT; seed++) {
				pm.getProblem(i).clear();
				Solver solver = new EMEdPS(pm.getProblem(i), seed);
				solver.run();
			}

			System.out.println("Average result:");
			for (int task = 0; task < taskNum; task++) {
				System.out.println((task + 1) + ": " + Params.meanFitness[task]);
			}
		}
	}
}
