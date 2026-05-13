package com.go2super.resources.json.storeevent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class CommanderEventJson {
  private final String eventId;
  private List<PackData> pack;
  private List<RandomData> random;

  public PackData findByShortName(String shortName) {
    for (PackData data : getPack()) {
      if (data.getShortName().equals(shortName)) {
        return data;
      }
    }
    return null;
  }

  public RandomData pickOne() {
    List<RandomData> list = new ArrayList<>();
    for (RandomData rewardMeta : random) {
      for (int i = 0; i < rewardMeta.getWeight(); i++) {
        list.add(rewardMeta);
      }
    }
    Collections.shuffle(list);
    return list.get(0);
  }
}
