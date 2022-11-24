package com.hexagram2021.lpcalc.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.hexagram2021.lpcalc.Main;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.List;
import java.util.Map;

public class ConfigHelper {
	public record Composition(String name) implements ISerializable {
		@Override @Nonnull
		public JsonObject toJson() {
			JsonObject ret = new JsonObject();
			ret.addProperty("name", this.name);
			return ret;
		}

		public static Composition fromJson(JsonObject json) {
			return new Composition(json.get("name").getAsString());
		}
	}
	public record Material(String name, double price, Map<Composition, Double> compositions) implements ISerializable {
		@Override @Nonnull
		public JsonObject toJson() {
			JsonObject ret = new JsonObject();
			ret.addProperty("name", this.name);
			ret.addProperty("price", this.price);
			JsonArray aCompositions = new JsonArray();
			this.compositions.forEach((composition, amount) -> {
				JsonObject current = new JsonObject();
				current.addProperty("id", composition.name());
				current.addProperty("amount", amount);
				aCompositions.add(current);
			});
			ret.add("compositions", aCompositions);
			return ret;
		}

		public static Material fromJson(JsonObject json, List<Composition> registry) {
			Map<Composition, Double> compositions = Maps.newHashMap();

			JsonArray jsonArray = json.get("compositions").getAsJsonArray();
			jsonArray.forEach(jsonElement -> {
				JsonObject entry = jsonElement.getAsJsonObject();
				compositions.put(registry.stream().filter(composition -> composition.name.equals(entry.get("id").getAsString())).findFirst().orElseThrow(
						() -> new IllegalStateException("No ID named " + entry.get("id").getAsString())
				), entry.get("amount").getAsDouble());
			});

			return new Material(json.get("name").getAsString(), json.get("price").getAsDouble(), compositions);
		}
	}

	public final File configFile = new File("./config.json");

	public static class Sample {
		final Composition c1 = new Composition("粗蛋白");
		final Composition c2 = new Composition("粗纤维");
		final Composition c3 = new Composition("钠");
		final Material m1 = new Material("玉米", 2.85, ImmutableMap.of(this.c1, 7.85, this.c2, 1.6, this.c3, 0.02));
		final Material m2 = new Material("面粉", 3.35, ImmutableMap.of(this.c1, 15.5, this.c2, 1.4, this.c3, 0.12));

		JsonObject getJson(List<Composition> compositions, List<Material> materials) {
			JsonArray aComposition = new JsonArray();
			aComposition.add(this.c1.toJson());
			aComposition.add(this.c2.toJson());
			aComposition.add(this.c3.toJson());
			JsonArray aMaterial = new JsonArray();
			aMaterial.add(this.m1.toJson());
			aMaterial.add(this.m2.toJson());
			JsonObject ret = new JsonObject();
			ret.add("compositions", aComposition);
			ret.add("materials", aMaterial);

			compositions.add(this.c1);
			compositions.add(this.c2);
			compositions.add(this.c3);
			materials.add(this.m1);
			materials.add(this.m2);

			return ret;
		}
	}

	private final List<Composition> compositions = Lists.newArrayList();
	private final List<Material> materials = Lists.newArrayList();

	public ConfigHelper() {
		if(!this.configFile.isFile()) {
			this.createConfigFile();
		} else {
			this.loadConfigFile();
		}
	}

	public static void writeJsonToFile(Writer writer, String key, JsonElement json, int tab) throws IOException {
		writer.write(StringUtils.repeat('\t', tab));
		if(key != null) {
			writer.write("\"" + key + "\": ");
		}
		if(json.isJsonObject()) {
			writer.write("{\n");
			boolean first = true;
			for(Map.Entry<String, JsonElement> entry: json.getAsJsonObject().entrySet()) {
				if(first) {
					first = false;
				} else {
					writer.write(",\n");
				}
				writeJsonToFile(writer, entry.getKey(), entry.getValue(), tab + 1);
			}
			writer.write("\n" + StringUtils.repeat('\t', tab) + "}");
		} else if(json.isJsonArray()) {
			writer.write("[\n");
			boolean first = true;
			for (JsonElement element : json.getAsJsonArray()) {
				if (first) {
					first = false;
				} else {
					writer.write(",\n");
				}
				writeJsonToFile(writer, null, element, tab + 1);
			}
			writer.write("\n" + StringUtils.repeat('\t', tab) + "]");
		} else if(json.isJsonPrimitive()) {
			JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();
			if(jsonPrimitive.isBoolean()) {
				writer.write(String.valueOf(jsonPrimitive.getAsBoolean()));
			} else if(jsonPrimitive.isNumber()) {
				writer.write(String.valueOf(jsonPrimitive.getAsNumber().doubleValue()));
			} else if(jsonPrimitive.isString()) {
				writer.write('\"' + jsonPrimitive.getAsString() + '\"');
			}
		}
	}

	private void createConfigFile() {
		try {
			if(!this.configFile.createNewFile()) {
				Main.error("Config file already exists!");
				return;
			}
			Writer writer = new FileWriter(this.configFile);

			writeJsonToFile(writer, null, new Sample().getJson(this.compositions, this.materials), 0);

			writer.close();
		} catch (IOException exception) {
			Main.error(exception.getMessage());
		}
	}

	private void loadConfigFile() {
		try {
			Reader reader = new FileReader(this.configFile);
			JsonElement json = JsonParser.parseReader(reader);
			reader.close();

			JsonObject config = json.getAsJsonObject();
			config.get("compositions").getAsJsonArray().forEach(jsonElement -> this.compositions.add(Composition.fromJson(jsonElement.getAsJsonObject())));
			config.get("materials").getAsJsonArray().forEach(jsonElement -> this.materials.add(Material.fromJson(jsonElement.getAsJsonObject(), this.compositions)));
		} catch (IOException | IllegalStateException | JsonSyntaxException exception) {
			Main.error(exception.getMessage());
		}
	}
}
