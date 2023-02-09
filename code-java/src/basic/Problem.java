package basic;

import java.util.ArrayList;

/**
 * @author cuonglv.hust@gmail.com
 * @date 24/02/2021
 *
 */
public class Problem {

	public int DIM; // Unify search space dimension
	public int TASKS_NUM;
	public ArrayList<Task> tasks;
	
	public Problem() {
		tasks = new ArrayList<Task>();
		DIM = 0;
		TASKS_NUM = 0;
	}
	
	public void clear() {
		for (Task task: tasks) {
			task.currentBest = null;
			task.currentBestFitness = Double.MAX_VALUE;
		}
	}
	
	public void addTask(Task task) {
		tasks.add(task);
		TASKS_NUM++;
		DIM = Math.max(DIM, task.function.dim);
	}

	public Task getTask(int task_id) {
		return tasks.get(task_id);
	}

	public boolean updateBest(int task_id, double newFitness, double[] newBest) {
		Task task = tasks.get(task_id);
		return task.updateBest(newFitness, newBest);
	}
	
	public double getBestFitness(int task_id) {
		return tasks.get(task_id).currentBestFitness;
	}

	public double[] getBestSolution(int task_id) {
		return tasks.get(task_id).currentBest;
	}
}
