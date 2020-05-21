package server;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
	/**
	 * Format a Date instance to standard time string.
	 *
	 * @param t The Date instance.
	 * @return A standard time string.
	 */
	public static String formatDate(Date t) {
		if (t == null)
			return null;
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(t);
	}

	/**
	 * Get current time in standard time format.
	 *
	 * @return String of current time.
	 */
	public static String getCurrentTime() {
		return formatDate(new Date());
	}
}
