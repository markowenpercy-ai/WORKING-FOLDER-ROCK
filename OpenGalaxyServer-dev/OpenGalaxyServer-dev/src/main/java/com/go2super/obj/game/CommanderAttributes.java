package com.go2super.obj.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommanderAttributes {

    private int aim;
    private int electron;
    private int dodge;
    private int speed;

}
