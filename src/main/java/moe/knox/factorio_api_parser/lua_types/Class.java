package moe.knox.factorio_api_parser.lua_types;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class Class {
	public String name;
	public String parentClass;
	public String description;
	public String since;
	public Map<String, Method> methods = new HashMap<>();
	public Map<String, Attribute> attributes = new HashMap<>();

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !(o instanceof Class)) return false;
		Class aClass = (Class) o;
		if (!Objects.equals(name, aClass.name)) {
			System.out.printf("Class.equals - %s - parentClass -- %s != %s", name, name, aClass.name);
			return false;
		}
		if (!Objects.equals(parentClass, aClass.parentClass)) {
			System.out.printf("Class.equals - %s - parentClass -- %s != %s", name, parentClass, aClass.parentClass);
			return false;
		}
		if (!Objects.equals(description, aClass.description)) {
			System.out.printf("Class.equals - %s - description -- %s != %s", name, description, aClass.description);
			return false;
		}
		if (!Objects.equals(methods, aClass.methods)) {
			System.out.printf("Class.equals - %s - methods -- %s != %s", name, methods, aClass.methods);
			return false;
		}
		if (!Objects.equals(attributes, aClass.attributes)) {
			System.out.printf("Class.equals - %s - attributes -- %s != %s", name, attributes, aClass.attributes);
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, parentClass, description, methods, attributes);
	}

	public void saveToFile(FileOutputStream outputStream) throws IOException {
		// description
		if (description != null && !description.isEmpty()) {
			outputStream.write(("---" + description + "\n").getBytes());
		}

		// @since
		if (since != null && !since.isEmpty()) {
			outputStream.write(("---@since " + since + "\n").getBytes());
		}

		// @class
		outputStream.write(("---@class " + name + "").getBytes());
		if (parentClass != null && !parentClass.isEmpty()) {
			outputStream.write((" : " + parentClass).getBytes());
		}
		outputStream.write("\n".getBytes());

		// definition
		outputStream.write(("local " + name + " = {}\n\n").getBytes());
		outputStream.flush();

		// all methods
		for (Map.Entry<String, Method> entry : methods.entrySet()) {
			Method method = entry.getValue();
			method.saveToFile(outputStream, name);
		}

		// all attributes
		for (Map.Entry<String, Attribute> entry : attributes.entrySet()) {
			Attribute attribute = entry.getValue();
			attribute.saveToFile(outputStream, name);
		}
	}
}
