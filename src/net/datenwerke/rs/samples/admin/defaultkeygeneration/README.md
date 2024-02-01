# Default key generation
As of version 4.7.0 many entities (e.g Reports, Datasink, FileServerFiles) will require
a unique key within their TreeManager. Per default a 40 character key is generated
using a hashing based on the current timestamp and a random number. You can customize
the look of your default keys by providing your own implementation the corresponding 
hooks. The **KeyGeneratorService** provides two methods:

### generateDefaultKey() 
This generates a default key without further checks. To use your own 
implementation you need to register a GeneralGenerateDefaultKeyHook. Be aware that
only **one** hook of this kind should be registered. 
### generateDefaultKey(TreeDBManager treedbmanager) 
This generates a defaultkey until a unique key within the given treedbmanager is found.
This prevents the (extremly) rare occurence of possible collisions. You may register
a SpecificGenerateDefaultKeyHook to influence the used generation method. 
The order of importance is as follows:
1. SpecificGenerateDefaultKeyHook
2. GeneralGenerateDefaultKeyHook
3. Default implementation

While you can register more hooks of this kind, there should only ever be one 
for each TreeDBManager.

