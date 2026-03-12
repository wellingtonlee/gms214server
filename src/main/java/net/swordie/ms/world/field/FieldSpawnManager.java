package net.swordie.ms.world.field;

import net.swordie.ms.client.character.Char;
import net.swordie.ms.constants.GameConstants;
import net.swordie.ms.constants.MobConstants;
import net.swordie.ms.handlers.EventManager;
import net.swordie.ms.life.Life;
import net.swordie.ms.life.Summon;
import net.swordie.ms.life.Wreckage;
import net.swordie.ms.life.mob.Mob;
import net.swordie.ms.connection.packet.FieldPacket;
import net.swordie.ms.enums.FieldOption;
import net.swordie.ms.loaders.MobData;
import net.swordie.ms.util.Position;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Manages mob/life spawning and respawning for a {@link Field}.
 */
public class FieldSpawnManager {

    private static final Logger log = Logger.getLogger(FieldSpawnManager.class);
    private final Field field;

    public FieldSpawnManager(Field field) {
        this.field = field;
    }

    /**
     * Spawns a summon on the field, removing any existing summon of the same type from the same character.
     */
    public void spawnSummon(Summon summon) {
        Summon oldSummon = (Summon) field.getLifes().values().stream()
                .filter(s -> s instanceof Summon &&
                        ((Summon) s).getChr() == summon.getChr() &&
                        ((Summon) s).getSkillID() == summon.getSkillID())
                .findFirst().orElse(null);
        if (oldSummon != null) {
            field.removeLife(oldSummon.getObjectId(), false);
        }
        spawnLife(summon, null);
    }

    /**
     * Spawns an additional summon without removing the existing one.
     */
    public void spawnAddSummon(Summon summon) {
        spawnLife(summon, null);
    }

    /**
     * Removes a summon by skill ID and character ID.
     */
    public void removeSummon(int skillID, int chrID) {
        Summon summon = (Summon) field.getLifes().values().stream()
                .filter(s -> s instanceof Summon &&
                        ((Summon) s).getChr().getId() == chrID &&
                        ((Summon) s).getSkillID() == skillID)
                .findFirst().orElse(null);
        if (summon != null) {
            field.removeLife(summon.getObjectId(), false);
        }
    }

    /**
     * Spawns a wreckage object on the field.
     */
    public void spawnWreckage(Char chr, Wreckage wreckage) {
        field.addLife(wreckage);
        field.broadcastPacket(FieldPacket.addWreckage(wreckage, field.getWreckageByChrId(chr.getId()).size()));
        EventManager.addEvent(() -> removeWreckage(chr, wreckage), wreckage.getDuration(), TimeUnit.MILLISECONDS);
    }

    /**
     * Removes a single wreckage from the field.
     */
    public void removeWreckage(Char chr, Wreckage wreckage) {
        removeWreckage(chr, Arrays.asList(wreckage));
    }

    /**
     * Removes a list of wreckages from the field.
     */
    public void removeWreckage(Char chr, List<Wreckage> wreckageList) {
        field.broadcastPacket(FieldPacket.delWreckage(chr, wreckageList));
        for (Wreckage wreckage : wreckageList) {
            field.removeLife(wreckage);
        }
    }

    /**
     * Spawns a life on the field, assigning a controller if needed.
     */
    public void spawnLife(Life life, Char onlyChar) {
        field.addLife(life);
        if (field.getChars().size() > 0) {
            Char controller = null;
            if (field.getLifeToControllers().containsKey(life)) {
                controller = field.getLifeToControllers().get(life);
            }
            if (controller == null) {
                field.setRandomController(life);
            }
            life.broadcastSpawnPacket(onlyChar);
        }
    }

    /**
     * Respawns a mob at its home position with full HP/MP.
     */
    public void respawn(Mob mob) {
        mob.setHp(mob.getMaxHp());
        mob.setMp(mob.getMaxMp());
        mob.setPosition(mob.getHomePosition().deepCopy());
        spawnLife(mob, null);
    }

    /**
     * Spawns a mob with a specific appear type and option.
     */
    public Mob spawnMobWithAppearType(int id, int x, int y, int appearType, int option) {
        Mob mob = MobData.getMobDeepCopyById(id);
        Position pos = new Position(x, y);
        mob.setPosition(pos.deepCopy());
        mob.setPrevPos(pos.deepCopy());
        mob.setPosition(pos.deepCopy());
        mob.setNotRespawnable(true);
        mob.setAppearType((byte) appearType);
        mob.setOption(option);
        if (mob.getField() == null) {
            mob.setField(field);
        }
        spawnLife(mob, null);
        return mob;
    }

    /**
     * Spawns a mob at a given position.
     */
    public Mob spawnMob(int id, int x, int y, boolean respawnable, long hp) {
        Mob mob = MobData.getMobDeepCopyById(id);
        Position pos = new Position(x, y);
        mob.setPosition(pos.deepCopy());
        mob.setPrevPos(pos.deepCopy());
        mob.setPosition(pos.deepCopy());
        mob.setNotRespawnable(!respawnable);
        if (hp > 0) {
            mob.setHp(hp);
            mob.setMaxHp(hp);
        }
        if (mob.getField() == null) {
            mob.setField(field);
        }
        Foothold fh = field.findFootHoldBelow(pos);
        mob.setCurFoodhold(fh);
        mob.setHomeFoothold(fh);
        spawnLife(mob, null);
        if (MobConstants.isTimedDropMob(mob.getTemplateId())) {
            mob.startDropItemSchedule();
        }
        return mob;
    }

    /**
     * Generates mobs from MobGens on the field, respecting mob capacity limits.
     */
    public void generateMobs(boolean init) {
        if (init || field.getChars().size() > 0) {
            boolean buffed = (field.getChannel() >= GameConstants.BUFFED_CH_ST && field.getChannel() <= GameConstants.BUFFED_CH_END);
            int currentMobs = field.getMobs().size();
            List<MobGen> shuffledMobs = new ArrayList<>(field.getMobGens());
            Collections.shuffle(shuffledMobs);
            for (MobGen mg : shuffledMobs) {
                if (mg.canSpawnOnField(field)) {
                    mg.spawnMob(field, buffed);
                    currentMobs++;
                    if ((field.getFieldLimit() & FieldOption.NoMobCapacityLimit.getVal()) == 0
                            && currentMobs > field.getFixedMobCapacity()) {
                        break;
                    }
                }
            }
        }
        double kishinMultiplier = field.hasKishin() ? GameConstants.KISHIN_MOB_RATE_MULTIPLIER : 1;
        EventManager.addEvent(() -> generateMobs(false),
                (long) (GameConstants.BASE_MOB_RESPAWN_RATE / (field.getMobRate() * kishinMultiplier)));
    }
}
