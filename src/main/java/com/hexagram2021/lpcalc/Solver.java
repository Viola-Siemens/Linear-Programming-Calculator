package com.hexagram2021.lpcalc;

import Jama.Matrix;
import Jama.QRDecomposition;

public class Solver {
	private final Matrix A;
	private final Matrix B;
	private final Matrix C;
	boolean[] isBase;

	private double best = Double.MAX_VALUE;
	private Matrix best_x;

	private final int n, m;

	public Solver(int n, int m, double[][] A, double[] B, double[] C) {
		this.A = new Matrix(A, m, n);
		this.B = new Matrix(B, m);
		this.C = new Matrix(C, n);
		this.isBase = new boolean[n];
		this.n = n;
		this.m = m;
	}

	private void compute() {
		double[][] A2 = new double[this.m][this.m + 1];

		boolean flag = false;
		for(int i = 0; i < this.n; ++i) {
			if(this.isBase[i]) {
				if(flag) {
					this.builder.append(", %d".formatted(i + 1));
				} else {
					this.builder.append("| (%d".formatted(i + 1));
					flag = true;
				}
			}
		}
		this.builder.append(") | ");

		for(int i = 0; i < this.m; ++i) {
			int cnt = 0;
			for(int j = 0; j < this.n; ++j) {
				if(this.isBase[j]) {
					cnt += 1;
				} else {
					A2[i][j - cnt] = this.A.get(i, j);
				}
			}
		}

		Matrix mtx = new Matrix(A2, this.m, this.m);
		QRDecomposition comp = mtx.qr();

		if(!comp.isFullRank()) {
			this.builder.append("No solution | - |\n");
			return;
		}

		//QRx = B

		Matrix x = comp.solve(this.B);

		boolean failed = false;
		flag = false;
		double ans = 0;
		int cnt = 0;
		for(int i = 0; i < this.n; ++i) {
			if(this.isBase[i]) {
				if(flag) {
					this.builder.append(", 0");
				} else {
					this.builder.append("(0");
					flag = true;
				}
				cnt += 1;
			} else {
				if(flag) {
					this.builder.append(", %f".formatted(x.get(i - cnt, 0)));
				} else {
					this.builder.append("(%f".formatted(x.get(i - cnt, 0)));
					flag = true;
				}
				if(x.get(i - cnt, 0) < 0) {
					failed = true;
				} else {
					ans += this.C.get(i, 0) * x.get(i - cnt, 0);
				}
			}
		}
		if(failed) {
			this.builder.append(") | - |\n");
		} else {
			this.builder.append(") | %f |\n".formatted(ans));
			if(ans < this.best) {
				this.best = ans;
				this.best_x = new Matrix(this.n, 1);

				cnt = 0;
				for(int i = 0; i < this.n; ++i) {
					if(this.isBase[i]) {
						cnt += 1;
					} else {
						this.best_x.set(i, 0, x.get(i - cnt, 0));
					}
				}
			}
		}
	}

	private void solve(int depth, int select) {
		if(depth == this.n) {
			assert select == this.n - this.m : "ERROR: select = %d\n".formatted(select);
			this.compute();
			return;
		}
		if(select < this.n - this.m) {
			this.isBase[depth] = true;
			solve(depth + 1, select + 1);
			this.isBase[depth] = false;
		}
		if(this.m - depth + select > 0) {
			solve(depth + 1, select);
		}
	}

	StringBuilder builder;

	public void solve() {
		this.builder = new StringBuilder();
		this.builder.append("\n| 基变量 | 解 | 结果 |\n|---|---|---|\n");
		this.solve(0, 0);
		this.builder.append("\n全局最优解：%f\n".formatted(this.best));
		Main.log(this.builder.toString());
	}

	public double[] getAnswer() {
		return this.best_x.getColumnPackedCopy();
	}

	public double getBest() {
		return this.best;
	}
}
