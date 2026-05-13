package com.go2super.database.entity.sub;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.*;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class UserIPInfo {

    private String accountId;

    private int guid;
    private long userId;
    private String username;

    private String ip;

    private int count;
    private Date lastTime;

}
