package moe.knox.factorio_api_parser.lua_types;

import java.util.Objects;

public class MethodParameter {
	public String name;
	public String type;
	public String description;
	public boolean optional;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !(o instanceof MethodParameter)) return false;
		MethodParameter that = (MethodParameter) o;
		if (!Objects.equals(optional, that.optional)) {
			System.out.printf("Parameter.equals - %s - optional -- %s != %s", name, optional, that.optional);
			return false;
		}
		if (!Objects.equals(name, that.name)) {
			System.out.printf("Parameter.equals - %s - name -- %s != %s", name, name, that.name);
			return false;
		}
		if (!Objects.equals(type, that.type)) {
			System.out.printf("Parameter.equals - %s - type -- %s != %s", name, type, that.type);
			return false;
		}
		if (!Objects.equals(description, that.description)) {
			System.out.printf("Parameter.equals - %s - description -- %s != %s", name, description, that.description);
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type, description, optional);
	}
}
