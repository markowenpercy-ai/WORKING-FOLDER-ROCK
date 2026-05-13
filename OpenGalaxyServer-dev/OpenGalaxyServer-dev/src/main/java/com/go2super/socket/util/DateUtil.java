package com.go2super.socket.util;

import java.util.*;
import java.util.concurrent.*;

public class DateUtil {

    public static Long secondsFromOrigin(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(1969, 11, 31, 19, 0, 0);

        long millis = Long.valueOf(date.getTime() - calendar.getTime().getTime());
        return TimeUnit.MILLISECONDS.toSeconds(millis);
    }

    public static Long seconds(Date date) {

        long millis = Long.valueOf(now().getTime() - date.getTime());
        return TimeUnit.MILLISECONDS.toSeconds(millis);
    }

    public static Long remains(Date date) {

        long millis = Long.valueOf(date.getTime() - now().getTime());
        return TimeUnit.MILLISECONDS.toSeconds(millis);
    }

    public static Long remainsMillis(Date date) {

        return Long.valueOf(date.getTime() - now().getTime());
    }

    public static Date offsetMillis(Long offsetMillis) {

        Calendar calendar = calendar();
        calendar.add(Calendar.MILLISECOND, offsetMillis.intValue());
        return calendar.getTime();
    }

    public static Date nowMillis(int offsetMillis) {

        Calendar calendar = calendar();
        calendar.add(Calendar.MILLISECOND, offsetMillis);
        return calendar.getTime();
    }

    public static Date offsetYears(int offsetYears) {

        Calendar calendar = calendar();
        calendar.add(Calendar.YEAR, offsetYears);
        return calendar.getTime();
    }

    public static Date now(int offsetSeconds) {

        Calendar calendar = calendar();
        calendar.add(Calendar.SECOND, offsetSeconds);
        return calendar.getTime();
    }

    public static Date offset(Date date, int offsetSeconds) {

        Calendar calendar = calendar();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, offsetSeconds);
        return calendar.getTime();
    }

    public static boolean currentDay(Date date) {

        return sameDay(now(), date);
    }

    public static boolean sameDay(Date date1, Date date2) {

        Calendar now = calendar(date1);
        Calendar check = calendar(date2);
        return now.get(Calendar.DAY_OF_YEAR) == check.get(Calendar.DAY_OF_YEAR) &&
               now.get(Calendar.YEAR) == check.get(Calendar.YEAR);
    }

    public static Calendar calendar(Date date) {

        Calendar result = calendar();
        result.setTime(date);
        return result;
    }

    public static Calendar calendar() {

        return Calendar.getInstance();
    }

    public static Date date(long millis) {

        Date result = now();
        result.setTime(millis);
        return result;
    }

    public static Date now() {

        return new Date();
    }

    public static long millis() {

        return new Date().getTime();
    }

    public static boolean isMonday() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY;
    }

}
