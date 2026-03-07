package net.swordie.ms.life.drop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DropInfoTest {

    @Test
    void deepCopyProducesIndependentInstance() {
        DropInfo original = new DropInfo(1002357, 500, 1, 5);
        DropInfo copy = original.deepCopy();

        assertEquals(original.getItemID(), copy.getItemID());
        assertEquals(original.getChance(), copy.getChance());
        assertEquals(original.getMinQuant(), copy.getMinQuant());
        assertEquals(original.getMaxQuant(), copy.getMaxQuant());

        copy.setItemID(9999999);
        assertNotEquals(original.getItemID(), copy.getItemID());
    }

    @Test
    void moneyDropCopyIsIndependent() {
        DropInfo original = new DropInfo(800, 100, 500);
        DropInfo copy = original.deepCopy();

        assertEquals(original.getMinMoney(), copy.getMinMoney());
        assertEquals(original.getMaxMoney(), copy.getMaxMoney());

        copy.setMinMoney(0);
        assertNotEquals(original.getMinMoney(), copy.getMinMoney());
    }

    @Test
    void isMoneyReturnsTrueForMoneyDrops() {
        DropInfo moneyDrop = new DropInfo(800, 100, 500);
        assertTrue(moneyDrop.isMoney());

        DropInfo itemDrop = new DropInfo(1002357, 500);
        assertFalse(itemDrop.isMoney());
    }

    @Test
    void generateNextDropSetsQuantityWithinBounds() {
        DropInfo drop = new DropInfo(1002357, 1000, 3, 10);
        for (int i = 0; i < 100; i++) {
            drop.generateNextDrop();
            assertTrue(drop.getQuantity() >= 3, "Quantity below min: " + drop.getQuantity());
            assertTrue(drop.getQuantity() <= 10, "Quantity above max: " + drop.getQuantity());
        }
    }

    @Test
    void generateNextDropSetsMoneyWithinBounds() {
        DropInfo drop = new DropInfo(1000, 50, 200);
        for (int i = 0; i < 100; i++) {
            drop.generateNextDrop();
            assertTrue(drop.getMoney() >= 50, "Money below min: " + drop.getMoney());
            assertTrue(drop.getMoney() <= 200, "Money above max: " + drop.getMoney());
        }
    }

    @Test
    void toStringFormatsItemDrop() {
        DropInfo drop = new DropInfo(1002357, 500);
        String str = drop.toString();
        assertTrue(str.contains("1002357"));
        assertTrue(str.contains("500"));
    }

    @Test
    void toStringFormatsMoneyDrop() {
        DropInfo drop = new DropInfo(1000, 100, 500);
        String str = drop.toString();
        assertTrue(str.contains("mesos"));
    }

    @Test
    void defaultQuantityIsOne() {
        DropInfo drop = new DropInfo(1002357, 500);
        assertEquals(1, drop.getQuantity());
        assertEquals(1, drop.getMinQuant());
        assertEquals(1, drop.getMaxQuant());
    }
}
