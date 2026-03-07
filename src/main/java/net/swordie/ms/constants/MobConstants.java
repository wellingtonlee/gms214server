package net.swordie.ms.constants;

public class MobConstants {
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
