package com.go2super.controller;

import com.go2super.dto.response.BasicResponse;
import com.go2super.service.MetricService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("${application.services.metric}")
public class MetricController {

    @Autowired
    private MetricService service;

    @GetMapping("/online")
    public BasicResponse online(HttpServletRequest request) {

        return service.onlinePlayers(request);
    }

    @GetMapping("/last/planet")
    public BasicResponse create(HttpServletRequest request) {

        return service.lastPlanet(request);
    }

    @GetMapping("/patch")
    public BasicResponse patch(HttpServletRequest request) {

        return service.patch(request);
    }

}
