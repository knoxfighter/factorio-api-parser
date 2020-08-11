package moe.knox.factorio_api_parser;

import moe.knox.factorio_api_parser.lua_types.Attribute;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
	/**
	 * print the current progress with a pretty progress bar
	 *
	 * @param current
	 * @param max
	 * @param end     if `true` will add a `\n` to the end of the output
	 */
	public static void printCurrentProgress(int current, int max, String additionalName, boolean end) {
		StringBuilder stringBuilder = new StringBuilder("\r|");
		int amountOfEquals = ((int) (((float) current) / ((float) max) * 20));
		int amountOfSpaces = 20 - amountOfEquals - 1;
		for (int i = 0; i < amountOfEquals; i++) {
			stringBuilder.append("=");
		}
		for (int i = 0; i < amountOfSpaces; i++) {
			stringBuilder.append(" ");
		}
		stringBuilder.append(String.format("| %d/%d  %d%% - %s%s\r", current, max, ((int) (((float) current) / ((float) max) * 100)), additionalName, " ".repeat(10)));
		if (end) {
			stringBuilder.append("\n");
		}
		System.out.print(stringBuilder.toString());
		System.out.flush();
	}

	public static void main(String[] args) {
		String saveLocation = (args.length >= 1) ? args[0] : "../files";

		// Download and parse a single page
		Map<String, LuaApiParser.ParseOverviewResult> overviewResultMap = LuaApiParser.parseVersionList();

		// save the files
		System.out.println("Save everything to lua files:");
		AtomicInteger current = new AtomicInteger();
		overviewResultMap.forEach((versionName, parseOverviewResult) -> {
			String basePath = saveLocation + "/" + versionName + "/";

			printCurrentProgress(current.incrementAndGet(), overviewResultMap.size(), versionName, false);

			try {
				Files.createDirectories(Paths.get(basePath));
			} catch (IOException e) {
				e.printStackTrace();
				throw new UncheckedIOException(e);
			}

			parseOverviewResult.classes.forEach((className, aClass) -> {
				// open file to write to!
				try {
					FileOutputStream outputStream = new FileOutputStream(basePath + className + ".lua");
					aClass.saveToFile(outputStream);
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new UncheckedIOException(e);
				}
			});

			try {
				FileOutputStream outputStream = new FileOutputStream(basePath + "globals.lua");
				for (Attribute global : parseOverviewResult.globals) {
					global.saveToFile(outputStream, "");
				}
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new UncheckedIOException(e);
			}

			try {
				FileOutputStream outputStream = new FileOutputStream(basePath + "defines.lua");
				parseOverviewResult.defines.saveToFile(outputStream);
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new UncheckedIOException(e);
			}

			// copy in the hardcoded.lua file
			try {
				Files.copy(Main.class.getClassLoader().getResourceAsStream("hardcoded.lua"), Paths.get(basePath + "hardcoded.lua"), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
				throw new UncheckedIOException(e);
			}
		});
		printCurrentProgress(current.get(), current.get(), "Done!", true);
	}
}
