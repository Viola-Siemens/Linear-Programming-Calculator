package com.hexagram2021.lpcalc;

import com.hexagram2021.lpcalc.utils.ConfigHelper;

import javax.annotation.Nonnull;

public class Main {
	public static class TestData {
		public static final int N = 6;
		public static final int M = 3;
		public static final double[][] A = {
			{ 1, 1, 3, -1, 0, 0 },
			{ 2, 2, 5, 0, -1, 0 },
			{ 4, 1, 2, 0, 0, -1 }
		};
		public static final double[] B = { 30, 24, 36 };
		public static final double[] C = { 3, 1, 2, 0, 0, 0 };
	}

	public static void main(String[] args) {
		ConfigHelper helper = new ConfigHelper();
		Solver solver = new Solver(TestData.N, TestData.M, TestData.A, TestData.B, TestData.C);
		solver.solve();
	}

	public static void error(@Nonnull String message) {
		System.err.println(message);
	}
}
