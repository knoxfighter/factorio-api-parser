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
This list is manually maintained. The current timestamp of the last changes have a subpage: https://factorio-api.knox.moe/prototypes.json/version  
The prototypes are also available as LUA file. In the LUA files all prototypes typed `string` or `stringArray` are only in there as alias to `string` or `string[]`.
Also, everything of type `prototype` is stripped, cause there is no practical format to define it.

The JSON is a map from string to objects.
I wrote java classes, that provide the possibility to read and write the JSON with GSON.
These classes can be found in the [prototype type package](parser/prototype/src/main/java/moe/knox/prototype/types).
Entry class is [Prototype.java](parser/prototype/src/main/java/moe/knox/prototype/types/Prototype.java).

The object has the following fields:
- name [string] - The name of the prototype (same as key)
- type [string] - The type of this entry, defines type of `value`. (One of `table`, `prototype`, `alias`, `string`, `stringArray`)
- link [string] - The link for the definition in the factorio wiki
- description [string]
- value [object] - The value field can have 3 different types, based on the value of the `type` field.

Further information for each type and the type of the `value` field, is below:

### table
A table contains the information about what a prototype definition can have as properties.
A table also can have a different table as parent, which will combine both tables into one.

The `value` field is of type [Table](parser/prototype/src/main/java/moe/knox/prototype/types/Table.java).

#### Table 
The Table object has two fields:
- properties - Property[] - An array of the [Property](parser/prototype/src/main/java/moe/knox/prototype/types/Property.java) object.
- parent - string - The name of a Prototype that this table inherits all properties from. This Prototype also has to be of type table.

#### Property
The Property object has five fields:
- name - string - The name of the property
- type - string - The type of the property. Normally a Prototype or a primitive.
- description - string
- default - string - The default value of an optional property.
- optional - bool - Defines, if this property is optional or mandatory.

### prototype
The `value` field is a map from string to string.

`Key` of the map, is the name, that the prototype definition has to have in its `type` field.  
`Value` of the map, is the name of a Prototype (key of the overall map).

A prototype is a table, that contains a `type` field. The `type` is the key of this map.
It can be looked up, and the value then is the name of the prototype name in the overall table.
The prototype in the overall map has to be of type table. The properties of the table, then are used for this prototype of type `prototype`.

If one of the `Key` is an empty string `""`, then it is the default value.
Used for prototypes with default constructors, like [IngredientPrototype](https://wiki.factorio.com/Types/IngredientPrototype).

### alias
An alias is just what it says, an alias for something different. The `value` field is of type `string`, that just contains a different prototype.
It has the same layout as the `property->type` field. 

### string
The `value` field is a map from string to string.

`Key` of the map is a string, that can be used as value of this field. It is also possible to write anything into the string, these keys are just to make proposals for the autocompletion.  
`Value` of the map is a description for the key. It can be used to show more information about the given key.

### stringArray
This is basically the same as the `string` type. It just differs, that this is an array, that can have multiple of the values defined as `key`. A value can only be used once in the array.

## Run it yourself
### prototype diff checker
Will download all the prototype pages from the wiki and compare them to previously downloaded ones.
Create a file called `discordwebhook.env` in the prototype directory and add the link to the discordwebhook into it, so the differences will be posted on discord.

Look into the sourcecode for how it works and what it does. Basically it is used, to check for changes on the wiki, so I can transfer them to the prototypes.json.
