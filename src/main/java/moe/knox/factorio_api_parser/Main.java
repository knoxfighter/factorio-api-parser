package moe.knox.factorio_api_parser;

import moe.knox.factorio_api_parser.lua_types.Class;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class Main {
	public static void main(String[] args) {
		// Download and parse a single page
		Map<String, Class> stringClassMap = LuaApiParser.parseClassFromDownload("https://lua-api.factorio.com/latest/LuaEntity.html");
		System.out.println(stringClassMap);

		// save the file
		stringClassMap.forEach((s, aClass) -> {
			// open file to write to!
			try {
				FileOutputStream outputStream = new FileOutputStream("test/" + s + ".lua");
				aClass.saveToFile(outputStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}
