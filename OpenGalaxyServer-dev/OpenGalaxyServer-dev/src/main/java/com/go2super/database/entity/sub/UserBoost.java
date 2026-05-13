package com.go2super.database.entity.sub;

import com.go2super.database.entity.GameBoost;
import com.go2super.service.ResourcesService;
import com.go2super.socket.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import java.util.*;

@Builder
@AllArgsConstructor
@Data
public class UserBoost {

    private ObjectId gameBoostId;
    private Date until;

    public GameBoost boost() {

        return getGameBoost().get();
    }

    public Optional<GameBoost> getGameBoost() {

        return ResourcesService.getInstance().getBoostRepository().findById(gameBoostId);
    }

    public int getSeconds() {

        return DateUtil.remains(until).intValue();
    }

    public void addSeconds(int seconds) {

        setUntil(DateUtil.offset(until, seconds));
    }
    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        if(obj instanceof UserBoost)
        {
            UserBoost temp = (UserBoost) obj;
            if(this.getGameBoostId().equals(temp.getGameBoostId()))
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub

        return (this.getGameBoostId().hashCode());
    }
}
