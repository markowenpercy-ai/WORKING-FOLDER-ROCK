package com.go2super.resources.data.meta;

import com.go2super.resources.JsonData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BodyEffectMeta extends JsonData {

    private String type;
    private Object value;

    private double max;
    private double min;

}
