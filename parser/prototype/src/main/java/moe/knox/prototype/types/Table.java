package moe.knox.prototype.types;

import java.util.ArrayList;
import java.util.List;

public class Table extends PrototypeParent {
	public List<Property> properties = new ArrayList<>();
	public String parent;
	public String prototype;
}
