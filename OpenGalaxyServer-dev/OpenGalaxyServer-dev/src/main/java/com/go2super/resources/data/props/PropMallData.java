package com.go2super.resources.data.props;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PropMallData extends PropMetaData {

    private int amount = 1; // todo default 1?
    private String currency;
    private int value;
    private boolean bound;

    public int currencyCode() {

        switch (currency) {
            case "resource:voucher":
                return 1;
            case "resource:badge":
                return 2;
            case "resource:honor":
                return 3;
            case "resource:champion":
                return 4;
            default:
                return 0;
        }
    }

}
