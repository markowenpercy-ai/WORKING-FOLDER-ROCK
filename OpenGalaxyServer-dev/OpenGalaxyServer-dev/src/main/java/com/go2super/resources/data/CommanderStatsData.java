package com.go2super.resources.data;

import com.go2super.database.entity.sub.CommanderExpertise;
import com.go2super.resources.JsonData;
import com.go2super.resources.data.meta.CommanderExpertiseMeta;
import com.go2super.resources.data.meta.CommanderProcMeta;
import com.go2super.resources.data.meta.CommanderStatsMeta;
import com.go2super.resources.localization.Localization;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommanderStatsData extends JsonData {

    private int id;

    private String name;
    private String type;
    private String subType;

    private CommanderStatsMeta baseStats;
    private CommanderExpertiseMeta expertise;
    private CommanderProcMeta proc;

    public int typeCode() {

        switch (type) {
            case "skill":
                return 2;
            case "super":
                return 3;
            case "legendary":
                return 4;
            case "divine":
                return 5;
            default:
                return 1;
        }
    }

    public int subTypeCode() {

        switch (subType) {
            case "special":
                return 2;
            case "attack":
                return 3;
            case "extra":
                return 5;
            default:
                return 1;
        }
    }

    public String getName() {

        return name;
    }

    public String getLocalizedName() {

        return Localization.EN_US.get(name);
    }

    public CommanderExpertise getExpertise() {

        return expertise.getExpertise();
    }

}
