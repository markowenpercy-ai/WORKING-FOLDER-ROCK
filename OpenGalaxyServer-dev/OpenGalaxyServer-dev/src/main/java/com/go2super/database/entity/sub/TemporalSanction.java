package com.go2super.database.entity.sub;

import com.go2super.database.entity.Sanction;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.*;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class TemporalSanction extends Sanction {

    private String timeFormat;
    private Date until;

}
