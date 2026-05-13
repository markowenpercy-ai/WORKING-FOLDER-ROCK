package com.go2super.database.entity.sub;


import lombok.Data;

import java.util.*;

@Data
public class CorpTerritories {

    List<Integer> territories;

    public List<Integer> getTerritories() {

        return territories;
    }

    public void addTerritory(int planetId) {

        if (territories == null) {
            territories = new ArrayList<>();
        }

        territories.add(planetId);

    }

    public void removeRecruit(int planetId) {

        int planetToDelete = getTerritory(planetId);
        getTerritories().remove(planetToDelete);

    }

    public Integer getTerritory(int planetId) {

        for (Integer territory : territories) {
            if (territory == planetId) {
                return territory;
            }
        }

        return null;

    }

}
