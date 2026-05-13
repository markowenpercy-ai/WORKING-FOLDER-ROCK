package com.go2super.obj.utility;

import com.go2super.obj.type.InstanceType;
import com.go2super.resources.data.InstanceData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameInstance {

    private InstanceData data;
    private InstanceType type;

    public boolean isValid() {

        return data != null && type != null && data.getRewards() != null && !data.getRewards().isEmpty();
    }

}
