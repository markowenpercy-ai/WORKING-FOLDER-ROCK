package com.go2super.packet.rank;

import com.go2super.obj.utility.UnsignedShort;
import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseWarFieldPagePacket extends Packet {

    public static final int TYPE = 1463;
    private UnsignedShort pageId;
    private UnsignedShort pageNum;
    private List<WarFieldPage> data;
}
