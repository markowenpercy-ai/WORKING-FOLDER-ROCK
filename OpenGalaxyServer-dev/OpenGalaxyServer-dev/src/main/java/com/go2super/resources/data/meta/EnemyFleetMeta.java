package com.go2super.resources.data.meta;

import com.go2super.resources.JsonData;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.LayoutData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EnemyFleetMeta extends JsonData {

    private int x;
    private int y;

    private String layout;
    private EnemyStatsMeta stats;

    public LayoutData getLayoutData() {

        Optional<LayoutData> instanceLayout = ResourceManager.fetchLayout(layout);
        if (instanceLayout.isPresent()) {
            return instanceLayout.get();
        }
        return null;
    }

}
