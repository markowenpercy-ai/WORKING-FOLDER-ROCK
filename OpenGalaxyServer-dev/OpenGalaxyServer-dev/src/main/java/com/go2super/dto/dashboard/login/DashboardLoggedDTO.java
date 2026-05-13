package com.go2super.dto.dashboard.login;

import com.go2super.database.entity.type.DashboardRank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardLoggedDTO {

    private String username;
    private String[] permissions;

    private DashboardRank rank;
    private String token;

}
