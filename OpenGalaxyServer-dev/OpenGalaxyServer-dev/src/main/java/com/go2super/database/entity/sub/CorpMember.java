package com.go2super.database.entity.sub;

import com.go2super.database.entity.User;
import com.go2super.service.UserService;
import lombok.Builder;
import lombok.Data;

import java.util.*;

@Data
@Builder
public class CorpMember {

    private int guid;

    /*
        0 -> recruit
        1 -> colonel
        2 -> commandant
        3 -> captain
        4 -> soldier
    */
    private int rank;

    private int contribution;
    private int donateResources;
    private int donateMallPoints;

    public Optional<User> getUser() {

        User user = UserService.getInstance().getUserCache().findByGuid(guid);
        return Optional.ofNullable(user);
    }

}
