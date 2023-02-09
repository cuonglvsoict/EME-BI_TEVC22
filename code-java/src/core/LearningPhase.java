package core;

import java.util.ArrayList;

import basic.Individual;
import basic.IndividualComparator;
import basic.Params;
import basic.Task;
import util.Utils;

public class LearningPhase {

	private final int M = 2; // number of operators

	private double[] sum_improv;
	private int[] consumed_fes;
	private int best_opcode; // 0 for pbest/1 and 1 for Gaussian mutation

	private Task task;
	private int gen;
	private ArrayList<Individual> pbest;

	// params for pbest/1
	private final int H = 10;
	private double[] mem_cr;
	private double[] mem_f;
	private ArrayList<Double> s_cr;
	private ArrayList<Double> s_f;
	private ArrayList<Double> diff_f;
	private int mem_pos;

	public LearningPhase(Task task) {
		this.task = task;
		pbest = new ArrayList<Individual>();

		sum_improv = new double[M];
		consumed_fes = new int[M];
		for (int i = 0; i < M; i++) {
			consumed_fes[i] = 1;
		}

		this.mem_cr = new double[H];
		this.mem_f = new double[H];
		for (int i = 0; i < H; i++) {
			this.mem_cr[i] = 0.5;
			this.mem_f[i] = 0.5;
		}

		this.s_cr = new ArrayList<Double>();
		this.s_f = new ArrayList<Double>();
		this.diff_f = new ArrayList<Double>();
		mem_pos = 0;

		gen = 0;
		best_opcode = 1;
	}

	public Individual evolve(ArrayList<Individual> pop, double sigma, double maxDelta) {
		this.gen++;

		this.updateBestOperator();
//		this.updateMemory();

		pop.sort(new IndividualComparator(task.task_id));
		Individual best = pop.get(0);

		int pbest_size = (int) (0.15 * pop.size());
		if (pbest_size < 5) {
			pbest_size = 5;
		}

		pbest.clear();
		pbest.addAll(pop.subList(0, pbest_size));

		ArrayList<Individual> nextPop = new ArrayList<Individual>();
		for (Individual indiv : pop) {
			int op_code = -1;
			double rand = Params.rand.nextDouble();

			for (int i = 0; i < M; i++) {
				if (i * Params.gamma < rand && rand <= (i + 1) * Params.gamma) {
					op_code = i;
					break;
				}
			}
			if (op_code == -1) {
				op_code = this.best_opcode;
			}

			consumed_fes[op_code]++;

			int r = Params.rand.nextInt(this.M);
			double cr = Utils.gauss(mem_cr[r], 0.1);
			double f = Utils.cauchy_g(mem_f[r], 0.1);

			int skill = indiv.skill_factor;
			Individual child = null;
			if (op_code == 0) {
				child = SearchOperators.pbest1(indiv, pop, pbest, cr, f);
//				child = SearchOperators.gaussMutation(indiv);
			} else if (op_code == 1) {
//				child = SearchOperators.pbest1(indiv, pop, pbest, cr, f);
				child = SearchOperators.gaussMutation(indiv);
			} else {
				System.err.println("Phase2::Error, unexpected opcode: " + op_code);
				System.exit(-1);
			}

			child.skill_factor = skill;
			child.setFitness(skill, task.calculateFitnessValue(child.chromosome));

			Individual survival = null;
			double diff = indiv.getFitness(skill) - child.getFitness(skill);
			if (diff > 0) {
				survival = child;

				this.sum_improv[op_code] += diff;

				if (op_code == 0) {
					this.diff_f.add(diff);
					this.s_cr.add(cr);
					this.s_f.add(f);
				}
			} else if (diff == 0 || Params.rand.nextDouble() <= sigma * Math.exp(diff / maxDelta)) {
				survival = child;
			} else {
				survival = indiv;
			}

			nextPop.add(survival);
			best = survival.getFitness(skill) < best.getFitness(skill) ? survival : best;
		}

		pop.clear();
		pop.addAll(nextPop);

		return best;
	}

	private void updateMemory() {
		// update F, CR memory
		if (s_cr.size() > 0) {
			mem_cr[mem_pos] = 0;
			mem_f[mem_pos] = 0;
			double temp_sum_cr = 0;
			double temp_sum_f = 0;
			double sum_diff = 0;

			for (double d : diff_f) {
				sum_diff += d;
			}

			for (int i = 0; i < s_cr.size(); i++) {
				double weight = diff_f.get(i) / sum_diff;

				mem_f[mem_pos] += weight * s_f.get(i) * s_f.get(i);
				temp_sum_f += weight * s_f.get(i);

				mem_cr[mem_pos] += weight * s_cr.get(i) * s_cr.get(i);
				temp_sum_cr += weight * s_cr.get(i);
			}

			mem_f[mem_pos] /= temp_sum_f;

			if (temp_sum_cr == 0 || mem_cr[mem_pos] == -1)
				mem_cr[mem_pos] = -1;
			else
				mem_cr[mem_pos] /= temp_sum_cr;

			mem_pos++;
			if (mem_pos >= H) {
				mem_pos = 0;
			}

			s_cr.clear();
			s_f.clear();
			diff_f.clear();
		}
	}

	private void updateBestOperator() {
		if (gen > 1) {
			int best_op = -1;
			double best_rate = -1;

			for (int i = 0; i < M; i++) {
				double eta = sum_improv[i] / (1.0 * consumed_fes[i]);
				if (eta > best_rate) {
					best_op = i;
					best_rate = eta;
				}
			}

			if (best_rate > 0) {
				this.best_opcode = best_op;
			} else {
				this.best_opcode = Params.rand.nextInt(M);
			}

			for (int i = 0; i < M; i++) {
				sum_improv[i] = 0;
				consumed_fes[i] = 1;
			}
		}
	}
}
