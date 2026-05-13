package com.go2super.resources.data.meta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BuyEventMeta {
    private int id;
    private String name;
    private int count;
}
