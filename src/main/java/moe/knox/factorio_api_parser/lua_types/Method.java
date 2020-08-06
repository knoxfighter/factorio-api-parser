package moe.knox.factorio_api_parser.lua_types;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class Method {
	public String name;
	public String returnType;
	public String returnTypeDesc;
	public String description;
	public Map<String, MethodParameter> parameters = new HashMap<>();
	public boolean paramTable;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || !(o instanceof Method)) return false;
		Method method = (Method) o;
		if (!Objects.equals(name, method.name)) {
			System.out.printf("Method.equals - %s - name -- %s != %s", name, name, method.name);
			return false;
		}
		if (!Objects.equals(returnType, method.returnType)) {
			System.out.printf("Method.equals - %s - returnType -- %s != %s", name, returnType, method.returnType);
			return false;
		}
		if (!Objects.equals(returnTypeDesc, method.returnTypeDesc)) {
			System.out.printf("Method.equals - %s - returnTypeDesc -- %s != %s", name, returnTypeDesc, method.returnTypeDesc);
			return false;
		}
		if (!Objects.equals(description, method.description)) {
			System.out.printf("Method.equals - %s - description -- %s != %s", name, description, method.description);
			return false;
		}
		if (!Objects.equals(parameters, method.parameters)) {
			System.out.printf("Method.equals - %s - parameters -- %s != %s", name, parameters, method.parameters);
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, returnType, returnTypeDesc, description, parameters);
	}

	public void saveToFile(FileOutputStream outputStream, String parentClass) throws IOException {
		// description
		for (String s : description.split("\\n")) {
			outputStream.write(("---" + s + "\n").getBytes());
		}

		// parameter
		String paramList = "";
		boolean first = true;
		for (Map.Entry<String, MethodParameter> entry : parameters.entrySet()) {
			MethodParameter methodParameter = entry.getValue();
			outputStream.write(("---@param " + methodParameter.name).getBytes());
			if (methodParameter.type != null && !methodParameter.type.isEmpty()) {
				outputStream.write((" " + methodParameter.type).getBytes());
			}
			if (methodParameter.description != null && !methodParameter.description.isEmpty()) {
				outputStream.write((" " + methodParameter.description).getBytes());
			}
			outputStream.write(("\n").getBytes());

			if (!first) {
				paramList += ", ";
			}
			paramList += methodParameter.name;
			first = false;
		}

		// return
		if (returnType != null && !returnType.isEmpty()) {
			outputStream.write(("---@return " + returnType).getBytes());
			if (returnTypeDesc != null && !returnTypeDesc.isEmpty()) {
				outputStream.write((" " + returnTypeDesc).getBytes());
			}
			outputStream.write("\n".getBytes());
		}

		// signature
		outputStream.write(("function " + parentClass + "." + name + "(" + paramList + ") end\n\n").getBytes());
		outputStream.flush();
	}
}
