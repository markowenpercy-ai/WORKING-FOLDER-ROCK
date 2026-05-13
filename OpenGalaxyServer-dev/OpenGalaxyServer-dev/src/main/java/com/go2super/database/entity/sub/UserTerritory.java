package com.go2super.database.entity.sub;

import lombok.*;

import java.util.*;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class UserTerritory {

    private int farmLandId;
    private int fieldId;

    private int desiredProduction;
    private int totalProduction;
    private Date until;

    private List<Integer> thieves;

}
