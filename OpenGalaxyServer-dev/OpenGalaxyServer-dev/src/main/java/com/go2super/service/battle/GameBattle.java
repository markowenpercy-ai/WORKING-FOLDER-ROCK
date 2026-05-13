package com.go2super.service.battle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameBattle {

    private MatchRunnable runnable;
    private Thread thread;

    public Match getMatch() {

        return runnable.getMatch();
    }

}
