package com.go2super.dto.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Execution{
    public Execution(){}

    public ArrayList<Object> resolvers;
}
