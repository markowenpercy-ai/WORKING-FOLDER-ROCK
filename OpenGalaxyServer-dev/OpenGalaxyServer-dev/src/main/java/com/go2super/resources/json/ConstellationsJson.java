package com.go2super.resources.json;

import com.go2super.resources.data.InstanceData;
import com.go2super.resources.data.LayoutData;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class ConstellationsJson {

    private List<InstanceData> instances;
    private List<LayoutData> layout;

    public LayoutData getLayout(String nameKey) {

        for (LayoutData data : layout) {
            if (data.getName().equals(nameKey)) {
                return data;
            }
        }
        return null;
    }

    public InstanceData getConstellation(int id) {

        for (InstanceData data : instances) {
            if (data.getId() == id) {
                return data;
            }
        }
        return null;
    }

}
