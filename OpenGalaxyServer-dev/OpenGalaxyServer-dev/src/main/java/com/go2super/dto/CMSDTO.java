package com.go2super.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.go2super.dto.cms.CMSData;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CMSDTO {
    public CMSDTO(){}
    private CMSData data;
}

