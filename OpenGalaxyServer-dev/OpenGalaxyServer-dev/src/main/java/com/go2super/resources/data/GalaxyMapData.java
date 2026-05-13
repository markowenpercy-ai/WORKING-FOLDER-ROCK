package com.go2super.resources.data;

import com.go2super.obj.utility.GalaxyTile;
import com.go2super.resources.JsonData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GalaxyMapData extends JsonData {

    private int x;
    private int y;

    public GalaxyTile getTile() {

        return new GalaxyTile(x, y);
    }

}
