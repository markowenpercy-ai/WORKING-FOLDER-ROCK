package com.go2super.resources.data.meta;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BuyEventDataWithPropName {
  private int id;
  private String propName;
  private int count;
  private int cost;
}
