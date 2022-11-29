package com.hexagram2021.lpcalc.utils;

public interface IHasName {
	String name();

	default boolean equals(IHasName object) {
		return name().equals(object.name());
	}
}
