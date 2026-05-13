package com.go2super.service.battle.calculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipNegation {

    private double absorbGeneral;
    private double absorbExplosive;
    private double absorbMagnetic;
    private double absorbKinetic;
    private double absorbHeat;

    public void add(String damageType, double value) {

        switch (damageType) {
            case "explosive":
                absorbExplosive += value;
                break;
            case "magnetic":
                absorbMagnetic += value;
                break;
            case "kinetic":
                absorbKinetic += value;
                break;
            case "heat":
                absorbHeat += value;
                break;
        }

        absorbGeneral += value;

    }

    public void addGeneral(double value) {

        absorbGeneral += value;
    }

    public void addExplosive(double value) {

        absorbExplosive += value;
    }

    public void addMagnetic(double value) {

        absorbMagnetic += value;
    }

    public void addKinetic(double value) {

        absorbKinetic += value;
    }

    public void addHeat(double value) {

        absorbHeat += value;
    }

}
