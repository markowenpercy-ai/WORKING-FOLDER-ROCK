package com.go2super.logger.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@ToString
@SuperBuilder
public class UserActionLog extends Action {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Builder.Default
    @JsonProperty("guid")
    private int guid = -1;

    @Builder.Default
    @JsonProperty("user_id")
    private long userId = -1;

    @JsonProperty("username")
    private String username;
    @JsonProperty("account_name")
    private String accountName;
    @JsonProperty("account_id")
    private String accountId;
    @JsonProperty("account_email")
    private String accountEmail;
    @JsonProperty("optional_discord_id")
    private String optionalDiscordId;

    @JsonProperty("action")
    private String action;

    @JsonProperty("before_data")
    private Object beforeData;
    @JsonProperty("after_data")
    private Object afterData;

    public void setBeforeData(Object beforeData) {

        this.beforeData = objectMapper.valueToTree(beforeData).toString();
    }

    public void setAfterData(Object afterData) {

        this.afterData = objectMapper.valueToTree(afterData).toString();
    }

}
