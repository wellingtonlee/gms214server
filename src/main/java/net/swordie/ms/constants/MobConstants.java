package net.swordie.ms.constants;

import net.swordie.ms.life.drop.DropInfo;

import java.util.*;

public class MobConstants {

    // Boss template IDs — body/main mob for each boss
    public static final int EASY_ZAKUM_BODY = 8800022;
    public static final int NORMAL_ZAKUM_BODY = 8800002;
    public static final int CHAOS_ZAKUM_BODY = 8800102;

    public static final int EASY_HORNTAIL_SPONGE = 8810018;
    public static final int NORMAL_HORNTAIL_SPONGE = 8810118;
    public static final int CHAOS_HORNTAIL_SPONGE = 8810214;

    public static final int NORMAL_PINK_BEAN = 8820001;
    public static final int CHAOS_PINK_BEAN = 8820101;

    public static final int EASY_MAGNUS = 8880000;
    public static final int NORMAL_MAGNUS = 8880002;
    public static final int HARD_MAGNUS = 8880010;

    public static final int EASY_CYGNUS = 8850011;
    public static final int NORMAL_CYGNUS = 8850111;

    public static final int VON_LEON = 8840000;

    public static final int NORMAL_HILLA = 8840400;
    public static final int HARD_HILLA = 8840404;

    // Boss guaranteed drops — items that always drop on boss death
    private static final Map<Integer, List<DropInfo>> BOSS_GUARANTEED_DROPS = new HashMap<>();

    static {
        // Zakum — Eye of Fire accessory (1012070) and Zakum Helm pieces
        BOSS_GUARANTEED_DROPS.put(EASY_ZAKUM_BODY, List.of(
                new DropInfo(1012070, 1000),  // Eye of Fire
                new DropInfo(1002357, 1000)   // Zakum Helmet (Easy)
        ));
        BOSS_GUARANTEED_DROPS.put(NORMAL_ZAKUM_BODY, List.of(
                new DropInfo(1012070, 1000),  // Eye of Fire
                new DropInfo(1002357, 1000),  // Zakum Helmet
                new DropInfo(1002390, 500)    // Zakum Helmet 2 (50% chance via normal drop)
        ));
        BOSS_GUARANTEED_DROPS.put(CHAOS_ZAKUM_BODY, List.of(
                new DropInfo(1012070, 1000),  // Eye of Fire
                new DropInfo(1003174, 1000)   // Chaos Zakum Helmet
        ));

        // Horntail — Necklace drops
        BOSS_GUARANTEED_DROPS.put(EASY_HORNTAIL_SPONGE, List.of(
                new DropInfo(1122000, 1000)   // Horntail Necklace
        ));
        BOSS_GUARANTEED_DROPS.put(NORMAL_HORNTAIL_SPONGE, List.of(
                new DropInfo(1122000, 1000),  // Horntail Necklace
                new DropInfo(1032062, 500)    // Silver Blossom Ring (50%)
        ));
        BOSS_GUARANTEED_DROPS.put(CHAOS_HORNTAIL_SPONGE, List.of(
                new DropInfo(1122076, 1000),  // Chaos Horntail Necklace
                new DropInfo(1032062, 1000)   // Silver Blossom Ring
        ));

        // Pink Bean — Belt and pocket
        BOSS_GUARANTEED_DROPS.put(NORMAL_PINK_BEAN, List.of(
                new DropInfo(1132174, 1000),  // Pink Bean Belt
                new DropInfo(1012438, 500)    // Pink Bean Mark (50%)
        ));
        BOSS_GUARANTEED_DROPS.put(CHAOS_PINK_BEAN, List.of(
                new DropInfo(1132174, 1000),  // Pink Bean Belt
                new DropInfo(1012438, 1000),  // Pink Bean Mark
                new DropInfo(2591200, 500)    // Pink Bean Soul Shard (50%)
        ));

        // Magnus — Shoulder, badge
        BOSS_GUARANTEED_DROPS.put(EASY_MAGNUS, List.of(
                new DropInfo(1012478, 1000)   // Crystal Ventus Badge
        ));
        BOSS_GUARANTEED_DROPS.put(NORMAL_MAGNUS, List.of(
                new DropInfo(1012478, 1000),  // Crystal Ventus Badge
                new DropInfo(1152149, 1000)   // Royal Black Metal Shoulder
        ));
        BOSS_GUARANTEED_DROPS.put(HARD_MAGNUS, List.of(
                new DropInfo(1012478, 1000),  // Crystal Ventus Badge
                new DropInfo(1152150, 1000),  // Tyrant Hyades Cloak
                new DropInfo(2591100, 500)    // Magnus Soul Shard (50%)
        ));

        // Von Leon
        BOSS_GUARANTEED_DROPS.put(VON_LEON, List.of(
                new DropInfo(1003172, 1000),  // Royal Von Leon Helm
                new DropInfo(1052314, 500)    // Royal Von Leon Suit (50%)
        ));

        // Hilla
        BOSS_GUARANTEED_DROPS.put(NORMAL_HILLA, List.of(
                new DropInfo(1003282, 1000)   // Stone of Eternal Life (Blackheart pet)
        ));
        BOSS_GUARANTEED_DROPS.put(HARD_HILLA, List.of(
                new DropInfo(1003282, 1000),  // Stone of Eternal Life
                new DropInfo(2591000, 500)    // Hilla Soul Shard (50%)
        ));

        // Cygnus
        BOSS_GUARANTEED_DROPS.put(EASY_CYGNUS, List.of(
                new DropInfo(1003455, 1000)   // Empress Cygnus Helm
        ));
        BOSS_GUARANTEED_DROPS.put(NORMAL_CYGNUS, List.of(
                new DropInfo(1003455, 1000),  // Empress Cygnus Helm
                new DropInfo(1082543, 500)    // Empress Cygnus Gloves (50%)
        ));
    }

    /**
     * Returns guaranteed drops for a boss, or null if the mob is not a boss with guaranteed drops.
     */
    public static List<DropInfo> getBossGuaranteedDrops(int templateId) {
        return BOSS_GUARANTEED_DROPS.get(templateId);
    }

    /**
     * Returns whether the given template ID is a known boss.
     */
    public static boolean isBoss(int templateId) {
        return BOSS_GUARANTEED_DROPS.containsKey(templateId);
    }
    public static final int QUEST_MOB_START = 9000000;

    public static boolean isZakumBody(int templateId) {
        return templateId == 8800022 || templateId == 8800002 || templateId == 8800102;
    }

    public static boolean isZakumArm(int templateId) {
        final int ZAKUM_ARMS = 8;
        final int EASY_ZAKUM = 8800022;
        final int NORM_ZAKUM = 8800002;
        final int HARD_ZAKUM = 8800102;
        return (templateId > EASY_ZAKUM && templateId <= EASY_ZAKUM + ZAKUM_ARMS) ||
                (templateId > NORM_ZAKUM && templateId <= NORM_ZAKUM + ZAKUM_ARMS) ||
                (templateId > HARD_ZAKUM && templateId <= HARD_ZAKUM + ZAKUM_ARMS);

    }

    public static boolean isHorntailSponge(int templateId) {
        // Easy HT sponge, Normal HT sponge, Chaos HT sponge
        return templateId == 8810018 || templateId == 8810118 || templateId == 8810214;
    }

    public static boolean isHorntailPart(int templateId) {
        // Normal HT parts: 8810002-8810009 (heads, wings, tails, legs)
        // Chaos HT parts: 8810102-8810109
        return (templateId >= 8810002 && templateId <= 8810009)
                || (templateId >= 8810102 && templateId <= 8810109);
    }

    public static boolean isDamien(int templateId) {
        return templateId >= 8880100 && templateId <= 8880131;
    }
    
    public static boolean isTimedDropMob(int templateId) {
        switch (templateId) {
            case 9300907:
                return true;
            default:
                return false;
        }
    }

    public static double getBuffMultiplierFromRegion(int prefix) {
        double multi = prefix / 100D;
        if (prefix == 450) {
            // arcane river
            multi *= 3;
        } else if (prefix >= 900) {
            // quest maps
            return 1;
        }
        return 5 * Math.min(4, multi); // min 2x of multiplier (mainly victoria island), then x3 overall
    }
}
