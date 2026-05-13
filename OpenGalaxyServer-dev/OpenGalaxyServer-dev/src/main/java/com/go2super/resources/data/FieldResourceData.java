package com.go2super.resources.data;

import com.go2super.resources.JsonData;
import com.go2super.resources.data.meta.FieldGenerationMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FieldResourceData extends JsonData {

    private int id;
    private String name;

    private int kind;
    private int time;

    private FieldGenerationMeta generation;

}
