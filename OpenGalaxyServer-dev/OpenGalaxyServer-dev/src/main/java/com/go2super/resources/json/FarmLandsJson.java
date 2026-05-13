package com.go2super.resources.json;

import com.go2super.resources.data.FarmLandData;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class FarmLandsJson {

    private List<FarmLandData> farmLands;

}
