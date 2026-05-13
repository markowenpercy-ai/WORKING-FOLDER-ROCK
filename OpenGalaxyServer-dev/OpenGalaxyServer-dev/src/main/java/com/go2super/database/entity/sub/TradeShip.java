package com.go2super.database.entity.sub;

import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.Trade;
import com.go2super.service.PacketService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class TradeShip extends Trade {

    private int shipModelId;

    public ShipModel getShipModel() {

        return PacketService.getInstance().getShipModelCache().findByShipModelId(shipModelId);
    }

}
