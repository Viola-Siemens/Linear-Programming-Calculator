package com.hexagram2021.lpcalc;

import com.hexagram2021.lpcalc.utils.ConfigHelper;

import javax.annotation.Nonnull;

public class Main {
	public static void main(String[] args) {
		ConfigHelper helper = new ConfigHelper();
	}

	public static void error(@Nonnull String message) {
		System.err.println(message);
	}
}
