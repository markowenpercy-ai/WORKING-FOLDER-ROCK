package com.go2super.database.entity.sub;

import com.go2super.obj.game.CmosInfo;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.PropData;
import com.go2super.resources.data.props.PropChipData;
import lombok.*;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class BionicChip {

    private int chipId;
    private int chipExperience;
    private int holeId;

    private boolean bound;

    public boolean addExperience(BionicChip other) {

        if (other.getChipData().getAddExperience() == null) {
            other.getChipData().setAddExperience(0);
        }
        return addExperience(other.getChipExperience() + other.getChipData().getAddExperience());
    }

    public boolean addExperience(int chipExperience) {

        PropChipData chipData = getChipData();
        if (chipData == null || chipData.getNeedExperience() == null) {
            return false;
        }
        while (chipExperience > 0 && chipData.getNeedExperience() != null) {
            this.chipExperience += chipExperience;
            if (this.chipExperience >= chipData.getNeedExperience()) {
                chipExperience = this.chipExperience - chipData.getNeedExperience();
                this.chipId++;
                this.chipExperience = 0;
                chipData = getChipData();
            } else {
                break;
            }
        }
        return true;
    }

    public PropData getPropData() {

        return ResourceManager.getProps().getChipData(chipId);
    }

    public PropChipData getChipData() {

        return getPropData().getChipData();
    }

    public CmosInfo getCmosInfo() {

        return new CmosInfo(chipExperience, (short) chipId, (short) (bound ? 1 : 0));
    }

}
