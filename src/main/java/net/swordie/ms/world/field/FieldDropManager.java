package net.swordie.ms.world.field;

import net.swordie.ms.client.character.Char;
import net.swordie.ms.client.character.items.Item;
import net.swordie.ms.connection.packet.DropPool;
import net.swordie.ms.constants.GameConstants;
import net.swordie.ms.constants.ItemConstants;
import net.swordie.ms.handlers.EventManager;
import net.swordie.ms.life.drop.Drop;
import net.swordie.ms.life.drop.DropInfo;
import net.swordie.ms.loaders.ItemData;
import net.swordie.ms.loaders.containerclasses.ItemInfo;
import net.swordie.ms.util.FileTime;
import net.swordie.ms.util.Position;
import org.apache.log4j.Logger;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Manages drop-related operations for a {@link Field}, including creating, spawning,
 * and removing drops from the field.
 */
public class FieldDropManager {

    private static final Logger log = Logger.getLogger(FieldDropManager.class);
    private final Field field;

    public FieldDropManager(Field field) {
        this.field = field;
    }

    /**
     * Drops an item to this map, given a {@link Drop}, a starting Position and an ending Position.
     * Immediately broadcasts the drop packet.
     *
     * @param drop              The Drop to drop.
     * @param posFrom           The Position that the drop starts off from.
     * @param posTo             The Position where the drop lands.
     * @param ignoreTradability if the drop should ignore tradability (i.e., untradable items won't disappear)
     */
    public void drop(Drop drop, Position posFrom, Position posTo, boolean ignoreTradability) {
        boolean isTradable = true;
        Item item = drop.getItem();
        if (item != null) {
            ItemInfo itemInfo = ItemData.getItemInfoByID(item.getItemId());
            isTradable = ignoreTradability || (item.isTradable() && (ItemConstants.isEquip(item.getItemId()) || itemInfo != null && !itemInfo.isQuest()));
        }
        drop.setPosition(posTo);
        if (isTradable) {
            field.addLife(drop);
            field.getLifeSchedules().put(drop,
                    EventManager.addEvent(() -> field.removeDrop(drop.getObjectId(), 0, true, -1),
                            GameConstants.DROP_REMAIN_ON_GROUND_TIME, TimeUnit.SECONDS));
        } else {
            drop.setObjectId(field.getNewObjectID());
        }
        if (drop.getItem() != null && ItemConstants.isCollisionLootItem(drop.getItem().getItemId())) {
            field.broadcastPacket(DropPool.dropEnterFieldCollisionPickUp(drop, posFrom, 0));
        } else {
            for (Char chr : field.getChars()) {
                if (!chr.getClient().getWorld().isReboot() || drop.canBePickedUpBy(chr)) {
                    field.broadcastPacket(DropPool.dropEnterField(drop, posFrom, posTo, 0, drop.canBePickedUpBy(chr)));
                }
            }
        }
    }

    /**
     * Drops a {@link Drop} according to a given {@link DropInfo DropInfo}'s specification.
     *
     * @param dropInfo  The DropInfo specifying what to drop.
     * @param posFrom   The Position that the drop starts off from.
     * @param posTo     The Position where the drop lands.
     * @param ownerID   The owner's character ID.
     * @param explosive Whether this is an explosive drop.
     */
    public void drop(DropInfo dropInfo, Position posFrom, Position posTo, int ownerID, boolean explosive) {
        int itemID = dropInfo.getItemID();
        Item item;
        Drop drop = new Drop(-1);
        drop.setPosition(posTo);
        drop.setOwnerID(ownerID);
        drop.setExplosiveDrop(explosive);
        Set<Integer> quests = new HashSet<>();
        if (itemID != 0) {
            item = ItemData.getItemDeepCopy(itemID, true);
            if (item != null) {
                item.setQuantity(dropInfo.getQuantity());
                drop.setItem(item);
                ItemInfo ii = ItemData.getItemInfoByID(itemID);
                if (ii != null && ii.isQuest()) {
                    quests = ii.getQuestIDs();
                }
            } else {
                log.error("Was not able to find the item to drop! id = " + itemID);
                return;
            }
        } else {
            drop.setMoney(dropInfo.getMoney());
        }
        field.addLife(drop);
        drop.setExpireTime(FileTime.fromDate(LocalDateTime.now().plusSeconds(GameConstants.DROP_REMOVE_OWNERSHIP_TIME)));
        field.getLifeSchedules().put(drop,
                EventManager.addEvent(() -> field.removeDrop(drop.getObjectId(), 0, true, -1),
                        GameConstants.DROP_REMAIN_ON_GROUND_TIME, TimeUnit.SECONDS));
        EventManager.addEvent(() -> drop.setOwnerID(0), GameConstants.DROP_REMOVE_OWNERSHIP_TIME, TimeUnit.SECONDS);
        for (Char chr : field.getChars()) {
            if (chr.hasAnyQuestsInProgress(quests)) {
                field.broadcastPacket(DropPool.dropEnterField(drop, posFrom, posTo, ownerID, drop.canBePickedUpBy(chr)));
            }
        }
    }

    /**
     * Drops a Set of {@link DropInfo}s from a base Position.
     *
     * @param dropInfos The Set of DropInfos.
     * @param position  The Position the initial Drop comes from.
     * @param ownerID   The owner's character ID.
     */
    public void drop(Set<DropInfo> dropInfos, Position position, int ownerID) {
        drop(dropInfos, field.findFootHoldBelow(position), position, ownerID, 0, 0, false);
    }

    /**
     * Drops a {@link Drop} at a given Position. Calculates the Position that the Drop should land at.
     *
     * @param drop        The Drop that should be dropped.
     * @param position    The Position it is dropped from.
     * @param fromReactor if it is a quest item the item will disappear.
     */
    public void drop(Drop drop, Position position, boolean fromReactor) {
        int x = position.getX();
        Foothold fh = field.findFootHoldBelow(position);
        Position posTo = fh != null ? new Position(x, fh.getYFromX(x)) : position.deepCopy();
        drop(drop, position, posTo, fromReactor);
    }

    /**
     * Drops a Set of {@link DropInfo}s, locked to a specific {@link Foothold}.
     * Not all drops are guaranteed to be dropped, as this method calculates whether or not a Drop should drop,
     * according to the DropInfo's prop chance.
     *
     * @param dropInfos The Set of DropInfos that should be dropped.
     * @param fh        The Foothold this Set of DropInfos is bound to.
     * @param position  The Position the Drops originate from.
     * @param ownerID   The ID of the owner of all drops.
     * @param mesoRate  The added meso rate of the character.
     * @param dropRate  The added drop rate of the character.
     * @param explosive Whether the drops are explosive.
     */
    public void drop(Set<DropInfo> dropInfos, Foothold fh, Position position, int ownerID, int mesoRate, int dropRate, boolean explosive) {
        int x = position.getX();
        int minX = fh == null ? position.getX() : fh.getX1();
        int maxX = fh == null ? position.getX() : fh.getX2();
        int diff = 0;
        for (DropInfo dropInfo : dropInfos) {
            if (dropInfo.willDrop(dropRate)) {
                x = (x + diff) > maxX ? maxX - 10 : (x + diff) < minX ? minX + 10 : x + diff;
                Position posTo;
                if (fh == null) {
                    posTo = position.deepCopy();
                } else {
                    posTo = new Position(x, fh.getYFromX(x));
                }
                DropInfo copy = null;
                if (dropInfo.isMoney()) {
                    copy = dropInfo.deepCopy();
                    copy.setMoney((int) (dropInfo.getMoney() * (mesoRate / 100D)));
                }
                drop(copy != null ? copy : dropInfo, position, posTo, ownerID, explosive);
                diff = diff < 0 ? Math.abs(diff - GameConstants.DROP_DIFF) : -(diff + GameConstants.DROP_DIFF);
                dropInfo.generateNextDrop();
            }
        }
    }

    /**
     * Removes all drops from the field.
     */
    public void clearDrops() {
        Set<Drop> fieldDrops = new HashSet<>(field.getDrops());
        for (Drop drop : fieldDrops) {
            field.removeDrop(drop.getObjectId(), 0, false, 0);
        }
    }

    /**
     * Drops an item at a specific location.
     */
    public void dropItem(int itemId, int startPosX, int startPosY, int endPosX, int endPosY) {
        Drop drop = new Drop(field.getNewObjectID());
        drop.setItem(ItemData.getItemDeepCopy(itemId));
        Position startPos = new Position(startPosX, startPosY);
        Position endPos = new Position(endPosX, endPosY);
        drop(drop, startPos, endPos, true);
    }

    /**
     * Drops mesos at a specific location.
     */
    public void dropMeso(int mesoAmount, int startPosX, int startPosY, int endPosX, int endPosY) {
        Drop drop = new Drop(field.getNewObjectID(), mesoAmount);
        Position startPos = new Position(startPosX, startPosY);
        Position endPos = new Position(endPosX, endPosY);
        drop(drop, startPos, endPos, true);
    }

    /**
     * Drops items along a line from a start position.
     */
    public void dropItemsAlongLine(int[] items, int range, int startPosX, int startPosY, long msDelay) {
        if (items.length <= 0) {
            return;
        }
        var lrFh = field.getMinMaxNonWallFH();

        range = Math.max(range, items.length);
        int offset = Math.max((range / items.length) * 2, 3);
        for (int i = 0; i < items.length; i++) {
            int endPosX = startPosX - range + (offset * i);
            endPosX = Math.max(endPosX, lrFh.getLeft().getX1());
            endPosX = Math.min(endPosX, lrFh.getRight().getX1());

            if (items[i] <= 0) {
                continue;
            }

            if (items[i] > 999999) {
                dropItem(items[i], startPosX, startPosY, endPosX, startPosY);
            } else {
                dropMeso(items[i], startPosX, startPosY, endPosX, startPosY);
            }
        }
    }
}
