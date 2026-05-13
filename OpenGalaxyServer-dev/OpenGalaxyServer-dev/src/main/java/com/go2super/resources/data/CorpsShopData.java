package com.go2super.resources.data;

import com.go2super.resources.JsonData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CorpsShopData extends JsonData {

    private String design;

    private int minLevel;
    private int cost;
    private int amount;

}
