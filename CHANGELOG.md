# Changelog

All notable changes to the GMS v214 server are documented in this file.

## [Unreleased] — Round 2

### Critical Bug Fixes

- **Fixed thread-unsafe user tracking set** — `Server.java` used a plain `HashSet<Integer>` for tracking logged-in users, accessed from multiple client threads simultaneously. Under load, this could cause `ConcurrentModificationException` crashes. Changed to `ConcurrentHashMap.newKeySet()` for thread-safe concurrent access.
  - File: `src/main/java/net/swordie/ms/Server.java`

- **Fixed character deletion not persisting to database** — Deleting a character in the login screen removed it from the in-memory account but never called `DatabaseManager.deleteFromDB()`. The character would reappear after server restart.
  - File: `src/main/java/net/swordie/ms/handlers/LoginHandler.java`

- **Added null-safe map warp validation** — `Char.warp(int fieldId)` and `warp(int fieldId, int portalId)` could NPE when given an invalid or nonexistent field ID. Now checks for null before proceeding and sends a chat message to the player instead of crashing.
  - File: `src/main/java/net/swordie/ms/client/character/Char.java`

### Scroll System

- **Fixed Lucky Day scroll not boosting success rate** — The Lucky Day attribute was correctly applied to equipment (via `EquipAttribute.LuckyDay`) but the scroll upgrade handler never checked for it when computing success probability. Now adds +10% success rate (capped at 100%) when Lucky Day is active.
  - File: `src/main/java/net/swordie/ms/handlers/item/ItemUpgradeHandler.java`

### Crafting System

- **Added ingredient pre-check to crafting validation** — `MakingSkillRecipe.isAbleToBeUsedBy()` previously had a TODO for checking if the player had required materials. The crafting UI could show recipes as available when materials were missing. Now validates all ingredients before allowing the craft attempt.
  - File: `src/main/java/net/swordie/ms/loaders/containerclasses/MakingSkillRecipe.java`

- **Fixed crafting not deducting meso cost** — Recipes with a meso requirement (`reqMeso > 0`) were validated in `isAbleToBeUsedBy()` but the handler never actually deducted the mesos after crafting. Now deducts mesos alongside ingredient consumption.
  - File: `src/main/java/net/swordie/ms/handlers/user/SkillHandler.java`

- **Fixed infinite loop in crafting target selection** — The reward target selection used a `while(true)` loop that randomly picked items and checked if their `probWeight >= rand`. With low-weight targets, this could loop forever. Replaced with proper cumulative weighted random selection that always terminates.
  - File: `src/main/java/net/swordie/ms/handlers/user/SkillHandler.java`

- **Implemented recipe cooldowns** — Recipes with `coolTimeSec > 0` were never enforced. Now applies a skill cooldown timer after crafting and checks for active cooldowns before allowing re-craft.
  - Files: `MakingSkillRecipe.java`, `SkillHandler.java`

---

## [Unreleased] — Round 1

### Boss Mechanics

- **Fixed hardcoded boss damage** — Mob skill `Damage` (ID 176) was dealing a flat 5000 HP regardless of boss. Now reads the actual damage value from WZ data via `MobSkillStat.x`, matching all other mob skill cases. Affects all bosses that use damage skills (Lucid, Lotus, Magnus, etc.).
  - File: `src/main/java/net/swordie/ms/life/mob/skill/MobSkill.java`

- **Fixed Horntail linked-HP body parts taking zero damage** — The `totalDamage` accumulator in the attack handler was commented out, causing Horntail sponge mobs (8810202–8810209), regular HT parts (8810002–8810009), and Chaos HT parts (8810102–8810109) to receive 0 transferred damage. Individual hit damage was still applied, but the linked body mobs (8810214, 8810018, 8810118) were effectively unkillable.
  - File: `src/main/java/net/swordie/ms/handlers/user/AttackHandler.java`

### Drop System

- **Fixed NullPointerException crash in drop generation** — `getMostDamageChar()` could return null (e.g., if the killer disconnected), but the 2x Drop Coupon check called `.getCashInventory()` on it without a null guard. Added null check to prevent server crash.
  - File: `src/main/java/net/swordie/ms/life/mob/Mob.java`

- **Level-based drops now used as fallback only** — Previously, generic level-based consumable and equip drops were always added on top of mob-specific drops from the database, polluting boss drop tables with random potions and low-level gear. Now level-based drops only appear when a mob has no specific item drops defined in the `mob_drops` table.
  - File: `src/main/java/net/swordie/ms/life/mob/Mob.java`

- **Added drop data for 8 major bosses** — The following bosses previously had zero drops defined:
  - Magnus Normal (8880000): Nova Essence, Intense Power Crystal, Spell Traces, Power Elixirs
  - Magnus Hard (8880100): Tyrant Cloaks, Boots, and Belts (all 5 job variants), Nova Essence, Tyrant Nova Essence
  - CRA Crimson Queen (8870200): Root Abyss Earring, Badge, ores, crafting materials
  - CRA Vellum (8870300): Root Abyss Earring, Badge, ores, crafting materials
  - Lucid Normal (8880140): Arcane River Droplet Stones, Intense Power Crystal
  - Lucid Hard (8880141): Arcane Umbra Shoulders and Hats (all 5 job variants), Droplet Stones
  - Lotus Normal (8880300): Arcane River Droplet Stones, Intense Power Crystal
  - Lotus Hard (8880301): Arcane Umbra Suits and Shoes (all 5 job variants), Droplet Stones
  - File: `sql/2 - InitTables_drops.sql`

### Class Skills

- **HoYoung — Removed debug code** — `RITUAL_FAN_ACCELERATION` had a leftover debug line that instantly maxed out both Talisman Energy and Scroll Energy every time the skill was used. The spell gauge now accumulates naturally through gameplay.
  - File: `src/main/java/net/swordie/ms/client/jobs/anima/HoYoung.java`

- **HoYoung — Implemented Return to Cheongwoon** — The beginner skill `RETURN_TO_CHEONGWOON` (160001074) was defined but had no handler. Now warps the character to the current field's return map, matching how other return-to-town skills (Aran's Return to Rien, Pathfinder's Return to Partem) work.
  - File: `src/main/java/net/swordie/ms/client/jobs/anima/HoYoung.java`

- **Kanna — Fixed Haku's Blessing fan damage** — The fan's Magic Attack modifier was computed correctly in `hakuHakuBlessing()` but only stored in the `HakuBlessing` temporary stat, which was never used in the damage calculation. Now also applies the bonus as an `IndieMAD` temporary stat so it actually increases Kanna's (and party members') magic attack.
  - File: `src/main/java/net/swordie/ms/client/jobs/sengoku/Kanna.java`

- **Adele — Implemented Recalling Greatness periodic leveling** — The passive skill `RECALLING_GREATNESS` (150020006) was added to characters but never leveled up. Now starts a periodic timer that increments the buff level every 10 minutes while playing, providing +1 All Stat (STR/DEX/INT/LUK) and +1 ATT/MATT per level, up to a maximum of level 30 (+30 all stat, +30 ATT/MATT).
  - File: `src/main/java/net/swordie/ms/client/jobs/flora/Adele.java`

### Platform Compatibility

- **Replaced hardcoded Windows paths with platform-independent paths** — Six developer utility files contained absolute Windows paths (`D:\SwordieMS\...`, `C:\v207\...`) in their `main()` methods. These were code-generation tools, not production code, but would fail on Linux/macOS. All replaced with `ServerConstants.DIR`-based relative paths using forward slashes.
  - Files: `MobStat.java`, `OutHeader.java`, `GuildType.java`, `PartyType.java`, `ShopResultType.java`

- **Fixed Python NPC script hardcoded path** — `face_henesys1.py` had a hardcoded `C:/Users/Downloads/...` path. Replaced with `os.path.join(os.getcwd(), "wz", "Character.wz", "Face")`.
  - File: `scripts/npc/face_henesys1.py`
