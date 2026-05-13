package com.go2super.resources.json;

import com.go2super.resources.data.CorpsPirateData;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class CorpsPirateJson {

    private List<CorpsPirateData> pirates;

}
