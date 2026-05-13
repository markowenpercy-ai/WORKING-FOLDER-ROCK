package com.go2super.resources.data.props;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PropChestData extends PropMetaData {

    private PropContentData[] contents;

    private int contentAmount;
    private int contentChance;

    private boolean contentLock;
    private String[] randomContent;
    private String[] unlockedRandomContent;

    public List<String> getRandomContentList() {

        if (randomContent != null) {
            return Arrays.asList(randomContent);
        }
        return new ArrayList<>();
    }

    public List<String> getUnlockedRandomContentList() {

        if (unlockedRandomContent != null) {
            return Arrays.asList(unlockedRandomContent);
        }
        return new ArrayList<>();
    }

}
