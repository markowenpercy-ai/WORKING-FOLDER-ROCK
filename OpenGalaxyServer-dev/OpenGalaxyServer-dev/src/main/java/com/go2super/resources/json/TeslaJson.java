package com.go2super.resources.json;

import com.go2super.resources.data.TeslaData;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class TeslaJson {

    private List<TeslaData> teslas;

    public TeslaData lookup(int propId) {
        for (TeslaData data : teslas) {
            if (data.getPropId() == propId) {
                return data;
            }
        }
        return null;
    }

}
