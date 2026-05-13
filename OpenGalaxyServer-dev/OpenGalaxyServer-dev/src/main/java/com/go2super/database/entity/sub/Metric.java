package com.go2super.database.entity.sub;

import lombok.*;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class Metric {

    private String identifier;
    private int value;

    public void add(int amount) {

        value += amount;
    }

    public void sub(int amount) {

        value -= amount;
    }

    public void reset() {

        value = 0;
    }

}
