package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import com.go2super.obj.utility.SmartString;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeInfo extends BufferObject {

    private long sellUserId;
    private int sellerGuid;
    private String sellerName;

    private int indexId;
    private int id;
    private int num;
    private int price;
    private int spareTime;
    private int reserve;

    private int bodyId;
    private int tradeType;
    private int priceType;

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((8 - go2buffer.getBuffer().position() % 8) % 8);
        go2buffer.addLong(sellUserId);
        go2buffer.addString(SmartString.of(sellerName, 32));

        go2buffer.addInt(sellerGuid);
        go2buffer.addInt(indexId);
        go2buffer.addInt(id);
        go2buffer.addInt(num);
        go2buffer.addInt(price);
        go2buffer.addInt(spareTime);
        go2buffer.addInt(reserve);

        go2buffer.addShort(bodyId);
        go2buffer.addByte((byte) tradeType);
        go2buffer.addByte((byte) priceType);


    }

    @Override
    public TradeInfo trash() {

        return new TradeInfo();
    }

}
