package com.go2super.database.entity.type;

import lombok.Getter;

public enum UserRank {
    PALT(-2, -1, false, "permission.discord", "permission.block", "permission.blackList", "permission" +
            ".hasblock", "permission.notdisturb", "permission.openbag", "permission.event", "permission.coupon", "permission.giveuser"),
    ALT(-1, -1, false, "permission.discord", "permission.block", "permission.blackList", "permission" +
            ".hasblock", "permission.notdisturb", "permission.openbag", "permission.event", "permission.coupon", "permission.giveuser"),
    ADMIN(0, 100, true, "*"),
    GM(1, 107, false, "permission.restart", "permission.broadcast", "permission.i.g.c", "permission.spy", "permission.warn",
      "permission.mute", "permission.ban", "permission.icon", "permission.staff", "permission.discord", "permission.block",
      "permission.blackList", "permission.hasblock", "permission.notdisturb", "permission.unban", "permission.sban",
      "permission.userinfo", "permission.changenickname", "permission.openbag", "permission.event",
      "permission.coupon", "permission.eventsettings", "permission.match", "permission.feature", "permission.giveuser"),

    MOD(2, 101, false, "permission.warn", "permission.mute", "permission.ban", "permission.icon", "permission.staff",
      "permission.discord", "permission.block", "permission.blackList", "permission.hasblock", "permission.notdisturb",
      "permission.openbag", "permission.event", "permission.coupon", "permission.match", "permission.giveuser"),
    USER(3, -1, false, "permission.discord", "permission.block", "permission.blackList", "permission" +
      ".hasblock", "permission.notdisturb", "permission.openbag", "permission.event", "permission.coupon", "permission.giveuser",
      "permission.listcommanders", "permission.fleetpreset"),

    BATTLE_REVIEWER(4, 106, false, "permission.icon", "permission.discord", "permission.block", "permission.blackList", "permission.hasblock", "permission.notdisturb", "permission.get", "permission.add", "permission.openbag"),
    QA(5, 108, false, "permission.qa", "permission.openbag"),

    VIP(6, 102, false, "permission.icon", "permission.discord", "permission.block", "permission.blackList", "permission.hasblock", "permission.notdisturb", "permission.vip", "permission.openbag", "permission.giveuser", "permission.coupon", "permission.event", "permission.fleetpreset"),
    MVP(7, 104, false, "permission.icon", "permission.discord", "permission.block", "permission.blackList", "permission.hasblock", "permission.notdisturb", "permission.mvp", "permission.openbag", "permission.giveuser",  "permission.coupon", "permission.event", "permission.fleetpreset"),

    ;

    @Getter
    private final int id;
    @Getter
    private final int prefix;
    @Getter
    private final boolean admin;

    @Getter
    private final String[] permissions;

    UserRank(int id, int prefix, boolean admin, String... permissions) {

        this.id = id;
        this.prefix = prefix;
        this.admin = admin;

        if (permissions == null) {

            this.permissions = new String[0];
            return;

        }

        this.permissions = permissions;

    }

    public boolean hasAnyPermission(String[] permissions) {

        for (String permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
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
