package basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import util.crossover.SBX;

public class Population {

	public ArrayList<Individual> individuals;
	private Problem problem;

	public Population(Problem problem) {
		this.problem = problem;
		this.individuals = new ArrayList<Individual>();
	}

	public void randomInit(int size) {
		this.individuals.clear();
		for (int i = 0; i < size; i++) {
			int skill = i % problem.TASKS_NUM;
			Individual indiv = new Individual(problem.DIM);
			indiv.randomInit();
			indiv.skill_factor = skill;
			indiv.setFitness(skill, problem.getTask(skill).calculateFitnessValue(indiv.chromosome));
			this.individuals.add(indiv);
			
			problem.updateBest(skill, indiv.getFitness(skill), indiv.chromosome);
		}
	}

	public ArrayList<ArrayList<Individual>> getSubPops() {
		ArrayList<ArrayList<Individual>> subpops = new ArrayList<ArrayList<Individual>>();
		for (int i = 0; i < problem.TASKS_NUM; i++) {
			subpops.add(new ArrayList<Individual>());
		}

		for (Individual indiv : this.individuals) {
			subpops.get(indiv.skill_factor).add(indiv);
		}

		return subpops;
	}

	public void updateScalarFitness() {
		ArrayList<ArrayList<Individual>> subpops = this.getSubPops();

		for (int k = 0; k < problem.TASKS_NUM; k++) {
			subpops.get(k).sort(new IndividualComparator(k));

			for (int i = 0; i < subpops.get(k).size(); i++) {
				Individual indiv = subpops.get(k).get(i);
				indiv.scalar_fitness = 1.0 / (i + 1);
			}

			problem.updateBest(k, subpops.get(k).get(0).getFitness(k), subpops.get(k).get(0).chromosome);
		}
	}

	public ArrayList<Individual> crossover(Individual par1, Individual par2, boolean swap) {
		ArrayList<double[]> chromosomes = SBX.generateOffspring(par1.chromosome, par2.chromosome, swap);
		ArrayList<Individual> offspring = new ArrayList<Individual>();
		for (double[] d : chromosomes) {
			offspring.add(new Individual(d));

		}
		return offspring;
	}

	@SuppressWarnings("unchecked")
	public void executeSelection(int size) {
		Collections.sort(this.individuals);
		if (this.individuals.size() > size) {
			this.individuals.subList(size, this.individuals.size()).clear();
		}
	}

	public ArrayList<Individual> getIndividuals() {
		return this.individuals;
	}

	public Individual getIndividual(int index) {
		return this.individuals.get(index);
	}

	public void addIndividuals(Collection<Individual> indivs) {
		this.individuals.addAll(indivs);
	}

	public void directSwapVariables(Individual par1, Individual par2) {
		double tmp;
		for (int i = 0; i < this.problem.DIM; i++) {
			if (Params.rand.nextBoolean()) {
				tmp = par1.chromosome[i];
				par1.chromosome[i] = par2.chromosome[i];
				par2.chromosome[i] = tmp;
			}
		}
	}
}
