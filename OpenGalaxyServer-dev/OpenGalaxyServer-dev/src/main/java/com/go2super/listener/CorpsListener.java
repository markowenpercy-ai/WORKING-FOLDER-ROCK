package com.go2super.listener;

import com.go2super.database.entity.*;
import com.go2super.database.entity.sub.*;
import com.go2super.database.entity.type.MatchType;
import com.go2super.database.entity.type.PlanetType;
import com.go2super.obj.game.*;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.type.AuditType;
import com.go2super.obj.type.JumpType;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.obj.utility.VariableType;
import com.go2super.packet.Packet;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.construction.ResponseBuildInfoPacket;
import com.go2super.packet.corp.*;
import com.go2super.packet.fight.ResponseFightGalaxyBeginPacket;
import com.go2super.packet.mail.ResponseNewEmailNoticePacket;
import com.go2super.packet.ship.ResponseCreateShipTeamPacket;
import com.go2super.packet.ship.ResponseDeleteShipTeamBroadcastPacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.CorpsLevelData;
import com.go2super.resources.data.CorpsShopData;
import com.go2super.resources.data.InstanceData;
import com.go2super.resources.data.meta.CorpsLevelEffectMeta;
import com.go2super.resources.data.meta.CorpsLevelMeta;
import com.go2super.service.*;
import com.go2super.service.battle.MatchRunnable;
import com.go2super.service.battle.match.WarMatch;
import com.go2super.service.exception.BadGuidException;
import com.go2super.socket.util.DateUtil;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class CorpsListener implements PacketListener {

    @PacketProcessor
    public void onConsortiaInfo(RequestConsortiaInfoPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        UserBuildings buildings = user.getBuildings();
        List<UserBuilding> building = buildings.getBuildings(6);
        if (building == null || building.isEmpty()) {
            return;
        }

        List<CorpLeaderboard> corps = RankService.getCorpCache();
        List<ConsortiaInfo> consortiaInfoList = new ArrayList<>();

        for (int i = packet.getPageId() * 11; i < ((packet.getPageId() + 1) * 11); i++) {
            if (corps.size() > i) {
                Corp corp = corps.get(i).getLastCorp();
                consortiaInfoList.add(new ConsortiaInfo(corp.getName(), corp.getCorpId(), i));
            }
        }

        ResponseConsortiaInfoPacket responseConsortiaInfoPacket = ResponseConsortiaInfoPacket.builder()

                .consortiaCount(Long.valueOf(CorpService.getInstance().getCorpCache().count()).intValue())
                .pageId((short) (packet.getPageId() + 1))
                .dataLen((short) consortiaInfoList.size())
                .data(consortiaInfoList)
                .build();

        packet.reply(responseConsortiaInfoPacket);

    }

    @PacketProcessor
    public void onConsortiaInfo2(RequestConsortiaInfo2Packet packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        UserBuildings buildings = user.getBuildings();
        List<UserBuilding> building = buildings.getBuildings(6);
        if (building == null || building.isEmpty()) {
            return;
        }

        List<Corp> corps = CorpService.getInstance().getCorpCache().findByStartWithName(packet.getName().noSpaces());
        if (corps == null) {
            return;
        }

        Corp selection = !corps.isEmpty() ? corps.get(0) : null;

        if (selection != null) {
            for (Corp corp : corps) {
                if (corp.getName().equals(packet.getName().noSpaces())) {
                    selection = corp;
                    break;
                }
            }
        }

        List<ConsortiaInfo> consortiaInfoList = new ArrayList<>();

        if (selection != null) {
            consortiaInfoList.add(new ConsortiaInfo(selection.getName(), selection.getCorpId(), selection.getCorpId()));
        }

        ResponseConsortiaInfoPacket response = ResponseConsortiaInfoPacket.builder()
                .consortiaCount(consortiaInfoList.size())
                .dataLen((short) consortiaInfoList.size())
                .pageId((short) 0)
                .data(consortiaInfoList)
                .build();

        packet.reply(response);

    }

    @PacketProcessor
    public void onCreateConsortia(RequestCreateConsortiaPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserBuildings buildings = user.getBuildings();
        List<UserBuilding> building = buildings.getBuildings(6);
        if (building == null || building.isEmpty()) {
            return;
        }

        UserResources resources = user.getResources();
        if (resources.getGold() < 10000) {
            return;
        }

        resources.setGold(resources.getGold() - 10000);

        CorpService corpService = CorpService.getInstance();

        UserInventory inventory = user.getInventory();
        Prop prop = inventory.getProp(922);
        if (prop == null) {
            return;
        }

        Corp corpMemberVerify = CorpService.getInstance().getCorpByUser(user.getGuid());
        if (corpMemberVerify != null) {
            return;
        }

        List<Corp> corps = CorpService.getInstance().getCorpCache().findRecruitsByGuid(user.getGuid());

        if (corps != null) {
            for (int i = 0; i < corps.size(); i++) {
                corps.get(i).getMembers().removeRecruit(user.getGuid());
                corps.get(i).save();
            }
        }

        Corp corp = corpService.createCorp(packet.getName(),
                packet.getProclaim().getValue(),
                packet.getHeadId().getValue());

        CorpMember corpMember = corpService.createCorpMember(user.getGuid(), 1);
        corp.getMembers().addMember(corpMember);

        Corp searchCorp = CorpService.getInstance().getCorpCache().findByName(packet.getName().noSpaces());

        Pair<Boolean, Boolean> propRemove = user.getInventory().removeProp(prop, 1);
        if (!propRemove.getKey()) {
            return;
        }

        if (searchCorp != null) {

            ResponseCreateConsortiaPacket response = ResponseCreateConsortiaPacket.builder()
                    .ConsortiaId(corp.getCorpId())
                    .errorCode((byte) 1)
                    .propsCorpsPack(-1)
                    .lockFlag((byte) (propRemove.getValue() ? 1 : 0))
                    .build();

            packet.reply(response);
            return;

        }

        ResponseCreateConsortiaPacket response = ResponseCreateConsortiaPacket.builder()
                .ConsortiaId(corp.getCorpId())
                .errorCode((byte) 0)
                .propsCorpsPack(0)
                .lockFlag((byte) (propRemove.getValue() ? 1 : 0))
                .build();

        user.setConsortiaId(corp.getCorpId());
        user.setConsortiaJob((byte) corpMember.getRank());
        user.setConsortiaUnionLevel((byte) 0);

        user.getMetrics().add("action:corp.join", 1);
        user.update();
        user.save();

        corp.save();

        packet.reply(response);
        packet.reply(ResourcesService.getInstance().getPlayerResourcePacket(user));

    }

    @PacketProcessor
    public void onRefreshWall(RequestRefreshWallPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        ResponseRefreshWallPacket response = ResponseRefreshWallPacket.builder()
                .kind(1)
                .num(0)
                .propsId(-1)
                .build();

        packet.reply(response);

    }

    @PacketProcessor
    public void onConsortiaProclaim(RequestConsortiaProclaimPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserBuildings buildings = user.getBuildings();
        List<UserBuilding> building = buildings.getBuildings(6);
        if (building == null || building.isEmpty()) {
            return;
        }

        Corp corp = CorpService.getInstance().getCorpCache().findByCorpId(packet.getConsortiaId());
        CorpMember corpLead = corp.getMembers().getLeader();
        if (corpLead == null) {
            return;
        }

        User corpUserLeader = UserService.getInstance().getUserCache().findByGuid(corpLead.getGuid());

        ResponseConsortiaProclaimPacket response = ResponseConsortiaProclaimPacket.builder()
                .consortiaId(corp.getCorpId())
                .consortiaLeadUserId(corpUserLeader.getUserId())
                .cent(corp.getWealth())
                .consortiaLeadGuid(corpLead.getGuid())
                .consortiaLead(SmartString.of(corpUserLeader.getUsername(), 32))
                .proclaim(SmartString.of(corp.getPhilosophy(), 256))
                .maxMemberCount(UnsignedChar.of(corp.getMaxMembers()))
                .memberCount(UnsignedChar.of(corp.getMembers().getMembers().size()))
                .consortiaLevel(UnsignedChar.of(corp.getLevel()))
                .HeadId(UnsignedChar.of(corp.getIcon()))
                .limitJoin((byte) 20)
                .build();

        packet.reply(response);

    }

    @PacketProcessor
    public void onJoinConsortia(RequestJoinConsortiaPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserBuildings buildings = user.getBuildings();
        List<UserBuilding> building = buildings.getBuildings(6);
        if (building == null || building.isEmpty()) {
            return;
        }

        Corp corp = CorpService.getInstance().getCorpCache().findByCorpId(packet.getConsortiaId());
        if (corp == null) {
            return;
        }

        CorpMember corpMember = corp.getMembers().getMember(user.getGuid());
        if (corpMember != null) {
            return;
        }

        CorpMember corpRecruit = corp.getMembers().getRecruit(user.getGuid());
        if (corpRecruit != null) {
            return;
        }

        /*
        0 -> recruit
        1 -> colonel
        2 -> commandant
        3 -> captain
        4 -> soldier
        */

        /*
        0 -> passed
        1 -> corp is full
        2 -> the corp has set limitations on memberships
        */

        CorpMember newRecruit = CorpService.getInstance().createCorpMember(user.getGuid(), 0);
        corp.getMembers().addRecruit(newRecruit);

        if (corp.getMembers().getMembers().size() >= corp.getMaxMembers()) {

            ResponseJoinConsortiaPacket response = ResponseJoinConsortiaPacket.builder()
                    .errorCode(1)
                    .build();

            packet.reply(response);
            return;

        }

        ResponseJoinConsortiaPacket response = ResponseJoinConsortiaPacket.builder()
                .errorCode(0)
                .build();

        user.save();
        corp.save();
        packet.reply(response);

    }

    @PacketProcessor
    public void onDealConsortiaAuthUser(RequestDealConsortiaAuthUserPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserBuildings buildings = user.getBuildings();
        List<UserBuilding> building = buildings.getBuildings(6);
        if (building == null || building.isEmpty()) {
            return;
        }

        Corp corp = CorpService.getInstance().getCorpCache().findByGuid(user.getGuid());
        if (corp == null) {
            return;
        }

        CorpMember corpMember = corp.getMembers().getMember(user.getGuid());
        if (corpMember == null) {
            return;
        }

        CorpMember corpRecruit = corp.getMembers().getRecruit(packet.getObjGuid());
        if (corpRecruit == null) {
            return;
        }

        if (corpMember.getRank() == 1 || corpMember.getRank() == 2 || corpMember.getRank() == 3) {

            if (packet.getAgree() == 0) {

                corp.getMembers().removeRecruit(packet.getObjGuid());

                ResponseDealConsortiaAuthUserPacket response = ResponseDealConsortiaAuthUserPacket.builder()
                        .errorCode(0)
                        .agree(0)
                        .build();

                corp.save();
                packet.reply(response);

                ResponseJoinConsortiaPacket joinConsortiaPacket = ResponseJoinConsortiaPacket.builder()
                        .errorCode(1)
                        .build();

                Optional<LoggedGameUser> gameUserOptional = LoginService.getInstance().getGame(packet.getObjGuid());

                gameUserOptional.ifPresent(loggedGameUser -> loggedGameUser.getSmartServer().send(joinConsortiaPacket));

                return;
            }

            if (packet.getAgree() == 1) {
                List<Fleet> fleets = PacketService.getInstance().getFleetCache().findAllByGuid(packet.getObjGuid());
                for (Fleet fleet : fleets) {
                    if (fleet.isInTransmission() || fleet.isInMatch()) {
                        ResponseDealConsortiaAuthUserPacket response = ResponseDealConsortiaAuthUserPacket.builder()
                                .errorCode(2)
                                .agree(1)
                                .build();
                        packet.reply(response);
                        return;
                    }
                }


                if (corp.getMembers().getMembers().size() == corp.getMaxMembers()) {
                    ResponseDealConsortiaAuthUserPacket response = ResponseDealConsortiaAuthUserPacket.builder()
                            .errorCode(1)
                            .agree(1)
                            .build();
                    packet.reply(response);
                    return;
                }

                CorpMember corpMemberRecruit = corp.getMembers().getMember(packet.getObjGuid());
                if (corpMemberRecruit != null) {
                    return;
                }

                Corp corpVerify = CorpService.getInstance().getCorpByUser(packet.getObjGuid());
                if (corpVerify != null) {
                    return;
                }

                CorpMember newCorpMember = CorpService.getInstance().createCorpMember(packet.getObjGuid(), 0);
                corp.getMembers().addMember(newCorpMember);

                User newUserCorp = UserService.getInstance().getUserCache().findByGuid(newCorpMember.getGuid());

                newUserCorp.setConsortiaId(corp.getCorpId());
                newUserCorp.setConsortiaJob(newCorpMember.getRank());
                newUserCorp.setPirateReceived(false);
                newUserCorp.getMetrics().add("action:corp.join", 1);

                corp.getMembers().removeRecruit(packet.getObjGuid());

                if (corp.getWarehouseLevel() > -1) {

                    CorpsLevelData wareHouseLevelData = CorpService.getCorpsLevelData(1);
                    CorpsLevelMeta wareHouseLevelMeta = CorpService.getCorpsLevelMeta(wareHouseLevelData, corp.getWarehouseLevel());
                    List<CorpsLevelEffectMeta> wareHouseLevelEffectMeta = CorpService.getCorpsLevelEffectMeta(wareHouseLevelMeta);

                    newUserCorp.setCorpInventory(CorpService.createNewCorpInventory());
                    newUserCorp.getCorpInventory().setMaxStacks((int) wareHouseLevelEffectMeta.get(0).getValue());

                }

                newUserCorp.update();
                newUserCorp.save();

                corp.save();

                ResponseDealConsortiaAuthUserPacket response = ResponseDealConsortiaAuthUserPacket.builder()
                        .errorCode(0)
                        .agree(1)
                        .build();


                ResponseOperateConsortiaBroPacket responseOperateConsortiaBroPacket = ResponseOperateConsortiaBroPacket.builder()
                        .consortiaId(corp.getCorpId())
                        .type(0)
                        .propsCorpsPack(corp.getWarehouseLevel())
                        .job((byte) 0)
                        .unionLevel((byte) corp.getMergingLevel())
                        .shopLevel((byte) corp.getMallLevel())
                        .reserve2((byte) 0)
                        .needUnionValue(corp.getContributionMerge())
                        .needShopValue(corp.getContributionMall())
                        .build();

                packet.reply(response);
                Optional<LoggedGameUser> gameUserOptional = LoginService.getInstance().getGame(newCorpMember.getGuid());
                gameUserOptional.ifPresent(loggedGameUser -> {
                    loggedGameUser.setConsortiaId(corp.getCorpId());
                    loggedGameUser.getSmartServer().send(responseOperateConsortiaBroPacket);
                });
            }
        }
    }

    @PacketProcessor
    public void onLeaveConsortia(RequestConsortiaDelMemberPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserBuildings buildings = user.getBuildings();
        List<UserBuilding> building = buildings.getBuildings(6);
        if (building == null || building.isEmpty()) {
            return;
        }

        Corp corp = CorpService.getInstance().getCorpCache().findByGuid(user.getGuid());
        if (corp == null) {
            return;
        }

        CorpMember corpMember = corp.getMembers().getMember(user.getGuid());
        if (corpMember == null) {
            return;
        }

        CorpMember delCorpMember = corp.getMembers().getMember(packet.getDelGuid());
        if (delCorpMember == null) {
            return;
        }

        boolean leave = user.getGuid() == packet.getDelGuid();

        if (delCorpMember.getRank() == 1) {
            return;
        }
        if (!leave && corpMember.getRank() != 1 && corpMember.getRank() != 2) {
            return;
        }

        User toKick = UserService.getInstance().getUserCache().findByGuid(packet.getDelGuid());
        if (toKick == null) {
            return;
        }

        boolean sentEmail = false;

        CorpInventory corpInventory = toKick.getCorpInventory();
        if (corpInventory != null) {
            UserEmailStorage userEmailStorage = toKick.getUserEmailStorage();
            List<Prop> props = corpInventory.getCorpPropList();
            if (!CollectionUtils.isEmpty(props)) {
                Email email = Email.builder()
                        .autoId(userEmailStorage.nextAutoId())
                        .name("System")
                        .type(2)
                        .readFlag(0)
                        .date(DateUtil.now())
                        .goods(new ArrayList<>())
                        .guid(-1)
                        .subject("Corp Inventory")
                        .emailContent("Commander, these are the items you have in your corp inventory. Please take them to the warehouse.")
                        .build();
                for (Prop prop : props) {
                    email.addGood(EmailGood.builder()
                            .goodId(prop.getPropId())
                            .num(prop.getPropNum())
                            .lockNum(prop.getPropLockNum())
                            .build());
                }
                userEmailStorage.addEmail(email);
                sentEmail = true;
                String message = "User id: " + toKick.getUserId() + " has left the corp: " + corp.getName() + " (" + corp.getCorpId() + ")" + "\n" +
                        "Goods: " + email.getGoods() + "\n";
                message = message.substring(0, Math.min(message.length(), 2000));
                DiscordService.getInstance().getRayoBot().sendAudit("Corps Leave with items", message, Color.green, AuditType.INCIDENT);
            }
        }

        corp.getMembers().removeMember(delCorpMember.getGuid());

        toKick.setConsortiaId(-1);
        toKick.setConsortiaUnion(-1);
        toKick.setConsortiaJob(-1);
        toKick.setConsortiaUnionLevel(-1);
        toKick.setCorpInventory(null);

        toKick.save();
        corp.save();

        Optional<LoggedGameUser> gameUserOptional = LoginService.getInstance().getGame(toKick);
        if (gameUserOptional.isPresent()) {
            LoggedGameUser loggedGameUser = gameUserOptional.get();
            loggedGameUser.setConsortiaId(-1);
            if (sentEmail) {
                ResponseNewEmailNoticePacket response = ResponseNewEmailNoticePacket.builder()
                        .errorCode(0)
                        .build();
                loggedGameUser.getSmartServer().send(response);
            }
            ResponseOperateConsortiaBroPacket response = ResponseOperateConsortiaBroPacket.builder()
                    .consortiaId(corp.getCorpId())
                    .type(1)
                    .propsCorpsPack(0)
                    .job((byte) -1)
                    .unionLevel((byte) -1)
                    .shopLevel((byte) -1)
                    .reserve2((byte) 0)
                    .needUnionValue(-1)
                    .needShopValue(-1)
                    .build();
            loggedGameUser.getSmartServer().send(response);
        }

    }

    @PacketProcessor
    public void onConsortiaThrowRank(RequestConsortiaThrowRankPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Corp corp = CorpService.getInstance().getCorpCache().findByGuid(user.getGuid());
        if (corp == null) {
            return;
        }

        CorpMember userMember = corp.getMembers().getMember(user.getGuid());

        List<CorpMember> corpMembers = corp.getMembers().getMembers();
        corpMembers.sort((member1, member2) -> {
            if (member1.getRank() > member2.getRank()) {
                return -1;
            }
            if (member1.getRank() < member2.getRank()) {
                return 1;
            }
            return 0;
        });

        List<ConsortiaThrowRank> membersThrowRank = new ArrayList<>();
        int rank = 0;

        for (int i = packet.getPageId() * 10; i < ((packet.getPageId() + 1) * 10); i++) {
            if (corpMembers.size() > i) {
                CorpMember member = corpMembers.get(i);
                User userCorpMember = UserService.getInstance().getUserCache().findByGuid(member.getGuid());
                if (userCorpMember == null) {
                    continue;
                }
                membersThrowRank.add(new ConsortiaThrowRank(userCorpMember.getUsername(), userCorpMember.getUserId(), corpMembers.get(i).getDonateResources(), corpMembers.get(i).getDonateMallPoints(), corpMembers.get(i).getGuid(), rank++));
            }
        }

        ResponseConsortiaThrowRankPacket response = ResponseConsortiaThrowRankPacket.builder()
                .throwValue(1)
                .myWealth(userMember.getContribution())
                .dataLen((short) membersThrowRank.size())
                .memberCount((short) corpMembers.size())
                .data(membersThrowRank)
                .build();

        packet.reply(response);

    }

    @PacketProcessor
    public void onConsortiaThrowValue(RequestConsortiaThrowValuePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Corp corp = CorpService.getInstance().getCorpCache().findByGuid(user.getGuid());
        if (corp == null) {
            return;
        }

        CorpMember corpMember = corp.getMembers().getMember(user.getGuid());
        if (corpMember == null) {
            return;
        }

        if (packet.getKind() == 0) { // 1 -> Mall Points

            if (user.getResources().getMallPoints() < packet.getValue()) {
                return;
            }

            user.getResources().setMallPoints(user.getResources().getMallPoints() - packet.getValue());

            corpMember.setContribution(corpMember.getContribution() + packet.getValue());
            corpMember.setDonateMallPoints(corpMember.getDonateMallPoints() + packet.getValue());

            corp.setWealth(corp.getWealth() + packet.getValue());

            ResponseConsortiaThrowValuePacket response = ResponseConsortiaThrowValuePacket.builder()
                    .value(packet.getValue())
                    .kind(0)
                    .build();

            user.getMetrics().add("action:corp.contribute", packet.getValue());

            corp.save();
            user.update();
            user.save();

            packet.reply(response);
            return;

        }

        if (packet.getKind() == 1) { // 2 -> GOLD

            int value = packet.getValue() * 10000;
            if (user.getResources().getGold() < value) {
                return;
            }

            user.getResources().setGold(user.getResources().getGold() - value);

            corpMember.setContribution(corpMember.getContribution() + packet.getValue());
            corpMember.setDonateResources(corpMember.getDonateResources() + packet.getValue());

            corp.setWealth(corp.getWealth() + packet.getValue());

            ResponseConsortiaThrowValuePacket response = ResponseConsortiaThrowValuePacket.builder()
                    .value(value)
                    .kind(1)
                    .build();

            user.getMetrics().add("action:corp.contribute", packet.getValue());

            corp.save();
            user.update();
            user.save();

            packet.reply(response);
            return;

        }

        if (packet.getKind() == 2) { // 2 -> METAL

            int value = packet.getValue() * 10000;
            if (user.getResources().getMetal() < value) {
                return;
            }

            user.getResources().setMetal(user.getResources().getMetal() - value);

            corpMember.setContribution(corpMember.getContribution() + packet.getValue());
            corpMember.setDonateResources(corpMember.getDonateResources() + packet.getValue());

            corp.setWealth(corp.getWealth() + packet.getValue());

            ResponseConsortiaThrowValuePacket response = ResponseConsortiaThrowValuePacket.builder()
                    .value(value)
                    .kind(2)
                    .build();

            user.getMetrics().add("action:corp.contribute", packet.getValue());

            corp.save();
            user.update();
            user.save();

            packet.reply(response);
            return;

        }

        if (packet.getKind() == 3) { // 3 -> He3

            int value = packet.getValue() * 10000;
            if (user.getResources().getHe3() < value) {
                return;
            }

            user.getResources().setHe3(user.getResources().getHe3() - value);

            corpMember.setContribution(corpMember.getContribution() + packet.getValue());
            corpMember.setDonateResources(corpMember.getDonateResources() + packet.getValue());

            corp.setWealth(corp.getWealth() + packet.getValue());

            ResponseConsortiaThrowValuePacket response = ResponseConsortiaThrowValuePacket.builder()
                    .value(value)
                    .kind(3)
                    .build();

            user.getMetrics().add("action:corp.contribute", packet.getValue());

            corp.save();
            user.update();
            user.save();

            packet.reply(response);

        }
    }

    @PacketProcessor
    public void onConsortiaAuthUser(RequestConsortiaAuthUserPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserBuildings buildings = user.getBuildings();
        List<UserBuilding> building = buildings.getBuildings(6);
        if (building == null || building.isEmpty()) {
            return;
        }

        Corp corp = CorpService.getInstance().getCorpCache().findByGuid(user.getGuid());
        if (corp == null) {
            return;
        }

        List<CorpMember> corpRecruits = CorpService.getInstance().findRecruitsByPage(packet.getPageId(), 9, user.getGuid());
        List<ConsortiaAuthUser> consortiaAuthUsers = new ArrayList<>();

        for (int i = 0; i < corpRecruits.size(); i++) {
            User authUser = UserService.getInstance().getUserCache().findByGuid(corpRecruits.get(i).getGuid());
            if (authUser == null) {
                corp.getMembers().getRecruits().remove(corpRecruits.get(i));
                corp.save();
                continue;
            }
            consortiaAuthUsers.add(new ConsortiaAuthUser(authUser.getUsername(), authUser.getUserId(), -1, corpRecruits.get(i).getGuid(), authUser.getStats().getLevel(), -1));
        }

        ResponseConsortiaAuthUserPacket response = ResponseConsortiaAuthUserPacket.builder()
                .pageCount((short) (Math.ceil(corpRecruits.size() / 9)))
                .dataLen((short) consortiaAuthUsers.size())
                .data(consortiaAuthUsers)
                .build();

        packet.reply(response);

    }

    @PacketProcessor
    public void onConsortiaMySelf(RequestConsortiaMySelfPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserBuildings buildings = user.getBuildings();
        List<UserBuilding> building = buildings.getBuildings(6);
        if (building == null || building.isEmpty()) {
            return;
        }

        Corp corp = CorpService.getInstance().getCorpCache().findByGuid(user.getGuid());
        if (corp == null) {
            return;
        }

        CorpMember corpMember = corp.getMembers().getMember(user.getGuid());
        if (corpMember == null) {
            return;
        }

        CorpMember corpLeader = corp.getMembers().getLeader();
        if (corpLeader == null) {
            return;
        }

        int rank = RankService.getCorpRank(corp.getCorpId());
        User userLeader = UserService.getInstance().getUserCache().findByGuid(corpLeader.getGuid());

        ResponseConsortiaMySelfPacket response = ResponseConsortiaMySelfPacket.builder()
                .consortiaId(corp.getCorpId())
                .consortiaLeadUserId(userLeader.getUserId())
                .cent(corp.getWealth())
                .name(SmartString.of(corp.getName(), VariableType.MAX_NAME))
                .notice(SmartString.of(corp.getBulletin(), VariableType.MAX_MEMO))
                .proclaim(SmartString.of(corp.getPhilosophy(), VariableType.MAX_MEMO))
                .consortiaLead(SmartString.of(userLeader.getUsername(), VariableType.MAX_NAME))
                .jobName(corp.getConsortiaJobName())
                .consortiaGuid(corp.getCorpId())
                .sortId(-1)
                .wealth(corp.getWealth())
                .repairWealth(0)
                .memberCount(UnsignedChar.of(corp.getMembers().getMembers().size()))
                .maxMemberCount(UnsignedChar.of(corp.getMaxMembers()))
                .headId(UnsignedChar.of(corp.getIcon()))
                .level(UnsignedChar.of(corp.getLevel()))
                .holdGalaxy(UnsignedChar.of(corp.getResourcePlanets().size()))
                .maxHoldGalaxy(UnsignedChar.of(corp.getRbpLimit()))
                .storageLevel((byte) corp.getWarehouseLevel())
                .unionLevel((byte) corp.getMergingLevel())
                .myWealth(corpMember.getContribution())
                .upgradeTime(corp.getCorpUpgrade() != null ? DateUtil.remains(corp.getCorpUpgrade().getUntil()).intValue() : -1)
                .upgradeType((byte) (corp.getCorpUpgrade() != null ? corp.getCorpUpgrade().getTypeUpgrade() : -1))
                .piratePassLevel((byte) corp.getPiratesLevel())
                .attackUserLevel(UnsignedChar.of(0))
                .pirateNum((byte) corp.getPiratesNum())
                .attackUser(SmartString.of("", VariableType.MAX_NAME))
                .attackUserGalaxyId(0)
                .attackUserAssault(0)
                .sortId(rank)
                .build();

        List<CorpMember> corpMembers = corp.getMembers().getMembers();

        int memberStatus;

        packet.reply(response);
        ResponseConsortiaMemberPacket prepared = null;

        for (int i = 0; i < corpMembers.size(); i++) {

            User userCorp = UserService.getInstance().getUserCache().findByGuid(corp.getMembers().getMembers().get(i).getGuid());

            if (userCorp == null) {
                continue;
            }

            if (userCorp.isOnline()) {
                memberStatus = 1;
            } else {
                memberStatus = 0;
            }

            ConsortiaMember consortiaMember = new ConsortiaMember(userCorp.getUsername(), userCorp.getUserId(), -1, corp.getMembers().getMembers().get(i).getContribution(), corp.getMembers().getMembers().get(i).getGuid(), (char) userCorp.getStats().getLevel(), (char) memberStatus, (char) corp.getMembers().getMembers().get(i).getRank(), (char) -1);
            PacketService.getInstance().sendMoreInfoPacket(1, userCorp.getUserId(), packet);

            if (prepared == null || (prepared.getData().size() + 1) > 18) {
                if (prepared != null) {
                    packet.reply(prepared);
                }
                prepared = ResponseConsortiaMemberPacket.builder().data(new ArrayList<>()).dataLen(1).build();
                prepared.getData().add(consortiaMember);
            } else {
                prepared.getData().add(consortiaMember);
                prepared.setDataLen(prepared.getData().size());
            }

        }

        if (prepared != null) {
            packet.reply(prepared);
        }

    }

    @PacketProcessor
    public void onConsortiaField(RequestConsortiaFieldPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Corp corp = user.getCorp();
        if (corp == null) {
            return;
        }

        List<ResourcePlanet> resourcePlanets = corp.getResourcePlanets();
        if (resourcePlanets == null || resourcePlanets.isEmpty()) {
            return;
        }

        List<ConsortiaField> consortiaFields = new ArrayList<>();

        for (ResourcePlanet resourcePlanet : resourcePlanets) {

            List<Fleet> fleets = PacketService.getInstance().getFleetCache().findAllByGalaxyId(resourcePlanet.getPosition().galaxyId());

            ConsortiaField consortiaField = new ConsortiaField();

            consortiaField.setGalaxyId(resourcePlanet.getPosition().galaxyId());
            consortiaField.setMaxShipNum((byte) resourcePlanet.getMaxFleets());
            consortiaField.setShipNum(fleets.size());

            consortiaField.setNeedTime(DateUtil.remains(resourcePlanet.getStatusTime()).intValue());
            consortiaField.setStatus((byte) (resourcePlanet.isPeace() ? 0 : 1));
            consortiaField.setLevel((byte) resourcePlanet.getSSLevel());
            consortiaFields.add(consortiaField);

        }

        ResponseConsortiaFieldPacket response = ResponseConsortiaFieldPacket.builder()
                .data(consortiaFields)
                .dataLen(consortiaFields.size())
                .build();

        packet.reply(response);

    }

    @PacketProcessor
    public void onConsortiaUpdateJobName(RequestConsortiaUpdateJobNamePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null) {
            return;
        }

        UserBuildings buildings = user.getBuildings();
        List<UserBuilding> building = buildings.getBuildings(6);
        if (building == null || building.isEmpty()) {
            return;
        }

        Corp corp = CorpService.getInstance().getCorpCache().findByGuid(user.getGuid());
        if (corp == null) {
            return;
        }

        CorpMember userCorpMember = corp.getMembers().getMember(user.getGuid());
        if (userCorpMember.getRank() != 1) {
            return;
        }

        corp.setConsortiaJobName(packet.getConsortiaJobName());
        corp.save();

    }

    @PacketProcessor
    public void onEditConsortia(RequestEditConsortiaPacket packet) throws BadGuidException {

         /*
            0 -> recruit
            1 -> colonel
            2 -> commandant
            3 -> captain
            4 -> soldier
            */

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserBuildings buildings = user.getBuildings();
        List<UserBuilding> building = buildings.getBuildings(6);
        if (building == null || building.isEmpty()) {
            return;
        }

        Corp corp = CorpService.getInstance().getCorpCache().findByGuid(user.getGuid());
        if (corp == null) {
            return;
        }

        CorpMember userCorpMember = corp.getMembers().getMember(user.getGuid());
        if (userCorpMember.getRank() != 1) {
            return;
        }

        corp.setBulletin(packet.getNotice().noSpaces());
        corp.setPhilosophy(packet.getProclaim().noSpaces());

        corp.save();

    }

    @PacketProcessor
    public void onConsortiaGiven(RequestConsortiaGivenPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserBuildings buildings = user.getBuildings();
        List<UserBuilding> building = buildings.getBuildings(6);
        if (building == null || building.isEmpty()) {
            return;
        }

        Corp corp = CorpService.getInstance().getCorpCache().findByGuid(user.getGuid());
        if (corp == null) {
            return;
        }

        CorpMember leader = corp.getMembers().getMember(user.getGuid());
        if (leader == null || leader.getRank() != 1) {
            return;
        }

        CorpMember memberToTransfer = corp.getMembers().getMember(packet.getObjGuid());

        if (memberToTransfer != null) {

            User userToTransfer = UserService.getInstance().getUserCache().findByGuid(packet.getObjGuid());

            leader.setRank(2);
            memberToTransfer.setRank(1);
            userToTransfer.setConsortiaJob(1);

            ResponseConsortiaGivenPacket response = ResponseConsortiaGivenPacket.builder()
                    .errorCode(0)
                    .build();


            corp.save();
            user.save();
            userToTransfer.save();
            packet.reply(response);
            return;

        }

        ResponseConsortiaGivenPacket response = ResponseConsortiaGivenPacket.builder()
                .errorCode(1)
                .build();

        packet.reply(response);
    }

    @PacketProcessor
    public void onConsortiaSetOfficial(RequestConsortiaSetOfficialPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserBuildings buildings = user.getBuildings();
        List<UserBuilding> building = buildings.getBuildings(6);
        if (building == null || building.isEmpty()) {
            return;
        }

        Corp corp = CorpService.getInstance().getCorpCache().findByGuid(user.getGuid());
        if (corp == null) {
            return;
        }

        CorpMember corpMember = corp.getMembers().getMember(user.getGuid());
        if (corpMember == null) {
            return;
        }

        CorpMember corpMemberToTransfer = corp.getMembers().getMember(packet.getObjGuid());
        if (corpMemberToTransfer == null) {
            return;
        }

        if (corpMember.getRank() == 1 || corpMember.getRank() == 2) {

            User userTransfer = UserService.getInstance().getUserCache().findByGuid(corpMemberToTransfer.getGuid());

            corpMemberToTransfer.setRank(packet.getJob());
            userTransfer.setConsortiaJob(packet.getJob());

            corp.save();
            userTransfer.save();
            user.save();

            userTransfer.getLoggedGameUser().ifPresent(x -> {
                ResponseOperateConsortiaBroPacket response = ResponseOperateConsortiaBroPacket.builder()
                        .consortiaId(corp.getCorpId())
                        .type(2)
                        .propsCorpsPack(corp.getWarehouseLevel())
                        .job(packet.getJob())
                        .unionLevel((byte) corp.getMergingLevel())
                        .shopLevel((byte) corp.getMallLevel())
                        .reserve2((byte) 0)
                        .needUnionValue(corp.getContributionMerge())
                        .needShopValue(corp.getContributionMall())
                        .build();
                x.getSmartServer().send(response);
            });

        }

    }

    @PacketProcessor
    public void onInsertFlagConsortiaMember(RequestInsertFlagConsortiaMemberPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Corp corp = CorpService.getInstance().getCorpCache().findByCorpId(packet.getConsortiaId());
        if (corp == null) {
            return;
        }

        List<CorpMember> corpMemberList = corp.getMembers().getMembers();
        List<InsertFlagConsortiaMember> members = new ArrayList<>();

        // BotLogger.log(corpMemberList);

        for (int i = packet.getPageId() * 10; i < ((packet.getPageId() + 1) * 10); i++) {
            if (corpMemberList.size() > i) {


                User member = UserService.getInstance().getUserCache().findByGuid(corpMemberList.get(i).getGuid());

                if (member == null) {

                    System.out.println("ERROR ID: " + corpMemberList.get(i).getGuid());
                    System.out.println("ERROR CORP: " + corp.getName());
                    continue;

                }

                members.add(new InsertFlagConsortiaMember(member.getUsername(),
                        member.getGuid(),
                        member.getPlanet().getPosition().galaxyId(),
                        RankService.getInstance().getAttackPower(member),
                        (short) member.getPlanet().getPosition().getParentZone().zoneId(),
                        (char) member.getStats().getLevel(),
                        (char) corp.getMembers().getMembers().get(i).getRank()));
            }
        }

        List<ResourcePlanet> resourcePlanets = corp.getResourcePlanets();
        IntegerArray fields = new IntegerArray(VariableType.MAX_CONSORTIAFIELD);

        for (int hold = 0; hold < resourcePlanets.size(); hold++) {
            fields.getArray()[hold] = resourcePlanets.get(hold).getPosition().getParentZone().zoneId();
        }

        ResponseInsertFlagConsortiaMemberPacket response = ResponseInsertFlagConsortiaMemberPacket.builder()
                .name(SmartString.of(corp.getName(), VariableType.MAX_NAME))
                .throwWealth(corp.getWealth())
                .holdGalaxyArea(fields)
                .headId(UnsignedChar.of(corp.getIcon()))
                .level(UnsignedChar.of(corp.getLevel()))
                .holdGalaxy(UnsignedChar.of(resourcePlanets.size()))
                .memberCount(UnsignedChar.of(corp.getMembers().getMembers().size()))
                .data(members)
                .dataLen(members.size())
                .build();

        packet.reply(response);

    }

    @PacketProcessor
    public void onConsortiaBuyGoods(RequestConsortiaBuyGoodsPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserBuildings buildings = user.getBuildings();
        List<UserBuilding> building = buildings.getBuildings(6);
        if (building == null || building.isEmpty()) {
            return;
        }

        Corp corp = CorpService.getInstance().getCorpCache().findByGuid(user.getGuid());
        if (corp == null) {
            return;
        }

        CorpMember corpMember = corp.getMembers().getMember(user.getGuid());
        if (corpMember == null) {
            return;
        }
        if (corpMember.getContribution() < corp.getContributionMall()) {
            return;
        }

        CorpsShopData data = PacketService.getCorpShopData(packet.getGoodsId());
        ShipModel shipModel = PacketService.getCorpShipModel(data);

        if (packet.getNum() <= 0) {
            return;
        }
        if (corpMember.getContribution() < (data.getCost() * packet.getNum())) {
            return;
        }

        int num = packet.getNum() * 100;
        int currentShips = user.totalShips();

        if (currentShips + num > ShipFactoryListener.MAX_SHIPS) {

            ResponseConsortiaBuyGoodsPacket response = ResponseConsortiaBuyGoodsPacket.builder()
                    .errorCode(1)
                    .goodsId(packet.getGoodsId())
                    .price(data.getCost() * packet.getNum())
                    .build();

            packet.reply(response);
            return;

        }

        user.getShips().addShip(shipModel.getShipModelId(), num);
        corpMember.setContribution(corpMember.getContribution() - (data.getCost() * packet.getNum()));

        ResponseConsortiaBuyGoodsPacket response = ResponseConsortiaBuyGoodsPacket.builder()
                .errorCode(0)
                .goodsId(packet.getGoodsId())
                .price(data.getCost() * packet.getNum())
                .build();

        corp.save();
        user.save();
        packet.reply(response);

    }

    @PacketProcessor
    public void onConsortiaUpgrade(RequestConsortiaUpgradePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserBuildings buildings = user.getBuildings();
        List<UserBuilding> building = buildings.getBuildings(6);
        if (building == null || building.isEmpty()) {
            return;
        }

        Corp corp = CorpService.getInstance().getCorpCache().findByGuid(user.getGuid());
        if (corp == null) {
            return;
        }

        CorpMember corpMember = corp.getMembers().getMember(user.getGuid());

        if (corpMember == null) {
            return;
        }
        if (corpMember.getRank() != 1) {
            return;
        }
        if (corp.getCorpUpgrade() != null) {
            return;
        }

        if (packet.getKind() == 0) {

            if (corp.getLevel() >= 9) {
                return;
            }

            int nextLevel = corp.getLevel() + 1;

            CorpsLevelData corpsLevelData = CorpService.getCorpsLevelData(0);
            CorpsLevelMeta corpsLevelMeta = CorpService.getCorpsLevelMeta(corpsLevelData, nextLevel);

            if (corpsLevelData == null) {
                return;
            }
            if (corpsLevelMeta == null) {
                return;
            }

            if (corp.getWealth() < corpsLevelMeta.getCost()) {
                return;
            }
            int time = PacketService.getInstance().isFastCorpUpgrade() ? 1 : corpsLevelMeta.getTime();

            CorpUpgrade newCorpUpgrade = CorpUpgrade.builder()
                    .typeUpgrade(0)
                    .until(DateUtil.now(time))
                    .build();

            corp.setCorpUpgrade(newCorpUpgrade);

            ResponseConsortiaUpgradePacket response = ResponseConsortiaUpgradePacket.builder()
                    .kind(0)
                    .consortiaId(corp.getCorpId())
                    .needTime(time)
                    .build();

            corp.save();
            packet.reply(response);
            return;

        }

        if (packet.getKind() == 1) {

            if (corp.getWarehouseLevel() >= 9) {
                return;
            }

            CorpsLevelData corpsLevelData = CorpService.getCorpsLevelData(1);
            int nextLevel = corp.getWarehouseLevel() + 1;

            CorpsLevelMeta corpsLevelMeta = corpsLevelData.getLevels().get(nextLevel);
            if (corp.getWealth() < corpsLevelMeta.getCost()) {
                return;
            }
            if (corp.getLevel() <= corp.getWarehouseLevel()) {
                return;
            }

            int time = PacketService.getInstance().isFastCorpUpgrade() ? 1 : corpsLevelMeta.getTime();

            CorpUpgrade newCorpUpgrade = CorpUpgrade.builder()
                    .typeUpgrade(1)
                    .until(DateUtil.now(time))
                    .build();

            corp.setCorpUpgrade(newCorpUpgrade);

            ResponseConsortiaUpgradePacket response = ResponseConsortiaUpgradePacket.builder()
                    .kind(1)
                    .consortiaId(corp.getCorpId())
                    .needTime(time)
                    .build();

            corp.save();
            packet.reply(response);
            return;

        }

        if (packet.getKind() == 2) {

            if (corp.getMergingLevel() >= 9) {
                return;
            }
            CorpsLevelData corpsLevelData = CorpService.getCorpsLevelData(2);

            int nextLevel = corp.getMergingLevel() + 1;

            CorpsLevelMeta corpsLevelMeta = corpsLevelData.getLevels().get(nextLevel);
            if (corp.getWealth() < corpsLevelMeta.getCost()) {
                return;
            }
            if (corp.getLevel() <= corp.getMergingLevel()) {
                return;
            }

            int time = PacketService.getInstance().isFastCorpUpgrade() ? 1 : corpsLevelMeta.getTime();

            CorpUpgrade newCorpUpgrade = CorpUpgrade.builder()
                    .typeUpgrade(2)
                    .until(DateUtil.now(time))
                    .build();

            corp.setCorpUpgrade(newCorpUpgrade);

            ResponseConsortiaUpgradePacket response = ResponseConsortiaUpgradePacket.builder()
                    .kind(2)
                    .consortiaId(corp.getCorpId())
                    .needTime(time)
                    .build();

            corp.save();
            packet.reply(response);
            return;

        }

        if (packet.getKind() == 3) {

            if (corp.getMallLevel() >= 9) {
                return;
            }

            CorpsLevelData corpsLevelData = CorpService.getCorpsLevelData(3);
            int nextLevel = corp.getMallLevel() + 1;

            CorpsLevelMeta corpsLevelMeta = corpsLevelData.getLevels().get(nextLevel);
            if (corp.getWealth() < corpsLevelMeta.getCost()) {
                return;
            }
            if (corp.getLevel() <= corp.getMallLevel()) {
                return;
            }

            int time = PacketService.getInstance().isFastCorpUpgrade() ? 1 : corpsLevelMeta.getTime();

            CorpUpgrade newCorpUpgrade = CorpUpgrade.builder()
                    .typeUpgrade(3)
                    .until(DateUtil.now(time))
                    .build();

            corp.setCorpUpgrade(newCorpUpgrade);

            ResponseConsortiaUpgradePacket response = ResponseConsortiaUpgradePacket.builder()
                    .kind(3)
                    .consortiaId(corp.getCorpId())
                    .needTime(time)
                    .build();

            corp.save();
            packet.reply(response);

        }

    }

    @PacketProcessor
    public void onConsortiaUpgradeCancel(RequestConsortiaUpgradeCancelPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserBuildings buildings = user.getBuildings();
        List<UserBuilding> building = buildings.getBuildings(6);
        if (building == null || building.isEmpty()) {
            return;
        }

        Corp corp = CorpService.getInstance().getCorpCache().findByGuid(user.getGuid());
        if (corp == null) {
            return;
        }

        CorpMember corpMember = corp.getMembers().getMember(user.getGuid());
        if (corpMember == null) {
            return;
        }
        if (corp.getCorpUpgrade() == null) {
            return;
        }

        ResponseConsortiaUpgradeCancelPacket response = ResponseConsortiaUpgradeCancelPacket.builder()
                .kind(corp.getCorpUpgrade().getTypeUpgrade())
                .wealth(corp.getWealth())
                .build();

        corp.setCorpUpgrade(null);
        corp.save();
        packet.reply(response);

    }

    @PacketProcessor
    public void onConsortiaRank(RequestConsortiaRankPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        long corpCount = CorpService.getInstance().getCorpCache().count();
        if (corpCount <= 0) {
            return;
        }

        List<Corp> corps = CorpService.getInstance().getCorpCache().findAll();
        corps.sort((corp1, corp2) -> {
            if (corp1.getResourcePlanets().size() > corp2.getResourcePlanets().size()) {
                return -1;
            }
            if (corp1.getResourcePlanets().size() < corp2.getResourcePlanets().size()) {
                return 1;
            }
            if (corp1.getWealth() > corp2.getWealth()) {
                return -1;
            }
            if (corp1.getWealth() < corp2.getWealth()) {
                return 1;
            }
            return 0;
        });

        List<ConsortiaRank> consortiaRanks = new ArrayList<>();

        for (int i = packet.getPageId() * 6; i < ((packet.getPageId() + 1) * 6); i++) {
            if (corps.size() > i) {

                Corp corp = corps.get(i);
                List<ResourcePlanet> planets = corp.getResourcePlanets();
                IntegerArray fields = new IntegerArray(VariableType.MAX_CONSORTIAFIELD);

                for (int hold = 0; hold < planets.size(); hold++) {
                    fields.getArray()[hold] = planets.get(hold).getPosition().getParentZone().zoneId();
                }

                consortiaRanks.add(new ConsortiaRank(corp.getName(), corp.getCorpId(), i, corp.getWealth(), fields, (short) 0, (char) corp.getIcon(), (char) corp.getLevel(), (char) planets.size(), (char) corp.getMembers().getMembers().size(), (char) corp.getMaxMembers(), (char) 0));

            }
        }

        ResponseConsortiaRankPacket response = ResponseConsortiaRankPacket.builder()
                .consortiaCount((int) corpCount)
                .dataLen(consortiaRanks.size())
                .data(consortiaRanks)
                .build();

        packet.reply(response);

    }

    @PacketProcessor
    public void onConsortiaUpdateValue(RequestConsortiaUpdateValuePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserBuildings buildings = user.getBuildings();
        List<UserBuilding> building = buildings.getBuildings(6);
        if (building == null || building.isEmpty()) {
            return;
        }

        Corp corp = CorpService.getInstance().getCorpCache().findByGuid(user.getGuid());
        if (corp == null) {
            return;
        }

        CorpMember corpMember = corp.getMembers().getMember(user.getGuid());

        if (corpMember == null) {
            return;
        }
        if (corpMember.getRank() != 1) {
            return;
        }

        corp.setContributionMerge(packet.getNeedUnionValue());
        corp.setContributionMall(packet.getNeedShopValue());

        ResponseConsortiaUpdateValuePacket response = ResponseConsortiaUpdateValuePacket.builder()
                .needShopValue(corp.getContributionMall())
                .needUnionValue(corp.getContributionMerge())
                .build();

        corp.save();
        packet.reply(response);

    }

    @PacketProcessor
    public void onConsortiaPirateChoose(RequestConsortiaPirateChoosePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Corp userCorp = user.getCorp();
        if (userCorp == null) {
            return;
        }

        CorpMember userMember = userCorp.getMembers().getMember(user.getGuid());
        if (userMember == null || userMember.getRank() != 1) {
            return;
        }

        User objUser = UserService.getInstance().getUserCache().findByGuid(packet.getObjGuid());
        if (objUser == null) {
            return;
        }

        Corp objCorp = objUser.getCorp();
        if (objCorp == null || objCorp.getCorpId() != userCorp.getCorpId()) {
            return;
        }

        CorpMember objMember = objCorp.getMembers().getMember(objUser.getGuid());
        if (objMember == null) {
            return;
        }

        if (objUser.getPlanet().isInWar()) {

            ResponseConsortiaPirateChoosePacket response = new ResponseConsortiaPirateChoosePacket();
            response.setErrorCode(1);

            packet.reply(response);
            return;

        }

        if (objUser.getStats().hasTruce()) {

            ResponseConsortiaPirateChoosePacket response = new ResponseConsortiaPirateChoosePacket();
            response.setErrorCode(2);

            packet.reply(response);
            return;

        }

        ResponseConsortiaPirateChoosePacket response = new ResponseConsortiaPirateChoosePacket();

        response.setErrorCode(0);

        response.setObjGuid(objUser.getGuid());
        response.setObjName(SmartString.of(objUser.getUsername(), 32));

        response.setGalaxyId(objUser.getGalaxyId());
        response.setAssault(RankService.getInstance().getAttackPower(objUser));
        response.setLevelId(objUser.getStats().getLevel());

        packet.reply(response);

    }

    @PacketProcessor
    public void onConsortiaPirate(RequestConsortiaPiratePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }
        if (packet.getLevel() < 0 || packet.getLevel() > 9) {
            return;
        }

        Corp userCorp = user.getCorp();
        if (userCorp == null) {
            return;
        }

        CorpMember userMember = userCorp.getMembers().getMember(user.getGuid());
        if (userMember == null || userMember.getRank() != 1) {
            return;
        }

        User objUser = UserService.getInstance().getUserCache().findByGuid(packet.getObjGuid());
        if (objUser == null) {
            return;
        }

        UserPlanet objPlanet = objUser.getPlanet();
        if (objPlanet == null) {
            return;
        }

        Corp objCorp = objUser.getCorp();
        if (objCorp == null || objCorp.getCorpId() != userCorp.getCorpId()) {
            return;
        }

        CorpMember objMember = objCorp.getMembers().getMember(objUser.getGuid());
        if (objMember == null) {
            return;
        }
        if (objUser.getPlanet().isInWar()) {
            return;
        }
        if (objUser.getStats().hasTruce()) {
            return;
        }
        if (!(objCorp.getLevel() >= 2)) {
            return;
        }

        InstanceData instanceData = ResourceManager.getPirates().getPirate(packet.getLevel() + 1);
        if (instanceData == null) {
            return;
        }
        if (objCorp.getPiratesLevel() >= instanceData.getId()) {
            return;
        }
        instanceData.generatePirateEnemies(objPlanet.getPosition().galaxyId(), true, false);
        MatchRunnable runnable = BattleService.getInstance().makeWarMatch(objPlanet);
        if (runnable == null) {
            return;
        }
        WarMatch warMatch = (WarMatch) runnable.getMatch();
        warMatch.getMetadata().getObjects().put("real_type", MatchType.PIRATES_MATCH);

        warMatch.getMetadata().getStrings().put("consortia_name", objCorp.getName());
        warMatch.getMetadata().getIntegers().put("consortia_id", objCorp.getCorpId());

        warMatch.getMetadata().getIntegers().put("level", packet.getLevel());
        warMatch.getMetadata().getBooleans().put("pirates", true);

        for (LoggedGameUser loggedGameUser : warMatch.getViewers()) {

            ResponseBuildInfoPacket response = new ResponseBuildInfoPacket();
            response.setBuildInfoList(new ArrayList<>());

            List<Packet> warPackets = BattleService.getInstance().getWarJoinPackets(warMatch, loggedGameUser.getUpdatedUser(), response);
            warPackets.remove(0);

            loggedGameUser.setMatchViewing(warMatch.getId());
            packet.reply(warPackets);

        }

        if (!PacketService.getInstance().isTestMode()) {
            objCorp.setLastPirates(new Date());
        }

        objCorp.save();
        warMatch.start();

        for (LoggedGameUser loggedGameUser : LoginService.getInstance().getGameUsers()) {

            ResponseFightGalaxyBeginPacket broadcastWarPacket = GalaxyService.getInstance().getUserPlanetGalaxyBeginPacket(objPlanet, 2, packet.getLevel());

            broadcastWarPacket.setConsortiaId(objCorp.getCorpId());
            broadcastWarPacket.getConsortiaName().value(objCorp.getName());

            loggedGameUser.getSmartServer().send(broadcastWarPacket);

        }

    }

    @PacketProcessor
    public void onGotoResourceStar(RequestGotoResourceStarPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Corp userCorp = user.getCorp();
        if (userCorp == null) {
            return;
        }

        Planet planet = GalaxyService.getInstance().getPlanet(new GalaxyTile(packet.getGalaxyId()));
        if (planet == null || planet.getType() != PlanetType.RESOURCES_PLANET) {
            return;
        }

        ResourcePlanet resourcePlanet = (ResourcePlanet) planet;
        if (resourcePlanet == null || !resourcePlanet.isPeace()) {
            return;
        }

        Optional<Corp> planetCorp = resourcePlanet.getCorp();
        if (planetCorp.isEmpty()) {
            return;
        }

        Corp corp = planetCorp.get();
        if (corp.getCorpId() != userCorp.getCorpId()) {
            return;
        }

        int maxFleets = resourcePlanet.getMaxFleets();
        if (maxFleets <= 0) {
            return;
        }

        List<Fleet> current = PacketService.getInstance().getFleetCache().findAllByGalaxyId(planet.getPosition().galaxyId());
        if (current.size() >= maxFleets || current.size() + packet.getDataLen() >= maxFleets) {
            return;
        }

        ResponseGotoResourceStarPacket response = new ResponseGotoResourceStarPacket();
        response.setErrorCode(1);

        LinkedList<Fleet> toMove = new LinkedList<>();
        List<Fleet> fleets = user.getFleets();

        for (int index = 0; index < Math.min(packet.getDataLen(), 100); index++) {

            int shipTeamId = packet.getData().getArray()[index];
            if (shipTeamId == -1) {
                packet.reply(response);
                return;
            }

            Fleet fleet = fleets.stream().filter(cache -> cache.getShipTeamId() == shipTeamId).findFirst().orElse(null);
            if (fleet == null || fleet.getGuid() != packet.getGuid() || fleet.isInTransmission() || fleet.isInMatch()) {
                packet.reply(response);
                return;
            }

            toMove.add(fleet);

        }

        if (toMove.isEmpty()) {
            return;
        }

        for (Fleet fleet : toMove) {

            GalaxyTile targetTile = planet.getPosition();
            GalaxyTile from = new GalaxyTile(fleet.getGalaxyId());

            fleet.setGalaxyId(targetTile.galaxyId());
            fleet.setFleetTransmission(null);
            fleet.setFleetInitiator(null);

            ResponseCreateShipTeamPacket responseCreateShipTeamPacket = new ResponseCreateShipTeamPacket();

            responseCreateShipTeamPacket.setGalaxyMapId(0);
            responseCreateShipTeamPacket.setGalaxyId(targetTile.galaxyId());

            GalaxyFleetInfo fleetInfo = new GalaxyFleetInfo();

            fleetInfo.setShipTeamId(fleet.getShipTeamId());
            fleetInfo.setShipNum(fleet.ships());
            fleetInfo.setBodyId((short) fleet.getBodyId());
            fleetInfo.setReserve((short) 0);
            fleetInfo.setDirection((byte) fleet.getDirection());

            fleetInfo.setPosX((byte) fleet.getPosX());
            fleetInfo.setPosY((byte) fleet.getPosY());

            responseCreateShipTeamPacket.setGalaxyFleetInfo(fleetInfo);

            for (LoggedGameUser viewer : LoginService.getInstance().getPlanetViewers(targetTile.galaxyId())) {

                fleetInfo.setOwner((byte) (BattleService.getInstance().getFleetColor(viewer, fleet)));
                viewer.getSmartServer().send(response);

            }

            fleet.save();

            List<LoggedGameUser> loggedGameUsers = LoginService.getInstance().getPlanetViewers(from.galaxyId());

            for (LoggedGameUser loggedGameUser : loggedGameUsers) {

                ResponseDeleteShipTeamBroadcastPacket broadcast = new ResponseDeleteShipTeamBroadcastPacket();

                broadcast.setGalaxyMapId(0);
                broadcast.setGalaxyId(targetTile.galaxyId());
                broadcast.setShipTeamId(fleet.getShipTeamId());

                loggedGameUser.getSmartServer().send(broadcast);

            }
        }

        response.setErrorCode(0);
        packet.reply(response);

    }

    @PacketProcessor
    public void onConsortiaAttackInfo(RequestConsortiaAttackInfoPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null || packet.getPageId() < 0) {
            return;
        }

        Corp userCorp = user.getCorp();
        if (userCorp == null) {
            return;
        }

        List<User> members = new ArrayList<>();
        List<Planet> domain = new ArrayList<>();

        for (CorpMember member : userCorp.getMembers().getMembers()) {
            User memberUser = UserService.getInstance().getUserCache().findByGuid(member.getGuid());
            if (memberUser != null) {
                members.add(memberUser);
            }
        }

        for (User member : members) {
            domain.add(member.getPlanet());
        }
        domain.addAll(userCorp.getResourcePlanets());

        List<Fleet> transition = PacketService.getInstance().getFleetCache().getInTransmissionFleets();
        if (transition.isEmpty()) {
            return;
        }

        transition = transition.stream()
                .filter(fleet -> fleet.getFleetTransmission() != null && fleet.getFleetTransmission().getJumpType() == JumpType.ATTACK)
                .filter(x -> domain.stream().anyMatch(planet -> planet.getPosition().galaxyId() == x.getFleetTransmission().getGalaxyId()))
                .collect(Collectors.toList());


        if (transition.isEmpty()) {
            return;
        }

        List<List<Fleet>> partition = Lists.partition(transition, 6);

        List<Fleet> page = packet.getPageId() < partition.size() ? partition.get(packet.getPageId()) : new ArrayList<>();
        List<ConsortiaAttackInfo> attackInfos = new ArrayList<>();

        ResponseConsortiaAttackInfoPacket response = new ResponseConsortiaAttackInfoPacket();

        response.setAttackCount((short) transition.size());
        response.setData(attackInfos);

        for (Fleet fleet : page) {

            User owner = fleet.getUser();
            if (owner == null) {
                continue;
            }

            FleetTransmission fleetTransmission = fleet.getFleetTransmission();
            if (fleetTransmission == null) {
                continue;
            }

            GalaxyTile destination = new GalaxyTile(fleetTransmission.getGalaxyId());
            Planet targetPlanet = domain.stream().filter(dom -> dom.getPosition().equals(destination)).findFirst().orElse(null);
            if (targetPlanet == null) {
                continue;
            }

            ConsortiaAttackInfo info = new ConsortiaAttackInfo();

            info.setSourceGalaxyId(fleet.getGalaxyId());
            info.setSourceGuid(owner.getGuid());
            info.setSourceName(owner.getUsername());
            info.setSourceUserId(owner.getUserId());
            info.setReserve(fleet.getShipTeamId());

            info.setNeedTime(DateUtil.remains(fleetTransmission.getUntil()).intValue());

            Corp ownerCorp = owner.getCorp();
            if (ownerCorp != null) {
                info.setConsortiaName(ownerCorp.getName());
            } else {
                info.setConsortiaName("");
            }

            if (targetPlanet instanceof ResourcePlanet resourcePlanet) {

                info.setTargetName("Resource Bonus Planet");
                info.setTargetGuid(-1);
                info.setTargetUserId(resourcePlanet.getUserId());
                info.setTargetGalaxyId(resourcePlanet.getPosition().galaxyId());

            } else if (targetPlanet instanceof UserPlanet userPlanet) {

                User targetUser = members.stream().filter(member -> member.getUserId() == userPlanet.getUserId()).findFirst().orElse(null);

                info.setTargetName(targetUser.getUsername());
                info.setTargetGuid(targetUser.getGuid());
                info.setTargetUserId(targetUser.getUserId());
                info.setTargetGalaxyId(userPlanet.getPosition().galaxyId());

            }

            attackInfos.add(info);

        }

        response.setDataLen((short) attackInfos.size());
        packet.reply(response);

    }

}
