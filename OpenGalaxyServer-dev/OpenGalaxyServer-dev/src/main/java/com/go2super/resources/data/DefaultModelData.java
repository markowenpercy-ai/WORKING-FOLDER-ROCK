package com.go2super.resources.data;

import com.go2super.resources.JsonData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DefaultModelData extends JsonData {

    private int id;
    private String name;

    private int bodyId;
    private List<Integer> parts;

    public int[] getPartsArray() {

        int[] array = new int[parts.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = parts.get(i);
        }
        return array;
    }

}
