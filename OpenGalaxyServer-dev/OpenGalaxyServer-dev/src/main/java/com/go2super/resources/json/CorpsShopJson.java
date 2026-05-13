package com.go2super.resources.json;

import com.go2super.resources.data.CorpsShopData;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class CorpsShopJson {

    private List<CorpsShopData> ships;

}
