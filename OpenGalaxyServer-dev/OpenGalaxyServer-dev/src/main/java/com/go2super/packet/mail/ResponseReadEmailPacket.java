package com.go2super.packet.mail;

import com.go2super.obj.game.ReadEmail;
import com.go2super.obj.utility.SmartString;
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
public class ResponseReadEmailPacket extends Packet {

    public static final int TYPE = 1612;

    private int autoId;
    private int dataLen;

    SmartString content = SmartString.of(VariableType.MAX_EMAILCONTENT);

    private List<ReadEmail> data;


}
