package com.go2super.packet.corp;

import com.go2super.obj.game.InsertFlagConsortiaMember;
import com.go2super.obj.game.IntegerArray;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.obj.utility.VariableType;
import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseInsertFlagConsortiaMemberPacket extends Packet {

    public static final int TYPE = 1651;

    private SmartString name = SmartString.of(VariableType.MAX_NAME);

    private int throwWealth;

    private IntegerArray holdGalaxyArea;

    private UnsignedChar headId = UnsignedChar.of(0);
    private UnsignedChar level = UnsignedChar.of(0);
    private UnsignedChar holdGalaxy = UnsignedChar.of(0);
    private UnsignedChar memberCount = UnsignedChar.of(0);

    private int dataLen;

    private List<InsertFlagConsortiaMember> data;

}

