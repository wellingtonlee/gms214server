package net.swordie.ms;

import net.swordie.ms.constants.JobConstants;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created on 2/18/2017.
 */
public class ServerConstants {
	public static final String DIR = System.getenv().getOrDefault("SERVER_DIR", System.getProperty("user.dir"));
	public static final byte LOCALE = 8;
	public static final String WZ_DIR = DIR + "/wz";
	public static final String DAT_DIR = DIR + "/dat";
	public static final int MAX_CHARACTERS = JobConstants.LoginJob.values().length * 3;
	public static final String SCRIPT_DIR = DIR + "/scripts";
	public static final String RESOURCES_DIR = DIR + "/resources";
	public static final String HANDLERS_DIR = DIR + "/src/main/java/net/swordie/ms/handlers";
	public static final short VERSION = 214;
	public static final String MINOR_VERSION = "1";
	public static final int LOGIN_PORT = Integer.parseInt(System.getenv().getOrDefault("LOGIN_PORT", "8484"));
	public static final int API_PORT = Integer.parseInt(System.getenv().getOrDefault("API_PORT", "8483"));
	public static final byte[] CHANNEL_IP = parseChannelIp();
	public static final short CHAT_PORT = 0;
	public static final int BCRYPT_ITERATIONS = 10;
	public static final long TOKEN_EXPIRY_TIME = 60 * 24; // minutes
	public static boolean LOCAL_HOST_SERVER = false;
	public static final int RESTART_MINUTES = (int) getTimeTillMidnight();
	public static final boolean DAILY_RESTART = false;

	private static byte[] parseChannelIp() {
		String env = System.getenv("CHANNEL_IP");
		if (env == null || env.isEmpty()) {
			return new byte[]{54, 68, (byte) 160, (byte) 34};
		}
		String[] parts = env.split("\\.");
		return new byte[]{
			(byte) Integer.parseInt(parts[0]),
			(byte) Integer.parseInt(parts[1]),
			(byte) Integer.parseInt(parts[2]),
			(byte) Integer.parseInt(parts[3])
		};
	}

	public static long getTimeTillMidnight() {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return TimeUnit.MILLISECONDS.toMinutes(c.getTimeInMillis() - System.currentTimeMillis());
	}

}
