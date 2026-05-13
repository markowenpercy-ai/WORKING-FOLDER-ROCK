package com.go2super.resources.data;

import com.go2super.database.entity.sub.UserPlanet;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.resources.JsonData;
import com.go2super.resources.data.meta.FarmLandOffsetMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FarmLandData extends JsonData {

    private int id;
    private int station;

    private FarmLandOffsetMeta offset;

    public GalaxyTile calculateTile(UserPlanet userPlanet) {

        GalaxyTile planetPosition = userPlanet.getPosition();
        GalaxyTile farmLandPosition = new GalaxyTile(planetPosition.getX() + offset.getX(), planetPosition.getY() + offset.getY());
        return farmLandPosition;
    }

}
