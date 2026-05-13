package com.go2super.listener;

import com.go2super.database.entity.User;
import com.go2super.logger.BotLogger;
import com.go2super.obj.game.FriendInfo;
import com.go2super.obj.game.UserBlocked;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.friend.*;
import com.go2super.service.LoginService;
import com.go2super.service.UserService;
import com.go2super.service.exception.BadGuidException;
import com.go2super.socket.util.DateUtil;
import com.google.common.collect.Lists;
import org.springframework.util.CollectionUtils;

import java.util.*;

public class FriendListener implements PacketListener {

    private static final int SECONDS_BETWEEN_REQUESTS = 60 * 10;
    private static final List<UserBlocked> blocks = new ArrayList<>();

    @PacketProcessor
    public void onFriendList(RequestFriendListPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null) {
            return;
        }
        if (packet.getPageId() < 0) {
            return;
        }

        int maxPage = packet.getKind() == 0 ? 5 : 6;

        List<User> friends = user.findFriends();
        List<List<User>> pages = Lists.partition(friends, maxPage);

        if (pages.size() <= packet.getPageId()) {

            ResponseFriendListPacket response = new ResponseFriendListPacket();

            response.setDataLen((byte) -1);
            response.setKind((byte) -1);
            response.setFriendCount((short) -1);
            response.setFriendInfos(new ArrayList<>());

            packet.reply(response);
            return;

        }

        List<User> page = pages.get(packet.getPageId());
        FriendInfo reference = new FriendInfo();

        List<FriendInfo> infos = getInfos(page);

        while (infos.size() < maxPage) {
            infos.add(reference.trash());
        }

        ResponseFriendListPacket response = new ResponseFriendListPacket();

        response.setDataLen((byte) page.size());
        response.setKind((byte) packet.getKind());
        response.setFriendCount((short) friends.size());
        response.setFriendInfos(infos);

        packet.reply(response);

    }

    @PacketProcessor
    public void onAddFriend(RequestAddFriendPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null) {
            return;
        }

        if (packet.getObjGuid() == user.getGuid()) {
            return;
        }

        ResponseAddFriendPacket response = new ResponseAddFriendPacket();

        if (user.isFriend(packet.getObjGuid())) {
            return;
        }

        updateBlocks();

        if (isBlocked(packet.getGuid(), packet.getObjGuid())) {
            return;
        }

        Optional<LoggedGameUser> optional = LoginService.getInstance().getGame(packet.getObjGuid());

        check:
        if (optional.isPresent()) {

            LoggedGameUser gameUser = optional.get();
            User toUser = gameUser.getUpdatedUser();

            if (toUser == null) {
                break check;
            }


            List<Integer> blockUsers = toUser.getBlockUsers();
            if (!CollectionUtils.isEmpty(blockUsers)) {
                for (Integer blockUser : blockUsers) {
                    if (blockUser == user.getGuid()) {
                        break check;
                    }
                }
            }


            toUser.update();

            ResponseAddFriendAuthPacket popup = new ResponseAddFriendAuthPacket();

            popup.setSrcGuId(user.getGuid());
            popup.setSrcUserId(user.getUserId());
            popup.getSrcName().value(user.getUsername());

            for (int i = 0; i < 2; i++) {
                gameUser.getSmartServer().send(popup);
            }

            blocks.add(UserBlocked.builder()
                .fromGuid(packet.getGuid())
                .toGuid(packet.getObjGuid())
                .until(DateUtil.now(SECONDS_BETWEEN_REQUESTS))
                .build());

            response.setErrorCode(0);
            packet.reply(response);
            return;

        }

        response.setErrorCode(1);
        packet.reply(response);

    }

    @PacketProcessor
    public void onRemoveFriend(RequestDeleteFriendPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null) {
            return;
        }

        List<User> friends = user.findFriends();
        if (friends.isEmpty()) {
            return;
        }

        for (User friend : friends) {
            if (friend.getGuid() == packet.getFriendGuid()) {

                user.removeFriend(friend.getGuid());
                friend.removeFriend(user.getGuid());

                user.save();
                friend.save();
                return;

            }
        }

    }

    @PacketProcessor
    public void onFriendPassAuth(RequestFriendPassAuthPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null) {
            return;
        }

        if (user.isFriend(packet.getFriendGuid())) {
            return;
        }

        User friend = UserService.getInstance().getUserCache().findByGuid(packet.getFriendGuid());

        if (friend == null) {
            return;
        }

        updateBlocks();
        UserBlocked request = null;

        for (UserBlocked block : blocks) {
            if (block.getToGuid() == user.getGuid() && block.getFromGuid() == friend.getGuid()) {
                request = block;
            }
        }

        if (request == null) {
            return;
        }

        friend.addFriend(user.getGuid());
        user.addFriend(friend.getGuid());

        user.getMetrics().add("action:add.friend", 1);
        friend.getMetrics().add("action:add.friend", 1);

        user.update();
        friend.update();

        user.save();
        friend.save();

        ResponseFriendPassAuthPacket response = new ResponseFriendPassAuthPacket();

        response.setUserId(friend.getUserId());
        response.setFriendGuid(friend.getGuid());
        response.getFriendName().value(friend.getUsername());

        packet.reply(response);

    }

    private boolean isBlocked(int requester, int receiver) {

        for (UserBlocked blocked : blocks) {
            if (blocked.getFromGuid() == requester && blocked.getToGuid() == receiver) {
                return true;
            }
        }

        return false;

    }

    private List<FriendInfo> getInfos(List<User> list) {

        List<FriendInfo> infos = new ArrayList<>();

        for (User user : list) {

            FriendInfo info = new FriendInfo();

            info.setGuid(user.getGuid());
            info.setLevel(user.getStats().getLevel());
            info.getName().value(user.getUsername());
            info.setUserId(user.getUserId());
            info.setHeadId(1);
            info.setReserve(1);
            info.setStatus(user.isOnline() ? 1 : 0);

            infos.add(info);

        }

        return infos;

    }

    private void updateBlocks() {

        List<UserBlocked> toRemove = new ArrayList<>();

        for (UserBlocked blocked : blocks) {
            if (DateUtil.remains(blocked.getUntil()) <= 0) {
                toRemove.add(blocked);
            }
        }

        BotLogger.log("toremove = " + toRemove);
        blocks.removeAll(toRemove);

    }

}
