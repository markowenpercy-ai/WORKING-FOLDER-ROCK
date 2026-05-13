package com.go2super.database.repository.custom;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.database.entity.UserIP;
import com.go2super.database.entity.sub.*;
import com.go2super.packet.Packet;
import com.go2super.server.GameServerReceiver;
import com.go2super.service.AccountService;
import com.go2super.service.RiskService;
import com.go2super.socket.util.IPLocation;
import com.go2super.socket.util.dto.IPLookupDTO;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;

public class RiskIncidentRepositoryImpl implements RiskIncidentRepositoryCustom {

    private static final List<String> ISP_WHITELIST = Arrays.asList(
        "CloudMosa Inc.", // Puffin
        "Cogent Communications", // Puffin
        "Starhub Internet Pte Ltd" // Puffin
    );

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public List<SameIPIncident> getSameIPDetections(Account account) {

        Criteria criteria = Criteria.where("users.accountName").is(account.getUsername());
        Query query = new Query(criteria).restrict(SameIPIncident.class);

        return mongoTemplate.find(query, SameIPIncident.class);

    }

    @Override
    public Optional<SameIPIncident> checkSameIPAndSave(Account account, User user, GameServerReceiver serverReceiver) {

        // Register this IP
        RiskService.getInstance().getUserIPRepository().updateUserIP(account, user, serverReceiver);

        // Whois
        String IP = serverReceiver.getIp();
        String ISP = "Unknown";
        String COUNTRY = "Unknown";


        /*
        Optional<IPLookupDTO> optionalIpLookupDTO = IPLocation.getLocation(IP);
        isp:
        if (optionalIpLookupDTO.isPresent()) {

            String isp = optionalIpLookupDTO.get().getIsp();
            if (isp == null) {
                break isp;
            }

            if (ISP_WHITELIST.contains(isp)) {
                return Optional.empty();
            }

            ISP = isp;
            COUNTRY = optionalIpLookupDTO.get().getCountry();

        }

        if (COUNTRY == null) {
            COUNTRY = "Unknown";
        }
         */

        Criteria criteria = Criteria.where("ip").is(IP);
        Query query = new Query(criteria).restrict(SameIPIncident.class);

        Optional<SameIPIncident> optionalSameIPIncident = Optional.ofNullable(mongoTemplate.findOne(query, SameIPIncident.class));

        if (optionalSameIPIncident.isPresent()) {

            SameIPIncident sameIPIncident = optionalSameIPIncident.get();

            if (sameIPIncident.getUsers() == null) {
                sameIPIncident.setUsers(new ArrayList<>());
            }

            Optional<UserSameIPIncidentInfo> optionalUser = sameIPIncident.getUsers().stream().filter(other -> other.getGuid() == user.getGuid()).findFirst();

            if (optionalUser.isPresent()) {

                UserSameIPIncidentInfo userSameIPIncidentInfo = optionalUser.get();
                userSameIPIncidentInfo.setCount(userSameIPIncidentInfo.getCount() + 1);
                userSameIPIncidentInfo.setCountry(COUNTRY);
                userSameIPIncidentInfo.setIsp(ISP);

            } else {

                sameIPIncident.getUsers().add(UserSameIPIncidentInfo.builder()
                    .userId(user.getUserId())

                    .accountId(serverReceiver.getAccountId())
                    .discord(serverReceiver.getDiscordId())
                    .accountName(serverReceiver.getAccountName())
                    .email(serverReceiver.getAccountEmail())

                    .username(user.getUsername())
                    .guid(user.getGuid())
                    .userId(user.getUserId())

                    .ip(serverReceiver.getIp())
                    .country(COUNTRY)
                    .isp(ISP)

                    .count(1)
                    .timestamp(new Date())
                    .build());

            }

            mongoTemplate.save(sameIPIncident);
            return Optional.of(sameIPIncident);

        }

        SameIPIncident sameIPIncident = SameIPIncident.builder()
            .id(ObjectId.get())
            .ip(serverReceiver.getIp())
            .creation(new Date())
            .creator("Medusa")
            .ignore(false)
            .users(new ArrayList<>())
            .build();

        String ip = serverReceiver.getIp();
        boolean conflict = RiskService.getInstance().getUserIPRepository().hasIPConflict(account, serverReceiver.getIp());

        if (conflict) {

            List<UserIP> conflicts = RiskService.getInstance().getUserIPRepository().getIPConflict(serverReceiver.getIp());

            for (UserIP userIP : conflicts) {

                Optional<UserIPInfo> optionalUserIPInfo = userIP.getIps().stream().filter(other -> other.getIp().equals(ip)).findFirst();
                if (optionalUserIPInfo.isEmpty()) {
                    continue;
                }

                UserIPInfo userIPInfo = optionalUserIPInfo.get();

                UserSameIPIncidentInfo incidentInfo = UserSameIPIncidentInfo.builder()
                    .userId(userIPInfo.getUserId())
                    .guid(userIPInfo.getGuid())

                    .accountId(userIPInfo.getAccountId())
                    .username(userIPInfo.getUsername())

                    .ip(ip)
                    .country(COUNTRY)
                    .isp(ISP)

                    .count(1)
                    .timestamp(new Date())
                    .build();

                Optional<Account> optionalAccount = AccountService.getInstance().getAccountCache().findById(userIPInfo.getAccountId());
                if (optionalAccount.isPresent()) {

                    Account userAccount = optionalAccount.get();

                    if (userAccount.getDiscordHook() != null && userAccount.getDiscordHook().getDiscordId() != null) {
                        incidentInfo.setDiscord(userAccount.getDiscordHook().getDiscordId());
                    }

                    incidentInfo.setEmail(userAccount.getEmail());
                    incidentInfo.setAccountName(userAccount.getUsername());

                }

                sameIPIncident.getUsers().add(incidentInfo);

            }

            mongoTemplate.save(sameIPIncident);
            return Optional.of(sameIPIncident);

        }

        return Optional.empty();

    }

    @Override
    public Optional<BadGuidIncident> checkBadGuidAndSave(User user, GameServerReceiver serverReceiver, Packet packet, int targetGuid) {

        Criteria criteria = Criteria.where("guid").is(user.getGuid());
        Query query = new Query(criteria).restrict(BadGuidIncident.class);

        Optional<BadGuidIncident> optionalBadGuidIncident = Optional.ofNullable(mongoTemplate.findOne(query, BadGuidIncident.class));
        if (optionalBadGuidIncident.isPresent()) {

            BadGuidIncident badGuidIncident = optionalBadGuidIncident.get();

            if (badGuidIncident.getLastDetections() == null) {
                badGuidIncident.setLastDetections(new ArrayList<>());
            }

            badGuidIncident.getLastDetections().add(packet.getClass().getSimpleName());
            badGuidIncident.setLastDetection(new Date());
            badGuidIncident.setTotalCount(badGuidIncident.getTotalCount() + 1);

            mongoTemplate.save(badGuidIncident);
            return Optional.of(badGuidIncident);

        }

        BadGuidIncident badGuidIncident = BadGuidIncident.builder()
            .id(ObjectId.get())

            .accountId(serverReceiver.getAccountId())
            .discord(serverReceiver.getDiscordId())
            .accountName(serverReceiver.getAccountName())
            .email(serverReceiver.getAccountEmail())

            .guid(user.getGuid())
            .userId(user.getUserId())

            .targetGuid(targetGuid)

            .creation(new Date())
            .creator("Medusa")
            .ignore(false)

            .totalCount(1)

            .lastDetections(List.of(packet.getClass().getSimpleName()))
            .lastDetection(new Date())
            .build();

        mongoTemplate.save(badGuidIncident);
        return Optional.ofNullable(badGuidIncident);

    }

    @Override
    public Optional<PacketFloodIncident> checkPacketFloodAndSave(User user, GameServerReceiver serverReceiver, String packetName, double ppt) {

        Criteria criteria = Criteria.where("guid").is(user.getGuid());
        Query query = new Query(criteria).restrict(PacketFloodIncident.class);

        Optional<PacketFloodIncident> optionalPacketFloodIncident = Optional.ofNullable(mongoTemplate.findOne(query, PacketFloodIncident.class));
        if (optionalPacketFloodIncident.isPresent()) {

            PacketFloodIncident floodIncident = optionalPacketFloodIncident.get();

            floodIncident.setTotalReports(floodIncident.getTotalReports() + 1);
            floodIncident.setPpt((floodIncident.getPpt() + ppt) / floodIncident.getTotalReports());

            floodIncident.setLastDetection(new Date());

            if (floodIncident.getLastPackets() == null) {
                floodIncident.setLastPackets(new LinkedList<>());
            }

            if (floodIncident.getLastPackets().size() + 1 > 100) {
                floodIncident.getLastPackets().removeLast();
            }

            floodIncident.getLastPackets().addFirst(packetName);
            mongoTemplate.save(floodIncident);
            return Optional.of(floodIncident);

        }

        LinkedList<String> lastPackets = new LinkedList<>();
        lastPackets.addFirst(packetName);

        PacketFloodIncident floodIncident = PacketFloodIncident.builder()
            .id(ObjectId.get())

            .accountId(serverReceiver.getAccountId())
            .discord(serverReceiver.getDiscordId())
            .accountName(serverReceiver.getAccountName())
            .email(serverReceiver.getAccountEmail())
            .username(user.getUsername())

            .guid(user.getGuid())
            .userId(user.getUserId())

            .creation(new Date())
            .creator("Medusa")
            .ignore(false)
            .totalReports(1)
            .lastDetection(new Date())
            .lastPackets(lastPackets)

            .ppt(ppt)
            .build();

        mongoTemplate.save(floodIncident);
        return Optional.ofNullable(floodIncident);

    }

}
