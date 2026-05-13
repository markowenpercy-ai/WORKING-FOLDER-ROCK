package com.go2super.resources.data.props;

import com.go2super.resources.data.meta.GemMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PropGemData extends PropMetaData {

    private int type;
    private int level;
    private int color;

    private List<GemMeta> effects;

}
