package com.go2super.obj.utility;

public class GalaxyRegion {

    public static final int REGION_WIDTH = 10;
    public static final int REGION_HEIGHT = 10;
    public static final int MAP_HEIGHT = GalaxyZone.MAP_HEIGHT * GalaxyZone.ZONE_HEIGHT / REGION_HEIGHT;

    private final int x;
    private final int y;

    public GalaxyRegion(int x, int y) {

        this.x = x;
        this.y = y;
    }

    public GalaxyRegion(int regionId) {

        this.x = regionId / MAP_HEIGHT;
        this.y = regionId % MAP_HEIGHT;
    }

    public int getX() {

        return this.x;
    }

    public int getY() {

        return this.y;
    }

    public int regionId() {

        return this.x * MAP_HEIGHT + this.y;
    }

    public GalaxyTile offset(GalaxyZone offset) {

        return new GalaxyTile(offset.getMinBoundingRegion().regionId() + this.regionId());
    }

    public GalaxyTile getMinBoundingTile() {

        return new GalaxyTile(this.x * REGION_WIDTH, this.y * REGION_HEIGHT);
    }

    public GalaxyTile getMaxBoundingTile() {

        return new GalaxyTile(this.x * REGION_WIDTH + REGION_WIDTH - 1, this.y * REGION_HEIGHT + REGION_HEIGHT - 1);
    }

    public GalaxyZone getParentZone() {

        return new GalaxyZone(this.x * REGION_WIDTH / GalaxyZone.ZONE_WIDTH, this.y * REGION_HEIGHT / GalaxyZone.ZONE_HEIGHT);
    }

    public boolean contains(GalaxyTile gt) {

        return this.equals(gt.getParentRegion());
    }

    public boolean equals(GalaxyRegion gr) {

        return this.x == gr.x && this.y == gr.y;
    }

    public String toString() {

        return "GalaxyRegion#" + regionId() + " (" + this.x + ", " + this.y + ")";
    }

}
