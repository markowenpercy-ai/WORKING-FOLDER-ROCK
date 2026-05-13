package com.go2super.resources.data.meta;

import com.go2super.resources.JsonData;
import lombok.*;

import java.io.Serializable;
import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PartBonusesMeta extends JsonData implements Serializable {
    private String type;
    private List<PartBonusMeta> bonuses;


    public double getDouble(String attribute) {
        if (bonuses == null) {
            return 0;
        }
        for (PartBonusMeta bonus : bonuses) {
            if (bonus.getPart().equals(attribute)) {
                return bonus.getAmount();
            }
        }
        return 0;
    }
}
