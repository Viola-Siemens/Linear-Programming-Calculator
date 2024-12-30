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
	public record Composition(String name) implements ISerializable, IHasName {
		@Override @Nonnull
		public JsonObject toJson() {
			JsonObject ret = new JsonObject();
			ret.addProperty("name", this.name);
			return ret;
		}

		public static Composition fromJson(JsonObject json) {
			return new Composition(json.get("name").getAsString());
		}

		@Override
		public String toString() {
			return name;
		}
	}
	public record Constraint(Composition composition, double constraint, Constraint.ConstraintType type) implements ISerializable {
		public enum ConstraintType {
			EQUALS {
				@Override
				public int freeMulti() {
					return 0;
				}

				@Override
				public String getCharacter() {
					return "==";
				}

				@Override
				public String toString() {
					return "等于";
				}
			},
			LESS_THAN {
				@Override
				public int freeMulti() {
					return 1;
				}

				@Override
				public String getCharacter() {
					return "<=";
				}

				@Override
				public String toString() {
					return "少于";
				}
			},
			GREATER_THAN {
				@Override
				public int freeMulti() {
					return -1;
				}

				@Override
				public String getCharacter() {
					return ">=";
				}

				@Override
				public String toString() {
					return "多于";
				}
			};

			public abstract int freeMulti();
			public abstract String getCharacter();
		}

		@Override @Nonnull
		public JsonObject toJson() {
			JsonObject ret = new JsonObject();
			ret.addProperty("id", this.composition.name());
			ret.addProperty("constraint", this.constraint);
			ret.addProperty("type", this.type.name());
			return ret;
		}

		public static Constraint fromJson(JsonObject json, List<Composition> registry) {
			return new Constraint(registry.stream().filter(composition -> composition.name.equals(json.get("id").getAsString())).findFirst().orElseThrow(
					() -> new IllegalStateException("配置文件错误：没有名为“%s”的成分。\n解析自约束。".formatted(json.get("id").getAsString()))
			), json.get("constraint").getAsDouble(), Constraint.ConstraintType.valueOf(json.get("type").getAsString()));
		}
	}
	public record Material(String name, double price, Map<Composition, Double> compositions) implements ISerializable, IHasName {
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

			String name = json.get("name").getAsString();
			JsonArray jsonArray = json.get("compositions").getAsJsonArray();
			jsonArray.forEach(jsonElement -> {
				JsonObject entry = jsonElement.getAsJsonObject();
				compositions.put(registry.stream().filter(composition -> composition.name.equals(entry.get("id").getAsString())).findFirst().orElseThrow(
						() -> new IllegalStateException("配置文件错误：没有名为“%s”的成分。\n解析自原料“%s”。".formatted(entry.get("id").getAsString(), name))
				), entry.get("amount").getAsDouble());
			});

			return new Material(name, json.get("price").getAsDouble(), compositions);
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public final File configFile = new File("./config.json");

	public static class Sample {
		final Composition c1 = new Composition("粗蛋白");
		final Composition c2 = new Composition("粗纤维");
		final Composition c3 = new Composition("钠");
		final Material m1 = new Material("玉米", 2.85, Maps.newHashMap(ImmutableMap.of(this.c1, 7.85, this.c2, 1.6, this.c3, 0.02)));
		final Material m2 = new Material("面粉", 3.35, Maps.newHashMap(ImmutableMap.of(this.c1, 15.5, this.c2, 1.4, this.c3, 0.12)));

		final Constraint cst1 = new Constraint(this.c1, 10, Constraint.ConstraintType.GREATER_THAN);
		final Constraint cst2 = new Constraint(this.c2, 12, Constraint.ConstraintType.GREATER_THAN);
		final Constraint cst3 = new Constraint(this.c3, 0.2, Constraint.ConstraintType.GREATER_THAN);
		final Constraint cst4 = new Constraint(this.c3, 2, Constraint.ConstraintType.LESS_THAN);

		JsonObject getJson(List<Composition> compositions, List<Material> materials, List<Constraint> constraints) {
			JsonArray aComposition = new JsonArray();
			aComposition.add(this.c1.toJson());
			aComposition.add(this.c2.toJson());
			aComposition.add(this.c3.toJson());
			JsonArray aMaterial = new JsonArray();
			aMaterial.add(this.m1.toJson());
			aMaterial.add(this.m2.toJson());
			JsonArray aConstraints = new JsonArray();
			aConstraints.add(this.cst1.toJson());
			aConstraints.add(this.cst2.toJson());
			aConstraints.add(this.cst3.toJson());
			aConstraints.add(this.cst4.toJson());
			JsonObject ret = new JsonObject();
			ret.add("compositions", aComposition);
			ret.add("materials", aMaterial);
			ret.add("constraints", aConstraints);

			compositions.add(this.c1);
			compositions.add(this.c2);
			compositions.add(this.c3);
			materials.add(this.m1);
			materials.add(this.m2);
			constraints.add(this.cst1);
			constraints.add(this.cst2);
			constraints.add(this.cst3);
			constraints.add(this.cst4);

			return ret;
		}
	}

	private final List<Composition> compositions = Lists.newArrayList();
	private final List<Material> materials = Lists.newArrayList();
	private final List<Constraint> constraints = Lists.newArrayList();

	public int getCompositionCount() {
		return this.compositions.size();
	}
	public int getMaterialCount() {
		return this.materials.size();
	}
	public int getConstraintCount() {
		return this.constraints.size();
	}

	public List<Composition> getCompositions() {
		return this.compositions;
	}
	public List<Material> getMaterials() {
		return this.materials;
	}
	public List<Constraint> getConstraints() {
		return this.constraints;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public static boolean verifyRegistry(List<? extends IHasName> registry, String name) {
		for(IHasName entry: registry) {
			if(entry.name().equals(name)) {
				return false;
			}
		}
		return true;
	}

	public static void verifyRegistry(List<? extends IHasName> registry) {
		for(IHasName entry: registry) {
			for(IHasName entry2: registry) {
				if(entry != entry2 && entry.equals(entry2)) {
					Main.error("重复的注册名：%s".formatted(entry.name()));
				}
			}
		}
	}

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
				Main.error("配置文件已存在！");
				return;
			}
			Writer writer = new FileWriter(this.configFile);

			writeJsonToFile(writer, null, new Sample().getJson(this.compositions, this.materials, this.constraints), 0);

			writer.close();
		} catch (IOException exception) {
			Main.except(exception);
		}
	}

	public void commit() {
		try {
			if(!this.configFile.exists()) {
				Main.error("配置文件丢失！");
				return;
			}

			Writer writer = new FileWriter(this.configFile);

			writeJsonToFile(writer, null, getJson(this.compositions, this.materials, this.constraints), 0);

			writer.close();
		} catch (IOException exception) {
			Main.except(exception);
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
			config.get("constraints").getAsJsonArray().forEach(jsonElement -> this.constraints.add(Constraint.fromJson(jsonElement.getAsJsonObject(), this.compositions)));
		} catch (IOException | IllegalStateException | JsonSyntaxException exception) {
			Main.except(exception);
		}

		verifyRegistry(this.compositions);
		verifyRegistry(this.materials);
	}

	private static JsonObject getJson(List<Composition> compositions, List<Material> materials, List<Constraint> constraints) {
		JsonArray aComposition = new JsonArray();
		for(Composition composition: compositions) {
			aComposition.add(composition.toJson());
		}
		JsonArray aMaterial = new JsonArray();
		for(Material material: materials) {
			aMaterial.add(material.toJson());
		}
		JsonArray aConstraint = new JsonArray();
		for(Constraint constraint: constraints) {
			aConstraint.add(constraint.toJson());
		}
		JsonObject ret = new JsonObject();
		ret.add("compositions", aComposition);
		ret.add("materials", aMaterial);
		ret.add("constraints", aConstraint);

		return ret;
	}

	public LazyGenerator getLazyGenerator() {
		return new LazyGenerator();
	}

	public class LazyGenerator {
		private double[][] A = null;
		private double[] B = null;
		private double[] C = null;

		private int N = -1;

		private void initN() {
			int cnt = ConfigHelper.this.getMaterialCount();
			for (Constraint constraint : ConfigHelper.this.constraints) {
				if (constraint.type().freeMulti() != 0) {
					++cnt;
				}
			}
			this.N = cnt;
		}

		public double[][] getA() {
			if(this.A != null) {
				return this.A;
			}
			if(this.N < 0) {
				this.initN();
			}
			this.A = new double[ConfigHelper.this.getConstraintCount()][this.N];
			int tmp = ConfigHelper.this.getMaterialCount();
			for(int i = 0; i < ConfigHelper.this.getConstraintCount(); ++i) {
				for(int j = 0; j < ConfigHelper.this.getMaterialCount(); ++j) {
					this.A[i][j] = ConfigHelper.this.materials.get(j).compositions().getOrDefault(ConfigHelper.this.constraints.get(i).composition(), 0.0);
				}
				if(ConfigHelper.this.constraints.get(i).type().freeMulti() != 0) {
					this.A[i][tmp] = ConfigHelper.this.constraints.get(i).type().freeMulti();
					++tmp;
				}
			}

			return this.A;
		}

		public double[] getB() {
			if(this.B != null) {
				return this.B;
			}
			this.B = new double[ConfigHelper.this.getConstraintCount()];
			for(int i = 0; i < ConfigHelper.this.getConstraintCount(); ++i) {
				this.B[i] = ConfigHelper.this.constraints.get(i).constraint();
			}

			return this.B;
		}

		public double[] getC() {
			if(this.C != null) {
				return this.C;
			}
			if(this.N < 0) {
				this.initN();
			}
			this.C = new double[this.N];
			for(int i = 0; i < ConfigHelper.this.getMaterialCount(); ++i) {
				this.C[i] = ConfigHelper.this.materials.get(i).price();
			}

			return this.C;
		}

		public int getN() {
			if(this.N < 0) {
				this.initN();
			}
			return this.N;
		}

		public int getM() {
			return ConfigHelper.this.getConstraintCount();
		}
	}
}
