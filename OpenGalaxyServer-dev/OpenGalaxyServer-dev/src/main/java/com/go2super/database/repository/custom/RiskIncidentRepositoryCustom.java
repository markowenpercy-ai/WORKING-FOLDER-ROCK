package com.go2super.database.repository.custom;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.BadGuidIncident;
import com.go2super.database.entity.sub.PacketFloodIncident;
import com.go2super.database.entity.sub.SameIPIncident;
import com.go2super.packet.Packet;
import com.go2super.server.GameServerReceiver;

import java.util.*;

public interface RiskIncidentRepositoryCustom {

    List<SameIPIncident> getSameIPDetections(Account account);

    Optional<SameIPIncident> checkSameIPAndSave(Account account, User user, GameServerReceiver serverReceiver);

    Optional<BadGuidIncident> checkBadGuidAndSave(User user, GameServerReceiver serverReceiver, Packet packet, int guid);

    Optional<PacketFloodIncident> checkPacketFloodAndSave(User user, GameServerReceiver serverReceiver, String packetName, double ppt);

}
