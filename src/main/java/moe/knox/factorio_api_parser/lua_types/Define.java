package moe.knox.factorio_api_parser.lua_types;

import java.io.FileOutputStream;
import java.io.IOException;

public class Define {
	String name;
	String description;

	public Define(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public void saveToFile(FileOutputStream outputStream) throws IOException {
		String desc = description;
		if (desc != null && !desc.isEmpty()) {
			outputStream.write(("--- " + desc + "\n").getBytes());
		}
		outputStream.write(("---@type nil\n").getBytes());
		outputStream.write((name + " = nil\n\n").getBytes());
		outputStream.flush();
	}
}
