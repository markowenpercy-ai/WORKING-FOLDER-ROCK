package com.go2super.resources.data.props;

import com.go2super.resources.data.meta.ExpertiseGemMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PropExpertiseGemData extends PropMetaData {

    private int type;
    private int level;
    private int color;

    private List<ExpertiseGemMeta> effects;

}
