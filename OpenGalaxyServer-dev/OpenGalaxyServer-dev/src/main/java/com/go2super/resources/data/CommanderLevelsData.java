package com.go2super.resources.data;

import com.go2super.resources.JsonData;
import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommanderLevelsData extends JsonData {

    private int exp;
    private int gem;

}
