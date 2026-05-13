package com.go2super.obj.model;

import com.go2super.database.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoggedSessionUser {

    private User user;
    private String sessionKey;

}