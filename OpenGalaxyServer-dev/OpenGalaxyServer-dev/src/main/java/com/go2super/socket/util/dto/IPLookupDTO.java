package com.go2super.socket.util.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IPLookupDTO {

    private String ip;
    private String country;
    private String isp;

}
