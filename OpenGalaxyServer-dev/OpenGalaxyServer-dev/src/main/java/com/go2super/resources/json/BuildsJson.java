package com.go2super.resources.json;

import com.go2super.resources.data.BuildData;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class BuildsJson {

    private List<BuildData> buildings;

    public BuildData getBuild(String name) {

        for (BuildData data : buildings) {
            if (data.getName().equals(name)) {
                return data;
            }
        }
        return null;
    }

    public BuildData getBuild(int id) {

        for (BuildData data : buildings) {
            if (data.getId() == id) {
                return data;
            }
        }
        return null;
    }

}
