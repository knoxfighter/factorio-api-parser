package moe.knox.factorio_api_parser;


import moe.knox.factorio_api_parser.lua_types.Attribute;
import moe.knox.factorio_api_parser.lua_types.Class;
import moe.knox.factorio_api_parser.lua_types.Method;
import moe.knox.factorio_api_parser.lua_types.MethodParameter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LuaApiParserTest {

	@Test
	void parseClass() {
		String classTestFile = getClass().getClassLoader().getResource("ClassTest.html").getFile();
		Map<String, Class> actual = LuaApiParser.parseClass(classTestFile);
		Map<String, Class> expected = new HashMap<>() {
			{
				put("LuaControl", new Class() {
					{
						name = "LuaControl";
						description = "<p> This is an abstract base class containing the common functionality between <a href=\"LuaPlayer.html\">LuaPlayer</a> and entities (see <a href=\"LuaEntity.html\">LuaEntity</a>). When accessing player-related functions through a <a href=\"LuaEntity.html\">LuaEntity</a>, it must refer to a character entity.</p>";
						methods = new HashMap<>() {
							{
								put("get_inventory", new Method() {
									{
										name = "get_inventory";
										returnType = "LuaInventory";
										description = "<p>Get an inventory belonging to this entity. This can be either the \"main\" inventory or some auxiliary one, like the module slots or logistic trash slots.</p><p><div class=\"note\"><strong>Note: </strong> A given <a href=\"defines.html#defines.inventory\">defines.inventory</a> is only meaningful for the corresponding LuaObject type. EG: get_inventory(defines.inventory.character_main) is only meaningful if 'this' is a player character. You may get a value back but if the type of 'this' isn't the type referred to by the <a href=\"defines.html#defines.inventory\">defines.inventory</a> it's almost guaranteed to not be the inventory asked for.</div></p>";
										parameters = new HashMap<>() {
											{
												put("inventory", new MethodParameter() {
													{
														name = "inventory";
														type = "defines.inventory";
													}
												});
											}
										};
									}
								});

								put("get_main_inventory", new Method() {
									{
										name = "get_main_inventory";
										returnType = "LuaInventory";
										description = "<p>Gets the main inventory for this character or player if this is a character or player.</p>";
									}
								});

								put("set_gui_arrow", new Method() {
									{
										name = "set_gui_arrow";
										description = "<p>Create an arrow which points at this entity. This is used in the tutorial. For examples, see <code>control.lua</code> in the campaign missions.</p>";
										parameters = new HashMap<>() {
											{
												put("LuaControl_set_gui_arrow_param", new MethodParameter() {
													{
														name = "LuaControl_set_gui_arrow_param";
														type = "LuaControl_set_gui_arrow";
													}
												});
											}
										};
									}
								});

								put("clear_gui_arrow", new Method() {
									{
										name = "clear_gui_arrow";
										description = "<p>Removes the arrow created by <code>set_gui_arrow</code>.</p>";
									}
								});

								put("teleport", new Method() {
									{
										name = "teleport";
										returnType = "boolean";
										description = "<p>Teleport the entity to a given position, possibly on another surface.</p><p><div class=\"note\"><strong>Note: </strong> Some entities may not be teleported. For instance, transport belts won't allow teleportation and this method will always return <code>false</code> when used on any such entity.</div><div class=\"note\"><strong>Note: </strong> You can also pass 1 or 2 numbers as the parameters and they will be used as relative teleport coordinates <code>'teleport(0, 1)'</code> to move the entity 1 tile positive y. <code>'teleport(4)'</code> to move the entity 4 tiles to the positive x.</div></p>";
										parameters = new HashMap<>() {
											{
												put("position", new MethodParameter() {
													{
														name = "position";
														type = "Position";
														description = "Where to teleport to.";
													}
												});

												put("surface", new MethodParameter() {
													{
														name = "surface";
														type = "SurfaceSpecification";
														optional = true;
														description = "Surface to teleport to. If not given, will teleport to the entity's current surface. Only players and cars can be teleported cross-surface.";
													}
												});
											}
										};
									}
								});

								put("update_selected_entity", new Method() {
									{
										name = "update_selected_entity";
										description = "<p>Select an entity, as if by hovering the mouse above it.</p>";
										parameters = new HashMap<>() {
											{
												put("position", new MethodParameter() {
													{
														name = "position";
														type = "Position";
														description = "Position of the entity to select";
													}
												});
											}
										};
									}
								});

								put("begin_crafting", new Method() {
									{
										name = "begin_crafting";
										returnType = "uint";
										description = "<p>Begins crafting the given count of the given recipe</p>";
										parameters = new HashMap<>() {
											{
												put("LuaControl_begin_crafting_param", new MethodParameter() {
													{
														name = "LuaControl_begin_crafting_param";
														type = "LuaControl_begin_crafting";
													}
												});
											}
										};
									}
								});

								put("is_player", new Method() {
									{
										name = "is_player";
										returnType = "boolean";
										description = "<p>When <code>true</code> control adapter is a LuaPlayer object, <code>false</code> for entities including characters with players</p>";
									}
								});
							}
						};

						attributes = new HashMap<>() {
							{
								put("surface", new Attribute() {
									{
										name = "surface";
										type = "LuaSurface";
										description = "<p>The surface this entity is currently on.</p>";
										readOnly = true;
									}
								});

								put("force", new Attribute() {
									{
										name = "force";
										type = "ForceSpecification";
										description = "<p>The force of this entity. Reading will always give a <a href=\"LuaForce.html\">LuaForce</a>, but it is possible to assign either <a href=\"Builtin-Types.html#string\">string</a> or <a href=\"LuaForce.html\">LuaForce</a> to this attribute to change the force.</p>";
									}
								});

								put("opened", new Attribute() {
									{
										name = "opened";
										type = "LuaEntity|LuaItemStack|LuaEquipment|LuaEquipmentGrid|LuaPlayer|LuaGuiElement|defines.gui_type";
										description = "<p>The GUI target the player currently has open; <code>nil</code> if none.</p><p><div class=\"note\"><strong>Note: </strong> Write supports any of the types. Read will return the entity, equipment, element or nil.</div></p>";
									}
								});

								put("walking_state", new Attribute() {
									{
										name = "walking_state";
										type = "LuaControl_walking_state";
										description = "<p>Current walking state.</p><p>It is a table with two fields:</p>";
									}
								});

								put("crafting_queue", new Attribute() {
									{
										name = "crafting_queue";
										type = "LuaControl_crafting_queue[]";
										description = "<p>Gets the current crafting queue items.</p><p>Each CraftingQueueItem is a table:</p>";
										readOnly = true;
									}
								});

								put("auto_trash_filters", new Attribute() {
									{
										name = "auto_trash_filters";
										type = "table<string, uint>";
										description = "<p>The auto-trash filters. The keys are item prototype names, the values are the slot values.</p><p><div class=\"note\"><strong>Note: </strong> When called on a <a href=\"LuaPlayer.html\">LuaPlayer</a>, it must be associated with a character (see <a href=\"LuaPlayer.html#LuaPlayer.character\">LuaPlayer::character</a>).</div></p>";
									}
								});

								put("opened_gui_type", new Attribute() {
									{
										name = "opened_gui_type";
										type = "defines.gui_type";
										description = "<p>Returns the <a href=\"defines.html#defines.gui_type\">defines.gui_type</a> or <code>nil</code>.</p>";
										readOnly = true;
									}
								});
							}
						};
					}
				});
				put("LuaControl_crafting_queue", new Class() {
					{
						name = "LuaControl_crafting_queue";
						attributes = new HashMap<>() {
							{
								put("index", new Attribute() {
									{
										name = "index";
										type = "uint";
										description = "The crafting queue index";
									}
								});

								put("recipe", new Attribute() {
									{
										name = "recipe";
										type = "string";
										description = "The recipe.";
									}
								});

								put("count", new Attribute() {
									{
										name = "count";
										type = "uint";
										description = "The count being crafted.";
									}
								});
							}
						};
					}
				});
				put("LuaControl_walking_state", new Class() {
					{
						name = "LuaControl_walking_state";
						attributes = new HashMap<>() {
							{
								put("walking", new Attribute() {
									{
										name = "walking";
										type = "boolean";
										description = "If false, the player is currently not walking; otherwise it's going somewhere";
									}
								});

								put("direction", new Attribute() {
									{
										name = "direction";
										type = "defines.direction";
										description = "Direction where the player is walking";
									}
								});
							}
						};
					}
				});
				put("LuaControl_set_gui_arrow", new Class() {
					{
						name = "LuaControl_set_gui_arrow";
						attributes = new HashMap<>() {
							{
								put("type", new Attribute() {
									{
										name = "type";
										type = "string|'\"nowhere\"'|'\"goal\"'|'\"entity_info\"'|'\"active_window\"'|'\"entity\"'|'\"position\"'|'\"crafting_queue\"'|'\"item_stack\"'";
										description = "Where to point to. This field determines what other fields are mandatory. May be \"nowhere\", \"goal\", \"entity_info\", \"active_window\", \"entity\", \"position\", \"crafting_queue\",|\"item_stack\".";
									}
								});
								put("entity", new Attribute() {
									{
										name = "entity";
										type = "LuaEntity";
									}
								});
								put("position", new Attribute() {
									{
										name = "position";
										type = "Position";
									}
								});
								put("crafting_queueindex", new Attribute() {
									{
										name = "crafting_queueindex";
										type = "uint";
									}
								});
								put("inventory_index", new Attribute() {
									{
										name = "inventory_index";
										type = "defines.inventory";
									}
								});
								put("item_stack_index", new Attribute() {
									{
										name = "item_stack_index";
										type = "uint";
									}
								});
								put("source", new Attribute() {
									{
										name = "source";
										type = "string|'\"player\"'|'\"target\"'";
										description = "May be either \"player\"|\"target\".";
									}
								});
							}
						};
					}
				});
				put("LuaControl_begin_crafting", new Class() {
					{
						name = "LuaControl_begin_crafting";
						attributes = new HashMap<>() {
							{
								put("count", new Attribute() {
									{
										name = "count";
										type = "uint";
										description = "The count to craft.";
									}
								});
								put("recipe", new Attribute() {
									{
										name = "recipe";
										type = "string|LuaRecipe";
										description = "The recipe to craft.";
									}
								});
								put("silent", new Attribute() {
									{
										name = "silent";
										type = "boolean";
										optional = true;
										description = "If false and the recipe can't be crafted the requested number of times printing the failure is skipped.";
									}
								});
							}
						};
					}
				});
			}
		};
		assertEquals(expected, actual);
	}
}
