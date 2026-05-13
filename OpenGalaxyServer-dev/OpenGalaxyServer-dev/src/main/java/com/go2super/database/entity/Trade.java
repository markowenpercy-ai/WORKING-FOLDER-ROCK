package com.go2super.database.entity;

import com.go2super.database.entity.type.PriceType;
import com.go2super.database.entity.type.TradeType;
import com.go2super.service.TradeService;
import com.go2super.service.UserService;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.*;

@Document(collection = "game_trades")
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class Trade {

    @Id
    private ObjectId id;

    private int tradeId;

    private long sellerUserId;
    private int sellerGuid;

    private int sellId;
    private int amount;
    private int price;

    private Date until;
    private TradeType tradeType;
    private PriceType priceType;

    public User getSeller() {

        return UserService.getInstance().getUserCache().findByGuid(getSellerGuid());
    }

    public void save() {

        TradeService.getInstance().getTradeCache().save(this);
    }

}
