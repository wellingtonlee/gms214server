package net.swordie.ms.constants;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameConstantsTest {

    @Test
    void richesRuneDropCountIsPositive() {
        assertTrue(GameConstants.RICHES_RUNE_DROP_COUNT > 0);
    }

    @Test
    void richesRuneMesoPerDropIsPositive() {
        assertTrue(GameConstants.RICHES_RUNE_MESO_PER_DROP > 0);
    }

    @Test
    void darknessRuneEliteMobsIsPositive() {
        assertTrue(GameConstants.DARKNESS_RUNE_NUMBER_OF_ELITE_MOBS_SPAWNED > 0);
    }

    @Test
    void defaultFieldMobCapacityIsPositive() {
        assertTrue(GameConstants.DEFAULT_FIELD_MOB_CAPACITY > 0);
    }
}
