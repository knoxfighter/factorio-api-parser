import com.google.gson.GsonBuilder;
import moe.knox.prototype.types.JsonRoot;
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
}
