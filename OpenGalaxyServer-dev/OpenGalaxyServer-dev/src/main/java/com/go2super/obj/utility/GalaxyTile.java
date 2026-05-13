package com.go2super.obj.utility;

public class GalaxyTile {

    public static final int MAP_HEIGHT = GalaxyZone.MAP_HEIGHT * GalaxyZone.ZONE_HEIGHT;

    public static final int DIRECTION_NONE = -1;
    public static final int DIRECTION_NORTH = 0;
    public static final int DIRECTION_EAST = 1;
    public static final int DIRECTION_SOUTH = 2;
    public static final int DIRECTION_WEST = 3;

    private int x;
    private int y;

    public GalaxyTile() {

    }

    public GalaxyTile(int x, int y) {

        this.x = x;
        this.y = y;
    }

    public GalaxyTile(int galaxyId) {

        this.x = galaxyId / MAP_HEIGHT;
        this.y = galaxyId % MAP_HEIGHT;
    }

    public int getX() {

        return this.x;
    }

    public int getY() {

        return this.y;
    }

    public int galaxyId() {

        return this.x * MAP_HEIGHT + this.y;
    }

    public GalaxyTile offset(GalaxyRegion offset) {

        return new GalaxyTile(offset.getMinBoundingTile().galaxyId() + this.galaxyId());
    }

    public GalaxyTile offset(GalaxyZone offset) {

        return new GalaxyTile(offset.getMinBoundingTile().galaxyId() + this.galaxyId());
    }

    public GalaxyRegion getParentRegion() {

        return new GalaxyRegion(this.x / GalaxyRegion.REGION_WIDTH, this.y / GalaxyRegion.REGION_HEIGHT);
    }

    public GalaxyZone getParentZone() {

        return new GalaxyZone(this.x / GalaxyZone.ZONE_WIDTH, this.y / GalaxyZone.ZONE_HEIGHT);
    }

    public GameCell getCell() {

        return GameCell.of(x, y);
    }

    public GameCell getEntrance(GalaxyTile from) {

        if (from.getX() >= getX() && from.getY() > getY()) {
            return GameCell.of(24, 24); // South (Bottom)
        }
        if (from.getX() > getX()) {
            return GameCell.of(24, 0); // East (Right)
        }
        if (from.getY() >= getY()) {
            return GameCell.of(0, 24); // West (Left)
        }
        return GameCell.of(0, 0); // North (Top)
    }

    public double getDistance(GalaxyTile gt) {

        int dX = (int) Math.pow(gt.x - this.x, 2);
        int dY = (int) Math.pow(gt.y - this.y, 2);
        return Math.sqrt(dX + dY);
    }

    public double getManhattanDistance(GalaxyTile gt) {

        return Math.abs(gt.x - this.x) + Math.abs(gt.y - this.y);
    }

    public int getDirection(GalaxyTile gt) {

        int dX = gt.x - this.x;
        int dY = gt.y - this.y;
        if (dX <= 0 && dY < 0) {
            return DIRECTION_NORTH;
        } else if (dX > 0 && dY <= 0) {
            return DIRECTION_EAST;
        } else if (dX >= 0 && dY > 0) {
            return DIRECTION_SOUTH;
        } else if (dX < 0 && dY >= 0) {
            return DIRECTION_WEST;
        }
        //should only return if dX == 0 && dY == 0
        //or in other words, when THIS is the same tile as GT
        return DIRECTION_NONE;
    }

    public boolean equals(GalaxyTile gt) {

        return this.x == gt.x && this.y == gt.y;
    }

    public String toString() {

        return "GalaxyTile#" + galaxyId() + " (" + this.x + ", " + this.y + ")";
    }

}
