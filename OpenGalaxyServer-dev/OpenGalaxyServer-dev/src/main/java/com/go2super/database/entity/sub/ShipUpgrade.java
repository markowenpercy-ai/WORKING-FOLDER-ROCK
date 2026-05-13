package com.go2super.database.entity.sub;

import com.go2super.socket.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShipUpgrade {

    private int upgradeId;
    private Date until;

    public Long upgradeTime() {

        return DateUtil.remains(until);
    }

}
