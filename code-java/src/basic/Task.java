package basic;

import benchmark.Function;

/**
 * @author cuonglv.hust@gmail.com
 * @date 24/02/2021
 *
 */
public class Task {

	public int task_id;
	public Function function;

	public double[] currentBest;
	public double currentBestFitness;

	public double[] optima;

	public Task(int task_id, Function function) {
		this.task_id = task_id;
		this.function = function;
		this.currentBestFitness = Double.MAX_VALUE;
	}

	public Task(int task_id) {
		this.task_id = task_id;
		this.currentBestFitness = Double.MAX_VALUE;
	}

	public void setFunction(Function function) {
		this.function = function;
	}

	public boolean updateBest(double newFitness, double[] newBest) {
		if (this.currentBestFitness > newFitness) {
			this.currentBestFitness = newFitness;
			this.currentBest = newBest.clone();
			return true;
		}

		return false;
	}

	public double calculateFitnessValue(double[] x) {
		if (Params.countEvals > Params.maxEvals) {
			return Double.MAX_VALUE;
		}

		Params.countEvals++;

		double[] de_normalized = new double[this.function.dim];
		for (int i = 0; i < this.function.dim; i++) {
			de_normalized[i] = x[i] * (this.function.UB[i] - this.function.LB[i]) + this.function.LB[i];
		}

		double obj = function.getValue(de_normalized);
		if (obj < 0) {
			obj = 0;
		}

		return obj;
	}

}
