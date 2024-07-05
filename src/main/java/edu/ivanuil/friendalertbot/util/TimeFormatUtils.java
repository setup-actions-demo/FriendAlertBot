package edu.ivanuil.friendalertbot.util;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

public class TimeFormatUtils {

    public static String formatInterval(final long l) {
        long hr = TimeUnit.MILLISECONDS.toHours(l);
        long min = TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr));
        long sec = TimeUnit.MILLISECONDS.toSeconds(
                l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        long ms = TimeUnit.MILLISECONDS.toMillis(
                l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min)
                        - TimeUnit.SECONDS.toMillis(sec));
        return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
    }

    public static String formatInterval(final Timestamp end, final Timestamp start) {
        return formatInterval(end.getTime() - start.getTime());
    }

    public static String formatIntervalFromNow(final Timestamp start) {
        return formatInterval(new Timestamp(System.currentTimeMillis()), start);
    }

}
