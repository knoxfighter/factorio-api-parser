package moe.knox.factorio_api_parser;

import moe.knox.factorio_api_parser.lua_types.Attribute;

import java.io.FileOutputStream;
import java.io.IOException;

public class Main {
	public static void main(String[] args) {
		// Download and parse a single page
		LuaApiParser.ParseOverviewResult parseOverviewResult = LuaApiParser.parseOverviewPageFromDownload("https://lua-api.factorio.com/latest/");

		// save the file
		parseOverviewResult.classes.forEach((className, aClass) -> {
			// open file to write to!
			try {
				FileOutputStream outputStream = new FileOutputStream("test/" + className + ".lua");
				aClass.saveToFile(outputStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		try {
			FileOutputStream outputStream = new FileOutputStream("test/globals.lua");
			for (Attribute global : parseOverviewResult.globals) {
				global.saveToFile(outputStream, "");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
