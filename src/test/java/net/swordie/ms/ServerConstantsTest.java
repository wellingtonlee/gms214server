package net.swordie.ms;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerConstantsTest {

    @Test
    void versionIs214() {
        assertEquals(214, ServerConstants.VERSION);
    }

    @Test
    void loginPortIsValid() {
        assertTrue(ServerConstants.LOGIN_PORT > 0 && ServerConstants.LOGIN_PORT < 65536);
    }

    @Test
    void apiPortIsValid() {
        assertTrue(ServerConstants.API_PORT > 0 && ServerConstants.API_PORT < 65536);
    }

    @Test
    void channelIpHasFourOctets() {
        assertEquals(4, ServerConstants.CHANNEL_IP.length);
    }

    @Test
    void datDirIsSet() {
        assertNotNull(ServerConstants.DAT_DIR);
        assertFalse(ServerConstants.DAT_DIR.isEmpty());
    }

    @Test
    void wzDirIsSet() {
        assertNotNull(ServerConstants.WZ_DIR);
        assertFalse(ServerConstants.WZ_DIR.isEmpty());
    }
}
