package com.go2super.database.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.*;

@Document(collection = "game_account_sessions")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountSession {

    @Id
    private ObjectId id;

    @Transient
    private Account reference;

    @Column(unique = true)
    private String accountId;
    @Column(unique = true)
    private String token;

    private String sessionKey;

    private boolean expired;

    private Date loginDate;
    private Date untilDate;

    public AccountSession reference(Account account) {

        this.reference = account;
        return this;
    }

}
