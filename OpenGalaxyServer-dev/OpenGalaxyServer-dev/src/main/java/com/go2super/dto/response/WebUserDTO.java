package com.go2super.dto.response;

import com.go2super.database.entity.type.UserRank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebUserDTO {

    private String email;
    private String username;

    private int vip;
    private UserRank rank;

    private String token;
    private int maxPlanet;

}
