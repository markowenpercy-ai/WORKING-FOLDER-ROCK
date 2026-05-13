package com.go2super.database.entity.sub;

import lombok.*;

import java.util.*;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class UserChips {

    private int[] phases = new int[]{1, 0, 0, 0, 0};

    private int slots = 15;
    private List<BionicChip> chips = new ArrayList<>();

}
