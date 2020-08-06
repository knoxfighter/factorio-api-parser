package moe.knox.factorio_api_parser.lua_types;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Defines {
	public List<String> classes = new ArrayList<>();
	public List<Define> values = new ArrayList<>();

	public void saveToFile(FileOutputStream outputStream) throws IOException {
		// save defines to file
		outputStream.write(("---@class defines\n").getBytes());
		outputStream.write(("defines = {}\n\n").getBytes());

		for (String defineClass : classes) {
			outputStream.write(("---@class " + defineClass + "\n").getBytes());
			outputStream.write((defineClass + " = {}\n\n").getBytes());
		}

		for (Define value : values) {
			value.saveToFile(outputStream);
		}

		outputStream.flush();
	}
}
