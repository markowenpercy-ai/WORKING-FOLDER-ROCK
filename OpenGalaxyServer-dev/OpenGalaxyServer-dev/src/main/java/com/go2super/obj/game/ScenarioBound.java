package com.go2super.obj.game;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class ScenarioBound {

    private int maximumX;
    private int maximumY;

    private int minimumX;
    private int minimumY;

}
