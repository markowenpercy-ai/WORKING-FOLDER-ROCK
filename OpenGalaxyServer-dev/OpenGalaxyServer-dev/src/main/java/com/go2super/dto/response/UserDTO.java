package com.go2super.dto.response;

import com.go2super.database.entity.sub.UserResources;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {

    private long userId;
    private String username;
    private String star;
    private int ground;
    private UserResources resources;
}
