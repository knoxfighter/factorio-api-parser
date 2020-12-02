package moe.knox.prototype.types;

import com.google.gson.annotations.SerializedName;

public class Property {
	public String name;
	public String type;
	public String description;
	@SerializedName("default")
	public String _default;
	public boolean optional;
}
