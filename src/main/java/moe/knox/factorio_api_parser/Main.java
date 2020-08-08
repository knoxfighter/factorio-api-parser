package moe.knox.factorio_api_parser;

import moe.knox.factorio_api_parser.lua_types.Attribute;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class Main {
	public static void main(String[] args) {
		// Download and parse a single page
		Map<String, LuaApiParser.ParseOverviewResult> overviewResultMap = LuaApiParser.parseVersionList();

		// save the file
		overviewResultMap.forEach((versionName, parseOverviewResult) -> {
			String basePath = "luaOutput/" + versionName + "/";

			try {
				Files.createDirectories(Paths.get(basePath));
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}

			parseOverviewResult.classes.forEach((className, aClass) -> {
				// open file to write to!
				try {
					FileOutputStream outputStream = new FileOutputStream(basePath + className + ".lua");
					aClass.saveToFile(outputStream);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});

			try {
				FileOutputStream outputStream = new FileOutputStream(basePath + "globals.lua");
				for (Attribute global : parseOverviewResult.globals) {
					global.saveToFile(outputStream, "");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				FileOutputStream outputStream = new FileOutputStream(basePath + "defines.lua");
				parseOverviewResult.defines.saveToFile(outputStream);
			} catch (IOException e) {
				e.printStackTrace();
			}

			// copy in the hardcoded.lua file
			try {
				Files.copy(Paths.get(Main.class.getClassLoader().getResource("hardcoded.lua").toURI()), Paths.get(basePath + "hardcoded.lua"), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		});
	}
}
