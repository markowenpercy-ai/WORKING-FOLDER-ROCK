package com.go2super.logger.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.*;

@Setter
@Getter
@ToString
@SuperBuilder
public class Action {

    @JsonProperty("scope")
    private String scope;
    @JsonProperty("category")
    private String category;

    @JsonProperty("hostname")
    private String hostname;
    @JsonProperty("ip")
    private String ip;

    @JsonProperty("message")
    private Object message;
    @JsonProperty("timestamp")
    private Date timestamp;

}
