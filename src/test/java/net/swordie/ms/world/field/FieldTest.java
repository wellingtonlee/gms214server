package net.swordie.ms.world.field;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class FieldTest {

    @Test
    void fieldConstructorInitializesConcurrentMaps() {
        Field field = new Field(100000000);
        assertNotNull(field.getLifeSchedules());
        assertInstanceOf(ConcurrentHashMap.class, field.getLifeSchedules());
    }

    @Test
    void removeScheduleHandlesMissingLifeGracefully() {
        Field field = new Field(100000000);
        // Should not throw when removing a life that was never scheduled
        assertDoesNotThrow(() -> field.removeSchedule(null, false));
    }

    @Test
    void fieldIdIsSetCorrectly() {
        Field field = new Field(100000000);
        assertEquals(100000000, field.getId());
    }

    @Test
    void lifeSchedulesStartsEmpty() {
        Field field = new Field(100000000);
        assertTrue(field.getLifeSchedules().isEmpty());
    }

    @Test
    void charsListStartsEmpty() {
        Field field = new Field(100000000);
        assertNotNull(field.getChars());
        assertTrue(field.getChars().isEmpty());
    }

    @Test
    void dropManagerIsInitialized() {
        Field field = new Field(100000000);
        assertNotNull(field.getDropManager());
    }

    @Test
    void spawnManagerIsInitialized() {
        Field field = new Field(100000000);
        assertNotNull(field.getSpawnManager());
    }

    @Test
    void effectManagerIsInitialized() {
        Field field = new Field(100000000);
        assertNotNull(field.getEffectManager());
    }
}
