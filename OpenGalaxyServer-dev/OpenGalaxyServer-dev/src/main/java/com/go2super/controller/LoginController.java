package com.go2super.controller;

import com.go2super.dto.AccountDTO;
import com.go2super.dto.AccountLoginDTO;
import com.go2super.dto.response.BasicResponse;
import com.go2super.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("${application.services.login}")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @CrossOrigin(origins = "*")
    @PostMapping("/login/account")
    public BasicResponse loginAccount(@Valid @RequestBody AccountLoginDTO dto) {
        return loginService.login(dto);
    }

    @PostMapping("/register/account")
    public BasicResponse registerAccount(@Valid @RequestBody AccountDTO dto) {

        return loginService.register(dto);
    }
}