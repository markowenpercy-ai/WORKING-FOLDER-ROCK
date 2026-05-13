package com.go2super.resources.data.props;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DropData extends PropMetaData {

    private int minValue;
    private int maxValue;

}
