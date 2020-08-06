package moe.knox.factorio_api_parser.lua_types;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class Attribute {
	public String name;
	public String type;
	public String description;
	public boolean readOnly;
	public boolean writeOnly;
	public boolean optional;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !(o instanceof Attribute)) return false;
		Attribute attribute = (Attribute) o;
		if (!Objects.equals(name, attribute.name)) {
			System.out.printf("Attribute.equals - %s - name -- %s != %s", name, name, attribute.name);
			return false;
		}
		if (!Objects.equals(readOnly, attribute.readOnly)) {
			System.out.printf("Attribute.equals - %s - readOnly -- %s != %s", name, readOnly, attribute.readOnly);
			return false;
		}
		if (!Objects.equals(writeOnly, attribute.writeOnly)) {
			System.out.printf("Attribute.equals - %s - writeOnly -- %s != %s", name, writeOnly, attribute.writeOnly);
			return false;
		}
		if (!Objects.equals(type, attribute.type)) {
			System.out.printf("Attribute.equals - %s - type -- %s != %s", name, type, attribute.type);
			return false;
		}
		if (!Objects.equals(description, attribute.description)) {
			System.out.printf("Attribute.equals - %s - description -- %s != %s", name, description, attribute.description);
			return false;
		}
		if (!Objects.equals(optional, attribute.optional)) {
			System.out.printf("Attribute.equals - %s - optional -- %s != %s", name, optional, attribute.optional);
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type, description, readOnly, writeOnly);
	}

	public void saveToFile(FileOutputStream outputStream, String parentClass) throws IOException {
		if (description != null && !description.isEmpty()) {
			for (String s : description.split("\n")) {
				outputStream.write(("---" + s + "\n").getBytes());
			}
		}
		outputStream.write(("---@type " + type + "\n").getBytes());
		if (parentClass != null && !parentClass.isEmpty()) {
			outputStream.write((parentClass + ".").getBytes());
		}
		outputStream.write((name + " = nil\n\n").getBytes());
		outputStream.flush();
	}
}
