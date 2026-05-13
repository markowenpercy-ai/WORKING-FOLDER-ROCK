package com.go2super.database.entity.sub;

import lombok.*;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class EmailGood {

    private int goodId;

    private int num;
    private int lockNum;

}
