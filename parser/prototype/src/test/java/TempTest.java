import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import moe.knox.prototype.types.Prototype;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TempTest {
	@org.junit.jupiter.api.Test
	void prototypeTest() {
		Map<String, Prototype> prototypes;
		// read prototypes from jsom file
		try (FileReader prototypeJsonRead = new FileReader("../types.json")) {
			Type mapType = new TypeToken<Map<String, Prototype>>() {}.getType();
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

		// save json again
		try (Writer writer = new FileWriter("../types.json")) {
			new GsonBuilder()
					.setPrettyPrinting()
					.registerTypeAdapter(Prototype.class, new Prototype.PrototypeSerializer())
					.create()
					.toJson(prototypes, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
