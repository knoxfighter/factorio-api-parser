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
import java.util.HashSet;
import java.util.Set;

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

	private static void parseLinkElementsAndTheirPage(Path newPath, Elements allPrototypeLinks) throws IOException {
		// generate unique list of links
		Set<String> links = new HashSet<>();
		for (Element allPrototypeLink : allPrototypeLinks) {
			String href = allPrototypeLink.attr("href");
			if (!href.equals("http://lua-api.factorio.com/latest/Concepts.html#Modifier")) {
				links.add(href);
			}
		}

		// iterate over the unique list
		for (String link : links) {
			Document subPage = Jsoup.connect("https://wiki.factorio.com" + link).get();
			Element subContentText = subPage.getElementById("mw-content-text");
			String subContentHtml = subContentText.html();
			String subContentClean = Jsoup.clean(subContentHtml, "https://wiki.factorio.com" + link, new CustomHtmlWhitelist());
			link = link.replace("/", "_");
			Files.writeString(newPath.resolve(link + ".html"), subContentClean, StandardOpenOption.CREATE_NEW);
		}
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

		// download and save all prototype pages
		Elements allPrototypeLinks = mainPage.select(".prototype-toc-section-title > a:first-child");
		parseLinkElementsAndTheirPage(newPath, allPrototypeLinks);

		// download all type pages
		Elements allTypeLinks = mainPage.select(".prototype-toc-item-info > a");
		parseLinkElementsAndTheirPage(newPath, allTypeLinks);

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
			DiscordWebhook discordWebhook = new DiscordWebhook("https://discordapp.com/api/webhooks/761954324184039465/7_ohKza2qosgvEH3EK70mvmA0o2wZ4Uaz-HJHDfLS3DBlyU24ah2dDQZKa9dWp7ja2Q5");
			DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
			embed.setTitle("Some changes to the prototype definitions happened");
			embed.setDescription(String.format("https://factorio-api.knox.moe/wiki/%s", diffFileName));
			discordWebhook.addEmbed(embed);
			discordWebhook.execute();
		}

		// move everything from the new directory to the old directory
		moveDirectory(newPath, oldPath);


		////////////////////////////////////////////////
		// remove all properties with the name "type" //
		////////////////////////////////////////////////

//		Map<String, Prototype> prototypes;
//		// read prototypes from jsom file
//		try (FileReader prototypeJsonRead = new FileReader("prototypes.json")) {
//			Type mapType = new TypeToken<Map<String, Prototype>>() {
//			}.getType();
//			prototypes = new GsonBuilder().create().fromJson(prototypeJsonRead, mapType);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//			return;
//		} catch (IOException e) {
//			e.printStackTrace();
//			return;
//		}
//
//		prototypes.forEach((s, prototype) -> prototype.properties.removeIf(property -> property.name.equals("type")));
//
//		// save json again
//		try (Writer writer = new FileWriter("prototypes.json")) {
//			new GsonBuilder().setPrettyPrinting().create().toJson(prototypes, writer);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
}
