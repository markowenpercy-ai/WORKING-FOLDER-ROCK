package com.go2super.hooks.discord.claim;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RayoPackage {

    private String packageName;
    private String packageId;

    private int propId;
    private int amount;

    private boolean custom;

}
