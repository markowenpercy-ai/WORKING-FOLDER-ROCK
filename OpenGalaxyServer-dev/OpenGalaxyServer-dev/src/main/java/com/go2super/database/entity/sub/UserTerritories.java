package com.go2super.database.entity.sub;

import com.go2super.obj.utility.VariableType;
import lombok.*;

import java.util.*;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class UserTerritories {

    private Date celestialUntil;

    private List<Integer> celestialHelpers;
    private List<UserTerritory> territories;

    public boolean addHelper(int helperGuid) {

        if (celestialHelpers == null) {
            celestialHelpers = new ArrayList<>();
        }

        if (celestialHelpers.size() < VariableType.MAX_HELPCOUNT) {
            celestialHelpers.add(helperGuid);
        }

        return true;

    }

    public UserTerritory findByFarmLandId(int farmLandId) {

        for (UserTerritory territory : territories) {
            if (territory.getFarmLandId() == farmLandId) {
                return territory;
            }
        }
        return null;
    }

    public List<Integer> getCelestialHelpers() {

        if (celestialHelpers == null) {
            celestialHelpers = new ArrayList<>();
        }
        return celestialHelpers;
    }

}
