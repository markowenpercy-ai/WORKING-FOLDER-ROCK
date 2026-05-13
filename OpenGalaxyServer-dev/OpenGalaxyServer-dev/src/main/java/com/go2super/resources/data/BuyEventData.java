package com.go2super.resources.data;

import com.go2super.resources.data.meta.BuyEventMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BuyEventData {
    private int id;
    private List<BuyEventMeta> props;
    private int cost;
    private int limit;
}
