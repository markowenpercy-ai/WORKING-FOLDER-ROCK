package com.go2super.dto.cms.global;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CMSEventList {
    public int total;
    public ArrayList<Item> items;
    public ArrayList<Status> statuses;
    public Links _links;
}
