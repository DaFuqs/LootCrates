# LootCrates

## About

LootCrates is mod for Minecraft for the fabric mod loader that adds additional chests and shulker boxes with a twist.
The container blocks do not generate naturally in the world.
Instead they are ment to be used by server administrators or modpack makers to distribute loot in structures, add them as drops, or use them as rewards for quests.

The loot crates are highly customizable on how they function.

## Example Usages

- loot crates that can be looted once per player in structures as a reward for exploration
- Use locked crates as a form of progression, handing out the keys as rewards for quests or advancements
- Use shulker crates in mob drops for that "loot chest" feel
- To build upon that: Use locked shulker crates instead, to encourage trading scatter the required keys around the landscape for players to find.
- Scatter keys around the landscape and place loot crates at the servers central square. To get players to run into each other for socializaton or PvP, fighting each other for their keys

## Rarities
Loot Boxes, Loot Shulker Chests and Keys come in all the vanilla rarities, each with their own unique look:
- Common
- Uncommon
- Rare
- Epic

# The blocks

## Screenshots

![All the Loot Boxes](./images/readme_screenshot_loot_boxes.png)
![All the Shulker Loot Boxes](./images/readme_screenshot_shulker_loot_boxes.png)

## Loot Crates
Loot crates cannot be broken or moved. Instead, they generate loot over time!

## Shulker Crates
Shulker Crates, like their loot crate counterparts, can generate loot.
Unlike their unbreakable counterparts they can be broken and picked up.
When unlocked they retain their items and can be used as backpacks.

## Keys
Crates may be locked, requiring a key with matching rarity to unlock.
Other than that the keys themselves are rather unspectacular.


# Overview over the available NBT data

The data is identical between loot and shulker crates.

NBT                       | NBT Type   | Effect
------------------------- | ---------- | ------
CustomName                | string     | The name which will be used instead of the containers default name in it's gui
LootTable                 | loot table | The loot table identifier to be used to fill the crate when it's interacted with
LootTableSeed             | long       | Seed for generating the loot table. 0 or ommitted uses a random seed. Setting a seed means that the generated loot will always be the same
LootGenerationTimeInTicks | long       | When the crate is accessed it will take that many ticks until new loot can be generated. Setting the value to to <= 0 results in generating the content just once - at the time of the first opening
Locked                    | boolean    | When true a key with matching rarity will be required to access it's contents
DoNotConsumeKeyOnUnlock   | boolean    | When true the opening key will be not be removed when unlocking the crate. "Locked" will have to be true to be effective.
OncePerPlayer             | boolean    | When true every player can only use the crate once to generate loot. If "LootGenerationTimeInTicks" is set to <= 0 loot will be generated once per player

**Warning:**
Handle some combinations with care. Or do you really want players to have portable shulker boxes, that generate loot every minute?

