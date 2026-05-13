package com.go2super.database.entity.sub;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.*;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class UserSameIPIncidentInfo {

    private String email;
    private String discord;
    private String accountName;
    private String username;

    private int guid;
    private long userId;

    private Boolean ignore;
    private String description;

    private String ip;
    private String country;
    private String isp;

    private int count;
    private Date timestamp;
    private String accountId;

}
