package com.go2super.dto.payment;

import com.go2super.dto.payment.sub.UserInformationDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class XsollaUserInfoDTO {

    private String status;
    private UserInformationDTO user;

}
