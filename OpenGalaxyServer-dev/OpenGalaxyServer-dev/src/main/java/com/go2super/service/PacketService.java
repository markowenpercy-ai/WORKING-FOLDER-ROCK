package com.go2super.service;

import com.go2super.database.cache.FleetCache;
import com.go2super.database.cache.PlanetCache;
import com.go2super.database.cache.SanctionCache;
import com.go2super.database.cache.ShipModelCache;
import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.User;
import com.go2super.logger.BotLogger;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.game.ShipModelInfo;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.obj.utility.UnsignedShort;
import com.go2super.obj.utility.WideString;
import com.go2super.packet.Packet;
import com.go2super.packet.custom.CustomConfigurationPacket;
import com.go2super.packet.custom.CustomMoreInfoPacket;
import com.go2super.packet.shipmodel.ResponseShipModelInfoDelPacket;
import com.go2super.packet.shipmodel.ResponseShipModelInfoPacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.CorpsShopData;
import com.go2super.resources.json.CorpsShopJson;
import com.go2super.resources.localization.Localization;
import com.go2super.service.battle.Match;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.*;

@Service
public class PacketService {

    private static PacketService instance;
    private static final Map<Integer, ShipModel> cachedModels = new HashMap<>();

    public static boolean DYNAMIC_MAINTENANCE = false;
    public static final Date STARTUP = new Date();

    @Getter
    private final ShipModelCache shipModelCache;
    @Getter
    private final PlanetCache planetCache;
    @Getter
    private final SanctionCache sanctionCache;
    @Getter
    private final FleetCache fleetCache;

    @Value("${application.game.gelf-endpoint}")
    @Getter
    private String gelfEndpoint;

    @Value("${application.game.gelf-scope}")
    @Getter
    private String gelfScope;

    @Value("${application.game.ip}")
    @Getter
    private String serverIp;

    @Value("${application.game.external-ip:127.0.0.1}")
    @Getter
    private String externalIp;

    @Value("${application.game.timeout}")
    @Getter
    private int timeout;

    @Value("${application.game.interval}")
    @Getter
    private int interval;

    @Value("${application.game.login}")
    @Getter
    private boolean login;

    @Value("${application.game.register}")
    @Getter
    private boolean register;

    @Value("${application.game.maintenance}")
    @Getter
    private boolean maintenance;

    @Value("${application.game.test-mode}")
    @Getter
    private boolean testMode;

    @Value("${application.game.fast-ship-building}")
    @Getter
    private boolean fastShipBuilding;

    @Value("${application.game.fast-corp-upgrade}")
    @Getter
    private boolean fastCorpUpgrade;

    @Value("${application.game.fast-transmission}")
    @Getter
    private boolean fastTransmission;

    

    @Value("${application.game.resources-url}")
    @Getter
    private String resourcesUrl;

    @Value("${application.game.hcaptcha-secret}")
    @Getter
    private String hcaptchaSecret;

    @Value("${application.game.whitelist}")
    @Getter
    private List<String> whitelist;

    @Value("${application.game.verbose}")
    @Getter
    private Integer verbose;

    @Value("${application.game.phase}")
    @Getter
    private Integer phase;

    @Value("${application.game.fast-rbp}")
    @Getter
    private boolean fastRbp;

    @Value("${application.game.smtp-host}")
    @Getter
    private String smtpHost;

    @Value("${application.game.smtp-username}")
    @Getter
    private String smtpUsername;

    @Value("${application.game.smtp-password}")
    @Getter
    private String smtpPassword;

    @Value("${application.game.default-admin-username}")
    @Getter
    private String defaultAdminUsername;

    @Value("${application.game.default-admin-email}")
    @Getter
    private String defaultAdminEmail;

    @Value("${application.game.default-admin-password}")
    @Getter
    private String defaultAdminPassword;

    @Autowired
    public PacketService(FleetCache fleetCache, SanctionCache sanctionCache, PlanetCache planetCache, ShipModelCache shipModelCache) {

        instance = this;

        this.fleetCache = fleetCache;
        this.sanctionCache = sanctionCache;
        this.planetCache = planetCache;
        this.shipModelCache = shipModelCache;

    }

    public boolean isFirstPhase() {

        return phase == 1;
    }

    public boolean isFastRbp() {

        return fastRbp;
    }

    public boolean isMaintenance() {

        return maintenance || DYNAMIC_MAINTENANCE;
    }

    public boolean hasFleetsDeployed(User user) {

        List<Fleet> fleets = PacketService.getInstance().getFleetCache().findAllByGuid(user.getGuid());

        for (Fleet fleet : fleets) {
            if (fleet.isInTransmission() || (fleet.getGalaxyId() != user.getPlanet().getPosition().galaxyId())) {
                Match match = fleet.getCurrentMatch();
                if (match != null && match.getMatchType().isVirtual()) {
                    continue;
                }
                return true;
            }
        }

        return false;

    }

    public List<Packet> getShipModels(User user, Set<Integer> shipModelIds) {

        List<Pair<ShipModelInfo, Boolean>> toSend = new ArrayList<>();
        List<Packet> packets = new ArrayList<>();

        List<ShipModel> shipModels = new ArrayList<>();
        shipModels.add(0, PacketService.getShipModel(0));

        for (int modelId : shipModelIds) {

            ShipModel model = shipModelCache.findByShipModelId(modelId);

            if (model == null) {
                user.getShips().ships = new ArrayList<>(user.getShips().ships.stream().filter(x -> x.getShipModelId() != modelId).toList());
                user.save();
                BotLogger.error("ShipModel = " + modelId + " NOT FOUND! DELETING!");
                continue;
            }

            if (toSend.stream().anyMatch(send -> send.getKey().getShipModelId() == modelId)) {
                continue;
            }

            // if((packet.getShipModelInfoList().size() + 1) > 7) {
            //
            //     packets.value(packet);
            //     packet = new ResponseShipModelInfoPacket();
            //
            // }

            if (model.getGuid() != user.getGuid() || model.isDeleted()) {
                toSend.add(Pair.of(ShipModelInfo.of(model.getName(), model.partNum(), model.getShipModelId() == 0 ? 1 : 0, model.getBodyId(), model.partArray(), model.getShipModelId()), true));
                continue;
            }

            toSend.add(Pair.of(ShipModelInfo.of(model.getName(), model.partNum(), model.getShipModelId() == 0 ? 1 : 0, model.getBodyId(), model.partArray(), model.getShipModelId()), false));
            continue;

        }

        List<Pair<ShipModelInfo, Boolean>> delModels = toSend.stream().filter(pair -> pair.getValue()).collect(Collectors.toList());
        List<Pair<ShipModelInfo, Boolean>> commonModels = toSend.stream().filter(pair -> !pair.getValue()).collect(Collectors.toList());

        if (delModels.size() > 0) {

            ResponseShipModelInfoDelPacket currentDel = new ResponseShipModelInfoDelPacket();

            for (Pair<ShipModelInfo, Boolean> pair : delModels) {

                if ((currentDel.getShipModelInfoList().size() + 1) > 7) {

                    packets.add(currentDel);
                    currentDel = new ResponseShipModelInfoDelPacket();

                }

                currentDel.getShipModelInfoList().add(pair.getKey());
                currentDel.setDataLen(UnsignedShort.of(currentDel.getShipModelInfoList().size()));
                continue;

            }

            if (!packets.contains(currentDel)) {
                packets.add(currentDel);
            }

        }

        if (commonModels.size() > 0) {

            ResponseShipModelInfoPacket currentCommon = new ResponseShipModelInfoPacket();

            for (Pair<ShipModelInfo, Boolean> pair : commonModels) {

                if ((currentCommon.getShipModelInfoList().size() + 1) > 7) {

                    packets.add(currentCommon);
                    currentCommon = new ResponseShipModelInfoPacket();

                }

                currentCommon.getShipModelInfoList().add(pair.getKey());
                currentCommon.setDataLen(UnsignedShort.of(currentCommon.getShipModelInfoList().size()));
                continue;

            }

            if (!packets.contains(currentCommon)) {
                packets.add(currentCommon);
            }

        }

        return packets;

    }

    public boolean sendMoreInfoPacket(int kind, User user, Packet from) {

        SmartServer smartServer = from.getSmartServer();
        if (smartServer.getSentMoreInfos().contains(user.getUserId())) {
            return false;
        }

        CustomMoreInfoPacket packet = getMoreInfoPacket(kind, user);

        smartServer.send(packet);
        smartServer.getSentMoreInfos().add(user.getUserId());
        return true;

    }

    public boolean sendMoreInfoPacket(int kind, long userId, Packet from) {

        SmartServer smartServer = from.getSmartServer();
        if (smartServer.getSentMoreInfos().contains(userId)) {
            return false;
        }

        CustomMoreInfoPacket packet = getMoreInfoPacket(kind, userId);

        smartServer.send(packet);
        smartServer.getSentMoreInfos().add(userId);
        return true;

    }

    public CustomMoreInfoPacket getMoreInfoPacket(int kind, User user) {

        CustomMoreInfoPacket packet = new CustomMoreInfoPacket();

        packet.setKind((byte) kind);
        packet.setGuid(user.getGuid());
        packet.setUserId(user.getUserId());
        packet.setIcon(Math.toIntExact(user.getUserId()));

        return packet;

    }

    public CustomConfigurationPacket getConfigurationPacket(User user) {

        CustomConfigurationPacket packet = new CustomConfigurationPacket();
        packet.setResourcesUrl(WideString.of(resourcesUrl, 52));

        return packet;

    }

    public CustomMoreInfoPacket getMoreInfoPacket(int kind, long userId) {

        User user = UserService.getInstance().getUserCache().findByUserId(userId);
        if (user == null) {
            return null;
        }

        CustomMoreInfoPacket customMoreInfoPacket = new CustomMoreInfoPacket();

        customMoreInfoPacket.setKind((byte) kind);
        customMoreInfoPacket.setGuid(user.getGuid());
        customMoreInfoPacket.setUserId(user.getUserId());
        customMoreInfoPacket.setIcon(Math.toIntExact(user.getUserId()));

        return customMoreInfoPacket;

    }

    public ResponseShipModelInfoDelPacket getTemporalShipModel(int shipModelId) {

        ShipModel shipModel = PacketService.getShipModel(shipModelId);

        ResponseShipModelInfoDelPacket response = new ResponseShipModelInfoDelPacket();
        response.setShipModelInfoList(new ArrayList<>());

        if (shipModel != null) {

            response.getShipModelInfoList().add(
                ShipModelInfo.of(
                    shipModel.getName(),
                    shipModel.partNum(),
                    shipModel.getShipModelId() == 0 ? 1 : 0,
                    shipModel.getBodyId(),
                    shipModel.partArray(),
                    shipModel.getShipModelId())
            );

        }

        response.setDataLen(UnsignedShort.of(response.getShipModelInfoList().size()));
        return response;

    }

    public static ShipModel getShipModel(int shipModel) {

        if (cachedModels.containsKey(shipModel)) {
            return cachedModels.get(shipModel);
        }

        ShipModel model = PacketService.getInstance().getShipModelCache().findByShipModelId(shipModel);

        if (model == null) {
            return null;
        }

        cachedModels.put(shipModel, model);
        return model;

    }

    public static CorpsShopData getCorpShopData(int index) {

        CorpsShopJson corpsShopJson = ResourceManager.getCorpsShopJson();

        BotLogger.log(corpsShopJson.getShips().size());

        if (index < 0 || index >= corpsShopJson.getShips().size()) {
            return null;
        }

        List<CorpsShopData> corpsShopData = corpsShopJson.getShips();
        CorpsShopData data = corpsShopData.get(index);

        return data;

    }

    public static ShipModel getCorpShipModel(CorpsShopData data) {

        String name = Localization.EN_US.get(data.getDesign());
        List<ShipModel> shipModels = PacketService.getInstance().getShipModelCache().findAllByNameAndGuid(name, -1);

        if (shipModels.isEmpty()) {
            return null;
        }

        return shipModels.get(0);

    }

    public List<ShipTeamNum> getAllShipNums(User user) {

        List<ShipTeamNum> result = new ArrayList<>();
        result.addAll(user.getShips().getShips());

        for (Fleet fleet : PacketService.getInstance().getFleetCache().findAllByGuid(user.getGuid())) {
            result.addAll(fleet.getFleetBody().getCells());
        }

        return result;

    }

    public boolean getFastShipBuilding() {

        return fastShipBuilding;
    }

    public static PacketService getInstance() {

        return instance;
    }

}
