# Loot Crates

## About

LootCrates is a Minecraft mod for the fabric mod loader. The mod adds additional chests and shulker boxes, with a twist.
The crates are meant to be used by server administrators or modpack makers to distribute loot in structures, add them as drops, or use them as rewards for quests.

The container blocks do not generate naturally in the world and are highly customizable on how they function.

## Example Usages
- Loot crate at spawn, that generates a start loadout once for each player
- Use locked crates as a form of progression, handing out the keys as rewards for quests or advancements
- Loot crates that can be looted once per player in structures as a reward for exploration
- Use shulker crates in mob drops for that "loot bags" feel
- To build upon that: Use locked shulker crates instead. To encourage trading scatter the required keys around the landscape for players to find.
- Make keys drop from special mobs or bosses. Players have to farm them to get access to their collected shulker crates
- Place instant regenerating, locked loot crates at the servers central square to get players to run into each other for socialization or PvP, fighting each other for their keys

## Rarities
All the loot bundles, crates, shulker crates and keys come in these rarities, each with their own unique look:
- Common
- Uncommon
- Rare
- Epic
- Ghastly
- Blazing

# Loot Bundles

![Loot Bundles](./images/loot_bundles.jpg)

Loot bundles come in all rarities. They can be used by players to get random content of a set loot table in their inventory.

https://user-images.githubusercontent.com/26429514/133820478-b6ddfea9-5b03-41af-ab50-f85598c2e4f0.mp4

### Available Loot Bundle Item tags
Tag                       | NBT Type      | Effect
------------------------- | ------------- | ------
LootTable                 | loot table    | The loot table identifier to be used to fill the crate when it's interacted with
LootTableSeed             | long          | Seed for generating the loot table. 0 or omitted uses a random seed. Setting a seed means that the generated loot will always be the same


# Loot Crates

## Screenshots

![All the Loot Crates, Shulkers and keys](./images/crates.png)

## Loot Crate Chests
Loot crates cannot be broken or moved. Instead, they generate loot over time!

## Shulker Crates
Shulker crates, like their loot crate counterparts, can generate loot.
But unlike their unbreakable counterparts they can be broken and picked up.
They also retain their items and can be used as backpacks.

## Loot Barrels
By default, these behave exactly like loot crates.

## Keys
Crates may be locked, requiring a key with matching rarity to unlock.
Other than that the keys themselves are rather unspectacular.

# Customization

## Available Item tag data
Those have to be set under the tag "BlockEntityTag" to have an effect (analog to vanilla items).
The data is identical between loot and shulker crates.

Tag                       | NBT Type      | Effect
------------------------- | ------------- | ------
LootTable                 | loot table    | The loot table identifier to be used to fill the crate when it's interacted with
LootTableSeed             | long          | Seed for generating the loot table. 0 or omitted uses a random seed. Setting a seed means that the generated loot will always be the same
ReplenishTimeTicks        | long          | When the crate is accessed it will take that many ticks until new loot can be generated. Setting the value to <= 0 results in generating the content just once - at the time of the first opening.
Locked                    | boolean       | When true a key with matching rarity will be required to unlock the crate and access it's contents
Trapped                   | boolean       | When true the crate will output a redstone signal when opened, similar to a Trapped Chest
DoNotConsumeKeyOnUnlock   | boolean       | When true opening a locked crate will not consume the key.
OncePerPlayer             | boolean       | When true every player can only use the crate once to generate loot. "ReplenishTimeTicks" has to be set to >0.
Inventory                 | list of items | Analog vanilla shulker chests. Defines the items stored in the crate. Can be used to define items that are always present in a crate at first opening. (chest crates can have this set, too. They just don't set it when broken, because they are meant to be unbreakable outside creative)

**Warning:**

Handle some combinations with care. Or do you really want players to have portable containers that generate new loot every minute?

## Example

Following examples use a Rare Chest Loot Crate named "Bastion Treasure Crate" containing the vanilla bastion_treasure loot table, that is locked and will be restocked every 60 ticks (3 seconds).

## How Loot Crates work internally
![Untitled Diagram](https://user-images.githubusercontent.com/26429514/134002391-1b9e0e91-6fec-4355-98b3-3d50582b82c0.png)

### Give-Command

    /give @p lootcrates:rare_chest_loot_crate{BlockEntityTag: {LootTable: "minecraft:chests/bastion_treasure", Locked: 1b, ReplenishTimeTicks: 60L}, display: {Name: '{"text":"Bastion Treasure Crate"}'}} 1
    
### SetBlock-Command

    /setblock ~ ~ ~ lootcrates:rare_chest_loot_crate{LootTable: "minecraft:chests/bastion_treasure", Locked: 1b, ReplenishTimeTicks: 60L, CustomName: '{"text":"Bastion Treasure Crate"}'}
    
### Loot table

```json
{
	"pools": [{
		"rolls": 1,
		"entries": [{
			"type": "item",
			"name": "lootcrates:rare_chest_loot_crate",
			"weight": 1,
			"functions": [{
				"function": "set_nbt",
				"tag": "{BlockEntityTag: {LootTable: \"minecraft:chests/bastion_treasure\", Locked: 1b, ReplenishTimeTicks: 60L}, display: {Name: '{\"text\":\"Bastion Treasure Crate\"}'}}"
			}]
		}]
	}]
}
```
