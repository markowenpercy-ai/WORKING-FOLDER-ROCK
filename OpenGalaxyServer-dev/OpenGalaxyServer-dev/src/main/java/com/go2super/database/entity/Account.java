package com.go2super.database.entity;

import com.go2super.database.entity.sub.DiscordHook;
import com.go2super.database.entity.type.AccountStatus;
import com.go2super.database.entity.type.UserRank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.*;

@Document(collection = "game_accounts")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    private ObjectId id;

    @Column(unique = true)
    private String username;
    @Column(unique = true)
    private String email;

    @Field(name = "discord_hook")
    private DiscordHook discordHook;

    private String password;
    private String lastIp;

    private int vip;
    private Date banUntil;
    private Boolean maintenanceBypass;

    private Date lastConnection;
    private Date registerDate;

    private AccountStatus accountStatus;
    private UserRank userRank;
    private Long mainPlanetId;

}
