package com.go2super.resources.data.props;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PropContainerData extends PropMetaData {

    private PropContentData[] contents;

    public PropContentData getFirstContent() {

        if (contents.length > 0) {
            return contents[0];
        }
        return null;
    }

}
