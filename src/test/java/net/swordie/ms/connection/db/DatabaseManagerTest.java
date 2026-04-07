package net.swordie.ms.connection.db;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {

    @Test
    void getSessionReturnsNullBeforeInit() {
        // Before init() is called, sessionFactory is null, so getSession() should return null
        assertNull(DatabaseManager.getSession());
    }

    @Test
    void getObjFromDBHandlesNullSessionGracefully() {
        // When session factory is not initialized, getSession returns null
        // This should result in a NullPointerException from try-with-resources
        assertThrows(NullPointerException.class, () -> {
            DatabaseManager.getObjFromDB(Object.class, 1);
        });
    }

    @Test
    void saveToDBHandlesNullSessionGracefully() {
        assertThrows(NullPointerException.class, () -> {
            DatabaseManager.saveToDB(new Object());
        });
    }

    @Test
    void deleteFromDBHandlesNullSessionGracefully() {
        assertThrows(NullPointerException.class, () -> {
            DatabaseManager.deleteFromDB(new Object());
        });
    }
}
