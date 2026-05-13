package com.go2super.database.entity.sub;

import lombok.*;


@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class UserLeagueStats {

    private int wins;
    private int losses;
    private int draws;
    private int league;
}
