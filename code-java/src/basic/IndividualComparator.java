package basic;

import java.util.Comparator;

/**
 * @author cuonglv.hust@gmail.com
 * @date 24/02/2021
 *
 */
public class IndividualComparator implements Comparator<Individual> {
	int task;

	public IndividualComparator(int task) {
		this.task = task;
	}

	@Override
	public int compare(Individual o1, Individual o2) {
		// TODO Auto-generated method stub
		return Double.valueOf(o1.getFitness(task)).compareTo(o2.getFitness(task));
	}
}