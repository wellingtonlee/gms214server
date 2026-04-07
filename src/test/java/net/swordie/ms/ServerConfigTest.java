package net.swordie.ms;

import net.swordie.ms.enums.WorldId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerConfigTest {

    @Test
    void defaultUserLimitIs20() {
        assertEquals(20, ServerConfig.USER_LIMIT);
    }

    @Test
    void defaultWorldIdIsBera() {
        assertEquals(WorldId.Bera, ServerConfig.WORLD_ID);
    }

    @Test
    void defaultServerNameIsMS() {
        assertEquals("MS", ServerConfig.SERVER_NAME);
    }

    @Test
    void defaultMaxCharactersIs30() {
        assertEquals(30, ServerConfig.MAX_CHARACTERS);
    }

    @Test
    void userLimitIsPositive() {
        assertTrue(ServerConfig.USER_LIMIT > 0);
    }

    @Test
    void maxCharactersIsPositive() {
        assertTrue(ServerConfig.MAX_CHARACTERS > 0);
    }

    @Test
    void serverNameIsNotEmpty() {
        assertNotNull(ServerConfig.SERVER_NAME);
        assertFalse(ServerConfig.SERVER_NAME.isEmpty());
    }
}
