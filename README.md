# MSNPCs
Simple and small NPC library for Minestom.
One of its key features is that it automatically removes npcs from chat completions and the player report menu.
[MSNameTags](https://github.com/EcholightMC/MSNameTags) support is built-in by setting the username string tag for the npc, helping it recognize if it's a Player NPC or not.

## Usage
### Without [MSNameTags](https://github.com/EcholightMC/MSNameTags)
```java
GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
NPCManager npcManager = new NPCManager(true, eventHandler);
NPC npc = npcManager.createNPC(EntityType.PLAYER, "Bill", null, null); // if name is null npc id will be used instead
npc.setInstance(instance, position);
```
### With [MSNameTags](https://github.com/EcholightMC/MSNameTags)
```java
MiniMessage miniMessage = MiniMessage.miniMessage();
GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
NameTagManager nameTagManager = new NameTagManager(true, eventHandler);
NPCManager npcManager = new NPCManager(eventHandler);
NPC npc = npcManager.createNPC(EntityType.PLAYER, null, null, null); // if name is null npc id will be used instead
npc.setInstance(instance, position);
NameTag nameTag = nameTagManager.createNameTag(npc);
nameTag.setText(miniMessage.deserialize("<red><b>Billy"));
```
## Add as Dependency
### Gradle
```gradle
repositories {
  ..
  maven {
    url = "https://maven.hapily.me/releases"
  }
}
```
```gradle
dependencies {
  ..
  implementation("com.github.echolightmc:MSNPCs:1.4-SNAPSHOT") {
    exclude group: "net.minestom", module: "minestom-snapshots"
  }
}
```
