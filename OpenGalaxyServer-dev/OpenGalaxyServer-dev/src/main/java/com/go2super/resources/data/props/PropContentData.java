package com.go2super.resources.data.props;

import com.go2super.resources.JsonData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PropContentData extends JsonData {

    private String resource;

    private int max;
    private int min;
    private int value;

}
