package com.go2super.socket.util;

import com.go2super.socket.util.dto.IPLookupDTO;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import lombok.SneakyThrows;

import java.util.*;

public class IPLocation {

    @SneakyThrows
    public static Optional<IPLookupDTO> getLocation(String ip) {

        String main_host = "http://ip-api.com/json/" + ip;
        String alternative_host = "https://api.iplocation.net/?ip=" + ip;
        HttpResponse<JsonNode> response = null;
        String isp = null;
        String country = null;
        try{
            response = Unirest.get(main_host).asJson();

            main:
            if (response.getStatus() == 200) {

                JsonNode result = response.getBody();

                if (result.getObject().has("country")) {
                    country = result.getObject().getString("country");
                }

                if (result.getObject().has("isp")) {
                    isp = result.getObject().getString("isp");
                } else if (result.getObject().has("org")) {
                    isp = result.getObject().getString("org");
                } else {
                    break main;
                }

                return Optional.ofNullable(IPLookupDTO.builder()
                        .ip(ip)
                        .country(country)
                        .isp(isp)
                        .build());

            }
        }
        catch(Exception ex){

        }
        response = Unirest.get(alternative_host)
            .asJson();

        if (response.getStatus() == 200) {

            JsonNode result = response.getBody();

            if (country == null && result.getObject().has("country")) {
                country = result.getObject().getString("country");
            }

            if (result.getObject().has("isp")) {
                isp = result.getObject().getString("isp");
            }

        }

        if (isp == null) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(IPLookupDTO.builder()
                .ip(ip)
                .country(country)
                .isp(isp)
                .build());
        }
    }
}
