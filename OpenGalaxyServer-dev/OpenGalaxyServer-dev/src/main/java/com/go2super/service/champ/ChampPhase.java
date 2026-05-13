package com.go2super.service.champ;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public record ChampPhase(
        DayOfWeek registrationDayStart,
        LocalTime registrationTimeStart,

        DayOfWeek registrationDayEnd,
        LocalTime registrationTimeEnd,

        DayOfWeek matchDay,

        LocalTime matchStart,
        LocalTime matchEnd,

        Phase phase,

        int maxUserCount

) {

    public enum Phase {
        QUALIFICATION,
        FINAL
    }


    public boolean isRegistrationTime() {
        DayOfWeek nowDay = DayOfWeek.from(LocalDate.now());
        LocalTime nowTime = LocalTime.now();
        boolean isNowAfterOrEqualStartDayAndTime =
                (nowDay.compareTo(registrationDayStart) > 0 ||
                        (nowDay.equals(registrationDayStart) && nowTime.isAfter(registrationTimeStart)));

        boolean isNowBeforeOrEqualEndDayAndTime =
                (nowDay.compareTo(registrationDayEnd) < 0 ||
                        (nowDay.equals(registrationDayEnd) && nowTime.isBefore(registrationTimeEnd)));

        return isNowAfterOrEqualStartDayAndTime && isNowBeforeOrEqualEndDayAndTime;
    }

    public boolean isMatchTime() {
        DayOfWeek nowDay = DayOfWeek.from(LocalDate.now());
        LocalTime nowTime = LocalTime.now();
        return nowDay.compareTo(matchDay) == 0
                && !nowTime.isBefore(matchStart)
                && !nowTime.isAfter(matchEnd);
    }

    public boolean isActive() {
        return isRegistrationTime() || isMatchTime();
    }


}