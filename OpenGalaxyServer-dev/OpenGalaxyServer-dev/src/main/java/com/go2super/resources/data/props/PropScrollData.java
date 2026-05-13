package com.go2super.resources.data.props;

import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.PropData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PropScrollData extends PropMetaData {

    private String commander;
    private String[] srcCommander;

    public Optional<PropData> getPropDataResult() {

        return ResourceManager.getProps().getCommanderData(commander);
    }

}
