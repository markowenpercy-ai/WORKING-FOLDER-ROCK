package com.go2super.service;

import com.go2super.database.cache.DashboardAccountCache;
import com.go2super.database.entity.DashboardAccount;
import com.go2super.database.entity.DashboardAccountSession;
import com.go2super.database.repository.DashboardAccountSessionRepository;
import com.go2super.dto.dashboard.login.DashboardLoggedDTO;
import com.go2super.dto.dashboard.login.DashboardLoginDTO;
import com.go2super.dto.response.BasicResponse;
import com.go2super.socket.util.Crypto;
import com.go2super.socket.util.DateUtil;
import lombok.Getter;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class DashboardLoginService {

    private static DashboardLoginService dashboardLoginService;

    @Getter
    private final DashboardAccountSessionRepository sessionRepository;
    @Getter
    private final DashboardAccountCache accountCache;

    @Autowired
    public DashboardLoginService(DashboardAccountSessionRepository sessionRepository, DashboardAccountCache accountCache) {

        dashboardLoginService = this;

        this.sessionRepository = sessionRepository;
        this.accountCache = accountCache;

    }

    public BasicResponse login(DashboardLoginDTO dto, HttpServletRequest request) {

        Optional<DashboardAccount> accountOptional = accountCache.findByEmail(dto.getUsername());

        if (accountOptional.isEmpty()) {
            return BasicResponse
                .builder()
                .code(HttpStatus.NOT_ACCEPTABLE.value())
                .message("ACCOUNT_NOT_FOUND")
                .display("Account not found")
                .build();
        }

        DashboardAccount account = accountOptional.get();

        if (!Crypto.decrypt(account.getPassword()).equals(dto.getPassword())) {
            return BasicResponse
                .builder()
                .code(HttpStatus.NOT_ACCEPTABLE.value())
                .message("ACCOUNT_NOT_FOUND")
                .display("Account not found")
                .build();
        }

        List<DashboardAccountSession> accountSessions = sessionRepository.findByAccountIdAndExpired(account.getId().toString(), false);

        for (DashboardAccountSession accountSession : accountSessions) {

            accountSession.setExpired(true);
            sessionRepository.save(accountSession);

        }

        String token = RandomStringUtils.randomAlphanumeric(40);

        DashboardAccountSession accountSession = DashboardAccountSession.builder()
            .id(ObjectId.get())
            .expired(false)
            .accountId(account.getId().toString())
            .loginDate(DateUtil.now())
            .untilDate(DateUtil.now(60 * 60 * 12))
            .reference(account)
            .token(token)
            .build();

        sessionRepository.save(accountSession);

        DashboardLoggedDTO loggedDTO = DashboardLoggedDTO.builder()
            .username(account.getEmail())

            .rank(account.getRank())
            .permissions(account.getRank().getPermissions())

            .token(token)
            .build();

        return BasicResponse
            .builder()
            .code(HttpStatus.OK.value())
            .message("OK")
            .display("Logged in, redirecting...")
            .data(loggedDTO)
            .build();

    }

    public static DashboardLoginService getInstance() {

        return dashboardLoginService;
    }

}
