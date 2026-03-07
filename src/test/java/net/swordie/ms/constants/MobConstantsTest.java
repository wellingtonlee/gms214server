package net.swordie.ms.constants;

import net.swordie.ms.life.drop.DropInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MobConstantsTest {

    // Zakum body checks
    @Test
    void isZakumBodyRecognizesAllDifficulties() {
        assertTrue(MobConstants.isZakumBody(8800022));  // Easy
        assertTrue(MobConstants.isZakumBody(8800002));  // Normal
        assertTrue(MobConstants.isZakumBody(8800102));  // Chaos
    }

    @Test
    void isZakumBodyRejectsNonZakum() {
        assertFalse(MobConstants.isZakumBody(100100));
        assertFalse(MobConstants.isZakumBody(8800003)); // Arm, not body
    }

    // Zakum arm checks
    @Test
    void isZakumArmRecognizesArms() {
        assertTrue(MobConstants.isZakumArm(8800003));   // Normal arm 1
        assertTrue(MobConstants.isZakumArm(8800010));   // Normal arm 8
        assertTrue(MobConstants.isZakumArm(8800023));   // Easy arm 1
        assertTrue(MobConstants.isZakumArm(8800103));   // Chaos arm 1
    }

    @Test
    void isZakumArmRejectsBody() {
        assertFalse(MobConstants.isZakumArm(8800002));  // Body, not arm
    }

    // Horntail checks
    @Test
    void isHorntailSpongeRecognizesAllDifficulties() {
        assertTrue(MobConstants.isHorntailSponge(8810018));   // Easy
        assertTrue(MobConstants.isHorntailSponge(8810118));   // Normal
        assertTrue(MobConstants.isHorntailSponge(8810214));   // Chaos
    }

    @Test
    void isHorntailSpongeRejectsNonSponge() {
        assertFalse(MobConstants.isHorntailSponge(8810002));  // Part, not sponge
        assertFalse(MobConstants.isHorntailSponge(100100));
    }

    @Test
    void isHorntailPartRecognizesParts() {
        assertTrue(MobConstants.isHorntailPart(8810002));
        assertTrue(MobConstants.isHorntailPart(8810009));
        assertTrue(MobConstants.isHorntailPart(8810102));
        assertTrue(MobConstants.isHorntailPart(8810109));
    }

    @Test
    void isHorntailPartRejectsSponge() {
        assertFalse(MobConstants.isHorntailPart(8810018));
    }

    // Damien check
    @Test
    void isDamienRecognizesDamienRange() {
        assertTrue(MobConstants.isDamien(8880100));
        assertTrue(MobConstants.isDamien(8880131));
        assertFalse(MobConstants.isDamien(8880099));
        assertFalse(MobConstants.isDamien(8880132));
    }

    // Boss guaranteed drops
    @Test
    void isBossRecognizesKnownBosses() {
        assertTrue(MobConstants.isBoss(MobConstants.EASY_ZAKUM_BODY));
        assertTrue(MobConstants.isBoss(MobConstants.NORMAL_ZAKUM_BODY));
        assertTrue(MobConstants.isBoss(MobConstants.CHAOS_ZAKUM_BODY));
        assertTrue(MobConstants.isBoss(MobConstants.EASY_HORNTAIL_SPONGE));
        assertTrue(MobConstants.isBoss(MobConstants.NORMAL_PINK_BEAN));
        assertTrue(MobConstants.isBoss(MobConstants.HARD_MAGNUS));
        assertTrue(MobConstants.isBoss(MobConstants.VON_LEON));
    }

    @Test
    void isBossRejectsRegularMobs() {
        assertFalse(MobConstants.isBoss(100100));   // Snail
        assertFalse(MobConstants.isBoss(0));
        assertFalse(MobConstants.isBoss(-1));
    }

    @Test
    void getBossGuaranteedDropsReturnsNullForNonBoss() {
        assertNull(MobConstants.getBossGuaranteedDrops(100100));
    }

    @Test
    void getBossGuaranteedDropsReturnsDropsForZakum() {
        List<DropInfo> drops = MobConstants.getBossGuaranteedDrops(MobConstants.EASY_ZAKUM_BODY);
        assertNotNull(drops);
        assertFalse(drops.isEmpty());
        assertTrue(drops.stream().anyMatch(d -> d.getItemID() == 1012070)); // Eye of Fire
    }

    @Test
    void getBossGuaranteedDropsReturnsDropsForHorntail() {
        List<DropInfo> drops = MobConstants.getBossGuaranteedDrops(MobConstants.CHAOS_HORNTAIL_SPONGE);
        assertNotNull(drops);
        assertTrue(drops.stream().anyMatch(d -> d.getItemID() == 1122076)); // Chaos HT Necklace
    }

    @Test
    void getBossGuaranteedDropsReturnsDropsForMagnus() {
        List<DropInfo> drops = MobConstants.getBossGuaranteedDrops(MobConstants.HARD_MAGNUS);
        assertNotNull(drops);
        assertFalse(drops.isEmpty());
    }

    // Timed drop mob check
    @Test
    void isTimedDropMobRecognizesKnownMob() {
        assertTrue(MobConstants.isTimedDropMob(9300907));
        assertFalse(MobConstants.isTimedDropMob(100100));
    }
}
