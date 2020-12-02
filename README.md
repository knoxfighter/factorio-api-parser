# Factorio API as LUA and JSON files.

The goal of this project is to provide the factorio LUA API and the prototype definitions in a way, that computers can easily work with it.
Currently, the lua-API is served as LUA files, and the prototype definitions are served as json files. If you want any other format or LUA dialect, just say so as Issue or create a Pull Request, I am happy to add it to the project/website.

This project is still work in progress, thing might change over time.

## LUA-API
The LUA-API has a few pages, where everything is available to download.

### [https://factorio-api.knox.moe/api/](https://factorio-api.knox.moe/api/)
Contains a JSON array, with all the available versions. You can download it and select a version you want.
`latest` is not available, but the array is sorted from lowest to highest with the [go-version](http://github.com/hashicorp/go-version) library.

### [https://factorio-api.knox.moe/api/{version}/](https://factorio-api.knox.moe/api/1.0.0/)
Contains a JSON array, with all the available LUA files. You can iterate over all entries and then download the specified files. 
Replace `{version}`, with your selected version.
The integration is only fully available, when you download all the files. The files also have cross-references in them.

### [https://factorio-api.knox.moe/api/{version}/{file}](https://factorio-api.knox.moe/api/1.0.0/AmmoType.lua)
This route will serve a specific LUA file. Replace `{file}`, with a single filename.
The LUA files were built for EmmyLUA (a Plugin for Jetbrains IntelliJ)

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

### alias
An alias is just what it says, an alias for something different. The `value` field is of type `string, that just contains a different prototype.
It has the same layout as the `property->type` field. 

### string
The `value` field is a map from string to string.

`Key` of the map is a string, that can be used as value of this field. It is also possible to write anything into the string, these keys are just to make proposals for the autocompletion.  
`Value` of the map is a description for the key. It can be used to show more information about the given key.

### stringArray
This is basically the same as the `string` type. It just differs, that this is an array, that can have multiple of the values defined as `key`. A value can only be used once in the array.
Additional values, that are not part of the keys can also be used.

## Run it yourself
### prototype diff checker
Will download all the prototype pages from the wiki and compare them to previously downloaded ones.
Create a file called `discordwebhook.env` in the prototype directory and add the link to the discordwebhook into it, so the differences will be posted on discord.

Look into the sourcecode for how it works and what it does. Basically it is used, to check for changes on the wiki, so I can transfer them to the prototypes.json.
