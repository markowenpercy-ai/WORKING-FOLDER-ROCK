package com.go2super.resources.json.storeevent;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PackData {
  private int id;
  private String name;
  private String shortName;
  private int count;
  private int cost;
  private int limit;
}
