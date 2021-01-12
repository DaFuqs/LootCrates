# Loot Crates

## About

LootCrates is a Minecraft mod for the fabric mod loader. The mod adds additional chests and shulker boxes, with a twist.
The crates are meant to be used by server administrators or modpack makers to distribute loot in structures, add them as drops, or use them as rewards for quests.

The container blocks do not generate naturally in the world and are highly customizable on how they function.

## Example Usages

- Loot crates that can be looted once per player in structures as a reward for exploration
- Use locked crates as a form of progression, handing out the keys as rewards for quests or advancements
- Use shulker crates in mob drops for that "loot chest" feel
- To build upon that: Use locked shulker crates instead. To encourage trading scatter the required keys around the landscape for players to find.
- Place instant regenerating, locked loot crates at the servers central square to get players to run into each other for socializaton or PvP, fighting each other for their keys

## Rarities
All the loot crates, shulker crates and keys come in all the vanilla rarities, each with their own unique look:
- Common
- Uncommon
- Rare
- Epic

# The blocks

## Screenshots

![All the Loot Crates](./images/readme_screenshot_loot_crates.png)
![All the Shulker_Crates](./images/readme_screenshot_shulker_crates.png)

## Loot Crates
Loot crates cannot be broken or moved. Instead they generate loot over time!

## Shulker Crates
Shulker crates, like their loot crate counterparts, can generate loot.
But unlike their unbreakable counterparts they can be broken and picked up.
They also retain their items and can be used as backpacks.

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
Locked                    | boolean    | When true a key with matching rarity will be required to unlock the crate and access it's contents
DoNotConsumeKeyOnUnlock   | boolean    | When true the opening key will be not be removed when unlocking the crate. "Locked" will have to be true to be effective.
OncePerPlayer             | boolean    | When true every player can only use the crate once to generate loot. If "LootGenerationTimeInTicks" is set to <= 0 loot will be generated once per player

**Warning:**

Handle some combinations with care. Or do you really want players to have portable containers that generate new loot every minute?

