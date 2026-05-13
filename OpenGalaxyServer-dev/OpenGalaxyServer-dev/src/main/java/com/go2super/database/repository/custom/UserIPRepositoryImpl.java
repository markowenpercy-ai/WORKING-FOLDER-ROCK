package com.go2super.database.repository.custom;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.database.entity.UserIP;
import com.go2super.database.entity.sub.UserIPInfo;
import com.go2super.server.GameServerReceiver;
import org.apache.mina.util.ConcurrentHashSet;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class UserIPRepositoryImpl implements UserIPRepositoryCustom {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public void updateUserIP(Account account, User user, GameServerReceiver serverReceiver) {

        Criteria criteria = Criteria.where("accountId").is(account.getId().toString());
        Query query = new Query(criteria).restrict(UserIP.class);

        Optional<UserIP> optionalUserIP = Optional.ofNullable(mongoTemplate.findOne(query, UserIP.class));
        if (optionalUserIP.isEmpty()) {

            UserIP userIP = UserIP.builder()
                .id(ObjectId.get())
                .email(account.getEmail())
                .accountId(account.getId().toString())
                .accountName(account.getUsername())
                .ips(new ArrayList<>())
                .build();

            UserIPInfo userIPInfo = UserIPInfo.builder()
                .accountId(account.getId().toString())
                .ip(serverReceiver.getIp())
                .guid(user.getGuid())
                .userId(user.getUserId())
                .username(user.getUsername())
                .count(1)
                .lastTime(new Date())
                .build();

            userIP.getIps().add(userIPInfo);
            mongoTemplate.save(userIP);
            return;

        }

        UserIP userIP = optionalUserIP.get();

        Optional<UserIPInfo> optionalUserIPInfo = userIP.getIps().stream().filter(other -> other.getIp().equals(serverReceiver.getIp())).findFirst();
        if (optionalUserIPInfo.isPresent()) {

            UserIPInfo userIPInfo = optionalUserIPInfo.get();
            userIPInfo.setCount(userIPInfo.getCount() + 1);
            userIPInfo.setLastTime(new Date());

            mongoTemplate.save(userIP);
            return;

        }

        UserIPInfo userIPInfo = UserIPInfo.builder()
            .accountId(account.getId().toString())
            .ip(serverReceiver.getIp())
            .count(1)
            .lastTime(new Date())
            .guid(user.getGuid())
            .userId(user.getUserId())
            .username(user.getUsername())
            .build();

        userIP.getIps().add(userIPInfo);
        mongoTemplate.save(userIP);

    }

    @Override
    public boolean hasIPConflict(Account account, String ip) {

        Criteria criteria = new Criteria().andOperator(Criteria.where("ips.ip").in(ip), Criteria.where("accountId").ne(account.getId().toString()));
        Query query = new Query(criteria);

        return mongoTemplate.exists(query, UserIP.class);

    }

    @Override
    public List<UserIP> getIPConflict(String ip) {

        Criteria criteria = Criteria.where("ips.ip").in(ip);
        Query query = new Query(criteria);

        List<UserIP> userIPs = mongoTemplate.find(query, UserIP.class);
        userIPs = userIPs.stream().filter(distinctBy(UserIP::getAccountId)).collect(Collectors.toList());

        return userIPs;

    }

    public static <T> Predicate<T> distinctBy(Function<? super T, ?> f) {

        Set<Object> objects = new ConcurrentHashSet<>();
        return t -> objects.add(f.apply(t));
    }

}
