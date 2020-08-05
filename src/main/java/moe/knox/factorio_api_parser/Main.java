package moe.knox.factorio_api_parser;

import moe.knox.factorio_api_parser.lua_types.Class;

import java.util.Map;

public class Main {
	public static void main(String[] args) {
		// Download and parse a single page
		Map<String, Class> stringClassMap = LuaApiParser.parseClassFromDownload("https://lua-api.factorio.com/latest/LuaEntity.html");
		System.out.println(stringClassMap);
	}
}
