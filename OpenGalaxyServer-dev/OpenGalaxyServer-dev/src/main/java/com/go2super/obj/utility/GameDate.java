package com.go2super.obj.utility;

import lombok.Data;

import java.time.LocalTime;
import java.time.temporal.ChronoField;

@Data
public class GameDate {

    private int seconds;

    public GameDate(int seconds) {

        this.seconds = seconds;
    }

    public GameDate() {

        now();
    }

    public void now() {

        this.seconds = LocalTime.now().get(ChronoField.SECOND_OF_DAY);
    }

    public int getSeconds() {

        return seconds;
    }
}
