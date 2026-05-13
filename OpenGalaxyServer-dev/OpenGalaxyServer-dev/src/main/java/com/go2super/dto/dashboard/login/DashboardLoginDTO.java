package com.go2super.dto.dashboard.login;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardLoginDTO {

    private String username;
    private String password;

}