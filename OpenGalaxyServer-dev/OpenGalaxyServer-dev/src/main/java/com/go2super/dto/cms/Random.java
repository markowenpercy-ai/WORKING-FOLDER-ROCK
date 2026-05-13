package com.go2super.dto.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Random {
    public Random(){}
    public ArrayList<Iv> iv;
}
