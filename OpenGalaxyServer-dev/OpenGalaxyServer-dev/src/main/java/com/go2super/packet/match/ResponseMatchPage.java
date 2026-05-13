package com.go2super.packet.match;

import com.go2super.obj.game.MatchData;
import com.go2super.obj.utility.Trash;
import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class ResponseMatchPage extends Packet {
    public static final int TYPE = 1449;
    private int pageId;
    private int maxPageId;
    private int dataLen;
    @Trash(length = 10)
    private List<MatchData> data;
}
