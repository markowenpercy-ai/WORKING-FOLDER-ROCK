package com.go2super.resources.json;

import com.go2super.resources.data.PropData;
import com.go2super.resources.data.props.PropBodyData;
import com.go2super.resources.data.props.PropPartData;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class PropsJson {

    private List<PropData> props;

    private List<PropData> gems = new ArrayList<>();
    private List<PropData> scrolls = new ArrayList<>();
    private List<PropData> chips = new ArrayList<>();
    private List<PropData> commanders = new ArrayList<>();
    private List<PropData> instanceDrop = new ArrayList<>();
    private List<PropData> inSell = new ArrayList<>();

    public Optional<PropData> getCommanderData(String commanderNameId) {

        return getCommanders().stream()
            .filter(c -> c.getCommanderData().getCommander().getName().equals(commanderNameId))
            .findFirst();
    }

    public Optional<PropBodyData> getBodyData(String bodyName) {

        return getProps().stream()
            .filter(prop -> prop.getType().equals("blueprintBody"))
            .filter(prop -> prop.getBodyData().getBody().equals(bodyName))
            .map(prop -> prop.getBodyData())
            .findFirst();
    }

    public Optional<PropPartData> getPartData(String partName) {

        return getProps().stream()
            .filter(prop -> prop.getType().equals("blueprintPart"))
            .filter(prop -> prop.getPartData().getPart().equals(partName))
            .map(prop -> prop.getPartData())
            .findFirst();
    }

    public PropData getGemData(int id) {

        for (PropData data : getGems()) {
            if (data.getId() == id) {
                return data;
            }
        }
        return null;
    }

    public PropData getChipData(int id) {

        for (PropData data : getChips()) {
            if (data.getId() == id) {
                return data;
            }
        }
        return null;
    }

    public PropData getData(int id) {

        for (PropData data : props) {
            if (data.getId() == id) {
                return data;
            }
        }
        return null;
    }

    public PropData getData(String name) {

        for (PropData data : props) {
            if (data.getName().equals(name)) {
                return data;
            }
        }
        return null;
    }

    public List<PropData> getInstanceDrop() {

        if (instanceDrop.size() == 0) {
            for (PropData data : props) {
                if (data.getType().equals("Instance Drop")) {
                    instanceDrop.add(data);
                }
            }
        }
        return instanceDrop;
    }

    public List<PropData> getChips() {

        if (chips.size() == 0) {
            for (PropData data : props) {
                if (data.getType().equals("chip")) {
                    chips.add(data);
                }
            }
        }
        return chips;
    }

    public List<PropData> getGems() {

        if (gems.size() == 0) {
            for (PropData data : props) {
                if (data.getType().equals("gem")) {
                    gems.add(data);
                }
            }
        }
        return gems;
    }

    public List<PropData> getCommanders() {

        if (commanders.size() == 0) {
            for (PropData data : props) {
                if (data.getType().equals("commander")) {
                    commanders.add(data);
                }
            }
        }
        return commanders;
    }

    public List<PropData> getScrolls() {

        if (scrolls.size() == 0) {
            for (PropData data : props) {
                if (data.getType().equals("scroll")) {
                    scrolls.add(data);
                }
            }
        }
        return scrolls;
    }

    public List<PropData> getInSell() {

        if (inSell.size() == 0) {
            for (PropData data : props) {
                if (data.hasMallData()) {
                    inSell.add(data);

                }
            }
        }
        return inSell;
    }

}
