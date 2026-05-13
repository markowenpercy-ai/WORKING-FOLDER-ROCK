package com.go2super.obj.utility;

public class GalaxyZone {

    public static final int ZONE_WIDTH = 60;
    public static final int ZONE_HEIGHT = 60;
    public static final int MAP_HEIGHT = 7;

    private final int x;
    private final int y;

    public GalaxyZone(int x, int y) {

        this.x = x;
        this.y = y;
    }

    public GalaxyZone(int zoneId) {

        this.x = zoneId / MAP_HEIGHT;
        this.y = zoneId % MAP_HEIGHT;
    }

    public int getX() {

        return this.x;
    }

    public int getY() {

        return this.y;
    }

    public int zoneId() {

        return this.x * MAP_HEIGHT + this.y;
    }

    public GalaxyTile getMinBoundingTile() {

        return new GalaxyTile(this.x * ZONE_WIDTH, this.y * ZONE_HEIGHT);
    }

    public GalaxyTile getMaxBoundingTile() {

        return new GalaxyTile(this.x * ZONE_WIDTH + ZONE_WIDTH - 1, this.y * ZONE_HEIGHT + ZONE_HEIGHT - 1);
    }

    public GalaxyRegion getMinBoundingRegion() {

        return new GalaxyRegion(this.x * ZONE_WIDTH / GalaxyRegion.REGION_WIDTH, this.y * ZONE_HEIGHT / GalaxyRegion.REGION_HEIGHT);
    }

    public GalaxyRegion getMaxBoundingRegion() {

        final int W = ZONE_WIDTH / GalaxyRegion.REGION_WIDTH;
        final int H = ZONE_HEIGHT / GalaxyRegion.REGION_HEIGHT;

        return new GalaxyRegion(this.x * W + W - 1, this.y * H + H - 1);
    }

    public boolean contains(GalaxyTile gt) {

        return this.equals(gt.getParentZone());
    }

    public boolean contains(GalaxyRegion gr) {

        return this.equals(gr.getParentZone());
    }

    public boolean equals(GalaxyZone gz) {

        return this.x == gz.x && this.y == gz.y;
    }

    public String toString() {

        return "GalaxyZone#" + zoneId() + " (" + this.x + ", " + this.y + ")";
    }

}
