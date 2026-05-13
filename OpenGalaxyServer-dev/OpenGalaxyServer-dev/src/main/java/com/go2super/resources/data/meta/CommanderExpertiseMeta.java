package com.go2super.resources.data.meta;

import com.go2super.database.entity.sub.CommanderExpertise;
import com.go2super.database.entity.type.ExpertiseType;
import com.go2super.resources.JsonData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommanderExpertiseMeta extends JsonData {

    private ExpertiseType ballistic;
    private ExpertiseType directional;
    private ExpertiseType missile;
    private ExpertiseType shipBased;
    private ExpertiseType building;
    private ExpertiseType frigate;
    private ExpertiseType cruiser;
    private ExpertiseType battleship;

    public CommanderExpertise getExpertise() {

        return CommanderExpertise.builder()
            .ballistic(ballistic)
            .directional(directional)
            .missile(missile)
            .carrier(shipBased)
            .defend(building)
            .frigate(frigate)
            .cruiser(cruiser)
            .battleShip(battleship)
            .build();
    }

}
