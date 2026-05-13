package com.go2super.dto.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FindEventitemsContent {
    public FindEventitemsContent(){}
    private CMSData data;
}
