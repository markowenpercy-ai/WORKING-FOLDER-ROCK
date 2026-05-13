package com.go2super.listener;

import com.go2super.database.entity.Corp;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.CorpMember;
import com.go2super.database.entity.sub.CorpMembers;
import com.go2super.obj.game.IntegerArray;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.map.RequestMapAreaPacket;
import com.go2super.packet.map.RequestMapBlockPacket;
import com.go2super.packet.map.ResponseConsortiaStarPacket;
import com.go2super.packet.map.ResponseMapBlockFightPacket;
import com.go2super.service.GalaxyService;
import com.go2super.service.LoginService;
import com.go2super.service.UserService;
import com.go2super.service.exception.BadGuidException;

public class MapListener implements PacketListener {

    @PacketProcessor
    public void onMapBlock(RequestMapBlockPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Corp corp = user.getCorp();
        if (corp != null) {

            CorpMembers members = corp.getMembers();

            ResponseConsortiaStarPacket responseConsortiaStarPacket = new ResponseConsortiaStarPacket();

            responseConsortiaStarPacket.setGalaxyMapId((short) 0);
            responseConsortiaStarPacket.setDataLen((short) 250);

            int index = 0;
            int[] data = new int[250];

            for (CorpMember member : members.getMembers()) {

                if (member.getGuid() == user.getGuid()) {
                    continue;
                }

                User corpUser = UserService.getInstance().getUserCache().findByGuid(member.getGuid());
                if (corpUser == null) {
                    continue;
                }

                data[index++] = corpUser.getPlanet().getPosition().galaxyId();

            }

            responseConsortiaStarPacket.setData(new IntegerArray(data));
            packet.reply(responseConsortiaStarPacket);

        }

        ResponseMapBlockFightPacket responseMapBlockFightPacket = new ResponseMapBlockFightPacket();

        responseMapBlockFightPacket.setBlockId(packet.getBlockId());
        responseMapBlockFightPacket.setGalaxyMapId((short) 0);
        responseMapBlockFightPacket.setDataLen((short) 250);

        int[] data = new int[250];

        responseMapBlockFightPacket.setData(new IntegerArray(data));
        packet.reply(responseMapBlockFightPacket);

    }

    @PacketProcessor
    public void onMapArea(RequestMapAreaPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null) {
            return;
        }

        if (packet.getRegionId().getArray().length > 16) {
            return;
        }

        for (int regionId : packet.getRegionId().getArray()) {
            packet.reply(GalaxyService.getInstance().getMapAreaPacketByRegionId(user, regionId));
        }

    }

}
