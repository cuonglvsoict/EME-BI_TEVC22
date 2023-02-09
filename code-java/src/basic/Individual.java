package basic;

/**
 * Each value of the chromosome is normalized into the range [0,1]
 * 
 * @author cuonglv.hust@gmail.com
 * @date 24/02/2021
 *
 */
public class Individual implements Comparable {

	private static int counter = 0;
	private int individual_id;

	public int dim;
	public double[] chromosome;
	public int skill_factor;
	public double scalar_fitness;
	private double skill_fitness; // fitness value of skill task

	public Individual(int dim) {
		this.individual_id = Individual.counter++;
		this.dim = dim;
		this.chromosome = new double[dim];
		this.skill_fitness = Double.MAX_VALUE;
	}

	public Individual(double[] chromosome) {
		this.individual_id = Individual.counter++;
		this.dim = chromosome.length;
		this.chromosome = chromosome;
		this.skill_fitness = Double.MAX_VALUE;
	}

	public void randomInit() {
		for (int i = 0; i < dim; i++) {
			this.chromosome[i] = Params.rand.nextDouble();
		}
	}

	public void setFitness(int task_id, double value) {
		if (task_id == this.skill_factor) {
			this.skill_fitness = value;
		}
	}

	public double getFitness(int task_id) {
		if (task_id == this.skill_factor) {
			return this.skill_fitness;
		} else {
			return Double.MAX_VALUE;
		}
	}

	public int getID() {
		return this.individual_id;
	}

	public double getEuclidianDistance(Individual indiv, double max, double min) {
		double dis = 0;
		for (int i = 0; i < this.dim; i++) {
			double x1 = (this.chromosome[i] - min) / (max - min);
			double x2 = (indiv.chromosome[i] - min) / (max - min);

			dis += (x1 - x2) * (x1 - x2);
		}
		return Math.sqrt(dis);
	}
	
	public double getEuclidianDistance(double[] other, double max, double min) {
		double dis = 0;
		for (int i = 0; i < this.dim; i++) {
			double x1 = (this.chromosome[i] - min) / (max - min);
			double x2 = (other[i] - min) / (max - min);

			dis += (x1 - x2) * (x1 - x2);
		}
		return Math.sqrt(dis);
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		Individual other = (Individual) o;
		return -Double.valueOf(this.scalar_fitness).compareTo(other.scalar_fitness);
	}

}
