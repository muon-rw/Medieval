## 1.7.0
- Backport Dynamic HUD Bars
- Fixed glass breaking sound bug when nearby player is wearing a Frostward ring
- Disable Dungeons and Combat book usage
- Nerfed a few problematic spells

## 1.6.1
- Experimental - disable Entity Linked Item Stack-related logic in Apoli. 
(This should be a *massive* performance increase in some cases, but will break some custom Origins!)

## 1.6.0
- Allow Town Portal scroll usage in any dimension
- Fix Town Portal scrolls in spotty multiplayer environments

## 1.5.10
- Fix class initialization bug

## 1.5.9
- Make Traveler's Backpack hose drinking restore Survive thirst (due to Nyf's Compat not being updated)

## 1.5.8
- Disable a few more checks in Survive if temp isn't loaded
- Fix Skill Tree crafting bonus support for: Farmer's Delight Skillet, Eidolon Worktable, Tetra Workbenches, Wizards Reborn Arcane Workbench

## 1.5.7
- Minor Internal changes for OtherworldApoth (Will crash with 0.1.0 of OtherworldApoth)

## 1.5.6
- Also sync Skill Tree levels on point allocation

## 1.5.5
- Fix Just Leveling screen modifications not applying

## 1.5.4
- Fix level rendering bug introduced in 1.5.3

## 1.5.3
- Fix Town Portal Scrolls sometimes teleporting to unsafe locations

## 1.5.2
- Start JL at level 2 instead of level 3 (so that players need to spend all 80 points to reach level 20)

## 1.5.1
- Add level progress % to just leveling client level render
- Normalize Just Leveling "player level" around 80 max points, //todo: config

## 1.5.0
- Render player levels as the player's skill tree points instead of always level 1 - hide if no leveling mod is loaded //todo: see if PST data needs to be synced as well
- Add Just Leveling compat to the above feature. Syncs on level-up

## 1.4.6
- Fix other log spam

## 1.4.5
- Fix log spam
- Fix Autoleveling being required again

## 1.4.4
- Fix Autoleveling's config attribute modifiers per level never applying

## 1.4.3
- Add Autoleveling entity-specific attribute_modifiers per level

## 1.4.2
- Make Autoleveling integrations optional

## 1.4.1
- Optional crafting menu integrations, don't require IPL
- Fix IPL + Traveler's Backpack integration sometimes not working 

## 1.4.0
- IPL integrations: Celestisynth, Traveler's Backpack, Tom's Simple Storage, Ars Nouveau (storage lecterns)
- More mixin configuring

## 1.3.0
- Added mob leveling based on local players' Passive Skill Tree points (may add other player scaling options in the future)
- Added mob leveling based on structure
- Added config options
- Cancel Apotheosis Treasure Goblin gem trigger on Target Dummies

## 1.2.4
- Soft check for FTB Chunks/Passive Skill Tree instead of crashing
- Add description to Challenge Orb

## 1.2.3
- Fix crash on dedicated servers
- Fixed town portal scrolls sometimes teleporting to unsafe positions
- Restricted town portal scrolls to Overworld (may change later, other dimensions were inconsistent)

## 1.2.2
- Cancel leveling for fully passive mobs (cows, chickens, etc)
- Clean up some unused event dynamic leveling stuff

## 1.2.1
- I don't remember. Why are you reading this? Who is downloading this?

## 1.2.0
- Challenge Orbs
- Town Portal Scrolls
- Force fix stamina HUD
- Integrate levels+health bars
- Outline dynamic leveling

## 1.0.0
* Initial Release