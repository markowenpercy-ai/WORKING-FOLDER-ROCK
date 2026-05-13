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
public class FieldGenerationMeta extends JsonData {

    private String resource;
    private int value;

}
