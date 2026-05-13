package com.go2super.listener;

import com.go2super.database.entity.Planet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserPlanet;
import com.go2super.database.entity.sub.UserResources;
import com.go2super.database.entity.sub.UserTerritories;
import com.go2super.database.entity.sub.UserTerritory;
import com.go2super.obj.game.FieldResource;
import com.go2super.obj.game.FriendFieldStatus;
import com.go2super.obj.game.IntegerArray;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.obj.utility.VariableType;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.field.*;
import com.go2super.packet.station.RequestUpdatePlayerNamePacket;
import com.go2super.packet.station.ResponseUpdatePlayerNamePacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.FarmLandData;
import com.go2super.resources.data.FieldResourceData;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.ResourcesService;
import com.go2super.service.UserService;
import com.go2super.service.exception.BadGuidException;
import com.go2super.socket.util.DateUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.*;

public class StationListener implements PacketListener {

    String usernamePattern = "^[\\w\\d\\S]{1,32}$";
    Pattern pattern = Pattern.compile(usernamePattern);

    @PacketProcessor
    public void onGetFieldResource(RequestGetFieldResourcePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserPlanet userPlanet = user.getPlanet();
        UserTerritories territories = user.getTerritories();

        UserTerritory userTerritory = null;
        boolean celestialBase = true;

        if (packet.getGalaxyId() != -1) {

            FarmLandData reference = userPlanet.getAdjacentByGalaxyId(packet.getGalaxyId());

            if (reference == null) {
                return;
            }

            userTerritory = territories.findByFarmLandId(reference.getId());
            celestialBase = false;

        }

        if (celestialBase) {

            ResponseGetFieldResourcePacket response = new ResponseGetFieldResourcePacket();
            response.setGalaxyId(packet.getGalaxyId());

            FieldResourceData fieldResourceData = ResourceManager.getFieldResources().findByName("field:celestial.industrial.base");

            if (territories.getCelestialUntil() == null) {

                territories.setCelestialUntil(null);
                user.save();
                return;

            }

            if (territories.getCelestialUntil().after(new Date())) {
                return;
            }
            int totalProduction = fieldResourceData.getGeneration().getValue();

            if ("resource:voucher".equals(fieldResourceData.getGeneration().getResource())) {
                user.getResources().addVouchers(totalProduction);
                response.setCoins(totalProduction);
            }

            territories.setCelestialHelpers(new ArrayList<>());
            territories.setCelestialUntil(null);

            user.getMetrics().add("action:harvest.celestial", 1);
            user.update();
            user.save();

            packet.reply(response);
            return;

        }

        if (userTerritory == null) {
            return;
        }
        if (userTerritory.getFieldId() == -1) {
            return;
        }

        user.getStats().addExp(100);

        FieldResourceData fieldResourceData = ResourceManager.getFieldResources().findById(userTerritory.getFieldId());
        int totalProduction = userTerritory.getTotalProduction();

        ResponseGetFieldResourcePacket response = new ResponseGetFieldResourcePacket();
        response.setGalaxyId(packet.getGalaxyId());

        switch (fieldResourceData.getGeneration().getResource()) {

            case "resource:metal":
                user.getResources().addMetal(totalProduction);
                response.setMetal(totalProduction);
                break;

            case "resource:he3":
                user.getResources().addHe3(totalProduction);
                response.setGas(totalProduction);
                break;

        }

        userTerritory.setTotalProduction(0);
        userTerritory.setDesiredProduction(0);
        userTerritory.setThieves(new ArrayList<>());
        userTerritory.setFieldId(-1);
        userTerritory.setUntil(null);

        user.getMetrics().add("action:harvest.anycomet", 1);
        user.update();
        user.save();

        packet.reply(ResourcesService.getInstance().getPlayerResourcePacket(user));
        packet.reply(response);

    }

    @PacketProcessor
    public void onDeleteFieldResource(RequestDeleteFieldResourcePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null) {
            return;
        }

        UserPlanet userPlanet = user.getPlanet();
        UserTerritories territories = user.getTerritories();

        FarmLandData reference = userPlanet.getAdjacentByGalaxyId(packet.getGalaxyId());

        if (reference == null) {
            return;
        }

        UserTerritory userTerritory = territories.findByFarmLandId(reference.getId());

        if (userTerritory == null) {
            return;
        }

        if (userTerritory.getFieldId() == -1) {
            return;
        }

        userTerritory.setThieves(new ArrayList<>());
        userTerritory.setUntil(null);
        userTerritory.setTotalProduction(0);
        userTerritory.setDesiredProduction(0);
        userTerritory.setFieldId(-1);

        user.save();

    }

    @PacketProcessor
    public void onGrowFieldResource(RequestGrowFieldResourcePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserPlanet userPlanet = user.getPlanet();
        UserTerritories territories = user.getTerritories();

        FarmLandData reference = userPlanet.getAdjacentByGalaxyId(packet.getGalaxyId());
        if (reference == null) {
            return;
        }

        FieldResourceData fieldResourceData = ResourceManager.getFieldResources().findById(packet.getResourceId());
        if (fieldResourceData == null) {
            return;
        }

        UserTerritory userTerritory = territories.findByFarmLandId(reference.getId());

        if (userTerritory == null) {

            userTerritory = UserTerritory.builder()
                .farmLandId(reference.getId())
                .fieldId(-1)
                .thieves(new ArrayList<>())
                .build();

            territories.getTerritories().add(userTerritory);

        }

        if (userTerritory.getFieldId() != -1) {
            return;
        }
        int totalProduction = fieldResourceData.getGeneration().getValue();

        userTerritory.setTotalProduction(totalProduction);
        userTerritory.setDesiredProduction(totalProduction);
        userTerritory.setFieldId(fieldResourceData.getId());
        userTerritory.setUntil(DateUtil.now(fieldResourceData.getTime()));

        user.getMetrics().add("action:craft.anycomet", 1);
        user.update();
        user.save();

        ResponseGrowFieldResourcePacket response = new ResponseGrowFieldResourcePacket();

        response.setResourceId(packet.getResourceId());
        response.setErrorCode(0);

        response.setNeedTime(fieldResourceData.getTime());
        response.setGalaxyId(packet.getGalaxyId());
        response.setNum(totalProduction);

        packet.reply(response);

    }

    @PacketProcessor
    public void onUpdatePlayerName(RequestUpdatePlayerNamePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        String name = "";

        for (char symbol : packet.getName().getValue().toCharArray()) {

            if ((byte) symbol == 0x00) {
                break;
            }

            name = name + symbol;

        }

        Matcher matcher = pattern.matcher(name);

        if (!matcher.find() || !StringUtils.isAlphanumeric(name)) {

            ResponseUpdatePlayerNamePacket response = ResponseUpdatePlayerNamePacket
                .builder()
                .errorCode((byte) 1) // 1 = EL NOMBRE NO ES VALIDO
                .build();

            packet.reply(response);
            return;

        }

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid()); // guid
        Optional<User> optionalUser = UserService.getInstance().getUserCache().findByUsername(name); // username

        if (user == null) {
            return;
        }

        if (optionalUser.isPresent()) {

            ResponseUpdatePlayerNamePacket response = ResponseUpdatePlayerNamePacket
                .builder()
                .errorCode((byte) 2) // 2 = USUARIO YA EXISTE
                .build();

            packet.reply(response);
            return;

        }

        UserResources resources = user.getResources();

        if (resources.getMallPoints() < 150) {

            ResponseUpdatePlayerNamePacket response = ResponseUpdatePlayerNamePacket
                .builder()
                .errorCode((byte) 3) // 3 = MALL POINTS INCOMPLETOS
                .build();

            packet.reply(response);
            return;

        }

        resources.setMallPoints(resources.getMallPoints() - 150);
        user.setUsername(name);

        ResponseUpdatePlayerNamePacket response = ResponseUpdatePlayerNamePacket
            .builder()
            .errorCode((byte) 0) // 0 = CAMBIO EXITOSO
            .build();

        user.save();
        packet.reply(response);

    }

    @PacketProcessor
    public void onFieldResource(RequestFieldResourcePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Planet planet = PacketService.getInstance().getPlanetCache().findByPosition(new GalaxyTile(packet.getGalaxyId()));
        if (planet == null || !(planet instanceof UserPlanet targetPlanet)) {
            return;
        }

        Optional<User> optionalTargetUser = targetPlanet.getUser();

        if (optionalTargetUser.isEmpty()) {
            return;
        }
        User targetUser = optionalTargetUser.get();
        UserTerritories userTerritories = targetUser.getTerritories();

        List<FarmLandData> farmLands = targetPlanet.getAdjacentFarmLands();
        List<FieldResource> fieldResources = new ArrayList<>();

        for (FarmLandData farmLand : farmLands) {

            GalaxyTile farmLandTile = farmLand.calculateTile(targetPlanet);
            UserTerritory territory = userTerritories.findByFarmLandId(farmLand.getId());

            if (territory != null) {

                int spareTime = territory.getUntil() == null ? 0 : DateUtil.remains(territory.getUntil()).intValue();
                spareTime = (spareTime < 0 ? 0 : spareTime);

                List<Integer> thieves = territory.getThieves();

                if (thieves == null) {
                    thieves = new ArrayList<>();
                }

                boolean canThieve = thieves.size() < 5;

                fieldResources.add(new FieldResource(spareTime, targetUser.getGuid(), farmLandTile.galaxyId(), territory.getTotalProduction(), territory.getFieldId(), 0, thieves.size(), canThieve ? 1 : 0));
                continue;

            }

            fieldResources.add(new FieldResource(0, targetUser.getGuid(), farmLandTile.galaxyId(), 0, -1, 0, 0, 0));
            continue;

        }

        for (int i = fieldResources.size(); i < 12; i++) {
            fieldResources.add(new FieldResource(0, -1, -1, 0, -1, 1, 0, 0));
        }

        List<Integer> helpers = userTerritories.getCelestialHelpers();
        int[] helpGuid = new int[VariableType.MAX_HELPCOUNT];

        for (int i = 0; i < VariableType.MAX_HELPCOUNT; i++) {
            if (helpers.size() > i) {
                helpGuid[i] = helpers.get(i);
            } else {
                helpGuid[i] = -1;
            }
        }

        int centerStatus = 0;
        int time = 0;

        if (helpers.size() == VariableType.MAX_HELPCOUNT) {

            centerStatus = 1;

            if (userTerritories.getCelestialUntil() == null) {
                FieldResourceData fieldResourceData = ResourceManager.getFieldResources().findByName("field:celestial.industrial.base");
                userTerritories.setCelestialUntil(DateUtil.now(fieldResourceData.getTime()));
            }

            time = DateUtil.remains(userTerritories.getCelestialUntil()).intValue();

        }

        // todo value bonusses
        ResponseFieldResourcePacket response = ResponseFieldResourcePacket.builder()

            .galaxyMapId(packet.getGalaxyMapId())
            .galaxyId(packet.getGalaxyId())

            .consortiaPer((byte) 0)

            .friendFlag((byte) (user.getGuid() == targetUser.getGuid() ? 0 : !user.isFriend(targetUser.getGuid()) ? 2 : 1))
            .fieldCenterStatus((byte) centerStatus)

            .techPerMetal((byte) 0)
            .techPerGas((byte) 0)
            .techPerMoney((byte) 0)

            .propsPerMetal((byte) 0)
            .propsPerMoney((byte) 0)
            .propsPerGas((byte) 0)

            .data(fieldResources)
            .fieldCenterTime(time)
            .helpGuid(new IntegerArray(helpGuid))
            .build();

        packet.reply(response);

    }


    @PacketProcessor
    public void onFriendFieldStatus(RequestFriendFieldStatusPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        List<User> requested = new ArrayList<>();

        // Only available for "Game Friends"
        // so packet.getData() will be only guid's
        if (packet.getKind() != 0) {
            return;
        }

        for (Long requestId : packet.getData().getArray()) {

            int requestGuid = requestId.intValue();

            if (!user.isFriend(requestGuid)) {
                continue;
            }

            requested.add(UserService.getInstance().getUserCache().findByGuid(requestGuid));

        }

        List<FriendFieldStatus> friendsStatus = new ArrayList<>();

        if (requested.size() <= 0) {

            ResponseFriendFieldStatusPacket response = new ResponseFriendFieldStatusPacket();

            response.setDataLen((short) 0);
            response.setKind((short) 0);
            response.setData(new ArrayList<>());

            packet.reply(response);
            return;

        }

        boolean sentMoreInfos = false;

        for (User friend : requested) {

            UserTerritories friendTerritories = friend.getTerritories();

            if (friendTerritories.getCelestialHelpers() == null) {
                friendTerritories.setCelestialHelpers(new ArrayList<>());
            }

            boolean canHelp = false;
            boolean canThieve = false;

            if (friendTerritories.getCelestialHelpers().size() != VariableType.MAX_HELPCOUNT &&
                !friendTerritories.getCelestialHelpers().contains(user.getGuid())) {
                canHelp = true;
            }

            for (UserTerritory friendTerritory : friendTerritories.getTerritories()) {

                if (friendTerritory.getThieves() == null) {
                    friendTerritory.setThieves(new ArrayList<>());
                }

                if (friendTerritory.getFieldId() == -1 ||
                    friendTerritory.getThieves().size() >= VariableType.MAX_FIELDTHIEVES ||
                    friendTerritory.getTotalProduction() <= 0) {
                    continue;
                }

                if (friendTerritory.getThieves().contains(user.getGuid())) {
                    continue;
                }

                if (friendTerritory.getUntil() == null || DateUtil.remains(friendTerritory.getUntil()) > 0) {
                    continue;
                }

                canThieve = true;
                break;

            }

            friendsStatus.add(
                new FriendFieldStatus(friend.getUserId(),
                    friend.getGuid(),
                    friend.getPlanet().getPosition().getParentRegion().regionId(),
                    friend.getPlanet().getPosition().galaxyId(),
                    (short) -1,
                    (char) (canHelp ? 1 : -1),
                    (char) (canThieve ? 1 : -1)));
            if (PacketService.getInstance().sendMoreInfoPacket(1, friend.getUserId(), packet)) {
                sentMoreInfos = true;
            }

        }

        ResponseFriendFieldStatusPacket response = new ResponseFriendFieldStatusPacket();

        response.setDataLen((short) friendsStatus.size());
        response.setKind(packet.getKind());
        response.setData(friendsStatus);

        if (sentMoreInfos) {
            packet.reply(response);
        } else {
            packet.reply(response);
        }

    }

    @PacketProcessor
    public void onFriendHelpFieldCenter(RequestHelpFieldCenterResourcePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        User friend = UserService.getInstance().getUserCache().findByGuid(packet.getObjGuid());
        if (friend == null || !user.isFriend(friend.getGuid())) {
            return;
        }

        UserTerritories friendTerritories = friend.getTerritories();

        if (friendTerritories.getCelestialHelpers() == null) {
            friendTerritories.setCelestialHelpers(new ArrayList<>());
        }

        if (friendTerritories.getCelestialHelpers().size() >= VariableType.MAX_HELPCOUNT ||
            friendTerritories.getCelestialHelpers().contains(user.getGuid())) {
            return;
        }

        friendTerritories.getCelestialHelpers().add(user.getGuid());

        if (friendTerritories.getCelestialHelpers().size() >= VariableType.MAX_HELPCOUNT) {

            FieldResourceData fieldResourceData = ResourceManager.getFieldResources().findByName("field:celestial.industrial.base");
            friendTerritories.setCelestialUntil(DateUtil.now(fieldResourceData.getTime()));

        }

        user.save();
        friend.save();

        ResponseHelpFieldCenterResourcePacket response = new ResponseHelpFieldCenterResourcePacket();

        response.setErrorCode(0);
        response.setObjGuid(friend.getGuid());

        packet.reply(response);

    }

    @PacketProcessor
    public void onThieveFieldResource(RequestThieveFieldResourcePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        User friend = UserService.getInstance().getUserCache().findByGuid(packet.getObjGuid());
        if (friend == null || !user.isFriend(friend.getGuid())) {
            return;
        }

        UserPlanet friendPlanet = friend.getPlanet();
        UserTerritories friendTerritories = friend.getTerritories();

        FarmLandData reference = friendPlanet.getAdjacentByGalaxyId(packet.getObjGalaxyId());
        if (reference == null) {
            return;
        }

        UserTerritory friendTerritory = friendTerritories.findByFarmLandId(reference.getId());
        if (user.getStats().getSp() - 1 < 0) {
            return;
        }
        if (friendTerritory == null || friendTerritory.getUntil() == null || friendTerritory.getFieldId() == -1) {
            return;
        }

        FieldResourceData fieldResourceData = ResourceManager.getFieldResources().findById(friendTerritory.getFieldId());
        if (DateUtil.remains(friendTerritory.getUntil()) > 0 || friendTerritory.getTotalProduction() <= 0) {
            return;
        }

        if (friendTerritory.getThieves() == null) {
            friendTerritory.setThieves(new ArrayList<>());
        }

        if (friendTerritory.getThieves().size() >= VariableType.MAX_FIELDTHIEVES ||
            friendTerritory.getThieves().contains(user.getGuid())) {
            return;
        }

        user.getStats().setSp(user.getStats().getSp() - 1);
        user.getStats().addExp(100);

        int reduce = friendTerritory.getDesiredProduction() / 10;
        int totalProduction = friendTerritory.getTotalProduction() - reduce;

        friendTerritory.setTotalProduction(totalProduction);
        friendTerritory.getThieves().add(user.getGuid());

        ResponseThieveFieldResourcePacket response = new ResponseThieveFieldResourcePacket();

        response.setErrorCode(0);
        response.setGalaxyId(packet.getObjGalaxyId());

        switch (fieldResourceData.getGeneration().getResource()) {

            case "resource:metal":
                user.getResources().addMetal(reduce);
                response.setMetal(reduce);
                break;

            case "resource:he3":
                user.getResources().addHe3(reduce);
                response.setGas(reduce);
                break;

        }

        user.save();
        friend.save();

        packet.reply(response);

    }

}
