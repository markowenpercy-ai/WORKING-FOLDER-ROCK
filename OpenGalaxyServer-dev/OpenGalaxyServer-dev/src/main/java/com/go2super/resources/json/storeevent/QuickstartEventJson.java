package com.go2super.resources.json.storeevent;

import com.go2super.resources.data.BuyEventData;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class QuickstartEventJson {
    private int eventId;
    private List<BuyEventData> awards;

    public BuyEventData findById(int id) {
        for (BuyEventData data : awards) {
            if (data.getId() == id) {
                return data;
            }
        }
        return null;
    }
}
