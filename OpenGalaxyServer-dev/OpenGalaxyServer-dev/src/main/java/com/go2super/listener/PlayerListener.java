package com.go2super.listener;

import com.go2super.database.entity.Corp;
import com.go2super.database.entity.Planet;
import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.*;
import com.go2super.obj.game.ShipBodyInfo;
import com.go2super.obj.game.*;
import com.go2super.obj.type.BonusType;
import com.go2super.obj.utility.*;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.boot.RequestPlayerInfoPacket;
import com.go2super.packet.boot.ResponseRoleInfoPacket;
import com.go2super.packet.chat.RequestLookupUserInfoPacket;
import com.go2super.packet.chat.RequestUserInfoPacket;
import com.go2super.packet.chat.ResponseLookupUserInfoPacket;
import com.go2super.packet.chat.ResponseUserInfoPacket;
import com.go2super.packet.commander.ResponseCommanderBaseInfoPacket;
import com.go2super.packet.construction.RequestStorageResourcePacket;
import com.go2super.packet.construction.ResponseStorageResourcePacket;
import com.go2super.packet.galaxy.RequestDeleteServerPacket;
import com.go2super.packet.galaxy.RequestGameServerListPacket;
import com.go2super.packet.instance.ResponseEctypePassPacket;
import com.go2super.packet.instance.ResponseEctypeStatePacket;
import com.go2super.packet.mail.ResponseNewEmailNoticePacket;
import com.go2super.packet.map.ResponseMapAreaPacket;
import com.go2super.packet.map.ResponseMapBlockPacket;
import com.go2super.packet.props.ResponsePropsInfoPacket;
import com.go2super.packet.reward.ResponseOnlineAwardPacket;
import com.go2super.packet.science.ResponseTechInfoPacket;
import com.go2super.packet.science.ResponseTechUpgradeInfoPacket;
import com.go2super.packet.ship.ResponseShipBodyInfoPacket;
import com.go2super.packet.shipmodel.ResponseCreateShipInfoPacket;
import com.go2super.packet.shipmodel.ResponseShipModelInfoDelPacket;
import com.go2super.packet.shipmodel.ResponseShipModelInfoPacket;
import com.go2super.packet.social.RequestFriendInfoPacket;
import com.go2super.packet.social.ResponseFriendInfoPacket;
import com.go2super.packet.task.ResponseTaskInfoPacket;
import com.go2super.packet.upgrade.ResponseShipBodyUpgradeInfoPacket;
import com.go2super.server.GameServerReceiver;
import com.go2super.service.*;
import com.go2super.service.exception.BadGuidException;
import com.go2super.socket.util.DateUtil;
import lombok.SneakyThrows;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class PlayerListener implements PacketListener {

    @SneakyThrows
    @PacketProcessor
    public void onRequestPlayerInfo(RequestPlayerInfoPacket packet) {

        GameServerReceiver serverReceiver = (GameServerReceiver) packet.getSmartServer();
        if (serverReceiver.getGuid() != packet.getGuid()) {
            return;
        }

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        // ? Update
        user.update();
        user.save();

        // ? Refresh
        user.getBuildings().refresh();

        // ? Start new information packets
        packet.reply(PacketService.getInstance().getConfigurationPacket(user));
        packet.reply(PacketService.getInstance().getMoreInfoPacket(0, user));

        // ? Send default game packets
        // ResponseMapBlockPacket MSG_RESP_MAPBLOCK [1]
        packet.reply(getMapPacket());

        // ResponseShipModelInfoPacket MSG_RESP_SHIPMODELINFO [2]
        packet.reply(getShipModelInfoPacket(user));
        packet.reply(getShipModelInfoDelPacket(user));

        // ResponseTechInfoPacket MSG_RESP_TECHINFO [3]
        packet.reply(getTechInfoPacket(user));

        // ResponseTechUpgradeInfoPacket MSG_RESP_TECHUPGRADEINFO [4]
        packet.reply(getTechUpgradeInfoPacket(user));

        // ResponsePropsInfoPacket MSG_RESP_PROPSINFO [5]
        packet.reply(getPropsInfoPacket(user));

        // ResponsePropsInfoPacket MSG_RESP_PROPSINFO [5, CORP_INVENTORY]
        packet.reply(getCorpPropsInfoPacket(user));

        // ResponseShipBodyInfoPacket MSG_RESP_SHIPBODYINFO [6]
        packet.reply(getShipBodyInfoPacket(user));

        // ResponseShipBodyUpgradeInfoPacket MSG_RESP_SHIPBODYUPGRADEINFO [7]
        packet.reply(getShipBodyUpgradeInfoPacket(user));

        // ResponseCommanderBaseInfoPacket MSG_RESP_COMMANDERBASEINFO [8]
        packet.reply(getCommanderBaseInfoPacket(user));

        // ResponseTaskInfoPacket MSG_RESP_TASKINFO [9]
        packet.reply(getTaskInfoPacket(user));

        // ResponseRoleInfoPacket MSG_ROLE_INFO [10]
        ResponseRoleInfoPacket roleInfoPacket = ResourcesService.getInstance().getRoleInfoPacket(user);
        if (roleInfoPacket != null) {
            packet.reply(roleInfoPacket);
        }

        // ResponsePlayerResourcePacket MSG_RESP_PLAYERRESOURCE [11]
        packet.reply(ResourcesService.getInstance().getPlayerResourcePacket(user));

        // ResponseCreateShipInfoPacket MSG_RESP_CREATESHIPINFO [12]
        packet.reply(getCreateShipInfoPacket(user));

        // ResponseTimeQueuePacket MSG_RESP_TIMEQUEUE [13]
        if (!user.getStats().getBuffs().isEmpty()) {
            packet.reply(user.getQueuesAsPacket());
        }

        // ResponseEctypePassPacket MSG_RESP_ECTYPEPASS [14]
        packet.reply(getEctypePassPacket(user));

        // ResponseEctypePassPacket MSG_RESP_ECTYPESTATE [15]
        ResponseEctypeStatePacket ectypeStatePacket = BattleService.getInstance().getCurrentEctypeState(user);
        if (ectypeStatePacket.getEctypeId() != -1 && ectypeStatePacket.getState() != 0) {
            packet.reply(ectypeStatePacket);
        }

        // ResponseOnlineAwardPacket MSG_RESP_ONLINEAWARD [16]
        ResponseOnlineAwardPacket onlineAwardPacket = ResourcesService.getInstance().getOnlineAwardPacket(user);
        if (onlineAwardPacket != null) {
            packet.reply(onlineAwardPacket);
        }

        // ResponseNewMailNoticePacket MSG_RESP_NEWEMAILNOTICE [17]
        if (user.getUserEmailStorage() != null && user.getUserEmailStorage().getUserEmails() != null && user.getUserEmailStorage().getUserEmails().stream().anyMatch(email -> email.getReadFlag() == 0)) {

            ResponseNewEmailNoticePacket newEmailNoticePacket = ResponseNewEmailNoticePacket.builder()
                    .errorCode(0)
                    .build();

            packet.reply(newEmailNoticePacket);

        }
        ShipFactoryService.getInstance().catchUpUserFactory(user);
    }

    @PacketProcessor
    public void onLookupUserInfo(RequestLookupUserInfoPacket packet) {

        GalaxyTile tile = new GalaxyTile(packet.getObjGalaxyId());
        Planet planet = GalaxyService.getInstance().getPlanet(tile);
        ResponseLookupUserInfoPacket response = new ResponseLookupUserInfoPacket();

        if (planet == null) {

            User user = null;

            if (packet.getObjGuid() != 0 && packet.getObjGuid() != -1) {
                user = UserService.getInstance().getUserCache().findByGuid(packet.getObjGuid());
            }

            if (user == null) {

                response.setPosY(0);
                response.setPosX(0);
                response.setGalaxyId(-1);
                response.setType(-1);
                response.setGuid(-1);
                response.getUserName().value("");

                packet.reply(response);

                return;

            }

            planet = user.getPlanet();

        }

        ResponseUserInfoPacket lookup;

        switch (planet.getType()) {

            case USER_PLANET:
                lookup = getUserPlanetInfo(planet);
                break;

            case HUMAROID_PLANET:
                lookup = getHumaroidUserInfo(planet);
                break;

            case RESOURCES_PLANET:
                lookup = getRBPUserInfo(planet);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + planet.getType());

        }

        response.setGuid(lookup.getGuid());
        response.setUserId(lookup.getUserId());
        response.setUserName(lookup.getUserName());

        response.setPosX(lookup.getPosX());
        response.setPosY(lookup.getPosY());
        response.setGalaxyId(lookup.getGalaxyId());
        response.setType(planet.getType().getCode());

        packet.reply(response);

    }

    @PacketProcessor
    public void onUserInfo(RequestUserInfoPacket packet) {

        LoginService.validate(packet, packet.getGuid());

        User requester = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (requester == null) {
            return;
        }

        Planet planet = null;
        User planetUser = null;

        if (packet.getObjGuid() >= 0) {

            User user = UserService.getInstance().getUserCache().findByGuid(packet.getObjGuid());
            if (user == null) {
                return;
            }

            planetUser = user;
            planet = user.getPlanet();

            PacketService.getInstance().sendMoreInfoPacket(1, planetUser, packet);

        } else if (packet.getObjGalaxyId() >= 0) {

            GalaxyTile tile = new GalaxyTile(packet.getObjGalaxyId());
            planet = GalaxyService.getInstance().getPlanet(tile);

        } else if (packet.getUserId() >= 0) {

            User user = UserService.getInstance().getUserCache().findByUserId(packet.getUserId());
            if (user == null) {
                return;
            }

            planetUser = user;
            planet = user.getPlanet();

            PacketService.getInstance().sendMoreInfoPacket(1, planetUser, packet);

        }

        if (planet == null) {
            return;
        }
        ResponseUserInfoPacket response;

        switch (planet.getType()) {

            case USER_PLANET:

                response = getUserPlanetInfo(planet);

                if (planetUser == null) {

                    planetUser = UserService.getInstance().getUserCache().findByUserId(planet.getUserId());
                    if (planetUser == null) {
                        return;
                    }

                    PacketService.getInstance().sendMoreInfoPacket(1, planetUser, packet);

                }

                break;

            case HUMAROID_PLANET:
                response = getHumaroidUserInfo(planet);
                break;

            case RESOURCES_PLANET:

                response = getRBPUserInfo(planet);
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + planet.getType());
        }

        int regionId = planet.getPosition().getParentRegion().regionId();
        ResponseMapAreaPacket mapAreaPacket = GalaxyService.getInstance().getMapAreaPacketByRegionId(requester, regionId);

        packet.reply(response, mapAreaPacket);


    }

    @PacketProcessor
    public void onGameServerListPacket(RequestGameServerListPacket packet) {


    }

    @PacketProcessor
    public void onDeleteServer(RequestDeleteServerPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        packet.getSmartServer().sendMessage("Feature not implemented yet");

        /*

        ResponseDeleteServerPacket response = new ResponseDeleteServerPacket();

        //todo system level

        UserEmailStorage userEmailStorage = user.getUserEmailStorage();


        if(userEmailStorage.getUserEmails().size() > 0){
            response.setErrorCode(4);
            packet.reply(response);
            return;
        }

        if(user.getFriends().size() > 0) {
            response.setErrorCode(5);
            packet.reply(response);
            return;
        }

        if(user.getConsortiaId() != -1){
            response.setErrorCode(3);
            packet.reply(response);
            return;
        }

        UserService.getInstance().getUserCache().delete(user);

        response.setErrorCode(0);
        packet.reply(response);

        BotLogger.log(userEmailStorage.getUserEmails().size());

        packet.reply(response);*/

    }

    @PacketProcessor
    public void onFriendInfo(RequestFriendInfoPacket packet) {

        User user = null;

        if (packet.getObjGuid() >= 0) {
            user = UserService.getInstance().getUserCache().findByGuid(packet.getObjGuid());
        } else if (packet.getObjUserId() >= 0) {
            user = UserService.getInstance().getUserCache().findByUserId(packet.getObjUserId());
        }

        if (user == null) {
            return;
        }

        UserPlanet userPlanet = user.getPlanet();

        ResponseFriendInfoPacket response = new ResponseFriendInfoPacket();

        response.setGuid(user.getGuid());
        response.setUserId(user.getUserId());
        response.setFightFlag(userPlanet.isInWar() ? 1 : 0);
        response.setStarType(0);
        response.setLevelId(user.getStats().getLevel());
        response.setExp(user.getStats().getExp());
        response.setGalaxyMapId(0);
        response.setGalaxyId(userPlanet.getPosition().galaxyId());

        packet.reply(response);

    }

    @PacketProcessor
    public void onStorageResource(RequestStorageResourcePacket packet) {

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        UserStorage storage = user.getStorage();

        WarehouseCapacity maximumStorage = UserService.getInstance().getMaxWarehouseStorage(user);

        ResponseStorageResourcePacket response = new ResponseStorageResourcePacket();

        response.setGas(storage.getHe3());
        response.setMetal(storage.getMetal());
        response.setMoney(storage.getGold());

        response.setStorageGas(maximumStorage.getHe3Capacity());
        response.setStorageMetal(maximumStorage.getMetalCapacity());
        response.setStorageMoney(maximumStorage.getGoldCapacity());

        packet.reply(response);
        UserService.getInstance().updateStats(user);

    }

    private ResponseUserInfoPacket getUserPlanetInfo(Planet planet) {

        UserPlanet userPlanet = (UserPlanet) planet;
        User user = userPlanet.getUser().orElse(null);

        ResponseUserInfoPacket packet = new ResponseUserInfoPacket();

        packet.setGuid(user.getGuid());
        packet.setUserId(userPlanet.getUserId());

        packet.getUserName().value(user.getUsername());
        packet.getConsortia().value("");

        packet.setRankId(user.getAttackPowerRank() + 1);
        packet.setPosX(userPlanet.getPosition().getX());
        packet.setPosY(userPlanet.getPosition().getY());

        List<UserBoost> truceBoosts = user.getStats().getUserBonus(BonusType.PLANET_PROTECTION);

        if (!truceBoosts.isEmpty()) {
            UserBoost userBoost = truceBoosts.get(0);
            if (userBoost.getUntil() != null && userBoost.getUntil().after(new Date())) {
                packet.setPeaceTime(DateUtil.remains(userBoost.getUntil()).intValue());
            }
        }

        packet.setGalaxyId(userPlanet.getPosition().galaxyId());

        packet.setSpaceLevel((byte) user.getSpaceStationLevel());
        packet.setCityLevel((byte) user.getCityLevel());

        packet.setLevelId(UnsignedChar.of(user.getStats().getLevel()));
        packet.setMatchLevel(UnsignedChar.of(user.getCurrentLeague()));

        packet.setPassMaxEctype(user.getStats().getInstance());

        Corp corp = CorpService.getInstance().getCorpCache().findByGuid(user.getGuid());

        if (corp == null) {

            packet.setConsortiaId(-1);
            userPlanet.setStarFace(0);

        } else {

            String name = CorpService.getCorpName(user.getGuid());

            packet.setConsortiaId(corp.getCorpId());
            packet.getConsortia().value(name);

        }

        UserFlag userFlag = user.getFlag();
        packet.setInsertFlagConsortia(SmartString.of("", 32));

        if (userFlag != null) {
            Date until = userFlag.getUntil();
            if (until != null && DateUtil.now().before(until)) {
                Corp enemyCorp = CorpService.getInstance().getCorpCache().findByCorpId(userFlag.getCorpId());
                if (enemyCorp != null) {
                    long time = new Date().getTime() - userFlag.getFrom().getTime();
                    packet.setPassInsertFlagTime((int) (time / 1000));
                    packet.setInsertFlagConsortia(SmartString.of(enemyCorp.getName(), 32));
                    packet.setInsertFlagConsortiaId(enemyCorp.getCorpId());
                }
            }
        }

        user.update();
        user.save();

        if (corp != null) {
            corp.save();
        }

        return packet;

    }

    private ResponseUserInfoPacket getHumaroidUserInfo(Planet planet) {

        HumaroidPlanet humaPlanet = (HumaroidPlanet) planet;
        ResponseUserInfoPacket packet = new ResponseUserInfoPacket();

        Corp corp = CorpService.getInstance().getCorpCache().findByCorpId(humaPlanet.getCurrentCorp());

        packet.setGuid(-1);
        packet.setUserId(planet.getUserId());

        packet.getUserName().value("Humaroid");
        packet.getConsortia().value("");

        if (humaPlanet.getStatusTime() != null) {

            if (humaPlanet.isDestroyed()) {
                packet.setPeaceTime(-DateUtil.remains(humaPlanet.getStatusTime()).intValue());
            } else {
                packet.setPeaceTime(DateUtil.remains(humaPlanet.getStatusTime()).intValue());
            }

            // System.out.println("Destroyed: " + humaPlanet.isDestroyed() + ", PeaceTime: " + packet.getPeaceTime());

        }

        packet.setGuid(-1);
        packet.setUserId(-1);
        packet.setInsertFlagConsortiaId(-1);

        if (corp != null) {

            packet.getConsortia().value(corp.getName());
            packet.setConsortiaId(corp.getCorpId());

        } else {

            packet.getConsortia().value("");
            packet.setConsortiaId(-1);

        }

        packet.getInsertFlagConsortia().value("");
        packet.getUserName().value("Resource Bonus Planet");

        packet.setRankId(-1);
        packet.setPosX(planet.getPosition().getX());
        packet.setPosY(planet.getPosition().getY());

        packet.setGuid(-1);
        packet.setUserId(planet.getUserId());
        packet.setGalaxyId(planet.getPosition().galaxyId());

        packet.setSpaceLevel((byte) -1);
        packet.setCityLevel((byte) -1);

        packet.setLevelId(UnsignedChar.of(humaPlanet.getCurrentLevel()));
        packet.setMatchLevel(UnsignedChar.of(0));

        packet.setPassMaxEctype(0);
        return packet;

    }

    private ResponseUserInfoPacket getRBPUserInfo(Planet planet) {

        ResourcePlanet resourcePlanet = (ResourcePlanet) planet;
        Corp corp = CorpService.getInstance().getCorpCache().findByCorpId(resourcePlanet.getCurrentCorp());

        ResponseUserInfoPacket packet = new ResponseUserInfoPacket();

        if (resourcePlanet.getStatusTime() != null) {

            if (resourcePlanet.isPeace()) {
                packet.setPeaceTime(DateUtil.remains(resourcePlanet.getStatusTime()).intValue());
            } else {
                packet.setPeaceTime(-DateUtil.remains(resourcePlanet.getStatusTime()).intValue());
            }

        }

        packet.setGuid(-1);
        packet.setUserId(-1);
        packet.setInsertFlagConsortiaId(-1);

        if (corp != null) {

            packet.getConsortia().value(corp.getName());
            packet.setConsortiaId(corp.getCorpId());

        } else {

            packet.getConsortia().value("");
            packet.setConsortiaId(-1);

        }

        packet.getInsertFlagConsortia().value("");
        packet.getUserName().value("Resource Bonus Planet");

        packet.setRankId(0);
        packet.setPosX(planet.getPosition().getX());
        packet.setPosY(planet.getPosition().getY());

        packet.setGuid(-1);
        packet.setUserId(planet.getUserId());
        packet.setGalaxyId(planet.getPosition().galaxyId());

        packet.setSpaceLevel((byte) resourcePlanet.getSSLevel());
        packet.setCityLevel((byte) -1);

        packet.setLevelId(UnsignedChar.of(0));
        packet.setMatchLevel(UnsignedChar.of(0));

        packet.setPassMaxEctype(0);
        return packet;

    }

    public void techUpgradeInfo(List<TechUpgradeInfo> techUpgradeInfo) {

        techUpgradeInfo.add(new TechUpgradeInfo(0, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(0, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(0, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(0, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(0, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(-1, 24227, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(-1710747216, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(1643976, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(0, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(35045152, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(1015837328, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(-2, -1, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(190589600, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(35045152, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(1015837328, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(190745472, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(-1710743504, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(190745472, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(-1710821344, 0, 0));
        techUpgradeInfo.add(new TechUpgradeInfo(-627261515, 9996, 0));

    }

    private List<ResponseShipModelInfoPacket> getShipModelInfoPacket(User user) {

        List<ResponseShipModelInfoPacket> packets = new ArrayList<>();
        ResponseShipModelInfoPacket packet = new ResponseShipModelInfoPacket();

        List<ShipModel> shipModels = PacketService.getInstance().getShipModelCache().findAllByGuidAndDeleted(user.getGuid(), false);
        shipModels.add(0, PacketService.getShipModel(0));

        for (ShipModel model : shipModels) {

            if ((packet.getShipModelInfoList().size() + 1) > 7) {

                packets.add(packet);
                packet = new ResponseShipModelInfoPacket();

            }

            packet.getShipModelInfoList().add(
                    ShipModelInfo.of(model.getName(), model.partNum(), model.getShipModelId() == 0 ? 1 : 0, model.getBodyId(), model.partArray(), model.getShipModelId())
            );
        }

        if (!packets.contains(packet)) {
            packets.add(packet);
        }

        for (ResponseShipModelInfoPacket response : packets) {
            response.setDataLen(UnsignedShort.of(response.getShipModelInfoList().size()));
        }

        return packets;

    }

    private List<ResponseShipModelInfoDelPacket> getShipModelInfoDelPacket(User user) {
        List<ResponseShipModelInfoDelPacket> packets = new ArrayList<>();
        //get existing player's ships
        ResponseShipModelInfoDelPacket packet = new ResponseShipModelInfoDelPacket();
        List<ShipModel> shipModels = new ArrayList<>();

        List<FactoryShip> factory = user.getShips().getFactory();
        if (!CollectionUtils.isEmpty(factory)) {
            for (FactoryShip ship : factory) {
                ShipModel shipModel = PacketService.getInstance().getShipModelCache().findByShipModelId(ship.getShipModelId());
                //since we get this from the user's ship factory so 100% this design is from this user
                if (shipModel.isDeleted()) {
                    shipModels.add(shipModel);
                }
            }
        }

        if (user.getShips().getRepair() != null) {
            List<ShipModel> bruiseShipList = new ArrayList<>();
            for (BruiseShip x : user.getShips().getRepair()) {
                ShipModel repairShip = PacketService.getInstance().getShipModelCache().findByShipModelId(x.getShipModelId());
                //we had added the design on above, hence we don't add on response
                if (shipModels.contains(repairShip)) {
                    continue;
                }
                //ship is from user and design is not deleted, hence we don't add on response
                if (repairShip.getGuid() == user.getGuid() && !repairShip.isDeleted()) {
                    continue;
                }
                //ship is wikes, hence we don't add on response
                if (repairShip.getShipModelId() == 0) {
                    continue;
                }
                //ship design is not from user, or ship design is deleted and not added yet to response
                bruiseShipList.add(repairShip);
            }
            shipModels.addAll(bruiseShipList);
        }
        if (user.getShips().getRepairFactory() != null && user.getShips().getRepairFactory().getShipModelId() != 0) {
            ShipModel repairing = PacketService.getInstance().getShipModelCache().findByShipModelId(user.getShips().getRepairFactory().getShipModelId());
            //design is from user's and it is deleted
            if(repairing.getGuid() == user.getGuid() && repairing.isDeleted()){
                shipModels.add(repairing);
            }
            //design is from someone else
            else if(repairing.getGuid() != user.getGuid() && repairing.getShipModelId() != 0){
                //we have not added on response, just add it
                if (!shipModels.contains(repairing)) {
                    shipModels.add(repairing);
                }
            }
        }

        for (ShipModel model : shipModels) {
            if ((packet.getShipModelInfoList().size() + 1) > 3) {
                packets.add(packet);
                packet = new ResponseShipModelInfoDelPacket();
            }
            packet.getShipModelInfoList().add(
                    ShipModelInfo.of(model.getName(), model.partNum(), model.getShipModelId() == 0 ? 1 : 0, model.getBodyId(), model.partArray(), model.getShipModelId())
            );
        }

        if (!packets.contains(packet)) {
            packets.add(packet);
        }
        for (ResponseShipModelInfoDelPacket response : packets) {
            response.setDataLen(UnsignedShort.of(response.getShipModelInfoList().size()));
        }
        return packets;
    }

    private ResponseTechUpgradeInfoPacket getTechUpgradeInfoPacket(User user) {
        ResponseTechUpgradeInfoPacket packet = new ResponseTechUpgradeInfoPacket();
        List<TechUpgradeInfo> techUpgradeInfo = packet.getTechUpgradeInfoList();
        UserTechs userTechs = user.getTechs();

        if (userTechs.getUpgrade() != null) {
            TechUpgrade techUpgrade = userTechs.getUpgrade();
            packet.getTechUpgradeInfoList().add(TechUpgradeInfo.builder()
                    .techId((short) techUpgrade.getId())
                    .needTime(DateUtil.remains(techUpgrade.getUntil()).intValue())
                    .creditFlag((short) 0)
                    .build());

            packet.setDataLen((short) 1);
        }

        packet.setIncTechPercent((short) 0);
        techUpgradeInfo(techUpgradeInfo);
        return packet;
    }

    private ResponseTechInfoPacket getTechInfoPacket(User user) {
        ResponseTechInfoPacket packet = new ResponseTechInfoPacket();
        List<TechInfo> techInfo = packet.getTechInfoList();

        for (UserTech userTech : user.getTechs().getTechs()) {
            techInfo.add(new TechInfo(userTech.getId(), userTech.getLevel() - 1));
        }

        packet.setDataLen(techInfo.size());
        return packet;
    }

    private ResponsePropsInfoPacket getPropsInfoPacket(User user) {
        UserInventory inventory = user.getInventory();
        ResponsePropsInfoPacket packet = new ResponsePropsInfoPacket();

        if (inventory == null || inventory.getPropList() == null) {
            packet.setDataLen(0);
            packet.setPropList(new ArrayList<>());
            return packet;
        }

        packet.setDataLen(inventory.getPropList().size());
        packet.setPropList(inventory.getPropList());
        return packet;
    }

    private ResponsePropsInfoPacket getCorpPropsInfoPacket(User user) {
        CorpInventory inventory = user.getCorpInventory();
        ResponsePropsInfoPacket packet = new ResponsePropsInfoPacket();
        if (inventory == null) {
            packet.setDataLen(0);
            packet.setPropList(new ArrayList<>());
        } else {
            packet.setDataLen(inventory.getCorpPropList().size());
            packet.setPropList(inventory.getCorpPropList());
        }
        return packet;
    }


    private ResponseShipBodyInfoPacket getShipBodyInfoPacket(User user) {
        ResponseShipBodyInfoPacket packet = new ResponseShipBodyInfoPacket();
        UserShipUpgrades shipUpgrades = user.getShipUpgrades();
        if (shipUpgrades.getCurrentBodies() == null) {
            shipUpgrades.setCurrentBodies(new ArrayList<>());
        }
        if (shipUpgrades.getCurrentParts() == null) {
            shipUpgrades.setCurrentParts(new ArrayList<>());
        }
        packet.setBodyNum((short) shipUpgrades.getCurrentBodies().size());
        packet.setPartNum((short) shipUpgrades.getCurrentParts().size());
        packet.setBodyId(shipUpgrades.getBodiesArray());
        packet.setPartId(shipUpgrades.getPartsArray());
        return packet;
    }

    private List<ResponseCommanderBaseInfoPacket> getCommanderBaseInfoPacket(User user) {
        return CommanderService.getInstance().getBaseInfoPacket(user);
    }

    private ResponseTaskInfoPacket getTaskInfoPacket(User user) {
        return TaskService.getInstance().getTaskInfo(user);
    }

    private ResponseCreateShipInfoPacket getCreateShipInfoPacket(User user) {

        double shipBuildBonus = 0.0;
        UserBuilding building = user.getBuildings().getBuilding("build:shipFactory");

        if (building != null) {
            shipBuildBonus = building.getLevelData().getEffect("shipBuildBonus").getValue();
        }

        UserShips ships = user.getShips();
        ResponseCreateShipInfoPacket response = new ResponseCreateShipInfoPacket();

        response.setMaxCreateShipNum(ShipFactoryListener.MAX_SHIPS - user.totalShips());
        response.setIncShipPercent((short) (shipBuildBonus * 100));
        response.setDataLen((short) ships.getFactory().size());

        List<CreateShipInfo> factory = user.getShips().getFactoryAsBuffer();
        CreateShipInfo reference = new CreateShipInfo();

        for (FactoryShip ship : ships.getFactory()) {
            factory.add(ship.packet());
        }

        while (factory.size() < 10) {
            factory.add(reference.trash());
        }

        response.setCreateShipList(factory);
        return response;

    }

    private ResponseShipBodyUpgradeInfoPacket getShipBodyUpgradeInfoPacket(User user) {

        UserShipUpgrades shipUpgrades = user.getShipUpgrades();
        ResponseShipBodyUpgradeInfoPacket response = new ResponseShipBodyUpgradeInfoPacket();

        if (shipUpgrades.getShipUpgrade() != null) {

            ShipUpgrade currentUpgrade = shipUpgrades.getShipUpgrade();
            ShipBodyInfo shipBodyInfo = ShipBodyInfo.builder()
                    .bodyPartId(currentUpgrade.getUpgradeId())
                    .needTime(DateUtil.remains(currentUpgrade.getUntil()).intValue())
                    .build();

            response.setBodyNum((short) 1);
            response.setBodyId(Collections.singletonList(shipBodyInfo));

        } else {

            response.setBodyNum((short) 0);
            response.setBodyId(Collections.singletonList(ShipBodyInfo.builder()
                    .bodyPartId(-1)
                    .needTime(-1)
                    .build()));

        }

        if (shipUpgrades.getPartUpgrade() != null) {

            ShipUpgrade currentUpgrade = shipUpgrades.getPartUpgrade();
            ShipBodyInfo shipBodyInfo = ShipBodyInfo.builder()
                    .bodyPartId(currentUpgrade.getUpgradeId())
                    .needTime(DateUtil.remains(currentUpgrade.getUntil()).intValue())
                    .build();

            response.setPartNum((short) 1);
            response.setPartId(Collections.singletonList(shipBodyInfo));

        } else {

            response.setPartNum((short) 0);
            response.setPartId(Collections.singletonList(ShipBodyInfo.builder()
                    .bodyPartId(-1)
                    .needTime(-1)
                    .build()));

        }

        response.setIncUpgradePercent(0);
        return response;

    }

    private ResponseEctypePassPacket getEctypePassPacket(User user) {

        ResponseEctypePassPacket packet = new ResponseEctypePassPacket();

        packet.setDataLen(user.getStats().getInstance());
        packet.fill(user.getStats().getInstance());

        return packet;

    }

    private ResponseMapBlockPacket getMapPacket() {

        ResponseMapBlockPacket packet = new ResponseMapBlockPacket();
        packet.setBlockCount(GalaxyService.getInstance().getCurrentZones());
        return packet;

    }

}