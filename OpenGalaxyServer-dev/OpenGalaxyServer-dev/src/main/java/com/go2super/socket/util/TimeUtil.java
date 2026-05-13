package com.go2super.socket.util;

import java.util.concurrent.*;

public class TimeUtil {

    public static String formatSeconds(int timeInSeconds) {

        return formatSeconds(timeInSeconds, ";");
    }

    public static String formatSeconds(int timeInSeconds, String separator) {

        int secondsLeft = timeInSeconds % 3600 % 60;
        int minutes = (int) Math.floor(timeInSeconds % 3600 / 60);
        int hours = (int) Math.floor(timeInSeconds / 3600);
        String HH = ((hours < 10) ? "0" : "") + hours;
        String MM = ((minutes < 10) ? "0" : "") + minutes;
        String SS = ((secondsLeft < 10) ? "0" : "") + secondsLeft;
        return HH + separator + MM + separator + SS;
    }

    public static String humanReadable(int timeInSeconds) {

        long days = TimeUnit.SECONDS.toDays(timeInSeconds);
        timeInSeconds -= TimeUnit.DAYS.toSeconds(days);

        long hours = TimeUnit.SECONDS.toHours(timeInSeconds);
        timeInSeconds -= TimeUnit.HOURS.toSeconds(hours);

        long minutes = TimeUnit.SECONDS.toMinutes(timeInSeconds);
        timeInSeconds -= TimeUnit.MINUTES.toSeconds(minutes);

        long seconds = TimeUnit.SECONDS.toSeconds(timeInSeconds);

        StringBuilder msg = new StringBuilder();
        if (days != 0) {
            msg.append(days + " day(s) ");
        }
        if (hours != 0) {
            msg.append(hours + " hours(s) ");
        }
        if (minutes != 0) {
            msg.append(minutes + " minutes(s) ");
        }
        if (seconds != 0) {
            msg.append(seconds + " seconds(s) ");
        }

        return msg.toString();

    }

}
