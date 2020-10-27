package moe.knox.prototype.types;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Prototype {
	@SerializedName("name")
	public String name;

	@SerializedName("type")
	public String type;

	@SerializedName("link")
	String link;

	@SerializedName("description")
	String description;

	public transient Table table = new Table();
	public transient String alias;
	public transient Map<String, String> stringMap = new HashMap<>();

	public static class PrototypeDeserializer implements JsonDeserializer<Prototype> {
		@Override
		public Prototype deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			// deserialize annotated fields
			Prototype prototype = new Gson().fromJson(json, Prototype.class);

			// get full json object
			JsonObject jsonObject = json.getAsJsonObject();

			// check if this object has a "value" field, where the information is saved
			if (jsonObject.has("value")) {
				JsonElement jsonValue = jsonObject.get("value");
					if (jsonValue != null && !jsonValue.isJsonNull()) {
					// check if prototype is of type table
					if (prototype.type.equals("table") && jsonValue.isJsonObject()) {
						Table table = new Gson().fromJson(jsonValue, Table.class);
						prototype.table = table;
					} else if (prototype.type.equals("alias") && jsonValue.isJsonPrimitive()) {
						prototype.alias = jsonValue.getAsString();
					} else if ((prototype.type.equals("prototype") || prototype.type.equals("string") || prototype.type.equals("stringArray")) && jsonValue.isJsonObject()) {
						prototype.stringMap = new Gson().fromJson(jsonValue, new TypeToken<Map<String, String>>() {}.getType());
					}
				}
			}
			return prototype;
		}
	}

	public static class PrototypeSerializer implements JsonSerializer<Prototype> {
		@Override
		public JsonElement serialize(Prototype src, Type typeOfSrc, JsonSerializationContext context) {
			// serialize annotated fields
			JsonElement jsonElement = new Gson().toJsonTree(src, typeOfSrc);

			// get overallElement as jsonObject
			JsonObject jsonObject = jsonElement.getAsJsonObject();

			if (src.type != null) {
				switch (src.type) {
					case "table":
						// serialize table
						JsonElement tableElement = new Gson().toJsonTree(src.table);

						// add to value field
						jsonObject.add("value", tableElement);
						break;
					case "alias":
						// serialize string with the alias
						jsonObject.addProperty("value", src.alias);
						break;
					case "prototype":
					case "string":
					case "stringArray":
						// serialize prototype map
						JsonElement protElement = new Gson().toJsonTree(src.stringMap);

						// add to value field
						jsonObject.add("value", protElement);
						break;
				}
			}
			return jsonElement;
		}
	}
}
