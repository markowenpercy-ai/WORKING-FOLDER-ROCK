package com.go2super.packet.corp;

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
public class ResponseConsortiaPirateChoosePacket extends Packet {

    public static final int TYPE = 1655;

    // 0 = Can choose
    // 1 = Target is in war
    // 2 = Target has truce
    private int errorCode;

    private int objGuid;
    private SmartString objName = SmartString.of(32);

    private int galaxyId;
    private int assault;
    private int levelId;

}
