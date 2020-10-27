package moe.knox.prototype.types;

import com.google.gson.annotations.SerializedName;

public class Property {
	public String name;
	String type;
	String description;
	@SerializedName("default")
	public String _default;
	boolean optional;
}
