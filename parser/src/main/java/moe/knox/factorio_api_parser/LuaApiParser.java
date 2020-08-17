package moe.knox.factorio_api_parser;

import moe.knox.factorio_api_parser.lua_types.*;
import moe.knox.factorio_api_parser.lua_types.Class;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LuaApiParser {
	public static String factorioApiBaseLink = "https://lua-api.factorio.com/latest/index.html";
	//language=RegExp
	public static String typePattern = "[\\w\\[\\].|]*(?:<[\\w\\[\\]<>.,| ]*>)?[\\w\\[\\]>\\)\\(]*";
	public static Pattern functionPattern = Pattern.compile(
			String.format(
					"^(?<name>\\w*)[{(](?<param>.*)[)}](?: → (?<return>%s))?",
					LuaApiParser.typePattern
			)
	);

	public static class FunctionResult {
		public String name;
		public String params;
		public String _return;
	}

	/**
	 * run the functionRegex onto the given string and return the result in the custom class
	 */
	public static FunctionResult parseFunctionRegex(String toParse) {
		Matcher matcher = functionPattern.matcher(toParse);
		if (matcher.find()) {
			FunctionResult functionResult = new FunctionResult();
			functionResult.name = matcher.group("name");
			functionResult._return = matcher.group("return");
			functionResult.params = matcher.group("param");
			return functionResult;
		}

		return null;
	}

	public static Pattern fieldPattern = Pattern.compile(
			String.format(
					"^(?<name>[\\w.]*)(?: :: )?(?<type>%s)(?: (?<optional>\\(optional\\)))?:?(?: (?<desc>[^\\[\\n]*)?(?:\\[(?<rw>[RW]{0,2})\\])?)?()",
					LuaApiParser.typePattern
			)
	);

	public static class FieldResult {
		public String name;
		public String type;
		public boolean optional;
		public String description;
		public boolean readOnly;
		public boolean writeOnly;
	}

	/**
	 * run the fieldRegex onto the given string and return the result as the custom class
	 */
	public static FieldResult parseFieldRegex(String toParse) {
		Matcher matcher = fieldPattern.matcher(toParse);

		if (matcher.find()) {
			FieldResult fieldResult = new FieldResult();
			fieldResult.name = matcher.group("name");
			fieldResult.type = matcher.group("type");

			String modeText = matcher.group("rw");
			if (modeText != null && !modeText.isEmpty()) {
				if (!modeText.contains("R")) {
					fieldResult.writeOnly = true;
				} else if (!modeText.contains("W")) {
					fieldResult.readOnly = true;
				}
			}

			String optional = matcher.group("optional");
			if (optional != null && !optional.isEmpty()) {
				fieldResult.optional = true;
			}

			fieldResult.description = matcher.group("desc");

			return fieldResult;
		}
		return null;
	}

	/**
	 * Remove every new line, remove multispaces
	 *
	 * @param s the string to clean
	 * @return the cleaned string
	 */
	public static String replaceUntilOneLine(String s) {
		return s.replace("\n", "").replaceAll(" +", " ").trim();
	}

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

		// replace function types
		s = s.replaceAll("function\\((.*?)\\)", "fun($1)");

		// replace dictionary types
		String old;
		do {
			old = s;
			s = s.replaceAll("^(.*)(?:(?:dictionary)|(?:CustomDictionary)) (.*) → (.*?)( \\[[RW]{0,2}\\])?$", "$1table<$2, $3>$4");
		} while (!s.equals(old));

		// replace alternatives
		s = s.replaceAll(" or ", "|");
		return s;
	}


	// =========================
	// ===== Brief Listing =====
	// =========================

	/**
	 * Parse one single standard method
	 *
	 * @param element     The content of a single line
	 * @param description The description that is used (at the right of the table)
	 * @return the parsed method, with all its parameters
	 */
	public static Method parseBriefListingMethod(String element, String description) {
		Method luaMethod = new Method();

		FunctionResult functionResult = parseFunctionRegex(element);
		if (functionResult != null) {
			luaMethod.name = functionResult.name;
			luaMethod.returnType = functionResult._return;

			String params = functionResult.params;
			for (String param : params.split(", ")) {
				if (!param.isEmpty()) {
					MethodParameter parameter = new MethodParameter();
					parameter.name = param;
					if (param.contains(".")) {
						parameter.name = "param";
						parameter.type = param;
					}
					luaMethod.parameters.put(parameter.name, parameter);
				}
			}

			luaMethod.description = description;
			return luaMethod;
		}
		return null;
	}

	/**
	 * Parse one single Method, that only has a Table as parameter. Syntax for such function: `function{param = value}`.
	 * As side-effect a new class will be generated and put into the classList given.
	 *
	 * @param element        The content of the single line
	 * @param description    The description to use (at the right of the table)
	 * @param upperClassName The name of the class where this Method is part of
	 * @param classList      All the classes that are yet parsed and will be parsed (this will be used to store a new Class for
	 * @return the method, that has as only parameter the newly created class
	 */
	public static Method parseBriefListingMethodTableParam(String element, String description, String upperClassName, List<Class> classList) {
		// is method with table as only param
		Method method = new Method();
		method.paramTable = true;

		FunctionResult functionResult = parseFunctionRegex(element);
		if (functionResult != null) {
			method.name = functionResult.name;
			method.returnType = functionResult._return;

			String params = functionResult.params;
			params = params.replace("=…", "");

			// add new class for these parameters
			Class newClass = new Class();
			newClass.name = upperClassName + "_" + method.name;
			classList.add(newClass);

			for (String param : params.split(", ")) {
				Attribute attribute = new Attribute();
				attribute.name = param;
				newClass.attributes.put(attribute.name, attribute);
			}
			MethodParameter methodParameter = new MethodParameter();
			methodParameter.name = newClass.name + "_param";
			methodParameter.type = newClass.name;
			method.parameters.put(methodParameter.name, methodParameter);

			method.description = description;

			return method;
		}

		return null;
	}

	/**
	 * Parse one single Field/Attribute out of a brief-listing.
	 *
	 * @param fullElement This is the text of the element to parse
	 * @param description The description for this attribute (if no other is found)
	 * @return the fully parsed Attribute
	 */
	public static Attribute parseBriefListingField(String fullElement, String description) {
		fullElement = replaceTypes(fullElement);

		FieldResult fieldResult = parseFieldRegex(fullElement);
		if (fieldResult != null) {
			Attribute attribute = new Attribute();
			attribute.name = fieldResult.name;
			attribute.type = fieldResult.type;
			attribute.readOnly = fieldResult.readOnly;
			attribute.writeOnly = fieldResult.writeOnly;
			attribute.description = (fieldResult.description != null) ? fieldResult.description : description;

			return attribute;
		}

		return null;
	}

	/**
	 * Parse one briefListing table on the top of the page
	 *
	 * @param briefListingElement    The JSoup Element of the complete table (".brief-listing")
	 * @param overallDescriptionHtml The description of the class (is normally written over the brief-listing element
	 * @return a list of classes, that are represent for this brief-listing (overall class and subclasses for table-functions and table return values)
	 */
	public static List<Class> parseBriefListingElement(Element briefListingElement, String overallDescriptionHtml, String currentVersion, ParseOverviewResult lastResult) {
		// create return List
		List<Class> returnList = new ArrayList<>();

		// Define new Class, where everything is saved
		Class luaClass = new Class();

		// add class to returnList
		returnList.add(luaClass);

		// add overall description
		luaClass.description = overallDescriptionHtml;

		// parse Class Name
		Element className = briefListingElement.selectFirst(".type-name");
		luaClass.name = className.text();
		addSince(lastResult, currentVersion, luaClass);

		// parse parent class
		String briefListingText = briefListingElement.text();
		String[] split = briefListingText.split("-", 2);
		String classDefText = split[0];
		String[] classDefTextA = classDefText.split("extends ");
		if (classDefTextA.length > 1) {
			luaClass.parentClass = classDefTextA[1];
		}

		// read all members
		Elements members = briefListingElement.select(".brief-members > tbody > tr");
		for (Element member : members) {
			Element descriptionElement = member.selectFirst(".description");
			// skip member if it doesnt has a description subelement
			if (descriptionElement == null) {
				continue;
			}

			String description = descriptionElement.html().strip();

			// member name and description
			String elementName = member.selectFirst(".element-name").text();
			elementName = replaceTypes(elementName);

			if (elementName.contains("(")) {
				// is method
				Method method = parseBriefListingMethod(elementName, description);

				if (method != null) {
					// Add finished method to class-list
					luaClass.methods.put(method.name, method);

					addSince(luaClass.name, lastResult, currentVersion, method);
				}
			} else if (elementName.contains("{")) {
				// is method with single table as param
				Method method = parseBriefListingMethodTableParam(elementName, description, luaClass.name, returnList);

				if (method != null) {
					luaClass.methods.put(method.name, method);

					addSince(luaClass.name, lastResult, currentVersion, method);
				}
			} else {
				// is field
				String fullElement = member.selectFirst(".header").text();
				Attribute attribute = parseBriefListingField(fullElement, description);

				if (attribute != null) {
					luaClass.attributes.put(attribute.name, attribute);

					addSince(luaClass.name, lastResult, currentVersion, attribute);
				}
			}
		}

		return returnList;
	}

	/**
	 * copy the since field of the last result into the current attribute or use the currentVersion.
	 * <p>
	 * side-effects in attribute!
	 *
	 * @param luaClassName   the name of the class, this attribute belongs to
	 * @param lastResult     The result of the last-parsed-version
	 * @param currentVersion The current parsed version number
	 * @param attribute      fill since field of this attribute
	 */
	public static void addSince(String luaClassName, ParseOverviewResult lastResult, String currentVersion, Attribute attribute) {
		// add @since to attribute
		// add since (see if class existed before)
		if (lastResult != null) {
			Class lastClass = lastResult.classes.get(luaClassName);

			// this class already existed, copy its since
			if (lastClass != null) {
				// get old attribute
				Attribute lastAttribute = lastClass.attributes.get(attribute.name);
				if (lastAttribute != null) {
					attribute.since = lastAttribute.since;
					return;
				}
			}
		}
		attribute.since = currentVersion;
	}

	/**
	 * copy the since field of the last result into the current attribute or use the currentVersion.
	 * <p>
	 * side-effects in method!
	 *
	 * @param luaClassName   the name of the class, this attribute belongs to
	 * @param lastResult     The result of the last-parsed-version
	 * @param currentVersion The current parsed version number
	 * @param method         fill since field of this method
	 */
	public static void addSince(String luaClassName, ParseOverviewResult lastResult, String currentVersion, Method method) {
		// add @since to method
		// add since (see if class existed before)
		if (lastResult != null) {
			Class lastClass = lastResult.classes.get(luaClassName);

			// this class already existed, copy its since
			if (lastClass != null) {
				// get old attribute
				Method lastMethod = lastClass.methods.get(method.name);
				if (lastMethod != null) {
					method.since = lastMethod.since;
					return;
				}
			}
		}
		method.since = currentVersion;
	}

	public static void addSince(ParseOverviewResult lastResult, String currentVersion, Class luaClass) {
		// add since (see if class existed before)
		if (lastResult != null) {
			Class lastClass = lastResult.classes.get(luaClass.name);

			// this class already existed, copy its since
			if (lastClass != null) {
				luaClass.since = lastClass.since;
				return;
			}
		}
		luaClass.since = currentVersion;
	}

	// ==========================
	// ===== Single Element =====
	// ==========================

	/**
	 * Parse a single parameter out of the single elements.
	 * This parses only a single line with one parameter and NOT a whole list.
	 * <p>
	 * Side-effect in luaMethod!
	 *
	 * @param text      the text to parse
	 * @param luaMethod the method to update with the additional information
	 */
	public static void parseDetailsMethodParams(String text, Method luaMethod) {
		text = replaceUntilOneLine(text);
		text = replaceTypes(text);

		if (text.isEmpty()) {
			return;
		}

		FieldResult fieldResult = parseFieldRegex(text);
		if (fieldResult != null) {
			String paramName = fieldResult.name;
			String typeName = fieldResult.type;
			if (paramName.contains(".")) {
				typeName = paramName;
				paramName = "param";
			}
			MethodParameter parameter = luaMethod.parameters.get(paramName);
			parameter.type = typeName;

			String desc = fieldResult.description;
			if (desc != null) {
				parameter.description = desc.trim();
			}

			parameter.optional = fieldResult.optional;
		} else {
			// suggest, this is only the type, without name
			String type = text.split(" ")[0];
			MethodParameter methodParameter = luaMethod.parameters.get(type);

			// only do it, if parameter found
			if (methodParameter != null) {
				// rebuild it, so it has name and type separated
				methodParameter.type = type;
				methodParameter.name = "param";
				luaMethod.parameters.put(methodParameter.name, methodParameter);
				luaMethod.parameters.remove(type);
			}
		}
	}

	/**
	 * Parse a single parameter out of the single elements
	 * This parses only a single line with one parameter and NOT a whole list.
	 * This will add the parsed line into the attribute's additional class.
	 * <p>
	 * Side-effect in classes!
	 *
	 * @param element        The JSoup element to parse
	 * @param upperClassName The name of the class, that the method of this parameter belongs to
	 * @param luaMethod      The method, where this parameter belongs to.
	 * @param classes        The result classes Map, where every class is saved.
	 */
	public static void parseDetailsMethodParamWithClass(Element element, String upperClassName, Method luaMethod, Map<String, Class> classes) {
		String text = element.text();
		if (text.contains("::")) {
			text = replaceUntilOneLine(text);
			text = replaceTypes(text);

			// parse liText with regex
			FieldResult fieldResult = parseFieldRegex(text);

			if (fieldResult != null) {
				String desc = fieldResult.description;
				// if description contains "::", dont run further.
				// This is done, so metaelement doesnt get parsed.
				if (desc != null && desc.contains("::")) {
					return;
				}

				String paramName = fieldResult.name;
				String fullClassName = upperClassName + "_" + luaMethod.name;
				Class additionalClass = classes.get(fullClassName);
				Attribute attribute = additionalClass.attributes.get(paramName);
				if (attribute == null) {
					//create attribute if it doesnt exist yet
					attribute = new Attribute();
					attribute.name = paramName;
					additionalClass.attributes.put(paramName, attribute);
				}
				attribute.type = fieldResult.type;

				if (desc != null) {
					attribute.description = desc.trim();
				}

				attribute.optional = fieldResult.optional;

				// set string liberals from <code>
				Elements codeElements = element.select("code");
				for (Element code : codeElements) {
					String codeText = code.text();
					codeText = codeText.replace("\"", "");
					attribute.type += "|'\"" + codeText + "\"'";
				}
			}
		}
	}

	/**
	 * Parse the `detail` css-class element.
	 * This mostly has information about Parameters and ReturnTypes and their description
	 * <p>
	 * Side-effects in luaMethod!
	 *
	 * @param element        The element to parse
	 * @param luaMethod      The method to save the information to
	 * @param upperClassName The name of the class, that the method of this parameter belongs to
	 */
	public static void parseDetails(Element element, Method luaMethod, Attribute luaAttribute, String upperClassName, Map<String, Class> classes) {
		// parse header, see if we have to do things
		Element detailHeader = element.selectFirst(".detail-header");

		if (detailHeader != null && luaMethod != null) {
			String headerText = detailHeader.text();
			Element detailContent = element.selectFirst(".detail-content");

			if (headerText.equals("Parameters")) {
				// parse details with Parameter information
				if (luaMethod != null && !luaMethod.paramTable) {
					for (Element singleLine : detailContent.children()) {
						// parse param and add information to already defined once
						parseDetailsMethodParams(singleLine.text(), luaMethod);
					}
				} else if (luaMethod != null && luaMethod.paramTable) {
					Elements allLi = detailContent.select("li");
					for (Element li : allLi) {
						// parse param single param with class
						parseDetailsMethodParamWithClass(li, upperClassName, luaMethod, classes);
					}
				}
			} else if (headerText.equals("Return value")) {
				// parse details with return value description
				luaMethod.returnTypeDesc = replaceUntilOneLine(detailContent.html());
			}
		} else {
			// no header found, add it to description
			String html = element.html();
			html = replaceUntilOneLine(html);

			if (luaMethod != null) {
				luaMethod.description += "<p>" + html + "</p>";
			} else if (luaAttribute != null) {
				luaAttribute.description += "<p>" + html + "</p>";
			}
		}
	}

	/**
	 * Parse a field-list of a single-table-method.
	 * This will add all found information (and all optional fields) to the existing method-class.
	 * luaMethod OR luaAttribute can be null. If both are null, a NullPointerException is thrown.
	 * <p>
	 * classes has side-effects!
	 * luaMethod has side-effects!
	 * luaAttribute has side-effects!
	 *
	 * @param upperClassName The name of the class, that the method of this parameter belongs to
	 * @param luaMethod      The method to change
	 * @param luaAttribute   The attribute to change
	 * @param element        The element to parse
	 * @param classes        The upper classes Map, where all classes are stored.
	 */
	public static void parseFieldList(String upperClassName, Method luaMethod, Attribute luaAttribute, Element element, Map<String, Class> classes) {
		Class newClass = new Class();
		newClass.name = upperClassName + "_" + (luaMethod != null ? luaMethod.name : luaAttribute.name);
		parseSingleAttribute(newClass, element);

		if (luaMethod != null && luaMethod.returnType != null) {
			boolean isArray = luaMethod.returnType.contains("[]");
			luaMethod.returnType = newClass.name;
			if (isArray) {
				luaMethod.returnType += "[]";
			}
		} else if (luaAttribute != null && luaAttribute.type != null) {
			boolean isArray = luaAttribute.type.contains("[]");
			luaAttribute.type = newClass.name;
			if (isArray) {
				luaAttribute.type += "[]";
			}
		}

		classes.put(newClass.name, newClass);
	}

	/**
	 * Parse a `.element` on the lower part of the html page.
	 * All the information gathered will be saved into the classes map directly.
	 * <p>
	 * side-effect in classes!
	 *
	 * @param subElement The `.element` to parse
	 * @param classes    The global classes map, where all classes are saved
	 */
	public static void parseSingleElement(Element subElement, Map<String, Class> classes) {
		String name = subElement.id();
		String[] nameSplit = name.split("\\.");
		String className = nameSplit[0];
		name = nameSplit[1];

		Class luaClass = classes.get(className);
		Method luaMethod = luaClass.methods.get(name);
		Attribute luaAttribute = luaClass.attributes.get(name);

		boolean first = true;
		Element elementContent = subElement.selectFirst(".element-content");
		for (Element contentChild : elementContent.children()) {
			if (contentChild.hasClass("detail")) {
				parseDetails(contentChild, luaMethod, luaAttribute, className, classes);
			} else if (contentChild.hasClass("field-list")) {
				// override type with this new class
				parseFieldList(luaClass.name, luaMethod, luaAttribute, contentChild, classes);
			} else if (contentChild.hasClass("example")) {
				// TODO IGNORE EXAMPLES FOR NOW
			} else {
				// clear previous description if this is first override
				if (first) {
					if (luaMethod != null) {
						luaMethod.description = "";
					} else if (luaAttribute != null) {
						luaAttribute.description = "";
					}
					first = false;
				}

				// add content to description
				String html = replaceUntilOneLine(contentChild.html());
				if (!html.isEmpty()) {
					if (luaMethod != null) {
						luaMethod.description += "<p>" + html + "</p>";
					} else if (luaAttribute != null) {
						luaAttribute.description += "<p>" + html + "</p>";
					}
				}
			}
		}
	}

	public static void parseConceptElement(Element element, Map<String, Class> classes) {
		String name = element.id();
		Class luaClass = classes.get(name);

		boolean first = true;
		Element elementContent = element.selectFirst(".element-content");
		for (Element contentChild : elementContent.children()) {
			if (contentChild.hasClass("field-list")) {
				parseSingleAttribute(luaClass, contentChild);
			} else if (contentChild.hasClass("example")) {
				// TODO IGNORE EXAMPLES FOR NOW
			} else if (contentChild.hasClass("element")) {
				parseSingleElement(contentChild, classes);
			} else {
				// add to description of class
				String text = contentChild.html();
				if (text != null && !text.isEmpty()) {
					if (first) {
						luaClass.description = "";
						first = false;
					} else {
						luaClass.description += "\n";
					}
					luaClass.description += replaceUntilOneLine(text);
				}
			}
		}
	}

	/**
	 * parses a brief-listing of attributes used e.g. as list of parameters of methods
	 * <p>
	 * side-effects in luaClass!
	 *
	 * @param luaClass     the class to append the attributes to
	 * @param contentChild the element that contains all the attributes
	 */
	private static void parseSingleAttribute(Class luaClass, Element contentChild) {
		for (Element param : contentChild.children()) {
			String paramText = param.text();
			paramText = replaceTypes(paramText);

			FieldResult fieldResult = parseFieldRegex(paramText);

			if (fieldResult != null) {
				Attribute attribute = new Attribute();
				attribute.name = fieldResult.name;
				attribute.type = fieldResult.type;
				attribute.description = fieldResult.description;
				attribute.writeOnly = fieldResult.writeOnly;
				attribute.readOnly = fieldResult.readOnly;

				luaClass.attributes.put(attribute.name, attribute);
			}
		}
	}

	// ==========================
	// ===== Parser Entries =====
	// ==========================

	/**
	 * Load the file and parse it
	 *
	 * @param fileName The fileName of the html file to parse
	 * @return a map of all the parsed classes
	 */
	public static Map<String, Class> parseClassPageFromFile(String fileName) {
		// Open class page
		Document page;
		try {
			File file = new File(fileName);
			page = Jsoup.parse(file, "utf-8");
			page.outputSettings(new Document.OutputSettings().prettyPrint(false));
		} catch (IOException e) {
			System.out.println("error opening the class API page");
			System.out.println(e.getMessage());
			return null;
		}
		return parseClassPage(page, null, null, false);
	}

	/**
	 * Download the page from the link and then parse it.
	 *
	 * @param link           The link to the page
	 * @param currentVersion the current parsed version number
	 * @param lastResult     the result of the previous parsed version
	 * @param concepts       different parsing for concepts page
	 * @return a map of all the parsed classes
	 */
	public static Map<String, Class> parseClassPageFromDownload(String link, String currentVersion, ParseOverviewResult lastResult, boolean concepts) {
		// Download class page
		try {
			Document page = Jsoup.connect(link).get();
			page.outputSettings(new Document.OutputSettings().prettyPrint(false));
			return parseClassPage(page, currentVersion, lastResult, concepts);
		} catch (IOException e) {
			System.out.println("error downloading the class API page");
			System.out.println(e.getMessage());
		}

		return null;
	}

	/**
	 * Parse the JSoup document of a single class (will also parse multiple classes, if they are on that page)
	 *
	 * @param page           The JSoup document to parse
	 * @param currentVersion the current parsed version number
	 * @param lastResult     the result of the previous parsed version
	 * @param concepts       different parsings for concepts page
	 * @return a map of all the parsed classes
	 */
	public static Map<String, Class> parseClassPage(Document page, String currentVersion, ParseOverviewResult lastResult, boolean concepts) {
		// create returned class-list
		Map<String, Class> classes = new HashMap<>();

		// parse class description
		Element descElement = page.selectFirst(".brief-description");
		String overallDescriptionHtml = "";
		if (descElement != null) {
			overallDescriptionHtml = replaceUntilOneLine(descElement.html());
		}

		Element briefListingOuter = page.selectFirst(".brief-listing");
		Elements briefListingElements = briefListingOuter.children();

		// Parse the "upper" part, This has the basic information about each function
		for (Element briefListingElement : briefListingElements) {
			if (briefListingElement.hasClass("brief-listing")) {
				List<Class> classList = parseBriefListingElement(briefListingElement, overallDescriptionHtml, currentVersion, lastResult);
				for (Class c : classList) {
					classes.put(c.name, c);
				}
			} else if (briefListingElement.hasClass("brief-listing-empty")) {
				String className = briefListingElement.selectFirst(".type-name").text();
				Class luaClass = new Class();
				luaClass.name = className;
				addSince(lastResult, currentVersion, luaClass);
				classes.put(className, luaClass);
			}
		}

		// Parse the lower part of the page (elements have more information about each function)
		Elements elements = page.select("body > .element");
		for (Element element : elements) {
			if (concepts) {
				parseConceptElement(element, classes);
			} else {
				Elements subElements = element.children();
				for (Element subElement : subElements) {
					if (!subElement.hasClass("element")) {
						continue;
					}

					parseSingleElement(subElement, classes);
				}
			}
		}

		return classes;
	}

	public static class ParseOverviewResult {
		Map<String, Class> classes;
		List<Attribute> globals;
		Defines defines;

		public ParseOverviewResult() {
			classes = new HashMap<>();
			globals = new ArrayList<>();
		}
	}

	/**
	 * Download and parse the overview page of a single version.
	 * This will start the parsing of everything, that is part of the api.
	 *
	 * @param link           the link to the overview page to download
	 * @param currentVersion the name of the version currently parsed
	 * @param lastResult     the result of the last parsed version
	 * @return result of this parsed version
	 */
	public static ParseOverviewResult parseOverviewPageFromDownload(String link, String currentVersion, ParseOverviewResult lastResult) {
		// Download overview page
		Document page;
		try {
			page = Jsoup.connect(link).get();
			page.outputSettings(new Document.OutputSettings().prettyPrint(false));
		} catch (IOException e) {
			System.out.println("error downloading the class API page");
			System.out.println(e.getMessage());
			return null;
		}

		// create result object
		ParseOverviewResult result = new ParseOverviewResult();

		// find first element with .brief-listing, its the list of classes
		Element briefListing = page.selectFirst(".brief-listing");
		Element tbody = briefListing.selectFirst("tbody");
		Elements children = tbody.children();
		for (int i = 0; i < children.size(); i++) {
			Element tr = children.get(i);
			Element trLink = tr.selectFirst(".header > a");
			String classPageLink = trLink.attr("href");
			Map<String, Class> parsedClasses = parseClassPageFromDownload(link + classPageLink, currentVersion, lastResult, false);
			result.classes.putAll(parsedClasses);
		}

		// parse globals
		Element globalsList = page.selectFirst(".field-list");
		for (Element global : globalsList.children()) {
			Attribute attribute = parseBriefListingField(global.text(), null);
			result.globals.add(attribute);
		}

		// parse defines
		result.defines = parseDefines(link);
		parseEvents(link, result.defines);

		// parse Concepts
		Map<String, Class> conceptsResult = parseClassPageFromDownload(link + "Concepts.html", currentVersion, lastResult, true);
		result.classes.putAll(conceptsResult);

		return result;
	}

	// =============================
	// ===== Additional Parser =====
	// =============================

	/**
	 * Download and parse the defines page.
	 *
	 * @param baseLink The link to a versions overview page
	 * @return all the parsed defines available in the downloaded version
	 */
	public static Defines parseDefines(String baseLink) {
		Document definesPage;
		try {
			definesPage = Jsoup.connect(baseLink + "defines.html").get();
		} catch (IOException e) {
			System.out.println("error downloading the defines page");
			System.out.println(e.getMessage());
			return null;
		}

		Defines defines = new Defines();
		Elements allDefines = definesPage.select(".element");
		for (Element define : allDefines) {
			String valueId = define.id();
			if (valueId.contains("events")) {
				continue;
			}
			if (define.is("tr")) {
				// is value
				String desc = define.selectFirst(".description").html();
				defines.values.add(new Define(valueId, desc));
			} else {
				// is class
				defines.classes.add(valueId);
			}
		}

		return defines;
	}

	/**
	 * Download and parse the events page. The events page has more information about the event based defines.
	 * <p>
	 * side-effects in defines!
	 *
	 * @param baseLink the link to a versions overview page
	 * @param defines  the previously parsed defines, that will be updated
	 */
	public static void parseEvents(String baseLink, Defines defines) {
		// parse event page here too
		Document eventsPage;
		String eventsLink = baseLink + "events.html";

		try {
			eventsPage = Jsoup.connect(eventsLink).get();
		} catch (IOException e) {
			System.out.println("error downloading the events page");
			e.printStackTrace();
			return;
		}

		for (Element event : eventsPage.select(".element")) {
			if (event.id().equals("All events")) {
				continue;
			}

			String eventName = event.id();
			if (!eventName.isEmpty()) {
				eventName = "defines.events." + eventName;
				String desc = event.selectFirst(".element-content").html();
				desc = replaceUntilOneLine(desc);
				defines.values.add(new Define(eventName, desc));
			}
		}
		defines.classes.add("defines.events");
	}

	/**
	 * Download and parse the page with all the versions on it.
	 * This will start a parse of all the found versions on the page.
	 *
	 * @return A map with the version as key and the parse-results of that version as value
	 */
	public static Map<String, ParseOverviewResult> parseVersionList() {
		// Download version list page
		Document versionsPage;
		try {
			versionsPage = Jsoup.connect("https://lua-api.factorio.com/").get();
		} catch (IOException e) {
			System.out.println("error downloading the versionsList page");
			e.printStackTrace();
			return null;
		}

		Map<String, ParseOverviewResult> result = new HashMap<>();

		Elements allLinks = versionsPage.select("a");
		String lastVersion = "";
		System.out.println("Parse all versions:");
		for (int i = allLinks.size() - 1, j = 1; i >= 0; i--, j++) {
			Element link = allLinks.get(i);
			String versionName = link.text();
			Main.printCurrentProgress(j, allLinks.size(), versionName, false);
			if (!versionName.equals("Latest version") && !versionName.equals("0.12.35")) {
				String href = link.attr("href");
				ParseOverviewResult overviewResult = parseOverviewPageFromDownload("https://lua-api.factorio.com" + href, versionName, result.get(lastVersion));
				result.put(versionName, overviewResult);
			}

			if (j != 1) {
				lastVersion = versionName;
			}
		}
		Main.printCurrentProgress(allLinks.size(), allLinks.size(), "Done!", true);

		return result;
	}
}
