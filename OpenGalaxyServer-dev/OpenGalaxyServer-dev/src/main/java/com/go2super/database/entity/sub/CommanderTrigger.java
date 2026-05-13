package com.go2super.database.entity.sub;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommanderTrigger implements Serializable {

    private double rate;

    private double accuracy;
    private double dodge;
    private double speed;
    private double electron;

    private double procA;
    private double procB;
    private double procC;
    private double procD;

}
