package com.go2super.database.entity.sub;

import com.go2super.obj.game.ShipTeamNum;
import lombok.Builder;
import lombok.Data;

import java.util.*;

@Builder
@Data
public class UserFactory {

    public List<ShipTeamNum> ships = new ArrayList<>();

}
