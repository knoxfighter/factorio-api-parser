package moe.knox.prototype;

import com.google.gson.GsonBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Parser {
	/**
	 * Replace every occurrence, that seems like it is a type, with its proper LUA representation
	 * - `array of type` -> `type[]`
	 * - `dictionary key → value` ->  `table<key, value>`
	 * - `type1 or type2` -> `type1|type2`
	 *
	 * @param s The string to replace on
	 * @return The newly created string, where everything is replaced (no side-effects)
	 */
	public static String replaceTypes(String s) {
		// replace array types
		s = s.replaceAll("array of ([\\w\\.]*)", "$1[]");
		s = s.replaceAll("table of ([\\w\\.]*)", "$1[]");

		// replace alternatives
		s = s.replaceAll(" or ", "|");
		return s;
	}

	public static Map<String, Prototype> fullParser() {
		Document document;
		try {
			document = Jsoup.connect("https://wiki.factorio.com/Prototype_overview").get();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		Set<String> types = new HashSet<>();
		Map<String, Prototype> prototypes = new HashMap<>();
		Prototype lastPrototype = null;
		Property lastProperty = null;

		Element prototpyeTocTable = document.selectFirst(".prototype-toc > tbody");
		for (Element prototypeRow : prototpyeTocTable.children()) {
			for (Element prototypeColumn : prototypeRow.children()) {
				if (prototypeColumn.hasClass("prototype-toc-section-title")) {
					// This is a new prototype
					Prototype prototype = new Prototype();

					Elements links = prototypeColumn.select("a");
					Element nameLink = links.get(0);
					prototype.name = nameLink.text();
					prototype.link = nameLink.attr("href");

					Element codeElement = prototypeColumn.selectFirst("code");
					String id = codeElement.text();
					if (!id.equals("abstract")) {
						prototype.id = codeElement.text();
					}

					if (links.size() >= 2) {
						Element parentLink = links.get(1);
						prototype.parent = parentLink.text();
					}

					lastPrototype = prototype;
					prototypes.put(prototype.name, prototype);
				} else if (prototypeColumn.hasClass("prototype-toc-item-name")) {
					Property property = new Property();
					if (prototypeColumn.select("a").size() == 0) {
						break;
					}
					property.name = prototypeColumn.text();

					lastProperty = property;
					lastPrototype.properties.add(property);
				} else if (prototypeColumn.hasClass("prototype-toc-item-info")) {
					String columnText = prototypeColumn.text();
					if (columnText.endsWith("(optional)")) {
						columnText = columnText.replace("(optional)", "");
						lastProperty.optional = true;
					}

					columnText = columnText.trim();
					columnText = replaceTypes(columnText);

					lastProperty.type = columnText;

					// get list of all used types
					Elements links = prototypeColumn.select("a");
					for (Element link : links) {
						types.add(link.attr("href"));
					}
				}
			}
		}

		return prototypes;
	}
}
