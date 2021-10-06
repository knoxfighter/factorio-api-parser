# Factorio API as JSON files.

The goal of this project is to provide factorio prototypes, in a computer readable way, in this case as JSON.  
The lua-api now has a json format, so it is not generated here anymore.   
If you want any other format, create an Issue or Pull Request.

## LUA-API
Not needed anymore, factorio is providing their own json with all needed information.  
Documentation: https://lua-api.factorio.com/latest/json-docs.html  
Direct-Link: https://lua-api.factorio.com/latest/runtime-api.json

## Prototypes
All Prototype definitions from the [factorio wiki](https://wiki.factorio.com/Prototype_definitions) are served in a really big json file.
This file can also be downloaded from my page: https://factorio-api.knox.moe/prototypes.json.  
This list is manually maintained. The current timestamp of the last changes has a subpage: https://factorio-api.knox.moe/prototypes.json/version  

### JSON
The JSON has the following fields:
- tables - Table[]
- aliases - Alias[]
- string - StringType[]
- prototypes - Prototype[]

All of those types have in common:
- name - String
- link - String
- description - String

I wrote java classes, that provide the possibility to read and write the JSON with GSON.
These classes can be found in the [prototype type package](parser/prototype/src/main/java/moe/knox/prototype/types).
Entry class is [JsonRoot.java](parser/prototype/src/main/java/moe/knox/prototype/types/JsonRoot.java).

### Table
A table contains the information about what a prototype definition can have as properties.
A table also can have a different table as parent, which will combine both tables into one.
If a table has the `prototype` field set, it is referenced in a `prototype` and has the given `prototype` as parent.

Additional fields:
- properties - Property[] - An array of the [Property](parser/prototype/src/main/java/moe/knox/prototype/types/Property.java) object.
- parent - String - The name of a Prototype that this table inherits all properties from. This Prototype also has to be of type table.
- prototype - String - The ID of the Prototype this table represents (will be null in most cases)

#### Property
The Property object has five fields:
- name - String - The name of the property
- type - String - The type of the property. Normally a Prototype or a primitive.
- description - String
- default - String - The default value of an optional property.
- optional - Boolean - Defines, if this property is optional or mandatory.

### Alias
An alias is just what it says, an alias for something different.
The original type is in the field `other`.
Normally a Prototype or a primitive type.

### string
A list of strings that this string can be set to. Only these strings are valid values.
This is often used for flags, flags will have an `alias` to an array of these strings.
These strings can be represented as an alias to a string literal type (e.g. `---@type "enemy"|"ally"|"friend`).

`Key` of the map is a string, that can be used as value of this field.   
`Value` of the map is a description for the key. It can be used to show more information about the given key.

### prototype
A map of string to string. This is a represantation of a Prototype.
A Prototype has to contain a `type` field, that contains the ID.

`Key` of the map, is the name, that the prototype definition has to have in its `type` field.  
`Value` of the map, is the name of a Table.

A prototype is a table, that contains a `type` field. The `type` is the key of this map.
It can be looked up, and the value then is the name of the prototype name in the overall table.
The prototype in the overall map has to be of type table. The properties of the table, then are used for this prototype of type `prototype`.
This Prototype has to be available as class, so the actual Prototypes can inherit the `type` field, they don't have them themselves.

If one of the `Key` is an empty string `""`, then it is the default value.
Used for prototypes with default constructors, like [IngredientPrototype](https://wiki.factorio.com/Types/IngredientPrototype).


## Run it yourself
### prototype diff checker
Will download all the prototype pages from the wiki and compare them to previously downloaded ones.
Create a file called `discordwebhook.env` in the prototype directory and add the link to the discordwebhook into it, so the differences will be posted on discord.

Look into the sourcecode for how it works and what it does. Basically it is used, to check for changes on the wiki, so I can transfer them to the prototypes.json.
