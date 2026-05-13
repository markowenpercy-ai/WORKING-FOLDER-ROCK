package com.go2super.database.entity.sub;

import com.go2super.database.entity.type.UserRank;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import javax.persistence.Id;
import java.util.*;

@Builder
@Data
public class WebUser {

    @Id
    private ObjectId id;

    private String email;
    private String username;
    private String password;
    private UserRank userRank;

    private int icon;
    private int vip;

    private String lastIp;
    private Date lastConnection;
    private Date registerDate;

}
