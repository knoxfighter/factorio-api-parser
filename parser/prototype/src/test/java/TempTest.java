import com.google.gson.GsonBuilder;
import moe.knox.prototype.types.*;
import org.junit.jupiter.api.Test;

import java.io.*;

public class TempTest {

	@Test
	void prototypeTest() {
		JsonRoot prototypes;
		// read prototypes from jsom file
		try (FileReader prototypeJsonRead = new FileReader("../prototypes.json")) {
			prototypes = new GsonBuilder()
					.create()
					.fromJson(prototypeJsonRead, JsonRoot.class);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}



//		try (Writer writer = new FileWriter("../prototypes.json")) {
//			new GsonBuilder()
//					.setPrettyPrinting()
//					.disableHtmlEscaping()
//					.create()
//					.toJson(prototypes, writer);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		// save json again
		try (Writer writer = new FileWriter("../prototypes_small.json")) {
			new GsonBuilder()
//					.setPrettyPrinting()
					.disableHtmlEscaping()
					.create()
					.toJson(prototypes, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	String replaceRegex = "(?!nil)(?!boolean)(?!number)(?!table)(?!any)(?!string)(\\b\\w+\\b)";
//
//	for (Table table : prototypes.tables) {
//		if (table.parent != null) {
//			table.parent = table.parent.replaceAll("(\\w+)", "PT_$1");
//		}
//
//		if (table.prototype != null) {
//			if (table.prototype.isEmpty()) {
//				table.prototype = null;
//			} else {
//				table.prototype = "PT_" + table.prototype;
//			}
//		}
//
//		table.name = "PT_" + table.name;
//
//		for (Property property : table.properties) {
//			property.type = property.type.replaceAll(replaceRegex, "PT_$1");
//		}
//	}
//
//		for (Alias alias : prototypes.aliases) {
//		alias.name = "PT_" + alias.name;
//
//		alias.other = alias.other.replaceAll(replaceRegex, "PT_$1");
//	}
//
//		for (StringType string : prototypes.strings) {
//		string.name = string.name.replaceAll(replaceRegex, "PT_$1");
//	}
//
//		for (StringType prototype : prototypes.prototypes) {
//		prototype.name = "PT_" + prototype.name;
//		prototype.value.replaceAll((key, value) -> value.replaceAll(replaceRegex, "PT_$1"));
//	}
}
