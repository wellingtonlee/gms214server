package net.swordie.ms.world.field;

import net.swordie.ms.client.character.Char;
import net.swordie.ms.client.character.runestones.RuneStone;
import net.swordie.ms.client.Client;
import net.swordie.ms.connection.packet.FieldPacket;
import net.swordie.ms.connection.packet.UserPacket;
import net.swordie.ms.constants.GameConstants;
import net.swordie.ms.enums.ObtacleAtomEnum;
import net.swordie.ms.enums.TextEffectType;
import net.swordie.ms.handlers.EventManager;
import net.swordie.ms.life.mob.Mob;
import net.swordie.ms.util.Position;
import net.swordie.ms.util.Util;
import net.swordie.ms.world.field.bosses.gollux.FallingCatcher;
import net.swordie.ms.world.field.fieldeffect.Effect;
import net.swordie.ms.world.field.obtacleatom.ObtacleAtomCreateType;
import net.swordie.ms.world.field.obtacleatom.ObtacleAtomInfo;
import net.swordie.ms.world.field.obtacleatom.ObtacleInRowInfo;
import net.swordie.ms.world.field.obtacleatom.ObtacleRadianInfo;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Manages field effects including weather, obstacle atoms, rune stones,
 * burning field mechanics, and Zakum-specific field operations for a {@link Field}.
 */
public class FieldEffectManager {

    private static final Logger log = Logger.getLogger(FieldEffectManager.class);
    private final Field field;

    private ScheduledFuture<?> runeStoneHordesTimer;

    public FieldEffectManager(Field field) {
        this.field = field;
    }

    /**
     * Activates a weather effect on the field.
     */
    public void blowWeather(int itemID, String message, int seconds, byte[] packedAvatarLook) {
        field.broadcastPacket(FieldPacket.removeBlowWeather());
        field.setWeatherItemID(itemID);
        field.setWeatherMsg(message);
        field.setWeatherEndTime(System.currentTimeMillis() + 1000L * seconds);
        field.setWeatherAvatarLook(packedAvatarLook);
        field.broadcastPacket(FieldPacket.blowWeather(itemID, message, seconds, packedAvatarLook));
    }

    /**
     * Handles rune stone usage.
     */
    public void useRuneStone(Client c, RuneStone runeStone) {
        Char chr = c.getChr();
        chr.write(FieldPacket.runeActSuccess());
        field.broadcastPacket(FieldPacket.runeStoneDisappear(chr.getId()));
        chr.write(FieldPacket.runeStoneSkillAck(runeStone.getRuneType()));
        field.setRuneStone(null);
        field.setLastRuneUsedTime(System.currentTimeMillis());
    }

    /**
     * Temporarily increases mob spawn rate after rune activation.
     */
    public void runeStoneHordeEffect(int mobRateMultiplier, int duration) {
        double prevMobRate = field.getMobRate();
        field.setMobRate(field.getMobRate() * mobRateMultiplier);
        if (runeStoneHordesTimer != null && !runeStoneHordesTimer.isDone()) {
            runeStoneHordesTimer.cancel(true);
        }
        runeStoneHordesTimer = EventManager.addEvent(() -> field.setMobRate(prevMobRate), duration, TimeUnit.SECONDS);
    }

    /**
     * Returns bonus EXP percentage from burning field level.
     */
    public int getBonusExpByBurningFieldLevel() {
        return field.getBurningFieldLevel() * GameConstants.BURNING_FIELD_BONUS_EXP_MULTIPLIER_PER_LEVEL;
    }

    /**
     * Displays the current burning field level to all characters.
     */
    public void showBurningLevel() {
        String string = "#fn ExtraBold##fs26#          Burning Field has been destroyed.          ";
        if (getBonusExpByBurningFieldLevel() > 0) {
            string = "#fn ExtraBold##fs26#          Burning Stage " + field.getBurningFieldLevel() + ": " + getBonusExpByBurningFieldLevel() + "% Bonus EXP!          ";
        }
        Effect effect = Effect.createFieldTextEffect(string, 50, 2000, 4,
                new Position(0, -200), 1, 4, TextEffectType.BurningField, 0, 0);
        field.broadcastPacket(UserPacket.effect(effect));
    }

    /**
     * Starts the burning field timer for eligible fields.
     */
    public void startBurningFieldTimer() {
        if (field.getMobGens().size() > 0
                && field.getMobs().stream().mapToInt(m -> m.getForcedMobStat().getLevel()).min().orElse(0) >= GameConstants.BURNING_FIELD_MIN_MOB_LEVEL) {
            field.setBurningFieldLevel(GameConstants.BURNING_FIELD_LEVEL_ON_START);
            EventManager.addFixedRateEvent(this::changeBurningLevel, 0, GameConstants.BURNING_FIELD_TIMER, TimeUnit.MINUTES);
        }
    }

    /**
     * Updates burning field level based on player presence.
     */
    public void changeBurningLevel() {
        boolean showMessage = true;

        if (field.getBurningFieldLevel() <= 0) {
            showMessage = false;
        }

        if (field.getChars().size() > 0 && field.getBurningFieldLevel() > 0) {
            field.setBurningFieldLevel(field.getBurningFieldLevel() - 1);
        } else if (field.getChars().size() <= 0 && field.getBurningFieldLevel() < GameConstants.BURNING_FIELD_MAX_LEVEL) {
            field.setBurningFieldLevel(field.getBurningFieldLevel() + 1);
            showMessage = true;
        }

        if (showMessage) {
            showBurningLevel();
        }
    }

    // --- Obstacle Atom methods ---

    /**
     * Clears all obstacle atoms from the field.
     */
    public void clearObtacle() {
        field.broadcastPacket(FieldPacket.clearObtacle());
    }

    /**
     * Creates obstacle atoms falling from the top of the field.
     */
    public void createObstacleAtom(ObtacleAtomEnum oae, int key, int damage, int velocity, int amount, int proc) {
        createObstacleAtom(oae, key, damage, velocity, 0, amount, proc);
    }

    /**
     * Creates obstacle atoms with a specific angle.
     */
    public void createObstacleAtom(ObtacleAtomEnum oae, int key, int damage, int velocity, int angle, int amount, int proc) {
        int xLeft = field.getVrLeft();
        int yTop = field.getVrTop();

        ObtacleInRowInfo obtacleInRowInfo = new ObtacleInRowInfo(4, false, 5000, 0, 0, 0);
        ObtacleRadianInfo obtacleRadianInfo = new ObtacleRadianInfo(4, 0, 0, 0, 0);
        Set<ObtacleAtomInfo> obtacleAtomInfosSet = new HashSet<>();

        for (int i = 0; i < amount; i++) {
            if (Util.succeedProp(proc)) {
                int randomX = new Random().nextInt(field.getWidth()) + xLeft;
                Position position = new Position(randomX, yTop);
                Foothold foothold = field.findFootHoldBelow(position);
                if (foothold != null) {
                    int footholdY = foothold.getYFromX(position.getX());
                    int height = Math.abs(position.getY() - footholdY);

                    obtacleAtomInfosSet.add(new ObtacleAtomInfo(oae.getType(), key, position, new Position(), oae.getHitBox(),
                            damage, 0, 0, height, 0, velocity, height, angle));
                }
            }
        }

        field.broadcastPacket(FieldPacket.createObtacle(ObtacleAtomCreateType.NORMAL, obtacleInRowInfo, obtacleRadianInfo, obtacleAtomInfosSet));
    }

    /**
     * Creates obstacle atoms that fall to the lowest foothold.
     */
    public void createObstacleAtomLowestEndPoint(ObtacleAtomEnum oae, int key, int damage, int velocity, int angle, int amount, int proc) {
        int xLeft = field.getVrLeft();
        int yTop = field.getVrTop();

        ObtacleInRowInfo obtacleInRowInfo = new ObtacleInRowInfo(4, false, 5000, 0, 0, 0);
        ObtacleRadianInfo obtacleRadianInfo = new ObtacleRadianInfo(4, 0, 0, 0, 0);
        Set<ObtacleAtomInfo> obtacleAtomInfosSet = new HashSet<>();

        for (int i = 0; i < amount; i++) {
            if (Util.succeedProp(proc)) {
                int randomX = new Random().nextInt(field.getWidth()) + xLeft;
                Position position = new Position(randomX, yTop);
                Foothold foothold = field.findLowestFootHoldBelow(position);
                if (foothold != null) {
                    int footholdY = foothold.getYFromX(position.getX());
                    int height = Math.abs(position.getY() - footholdY);

                    obtacleAtomInfosSet.add(new ObtacleAtomInfo(oae.getType(), key, position, new Position(), oae.getHitBox(),
                            damage, 0, 0, height, 0, velocity, height, angle));
                }
            }
        }

        field.broadcastPacket(FieldPacket.createObtacle(ObtacleAtomCreateType.NORMAL, obtacleInRowInfo, obtacleRadianInfo, obtacleAtomInfosSet));
    }

    // --- Falling Catcher methods ---

    /**
     * Creates a falling catcher at specific coordinates.
     */
    public void createFallingCatcherAtCoords(String name, int index, int x, int y) {
        ArrayList<Position> positions = new ArrayList<>();
        positions.add(new Position(x, y));
        field.broadcastPacket(FieldPacket.createFallingCatcher(new FallingCatcher(name, index, positions)));
    }

    /**
     * Creates a falling catcher at a character's position.
     */
    public void createFallingCatcherOnCharacter(Char chr, String name, int index) {
        ArrayList<Position> positions = new ArrayList<>();
        positions.add(chr.getPosition());
        chr.getField().broadcastPacket(FieldPacket.createFallingCatcher(new FallingCatcher(name, index, positions)));
    }

    // --- Zakum-specific methods ---

    /**
     * Toggles Zakum platform visibility.
     */
    public void toggleZakumPlatforms(boolean visible) {
        String[] platformNames = new String[]{
                "zdc1", "zdc2", "zdc3", "zdc4", "zdc5",
                "zdc6", "zdc7", "zdc8", "zdc9", "zdc10",
                "zdc11", "zdc12", "zdc13", "zdc14", "zdc15",
                "zdc16", "zdc17", "zdc18"
        };
        Position[] platformPositions = new Position[]{
                new Position(-464, -186), new Position(-388, -187), new Position(-310, -184), new Position(-514, -102), new Position(-439, -101),
                new Position(-362, -99), new Position(-512, -7), new Position(-436, -5), new Position(-358, -8), new Position(350, -189),
                new Position(426, -190), new Position(504, -187), new Position(384, -99), new Position(464, -102), new Position(546, -101),
                new Position(363, -7), new Position(439, -5), new Position(517, -8)
        };
        field.broadcastPacket(FieldPacket.footholdAppear(platformNames, visible, platformPositions));
        field.setZakPlatformsVisible(visible);
    }
}
