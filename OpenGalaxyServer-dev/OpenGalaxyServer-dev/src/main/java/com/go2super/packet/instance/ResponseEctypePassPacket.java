package com.go2super.packet.instance;

import com.go2super.obj.game.EctypeInfo;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseEctypePassPacket extends Packet {

    public static final int TYPE = 1436;

    private int dataLen;
    private List<EctypeInfo> ectypeList = new ArrayList<>();

    public void fill(int until) {

        ectypeList.clear();
        for (int i = 0; i < until; i++) {
            ectypeList.add(new EctypeInfo(i));
        }
        for (int i = ectypeList.size(); i < 50; i++) {
            ectypeList.add(new EctypeInfo(0));
        }
    }

}
