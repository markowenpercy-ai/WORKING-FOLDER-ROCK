package com.go2super.dto.payment.sub;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInformationDTO {

    private int id;
    private String name;
    private String level;

}
