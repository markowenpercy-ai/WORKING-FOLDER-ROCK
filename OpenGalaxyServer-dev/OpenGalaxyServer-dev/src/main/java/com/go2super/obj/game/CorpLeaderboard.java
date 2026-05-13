package com.go2super.obj.game;

import com.go2super.database.entity.Corp;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class CorpLeaderboard {

    private int corpId;
    private int rankId;

    private Corp lastCorp;

}
