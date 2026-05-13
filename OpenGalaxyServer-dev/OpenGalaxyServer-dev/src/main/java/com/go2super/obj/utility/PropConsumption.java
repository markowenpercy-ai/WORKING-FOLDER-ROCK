package com.go2super.obj.utility;

import com.go2super.database.entity.User;
import com.go2super.obj.game.Prop;
import com.go2super.packet.Packet;

public interface PropConsumption {

    boolean consume(Prop inventoryProp, int quantity, boolean lock, Packet packet, User user);

}
