package com.go2super.controller;

import com.go2super.dto.dashboard.login.DashboardLoginDTO;
import com.go2super.dto.response.BasicResponse;
import com.go2super.service.DashboardLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("${application.services.dashboard}")
public class DashboardController {

    @Autowired
    private DashboardLoginService dashboardLoginService;

    @PostMapping("/users")
    public BasicResponse login(@RequestBody DashboardLoginDTO dto, HttpServletRequest request) {

        return dashboardLoginService.login(dto, request);
    }

}
