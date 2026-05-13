package com.go2super.database.entity.sub;

import lombok.*;

import java.util.List;


@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class UserIglStats {
    private boolean claimed;
    private int entries;
    private List<String> fleetIds;
    private int rank;
}
