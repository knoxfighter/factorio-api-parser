---This is an abstract base class containing the common functionality between LuaPlayer and entities (see LuaEntity). When accessing player-related functions through a LuaEntity, it must refer to a character entity.
---@class LuaControl
local LuaControl = {}

---Get an inventory belonging to this entity. This can be either the "main" inventory or some auxiliary one, like the module slots or logistic trash slots.
---
---Note: A given defines.inventory is only meaningful for the corresponding LuaObject type. EG: get_inventory(defines.inventory.character_main) is only meaningful if 'this' is a player character. You may get a value back but if the type of 'this' isn't the type referred to by the defines.inventory it's almost guaranteed to not be the inventory asked for.
---
---@param inventory defines.inventory
---@return LuaInventory or nil if this entity doesn't have an inventory with the given index.
function LuaControl.get_inventory(inventory) end

---Gets the main inventory for this character or player if this is a character or player.
---@return LuaInventory or nil if this entity is not a character or player.
function LuaControl.get_main_inventory() end

--- [...]

---@class LuaControl_set_gui_arrow_param
local LuaControl_set_gui_arrow_param = {}

---Where to point to. This field determines what other fields are mandatory.
---@type string|'"nowhere"'|'"goal"'|'"entity_info"'|'"active_window"'|'"entity"'|'"position"'|'"crafting_queue"'|'"item_stack"'
LuaControl_set_gui_arrow_param.type = nil

---@type LuaEntity
LuaControl_set_gui_arrow_param.entity = nil

---@type Position
LuaControl_set_gui_arrow_param.position = nil

---@type uint
LuaControl_set_gui_arrow_param.crafting_queueindex = nil

---@type defines.inventory
LuaControl_set_gui_arrow_param.inventory_index = nil

---@type uint
LuaControl_set_gui_arrow_param.item_stack_index = nil

---@type string|'"player"'|'"target"'
LuaControl_set_gui_arrow_param.source = nil

---Create an arrow which points at this entity. This is used in the tutorial. For examples, see control.lua in the campaign missions.
---@param param LuaControl_set_gui_arrow_param
function LuaControl.set_gui_arrow(param) end
