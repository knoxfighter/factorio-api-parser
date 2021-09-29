import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import moe.knox.prototype.types.Property;
import moe.knox.prototype.types.Prototype;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TempTest {
	void appendProperties(Writer writer, Prototype parentPrototype, Prototype basePrototype, Map<String, Prototype> prototypes) throws IOException {
		for (Property property : parentPrototype.table.properties) {
			if (property.description != null && !property.description.isEmpty()) {
				writer.append(String.format("---%s\n", property.description));
			}
			if (property._default != null && !property._default.isEmpty()) {
				writer.append(String.format("---@default %s\n", property._default));
			}
			writer.append(String.format("---@optional %b\n", property.optional));
			writer.append(String.format("---@type %s\n", property.type));
			writer.append(String.format("%s.%s = nil\n\n", basePrototype.name, property.name));
		}

		if (parentPrototype.table.parent != null && !parentPrototype.table.parent.isEmpty()) {
			if (parentPrototype.table.parent.contains(":")) {
				for (String prototypeName : parentPrototype.table.parent.split(":")) {
					Prototype newPrototype = prototypes.get(prototypeName);
					appendProperties(writer, newPrototype, basePrototype, prototypes);
				}
			} else {
				Prototype newPrototype = prototypes.get(parentPrototype.table.parent);
				appendProperties(writer, newPrototype, basePrototype, prototypes);
			}
		}
	}

	@Test
	void prototypeTest() {
		Map<String, Prototype> prototypes;
		// read prototypes from jsom file
		try (FileReader prototypeJsonRead = new FileReader("../prototypes.json")) {
			Type mapType = new TypeToken<Map<String, Prototype>>() {
			}.getType();
			prototypes = new GsonBuilder()
					.registerTypeAdapter(Prototype.class, new Prototype.PrototypeDeserializer())
					.create()
					.fromJson(prototypeJsonRead, mapType);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		/**
		 * replace Animation with AnimationType
		 */
//		prototypes.forEach((s, prototype) -> {
//			if (prototype.type.equals("Sprite")) {
//				prototype.type = "SpriteType";
//			} else if (prototype.type.equals("Sprite[]")) {
//				prototype.type = "SpriteType[]";
//			}
//
//			if (prototype.alias != null && prototype.alias.equals("Sprite")) {
//				prototype.alias = "SpriteType";
//			} else if (prototype.alias != null && prototype.alias.equals("Sprite[]")) {
//				prototype.alias = "SpriteType[]";
//			}
//
//			if (prototype.type.equals("table")) {
//				if (prototype.table.parent != null && prototype.table.parent.equals("Sprite")) {
//					prototype.table.parent = "SpriteType";
//				} else if (prototype.table.parent != null && prototype.table.parent.equals("Sprite[]")) {
//					prototype.table.parent = "SpriteType[]";
//				}
//
//				prototype.table.properties.forEach(property -> {
//					if (property.type.equals("Sprite")) {
//						property.type = "SpriteType";
//					} else if (property.type.equals("Sprite[]")) {
//						property.type = "SpriteType[]";
//					}
//				});
//			}
//		});

		/**
		 * make IDs to Prototype Type
		 */
//		Map<String, String> stringMapA = new HashMap<>();
//		prototypes.forEach((s, prototype) -> {
//			String id = prototype.id;
//			if (id != null && !id.isEmpty()) {
//				stringMapA.put(id, prototype.name);
//				prototype.id = null;
//			}
//		});
//		Prototype prototype = new Prototype();
//		prototype.name = "Prototype";
//		prototype.type = "prototype";
//		prototype.stringMap = stringMapA;
//		prototypes.put("Prototype", prototype);

		/**
		 * move "id" to "name"
		 */
//		prototypes.forEach((s, prototype) -> {
//			prototype.name = prototype.id;
//			prototype.id = null;
//		});

		/**
		 * move Property._default to Property.default
		 */
//		prototypes.forEach((s, prototype) -> {
//			prototype.table.properties.forEach(property -> {
//				property._default = property.__default;
//				property.__default = null;
//			});
//		});

		/**
		 * move parent from direct to value
		 */
//		prototypes.forEach((key, prototype) -> {
//			prototype.table.parent = prototype.parent;
//			prototype.parent = null;
//		});

		/**
		 * move properties from direct to value
		 */
//		prototypes.forEach((key, prototype) -> {
//			prototype.table.properties = prototype.properties;
//			prototype.properties = null;
//		});
//		System.out.println(prototypes);

		/**
		 * Set type to "table"
		 */
//		prototypes.forEach((key, prototype) -> {
//			if (prototype.type != null) {
//				throw new NullPointerException();
//			}
//			prototype.type = "table";
//		});

		/**
		 * remove "Prototype/" from parent
		 */
//		prototypes.forEach((key, prototype) -> {
//			if (prototype.parent != null && !prototype.parent.isEmpty() && !prototype.parent.contains(":")) {
//				prototype.parent = prototype.parent.substring(prototype.parent.lastIndexOf("/") + 1);
//			}
//		});

		/**
		 * Remove "Prototype/" from keys and name
		 */
//		Set<String> OrigKeys = prototypes.keySet();
//		Set<String> keys = new HashSet<>(OrigKeys);
//		for (String key : keys) {
//			Prototype prototype = prototypes.remove(key);
//			prototypes.put(prototype.name, prototype);
//		}

		// save prototypes as LUA file
//		try {
//			Writer writer = new FileWriter("../../files/prototypes/prototypes.lua");
//
//			for (Map.Entry<String, Prototype> entry : prototypes.entrySet()) {
//				Prototype prototype = entry.getValue();
//				if (prototype.type.equals("table")) {
//					writer.append(String.format("---%s\n", prototype.description));
//					writer.append(String.format("---@class %s\n", prototype.name));
//					// do not use parent here, we add all the properties manually, this has to be done, cause multi-inheritance is not supported.
//					writer.append(String.format("local %s = {}\n\n", prototype.name));
//
//					appendProperties(writer, prototype, prototype, prototypes);
//				} else if (prototype.type.equals("alias")) {
//					writer.append(String.format("---@alias %s %s\n\n", prototype.name, prototype.alias));
//				} else if (prototype.type.equals("string")) {
//					writer.append(String.format("---@alias %s string\n\n", prototype.name));
//				} else if (prototype.type.equals("stringArray")) {
//					writer.append(String.format("---@alias %s string[]\n\n", prototype.name));
//				}
//			}
//			writer.flush();
//			writer.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		// save json again
		try (Writer writer = new FileWriter("../prototypes_small.json")) {
			new GsonBuilder()
//					.setPrettyPrinting()
					.disableHtmlEscaping()
					.registerTypeAdapter(Prototype.class, new Prototype.PrototypeSerializer())
					.create()
					.toJson(prototypes, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
