package com.go2super.database.entity.sub;

import com.go2super.database.entity.Trade;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.PropData;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class TradeItem extends Trade {

    private int propId;

    public PropData getData() {

        return ResourceManager.getProps().getData(propId);
    }

}
