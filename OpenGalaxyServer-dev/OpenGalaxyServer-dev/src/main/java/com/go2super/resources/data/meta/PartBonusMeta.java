package com.go2super.resources.data.meta;

import com.go2super.resources.JsonData;
import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PartBonusMeta extends JsonData implements Serializable {

    private double amount;
    private String part;

}
