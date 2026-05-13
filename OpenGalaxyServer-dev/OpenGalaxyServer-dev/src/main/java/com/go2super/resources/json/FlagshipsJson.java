package com.go2super.resources.json;

import com.go2super.resources.data.FlagshipData;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class FlagshipsJson {

    private List<FlagshipData> flagships;

    public FlagshipData lookup(int propId) {

        for (FlagshipData data : flagships) {
            if (data.getPropId() == propId) {
                return data;
            }
        }
        return null;
    }

}
