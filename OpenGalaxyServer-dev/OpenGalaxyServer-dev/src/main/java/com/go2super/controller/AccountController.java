package com.go2super.controller;

import com.go2super.dto.ChangePasswordDTO;
import com.go2super.dto.CreateUserDTO;
import com.go2super.dto.response.BasicResponse;
import com.go2super.service.AccountService;
import com.go2super.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("${application.services.account}")
public class AccountController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private AccountService accountService;

    @GetMapping("/play/user/{userId}")
    public BasicResponse play(@PathVariable("userId") int userId, HttpServletRequest request) {
        return accountService.play(userId, request);
    }

    @PostMapping("/create/user")
    public BasicResponse create(@RequestBody CreateUserDTO dto, HttpServletRequest request) {

        return accountService.createUser(dto, request);
    }

    @GetMapping("/logout/user")
    public BasicResponse logout(HttpServletRequest request) {
        return loginService.logout(request);
    }

    @PostMapping("/changepassword/user")
    public BasicResponse changePassword(@RequestBody ChangePasswordDTO dto, HttpServletRequest request) {
        return accountService.changePassword(dto, request);
    }

    @GetMapping("/list/user")
    public BasicResponse listOfUser(HttpServletRequest request) {

        return accountService.listOfUser(request);
    }
}
