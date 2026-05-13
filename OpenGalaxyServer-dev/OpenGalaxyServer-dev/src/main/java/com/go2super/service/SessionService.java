package com.go2super.service;

import com.go2super.database.cache.AccountCache;
import com.go2super.database.entity.Account;
import com.go2super.database.entity.AccountSession;
import com.go2super.database.repository.AccountSessionRepository;
import com.go2super.socket.util.DateUtil;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class SessionService {

    private static SessionService resourcesService;

    @Autowired
    @Getter
    private AccountSessionRepository accountSessionRepository;

    @Getter
    private final AccountCache accountCache;

    @Autowired
    public SessionService(AccountCache accountCache) {
        resourcesService = this;
        this.accountCache = accountCache;
    }

    public AccountSession registerAccountSession(Account account, String token) {
        removeAllSessionsByAccountId(account.getId().toString());
        AccountSession accountSession = AccountSession.builder()
                .accountId(account.getId().toString())
                .token(token)
                .expired(false)
                .loginDate(new Date())
                .untilDate(DateUtil.now(31 * 86400)) // 31 days hardcoded
                .build();

        return accountSessionRepository.save(accountSession.reference(account));

    }

    public Optional<AccountSession> getActiveAccountSessionByToken(String token) {
        Optional<AccountSession> sessionOptional = accountSessionRepository.findByToken(token);

        if (sessionOptional.isEmpty()) {
            return Optional.empty();
        }

        AccountSession accountSession = sessionOptional.get();
        Optional<Account> accountOptional = accountCache.findById(accountSession.getAccountId());

        if (new Date().getTime() > accountSession.getUntilDate().getTime() || accountSession.isExpired() || accountOptional.isEmpty()) {
            accountSessionRepository.delete(accountSession);
            return Optional.empty();
        }

        return Optional.of(accountSession.reference(accountOptional.get()));

    }


    public void removeActiveAccountSession(String token) {
        Optional<AccountSession> sessionOptional = accountSessionRepository.findByToken(token);
        if (sessionOptional.isEmpty()) {
            return;
        }
        accountSessionRepository.delete(sessionOptional.get());
    }

    public void removeAllSessionsByAccountId(String accountId) {
        List<AccountSession> sessions = accountSessionRepository.findByAccountId(accountId);
        if (sessions.isEmpty()) {
            return;
        }
        accountSessionRepository.deleteAll(sessions);
    }

    public void updateSessionKey(String accountId, String sessionKey) {
        Optional<AccountSession> sessionOptional = accountSessionRepository.findByAccountIdAndExpiredFalse(accountId);
        if (sessionOptional.isEmpty()) {
            return;
        }
        AccountSession session = sessionOptional.get();
        session.setSessionKey(sessionKey);
        accountSessionRepository.save(session);
    }

    public Optional<AccountSession> getActiveAccountSessionByAccountIdAndNotExpired(String accountId) {
        return accountSessionRepository.findByAccountIdAndExpiredFalse(accountId);
    }

    public static SessionService getInstance() {

        return resourcesService;
    }

}
