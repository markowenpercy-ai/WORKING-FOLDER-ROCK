package com.go2super.resources.data.meta;

import com.go2super.resources.JsonData;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.PropData;
import com.go2super.resources.data.props.PropChipData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChipRewardMeta extends JsonData {

    private int propId;
    private int propNum;

    private boolean broadcast;
    private ChipMoneyMeta chances;

    public PropData getPropData() {

        return ResourceManager.getProps().getChipData(propId);
    }

    public PropChipData getChipData() {

        return getPropData().getChipData();
    }

}
