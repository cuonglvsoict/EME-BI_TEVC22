package core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import basic.Individual;
import basic.Params;
import basic.Population;
import basic.Problem;
import util.Utils;

/**
 * @author cuonglv.hust@gmail.com
 * @date 03/23/2021
 *
 */
public class EMEdPS extends Solver {

	private Population pop;
	private int pop_size; // current population size
	private int min_pop_size;
	private int max_pop_size;

	private double[][] rmp;
	private ArrayList<Double>[][] delta_f;
	private ArrayList<Double>[][] s_rmp;

	private double[] D0;
	private LearningPhase[] phase2;

	@SuppressWarnings("unchecked")
	public EMEdPS(Problem problem, int seed) {
		super(problem, seed);
		pop = new Population(problem);
		pop_size = problem.TASKS_NUM * Params.max_pop_size;
		max_pop_size = pop_size;
		min_pop_size = problem.TASKS_NUM * Params.min_pop_size;

		rmp = new double[problem.TASKS_NUM][problem.TASKS_NUM];
		delta_f = new ArrayList[problem.TASKS_NUM][problem.TASKS_NUM];
		s_rmp = new ArrayList[problem.TASKS_NUM][problem.TASKS_NUM];
		phase2 = new LearningPhase[problem.TASKS_NUM];

		for (int i = 0; i < problem.TASKS_NUM; i++) {
			for (int j = 0; j < problem.TASKS_NUM; j++) {
				if (i != j) {
					rmp[i][j] = 0.3;
				} else {
					rmp[i][j] = 0;
				}

				delta_f[i][j] = new ArrayList<Double>();
				s_rmp[i][j] = new ArrayList<Double>();
			}

			phase2[i] = new LearningPhase(problem.getTask(i));
		}
	}

	@Override
	public void run() {
		System.out.println(this.getClass().getSimpleName() + " running, seed = " + randomSeed);

		Params.maxEvals = Params.MAX_EVALS * problem.TASKS_NUM;
		Params.countEvals = 0;

		long begin = System.currentTimeMillis();

		pop.randomInit(pop_size);
		D0 = this.calculateD();

		while (Params.countEvals < Params.maxEvals) {
			this.runPhase1();
			this.updateRMP();
			this.runPhase2();

//			System.out.print(Params.countEvals + "\t");
//			for (int i=0; i<problem.TASKS_NUM; i++) {
//				System.out.print(problem.getBestFitness(i) + "\t");
//			}
//			System.out.println();
		}

		long end = System.currentTimeMillis();
		Params.memRuntime[this.randomSeed] = end - begin;

		for (int i = 0; i < problem.TASKS_NUM; i++) {
			double fit = problem.getBestFitness(i) <= Params.TOLERANCE ? 0.0 : problem.getBestFitness(i);
			System.out.println((i + 1) + ": " + fit);
			Params.meanFitness[i] += fit / Params.REPEAT;
		}
		System.out.println("runtime: " + Params.memRuntime[this.randomSeed]);
		System.out.println("==================================");
	}

	@SuppressWarnings("unchecked")
	private void runPhase1() {
		pop.updateScalarFitness();
		Collections.sort(pop.individuals);

		int k = pop_size / 2;
		ArrayList<Individual> matingPool = new ArrayList<Individual>();
		matingPool.addAll(pop.individuals.subList(0, k));

		ArrayList<Individual> offspring = reproduction(pop_size, matingPool);
		pop.getIndividuals().clear();
		pop.addIndividuals(matingPool);
		pop.addIndividuals(offspring);
		pop.updateScalarFitness();

		// update population size
		pop_size = (int) Math.max(this.min_pop_size, this.max_pop_size
				+ (this.min_pop_size - this.max_pop_size) * (Params.countEvals / (1.0 * Params.maxEvals)));

		pop.executeSelection(pop_size);
	}

	private void runPhase2() {
		ArrayList<ArrayList<Individual>> subpops = pop.getSubPops();

		double[] D = this.calculateD();
		pop.individuals.clear();

		for (int i = 0; i < problem.TASKS_NUM; i++) {
			double maxFit = -Double.MAX_VALUE, minFit = Double.MAX_VALUE;

			for (Individual indiv : subpops.get(i)) {
				maxFit = maxFit < indiv.getFitness(i) ? indiv.getFitness(i) : maxFit;
				minFit = minFit > indiv.getFitness(i) ? indiv.getFitness(i) : minFit;
			}
			double maxDelta = maxFit - minFit + 1e-99;

			double sigma = D[i] > D0[i] ? 0 : (1.0 - D[i] / D0[i]);
//			System.out.print(sigma + "\t");

			Individual best = phase2[i].evolve(subpops.get(i), sigma, maxDelta);
			problem.updateBest(i, best.getFitness(i), best.chromosome);
			pop.individuals.addAll(subpops.get(i));
		}
//		System.out.println();
	}

	public ArrayList<Individual> reproduction(int size, Collection<Individual> matingPool) {
		ArrayList<Individual> offspring = new ArrayList<Individual>();
		ArrayList<ArrayList<Individual>> subpops = pop.getSubPops();

		ArrayList<Individual> pool = new ArrayList<Individual>();
		pool.addAll(matingPool);

		boolean stopping = false;
		int sub_size = size / problem.TASKS_NUM;
		int[] counter = new int[problem.TASKS_NUM];

		Individual p1, p2;
		ArrayList<Individual> child;
		while (!stopping) {
			p1 = pool.get(Params.rand.nextInt(pool.size()));
			do {
				p2 = pool.get(Params.rand.nextInt(pool.size()));
			} while (p1.getID() == p2.getID());

			int t1 = p1.skill_factor;
			int t2 = p2.skill_factor;

			if (counter[t1] >= sub_size && counter[t2] >= sub_size) {
				continue;
			}

			double rmpValue = Math.max(rmp[t1][t2], rmp[t2][t1]);
			rmpValue = Utils.gauss(rmpValue, 0.1);

			if (t1 == t2) {
				// intra-task crossover
				child = pop.crossover(p1, p2, true);

				for (Individual indiv : child) {
					indiv.skill_factor = t1;
					indiv.setFitness(t1, problem.getTask(t1).calculateFitnessValue(indiv.chromosome));
					counter[t1]++;
				}

				offspring.addAll(child);
			} else if (Params.rand.nextDouble() <= rmpValue) {
				// inter-task crossover
				child = pop.crossover(p1, p2, false);

				for (Individual indiv : child) {
					if (counter[t1] < sub_size
							&& Params.rand.nextDouble() < rmp[t1][t2] / (rmp[t1][t2] + rmp[t2][t1])) {
						indiv.skill_factor = t1;
						indiv.setFitness(t1, problem.getTask(t1).calculateFitnessValue(indiv.chromosome));
						offspring.add(indiv);
						counter[t1]++;

						double dif = p1.getFitness(t1) - indiv.getFitness(t1);
						if (dif > 0) {
							delta_f[t1][t2].add(dif);
							s_rmp[t1][t2].add(rmpValue);
						}

					} else if (counter[t2] < sub_size) {
						indiv.skill_factor = t2;
						indiv.setFitness(t2, problem.getTask(t2).calculateFitnessValue(indiv.chromosome));
						offspring.add(indiv);
						counter[t2]++;

						double dif = p2.getFitness(t2) - indiv.getFitness(t2);
						if (dif > 0) {
							delta_f[t2][t1].add(dif);
							s_rmp[t2][t1].add(rmpValue);
						}
					}
				}

			} else {
				// intra-task crossover
				if (counter[t1] < sub_size) {
					Individual p12 = subpops.get(t1).get(Params.rand.nextInt(subpops.get(t1).size()));
					while (p12.getID() == p1.getID()) {
						p12 = subpops.get(t1).get(Params.rand.nextInt(subpops.get(t1).size()));
					}

					child = pop.crossover(p1, p12, true);
					Individual c1 = child.get(0);
					c1.skill_factor = t1;
					offspring.add(c1);
					c1.setFitness(t1, problem.getTask(t1).calculateFitnessValue(c1.chromosome));
					counter[t1]++;
				}

				if (counter[t2] < sub_size) {
					Individual p22 = subpops.get(t2).get(Params.rand.nextInt(subpops.get(t2).size()));
					while (p22.getID() == p2.getID()) {
						p22 = subpops.get(t2).get(Params.rand.nextInt(subpops.get(t2).size()));
					}

					child = pop.crossover(p2, p22, true);
					Individual c2 = child.get(0);
					c2.skill_factor = t2;
					offspring.add(c2);
					c2.setFitness(t2, problem.getTask(t2).calculateFitnessValue(c2.chromosome));
					counter[t2]++;
				}
			}

			stopping = true;
			for (int i = 0; i < problem.TASKS_NUM; i++) {
				if (counter[i] < sub_size) {
					stopping = false;
					break;
				}
			}
		}

		return offspring;
	}

	private double[] calculateD() {
		double D[] = new double[problem.TASKS_NUM];
		ArrayList<ArrayList<Individual>> subpops = pop.getSubPops();

		for (int i = 0; i < problem.TASKS_NUM; i++) {
			double max = 0, min = 1;
			double sum_w = 0;
			double[] w = new double[subpops.get(i).size()];

			int idx = 0;
			for (Individual indiv : subpops.get(i)) {
				for (double x : indiv.chromosome) {
					max = max < x ? x : max;
					min = min > x ? x : min;
				}

				if (indiv.getFitness(indiv.skill_factor) > Params.TOLERANCE) {
					w[idx] = 1.0 / indiv.getFitness(indiv.skill_factor);
				} else {
					w[idx] = 1.0 / Params.TOLERANCE;
				}
				sum_w += w[idx++];
			}

			idx = 0;
			double[] best = problem.getBestSolution(i);
			for (Individual indiv : subpops.get(i)) {
				double d = indiv.getEuclidianDistance(best, max, min);
				D[i] += (w[idx++] / sum_w) * d;
			}
		}

		return D;
	}

	private void updateRMP() {
		for (int i = 0; i < problem.TASKS_NUM; i++) {
			for (int j = 0; j < problem.TASKS_NUM; j++) {
				if (i != j) {
					double meanS = 0;
					if (delta_f[i][j].size() > 0) {
						double sum = 0;
						for (double d : delta_f[i][j]) {
							sum += d;
						}

						double sum_tmp = 0, w;
						for (int k = 0; k < delta_f[i][j].size(); k++) {
							w = delta_f[i][j].get(k) / sum;
							meanS += w * s_rmp[i][j].get(k) * s_rmp[i][j].get(k);
							sum_tmp += w * s_rmp[i][j].get(k);
						}
						meanS /= sum_tmp;

						rmp[i][j] += Params.c * meanS;
					} else {
						rmp[i][j] = (1.0 - Params.c) * rmp[i][j];
					}

					rmp[i][j] = Math.max(0.1, Math.min(1, rmp[i][j]));

					delta_f[i][j].clear();
					s_rmp[i][j].clear();
				}
			}
		}
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}
}
