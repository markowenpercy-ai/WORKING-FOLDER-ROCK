package com.go2super.resources.json;

import com.go2super.resources.data.FieldResourceData;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class FieldResourcesJson {

    private List<FieldResourceData> fieldResources;

    public FieldResourceData findByName(String name) {

        for (FieldResourceData fieldResource : fieldResources) {
            if (fieldResource.getName().equals(name)) {
                return fieldResource;
            }
        }
        return null;
    }

    public FieldResourceData findById(int resourceId) {

        for (FieldResourceData fieldResource : fieldResources) {
            if (fieldResource.getId() == resourceId) {
                return fieldResource;
            }
        }
        return null;
    }

}
