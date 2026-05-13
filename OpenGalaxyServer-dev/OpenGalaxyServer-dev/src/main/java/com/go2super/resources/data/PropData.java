package com.go2super.resources.data;

import com.go2super.resources.JsonData;
import com.go2super.resources.data.props.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PropData extends JsonData {

    private int id;
    private String name;
    private String type;

    private PropMetaData data;
    private PropMallData[] mall;

    private int salvage;

    public PropGemData getGemData() {

        return (PropGemData) data;
    }

    public PropChipData getChipData() {

        return (PropChipData) data;
    }

    public PropExpertiseGemData getExpertiseGemData() {

        return (PropExpertiseGemData) data;
    }

    public PropCommanderData getCommanderData() {

        return (PropCommanderData) data;
    }

    public PropScrollData getScrollData() {

        return (PropScrollData) data;
    }

    public PropBodyData getBodyData() {

        return (PropBodyData) data;
    }

    public PropPartData getPartData() {

        return (PropPartData) data;
    }

    public PropBuffData getBuffData() {

        return (PropBuffData) data;
    }

    public DropData getDropData() {

        return (DropData) data;
    }

    public PropChestData getChestData() {

        return (PropChestData) data;
    }

    public PropMetaData getData() {

        return data;
    }

    public PropContainerData getContainerData() {

        return (PropContainerData) data;
    }

    public PropMallData[] getMallData() {
        // BotLogger.log("mall data");
        return mall;
    }

    public PropMallData getFirstMallData() {

        if (mall.length > 0) {
            return mall[0];
        }
        return null;
    }

    public boolean hasScrollData() {

        return data != null && data instanceof PropScrollData;
    }

    public boolean hasCommanderData() {

        return data != null;
    }

    public boolean hasMallData() {
        // BotLogger.log("ID: " + id + ", NAME: " + name);
        // BotLogger.log("MALL: " + mall);
        return mall != null && mall.length > 0;
    }

    public boolean hasCorsairValue() {

        return salvage != -1;
    }

}
