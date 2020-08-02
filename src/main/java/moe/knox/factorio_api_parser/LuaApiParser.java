package moe.knox.factorio_api_parser;

import moe.knox.factorio_api_parser.lua_types.Attribute;
import moe.knox.factorio_api_parser.lua_types.Class;
import moe.knox.factorio_api_parser.lua_types.Method;
import moe.knox.factorio_api_parser.lua_types.MethodParameter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LuaApiParser {
	public static String factorioApiBaseLink = "https://lua-api.factorio.com/latest/index.html";
	//language=RegExp
	public static String typePattern = "[\\w\\[\\].|]*(?:<[\\w\\[\\]<>.,| ]*>)?[\\w\\[\\]>]*";
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
					"^(?<name>\\w*) :: (?<type>%s)(?: (?<optional>\\(optional\\)))?:?(?: (?<desc>[^\\[\\n]*)?(?:\\[(?<rw>[RW]{0,2})\\])?)?()",
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

	public static String replaceUntilOneLine(String s) {
		return s.replace("\n", "").replaceAll(" +", " ").trim();
	}

	public static String replaceTypes(String s) {
		// replace array types
		s = s.replaceAll("array of (\\w*)", "$1[]");

		// replace dictionary types
		String old;
		do {
			old = s;
			s = s.replaceAll("^(.*)dictionary (.*) → (.*?)( \\[[RW]{0,2}\\])?$", "$1table<$2, $3>$4");
		} while (!s.equals(old));

		// replace alternatives
		s = s.replaceAll(" or ", "|");
		return s;
	}

	public static Map<String, Class> parseClass(String fileLink) {
		// Download class page
		Document page;
		try {
//            page = Jsoup.connect(fileLink).get();
			File file = new File(fileLink);
			page = Jsoup.parse(file, "utf-8");
			page.outputSettings(new Document.OutputSettings().prettyPrint(false));
			page.select("li").before("\\n");
		} catch (Exception e) {
			System.out.println("error downloading the class API page");
			System.out.println(e.getMessage());
			return null;
		}

		// create returned class-list
		Map<String, Class> classes = new HashMap<>();

		// parse class description
		Element descElement = page.selectFirst(".brief-description");
		String overallDescriptionHtml = replaceUntilOneLine(descElement.html());

		Element briefListingOuter = page.selectFirst(".brief-listing");
		Elements briefListingElements = briefListingOuter.children();

		// Parse the "upper" part, This has the basic information about each function
		for (Element briefListingElement : briefListingElements) {
			if (!briefListingElement.hasClass("brief-listing")) {
				continue;
			}

			// Define new Class, where everything is saved
			Class luaClass = new Class();

			// add overall description
			luaClass.description = overallDescriptionHtml;

			// parse Class Name
			Element className = briefListingElement.selectFirst(".type-name");
			luaClass.name = className.text();

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
				String description = member.selectFirst(".description").html().strip();

				// member name and description
				String elementName = member.selectFirst(".element-name").text();
				elementName = elementName.replaceAll("array of (\\w*)", "$1[]");

				if (elementName.contains("(")) {
					// is method
					Method luaMethod = new Method();

					FunctionResult functionResult = parseFunctionRegex(elementName);
					if (functionResult != null) {
						luaMethod.name = functionResult.name;
						luaMethod.returnType = functionResult._return;

						String params = functionResult.params;
						for (String param : params.split(", ")) {
							if (!param.isEmpty()) {
								MethodParameter parameter = new MethodParameter();
								parameter.name = param;
								luaMethod.parameters.put(parameter.name, parameter);
							}
						}

						luaMethod.description = description;

						// Add finished method to class-list
						luaClass.methods.put(luaMethod.name, luaMethod);
					}
				} else if (elementName.contains("{")) {
					// is method with table as only param
					Method method = new Method();
					method.paramTable = true;

					FunctionResult functionResult = parseFunctionRegex(elementName);
					if (functionResult != null) {
						method.name = functionResult.name;
						method.returnType = functionResult._return;

						String params = functionResult.params;
						params = params.replace("=…", "");

						// add new class for these parameters
						Class newClass = new Class();
						newClass.name = luaClass.name + "_" + method.name;
						classes.put(newClass.name, newClass);

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

						luaClass.methods.put(method.name, method);
					}
				} else {
					// is field
					String fullElement = member.selectFirst(".header").text();
					fullElement = replaceTypes(fullElement);

					FieldResult fieldResult = parseFieldRegex(fullElement);
					if (fieldResult != null) {
						Attribute attribute = new Attribute();
						attribute.name = fieldResult.name;
						attribute.type = fieldResult.type;
						attribute.readOnly = fieldResult.readOnly;
						attribute.writeOnly = fieldResult.writeOnly;
						attribute.description = (fieldResult.description != null) ? fieldResult.description : description;

						luaClass.attributes.put(attribute.name, attribute);
					}
				}
			}
			classes.put(luaClass.name, luaClass);
		}

		// Parse the lower part of the page (elements have more information about each function)
		Elements elements = page.select("body > .element");
		for (Element element : elements) {
			Elements subElements = element.children();
			for (Element subElement : subElements) {
				if (!subElement.hasClass("element")) {
					continue;
				}

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
						// parse header, see if we have to do things
						Element detailHeader = contentChild.selectFirst(".detail-header");

						String headerText = detailHeader.text();
						if (headerText.equals("Parameters")) {
							// parse details
							Element detailContent = contentChild.selectFirst(".detail-content");

							if (luaMethod != null && !luaMethod.paramTable) {
								for (Element singleLine : detailContent.children()) {
									// parse param and add information to already defined once
									String singleLineText = singleLine.text();
									singleLineText = replaceUntilOneLine(singleLineText);
									singleLineText = replaceTypes(singleLineText);

									FieldResult fieldResult = parseFieldRegex(singleLineText);
									if (fieldResult != null) {
										String paramName = fieldResult.name;
										MethodParameter parameter = luaMethod.parameters.get(paramName);
										parameter.type = fieldResult.type;

										String desc = fieldResult.description;
										if (desc != null) {
											parameter.description = desc.trim();
										}

										parameter.optional = fieldResult.optional;
									}
								}
							} else if (luaMethod != null && luaMethod.paramTable) {
								Elements allLi = detailContent.select("li");
								for (Element li : allLi) {
									String liText = li.text();
									if (liText.contains("::")) {
										liText = replaceUntilOneLine(liText);
										liText = replaceTypes(liText);

										// parse liText with regex
										FieldResult fieldResult = parseFieldRegex(liText);

										if (fieldResult != null) {
											String paramName = fieldResult.name;
											String fullClassName = luaClass.name + "_" + luaMethod.name;
											Class additionalClass = classes.get(fullClassName);
											Attribute attribute = additionalClass.attributes.get(paramName);
											if (attribute == null) {
												//create attribute if it doesnt exist yet
												attribute = new Attribute();
												attribute.name = paramName;
												additionalClass.attributes.put(paramName, attribute);
											}
											attribute.type = fieldResult.type;

											String desc = fieldResult.description;
											if (desc != null) {
												attribute.description = desc.trim();
											}

											attribute.optional = fieldResult.optional;

											// set string liberals from <code>
											Elements codeElements = li.select("code");
											for (Element code : codeElements) {
												String codeText = code.text();
												codeText = codeText.replace("\"", "");
												attribute.type += "|'\"" + codeText + "\"'";
											}
										}
									}
								}
							}
						}
					} else if (contentChild.hasClass("field-list")) {
						// override type with this new class
						Class newClass = new Class();
						newClass.name = luaClass.name + "_" + (luaMethod != null ? luaMethod.name : luaAttribute.name);
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

								newClass.attributes.put(attribute.name, attribute);
							}
						}

						boolean isArray = false;
						if (luaMethod != null) {
							isArray = luaMethod.returnType.contains("[]");
							luaMethod.returnType = newClass.name;
						} else if (luaAttribute != null) {
							isArray = luaAttribute.type.contains("[]");
							luaAttribute.type = newClass.name;
						}
						if (isArray) {
							luaAttribute.type += "[]";
						}

						classes.put(newClass.name, newClass);
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
		}

		return classes;
	}
}
