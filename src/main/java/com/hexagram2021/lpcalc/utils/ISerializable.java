package com.hexagram2021.lpcalc.utils;

import com.google.gson.JsonObject;

import javax.annotation.Nonnull;

public interface ISerializable {
	@Nonnull
	JsonObject toJson();
}
