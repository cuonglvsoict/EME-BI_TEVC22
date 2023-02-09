package basic;

import java.util.Random;

/**
 * @author cuonglv.hust@gmail.com
 * @date 24/02/2021
 *
 */
public class Params {
	
	public static Random rand;
	
	public static int countEvals;
	public static int maxEvals;
	
	public static final int REPEAT = 30;
	public static final int MAX_EVALS = 100000; // FOR ONE TASK
	public static int max_pop_size = 100; // FOR ONE TASK
	public static int min_pop_size = 20; // FOR ONE TASK
	
	public static double c = 0.06;
	public static double gamma = 0.3; // for 2 operators
	
	public static final double TOLERANCE = 1e-6;
	
	public static String BENCHMARKS_PATH = "WCCI-Competition/SO-Manytask-Benchmarks/Tasks/Benchmark_";

	public static long[] memRuntime;
	public static double[] meanFitness;
}
