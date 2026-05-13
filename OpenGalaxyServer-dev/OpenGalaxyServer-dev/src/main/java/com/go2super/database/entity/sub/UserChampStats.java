package com.go2super.database.entity.sub;

import lombok.*;


@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class UserChampStats {
    private int points;
    private int wins;
    private int shootdowns;
}
