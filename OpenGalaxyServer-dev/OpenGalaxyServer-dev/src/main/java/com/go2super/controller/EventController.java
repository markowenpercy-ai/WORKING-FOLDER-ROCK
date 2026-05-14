package com.go2super.controller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.go2super.database.entity.User;
import com.go2super.dto.EventBuyPackDTO;
import com.go2super.dto.cms.callback.Callback;
import com.go2super.dto.response.BasicResponse;
import com.go2super.obj.model.LoggedSessionAccount;
import com.go2super.service.AccountService;
import com.go2super.service.LoginService;
import com.go2super.service.StoreEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Basic;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("${application.services.account}")
public class EventController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private LoginService loginService;

    @GetMapping("/event/points")
    public BasicResponse GetEventPoints(HttpServletRequest request){
        Optional<LoggedSessionAccount> acc = loginService.getSessionAccount(request.getHeader("Authorization"));
        if(acc.isEmpty()){
            //token auth invalid
            return BasicResponse
                    .builder()
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .message("Unauthorized")
                    .build();
        }
        return BasicResponse.builder().code(HttpStatus.OK.value()).data(StoreEventService.getInstance().getStorePoints(acc.get().getAccount())).build();
    }

    @PostMapping("/event/purchase")
    public  BasicResponse Purchase(@RequestBody EventBuyPackDTO req, HttpServletRequest request) throws IOException {
        Optional<LoggedSessionAccount> acc = loginService.getSessionAccount(request.getHeader("Authorization"));
        if(acc.isEmpty()){
            //token auth invalid
            return BasicResponse
                    .builder()
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .message("Unauthorized")
                    .build();
        }
        List<User> userList = accountService.getUserCache().findByAccountId(acc.get().getAccount().getId().toString());
        if(userList.size() < 1){
            //???
            return BasicResponse
                    .builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .message("Not Found")
                    .build();
        }
        User user = userList.get(0);
        //buy item
        try{
            StoreEventService.getInstance().purchasePack(user, req.getId(), req.getCount(), req.getGuid());
            return BasicResponse.builder().code(HttpStatus.OK.value()).build();
        }
        catch(Exception ex){
            return BasicResponse.builder().code(500).message(ex.getMessage()).build();
        }
    }

    @PostMapping("/event/spin")
    public  BasicResponse Spin(@RequestBody EventBuyPackDTO req, HttpServletRequest request) throws IOException{
        Optional<LoggedSessionAccount> acc = loginService.getSessionAccount(request.getHeader("Authorization"));
        if(acc.isEmpty()){
            //token auth invalid
            return BasicResponse
                    .builder()
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .message("Unauthorized")
                    .build();
        }
        List<User> userList = accountService.getUserCache().findByAccountId(acc.get().getAccount().getId().toString());
        if(userList.size() < 1){
            //???
            return BasicResponse
                    .builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .message("Not Found")
                    .build();
        }
        User user = userList.get(0);
        //spin item
        try{
            return BasicResponse.builder().code(200).data(StoreEventService.getInstance().spinWheel(user, req.getGuid())).build();
        }
        catch(Exception ex){
            return BasicResponse.builder().code(500).message(ex.getMessage()).build();
        }
    }
    @PostMapping("/event/callback")
    public BasicResponse CallBack(@RequestBody Callback guid) throws IOException {
        if (guid == null || guid.getPayload() == null || guid.getPayload().getId() == null) {
            return BasicResponse.builder().code(400).message("Invalid callback payload").build();
        }
        StoreEventService.getInstance().GetCMSResponse(guid.getPayload().getId(), true);
        return BasicResponse.builder().code(200).build();
    }

    @GetMapping("/event/purchased/{guid}")
    public  BasicResponse GetPurchased(@PathVariable("guid") String guid, HttpServletRequest request){
        Optional<LoggedSessionAccount> acc = loginService.getSessionAccount(request.getHeader("Authorization"));
        if(acc.isEmpty()){
            //token auth invalid
            return BasicResponse
                    .builder()
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .message("Unauthorized")
                    .build();
        }
        List<User> userList = accountService.getUserCache().findByAccountId(acc.get().getAccount().getId().toString());
        if(userList.size() < 1){
            //???
            return BasicResponse
                    .builder()
                    .code(HttpStatus.NOT_FOUND.value())
                    .message("Not Found")
                    .build();
        }
        User user = userList.get(0);
        var data = StoreEventService.getInstance().GetEventEntry(user, guid);
        return BasicResponse.builder().code(200).data(data).build();
    }
}
