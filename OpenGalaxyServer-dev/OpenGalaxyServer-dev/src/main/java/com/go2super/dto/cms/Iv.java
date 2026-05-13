package com.go2super.dto.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.PropData;
import lombok.Builder;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Iv {
    public Iv(){}
    private int price;
    private int itemId;
    private int limit;

    private int count;
    private int weight;
    public PropData GetObject(){
        return ResourceManager.getProps().getData(itemId);
    }
}
