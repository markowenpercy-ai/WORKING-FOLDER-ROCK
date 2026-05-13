package com.go2super.database.entity.sub;

import lombok.Builder;
import lombok.Data;

import java.util.*;

@Data
@Builder
public class CorpUpgrade {

    private int typeUpgrade;

    private Date until;

}
