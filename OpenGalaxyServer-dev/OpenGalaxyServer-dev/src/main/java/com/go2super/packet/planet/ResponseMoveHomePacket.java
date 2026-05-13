package com.go2super.packet.planet;

import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMoveHomePacket extends Packet {

    public static final int TYPE = 1111;

    private SmartString consortiaName = SmartString.of(32);

    private int toGalaxyMapId;
    private int toGalaxyId;

    // Error codes:
    // 1 - There are fleets deployed
    // 2 - Planet occupied
    private int errorCode;

    private int propsId;
    private int lockFlag;


}
