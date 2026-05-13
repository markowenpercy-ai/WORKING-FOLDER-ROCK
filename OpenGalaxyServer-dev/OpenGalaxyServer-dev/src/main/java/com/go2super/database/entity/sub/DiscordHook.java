package com.go2super.database.entity.sub;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.*;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class DiscordHook {

    @Field(name = "linked_discord_before")
    private boolean linkedDiscordBefore;
    @Field(name = "last_claim")
    private Date lastClaim;

    @Field(name = "discord_code_expiration")
    private Date discordCodeExpiration;
    @Field(name = "discord_code")
    private String discordCode;
    @Field(name = "discord_id")
    private String discordId;

}
