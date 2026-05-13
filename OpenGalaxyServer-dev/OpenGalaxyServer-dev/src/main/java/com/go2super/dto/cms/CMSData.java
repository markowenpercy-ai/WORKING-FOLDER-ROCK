package com.go2super.dto.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import java.util.ArrayList;

@lombok.Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CMSData {
    public CMSData(){}
    public FindEventitemsContent findEventitemsContent;
    public EventType eventType;
    public CMSData data;
    public Random random;
    public ArrayList<Iv> iv;
}
