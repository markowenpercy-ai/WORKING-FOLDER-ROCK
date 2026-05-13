package com.go2super.resources.data.props;

import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.CommanderStatsData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PropCommanderData extends PropMetaData {

    private String commander;

    public CommanderStatsData getCommander() {

        return ResourceManager.getCommanders().getCommander(commander);
    }

}
