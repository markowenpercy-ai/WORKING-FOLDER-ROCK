package com.go2super.database.entity.type;

import lombok.Getter;

public enum DashboardRank {

    ADMIN(0, 100, true, "*"),
    GM(1, 107, false, "permission.restart", "permission.broadcast", "permission.i.g.c", "permission.spy", "permission.warn", "permission.mute", "permission.ban", "permission.icon", "permission.staff", "permission.discord", "permission.block", "permission.blackList", "permission.hasblock", "permission.notdisturb");

    @Getter
    private final int id;
    @Getter
    private final int prefix;
    @Getter
    private final boolean admin;

    @Getter
    private final String[] permissions;

    DashboardRank(int id, int prefix, boolean admin, String... permissions) {

        this.id = id;
        this.prefix = prefix;
        this.admin = admin;

        if (permissions == null) {

            this.permissions = new String[0];
            return;

        }

        this.permissions = permissions;

    }

    public boolean hasPermission(String permission) {

        if (isAdmin()) {
            return true;
        }
        if (permission == null) {
            return false;
        }

        for (String rankPermission : permissions) {
            if (rankPermission.equals(permission) || rankPermission.equals("*")) {
                return true;
            }
        }

        return false;

    }

}
