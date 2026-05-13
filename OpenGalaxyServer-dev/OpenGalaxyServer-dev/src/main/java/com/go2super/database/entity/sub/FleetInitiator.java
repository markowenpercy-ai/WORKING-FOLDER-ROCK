package com.go2super.database.entity.sub;

import com.go2super.obj.type.JumpType;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class FleetInitiator  implements Serializable {

    private JumpType jumpType;

}