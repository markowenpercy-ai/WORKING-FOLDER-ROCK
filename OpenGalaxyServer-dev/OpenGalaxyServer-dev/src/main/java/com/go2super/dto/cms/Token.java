package com.go2super.dto.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Token {
    public Token(){}
    @JsonProperty("access_token")
    private String access_token;
    @JsonProperty("expires_in")
    private int expires_in;
    @JsonProperty("scope")
    private String scope;
    @JsonProperty("token_type")
    private String token_type;
}
