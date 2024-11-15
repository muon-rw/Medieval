## 1.5.3
- Fix Town Portal Scrolls sometimes teleporting to unsafe locations
- Fix Passive whitelist only working with Animals

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