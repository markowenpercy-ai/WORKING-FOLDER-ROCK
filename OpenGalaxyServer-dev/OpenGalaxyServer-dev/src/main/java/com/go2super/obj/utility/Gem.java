package com.go2super.obj.utility;

import com.go2super.resources.data.props.PropGemData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class Gem {

    public int type;
    public int level;
    public int color;

    public static Gem of(PropGemData gemData) {

        return new Gem(gemData.getType(), gemData.getLevel(), gemData.getColor());
    }

}
