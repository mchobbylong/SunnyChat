package server;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
	public static String formatDate(Date t) {
		if (t == null)
			return null;
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(t);
	}

	public static String getCurrentTime() {
		return formatDate(new Date());
	}
}
