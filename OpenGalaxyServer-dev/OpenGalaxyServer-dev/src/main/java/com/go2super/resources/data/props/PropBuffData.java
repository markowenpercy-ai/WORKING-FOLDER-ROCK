package com.go2super.resources.data.props;

import com.go2super.resources.data.meta.BuffMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PropBuffData extends PropMetaData {

    private List<BuffMeta> buffs;

}
