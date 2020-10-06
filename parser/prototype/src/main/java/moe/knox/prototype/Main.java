package moe.knox.prototype;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.Comparator;

public class Main {
	static void deleteDirectory(Path path) throws IOException {
		if (Files.exists(path)) {
			Files.walk(path)
					.sorted(Comparator.reverseOrder())
					.map(Path::toFile)
					.forEach(File::delete);
		}
	}

	public static void moveDirectory(Path src, Path dest) throws IOException {
		Files.walk(src)
				.forEach(subSource -> {
					if (src.equals(subSource)) {
						return;
					}
					Path resolvedDest = dest.resolve(src.relativize(subSource));
					try {
						Files.move(subSource, resolvedDest, StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				});
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		String saveLocation = ((args.length >= 1) ? args[0] : "../files") + "/prototypes";

		Path newPath = Paths.get(saveLocation + "/new/");
		Path oldPath = Paths.get(saveLocation + "/old/");
		Path diffPath = Paths.get(saveLocation + "/wiki/");

		// clear new directory
		deleteDirectory(newPath);
		Files.createDirectories(newPath);

		// create old directory if not exists
		if (!Files.exists(oldPath)) {
			Files.createDirectories(oldPath);
		}

		// create diff directory if not exists
		if (!Files.exists(diffPath)) {
			Files.createDirectories(diffPath);
		}

		// download and save main page
		Document mainPage = Jsoup.connect("https://wiki.factorio.com/Prototype_overview").get();

		Element mainContentText = mainPage.getElementById("mw-content-text");
		String mainContentHtml = mainContentText.html();
		String mainContentClean = Jsoup.clean(mainContentHtml, "https://wiki.factorio.com/Prototype_overview", new CustomHtmlWhitelist());
		Files.writeString(newPath.resolve("overview.html"), mainContentClean, StandardOpenOption.CREATE_NEW);

		// download all prototype pages
		Elements allPrototypeLinks = mainPage.select(".prototype-toc-section-title > a:first-child");
		for (Element allPrototypeLink : allPrototypeLinks) {
			String href = allPrototypeLink.attr("href");
			Document subPage = Jsoup.connect("https://wiki.factorio.com" + href).get();
			Element subContentText = subPage.getElementById("mw-content-text");
			String subContentHtml = subContentText.html();
			String subContentClean = Jsoup.clean(subContentHtml, "https://wiki.factorio.com" + href, new CustomHtmlWhitelist());
			href = href.replace("/", "_");
			Files.writeString(newPath.resolve(href + ".html"), subContentClean, StandardOpenOption.CREATE_NEW);
		}


		// save to file named after current timestamp
		String diffFileName = String.valueOf(System.currentTimeMillis());
		String diffFilePath = diffPath.resolve(diffFileName).toString();
		String diffCommand = String.format("diff \"%s\" \"%s\" | tee \"%s\"", newPath.toString(), oldPath.toString(), diffFilePath);

		// run the difftool to check for changes
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command("/bin/sh", "-c", diffCommand);
		Process process = processBuilder.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		boolean someOutput = false;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
			someOutput = true;
		}
		int processResult = process.waitFor();
		System.out.println("diff-tool result: " + processResult);

		if (someOutput) {
			DiscordWebhook discordWebhook = new DiscordWebhook(System.getenv("webhookLink"));
			DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
			embed.setTitle("Some changes to the prototype definitions happened");
			embed.setDescription(String.format("https://factorio-api.knox.moe/wiki/%s", diffFileName));
			discordWebhook.addEmbed(embed);
			discordWebhook.execute();
		}

		// move everything from the new directory to the old directory
		moveDirectory(newPath, oldPath);

//		Map<String, Prototype> prototypes;
//		// read prototypes from jsom file
//		try (FileReader reader = new FileReader("prototypes.json")) {
//			Type mapType = new TypeToken<Map<String, Prototype>>() { }.getType();
//			prototypes = new GsonBuilder().create().fromJson(reader, mapType);
//		} catch (FileNotFoundException e) {
//			prototypes = Parser.fullParser();
//		} catch (IOException e) {
//			e.printStackTrace();
//			return;
//		}
//
//		Scanner scanner = new Scanner(System.in);

		// description editor
		/*try {
			for (Map.Entry<String, Prototype> entry : prototypes.entrySet()) {
				String s = entry.getKey();
				Prototype prototype = entry.getValue();
				if (prototype.description != null && prototype.description.isEmpty()) {
					// no description yet, print link and ask for it
					System.out.println("https://wiki.factorio.com" + prototype.link);
					System.out.print("Description: ");
					String desc = scanner.nextLine();
					if (desc.equals("exit")) {
						break;
					} else {
						prototype.description = desc.strip();
					}
				}

				boolean breaker = false;
				for (Property property : prototype.properties) {
					boolean printed = false;
					if (property.description != null && property.description.isEmpty()) {
						// no description yet, print link and ask for it
						System.out.println("https://wiki.factorio.com" + prototype.link + "#" + property.name);
						printed = true;
						System.out.print("Description: ");
						if (prototype.id != null && (prototype.id.equals("utility-sprites") || prototype.id.equals("utility-sounds") || prototype.id.equals("utility-constants"))) {
							property.description = "null";
						} else {
							String desc = scanner.nextLine();
							if (desc.equals("exit")) {
								breaker = true;
								break;
							} else {
								property.description = desc.strip();
							}
						}
					}
					if (property._default != null && property._default.isEmpty()) {
						// no default value yet, ask for it
						if (!printed) {
							System.out.println("https://wiki.factorio.com" + prototype.link + "#" + property.name);
						}
						System.out.print("Default: ");
						if (prototype.id != null && (prototype.id.equals("utility-sprites") || prototype.id.equals("utility-sounds") || prototype.id.equals("utility-constants"))) {
							property._default = "null";
						} else {
							String def = scanner.nextLine();
							if (def.equals("exit")) {
								breaker = true;
								break;
							} else {
								property._default = def.strip();
							}
						}
					}
				}
				if (breaker) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}*/

		// replace "null" with null and resave
		/*for (Map.Entry<String, Prototype> entry : prototypes.entrySet()) {
			String s = entry.getKey();
			Prototype prototype = entry.getValue();

			for (Property property : prototype.properties) {
				if (property._default.equals("null")) {
					property._default = null;
				}

				if (property.description.equals("null")) {
					property.description = null;
				}
			}
		}*/

//		try (Writer writer = new FileWriter("prototypes.json")) {
//			new GsonBuilder().setPrettyPrinting().create().toJson(prototypes, writer);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
}
