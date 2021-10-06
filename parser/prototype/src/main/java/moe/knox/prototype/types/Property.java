package moe.knox.prototype.types;

import com.google.gson.annotations.SerializedName;

public class Property {
	public String name;
	public String description;
	public String type;
	@SerializedName("default")
	public String _default;
	public boolean optional;
}
