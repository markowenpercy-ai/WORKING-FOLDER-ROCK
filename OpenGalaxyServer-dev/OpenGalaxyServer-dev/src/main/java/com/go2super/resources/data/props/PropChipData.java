package com.go2super.resources.data.props;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.go2super.resources.data.meta.ChipMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PropChipData extends PropMetaData {

    private int type;
    private int level;
    private String color;

    private Integer needExperience;
    @JsonSetter(nulls = Nulls.SKIP)
    private Integer addExperience = 0;

    private ChipMeta effect;

    public boolean hasToBound() {

        return color != null && (color.equals("purple") || color.equals("orange"));
    }

}
