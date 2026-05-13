package com.go2super.database.entity.sub;

import com.go2super.obj.type.JumpType;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

@Data
@Builder
public class FleetTransmission   implements Serializable {

    private int galaxyId;
    private JumpType jumpType;

    private int total;
    private Date until;

}
