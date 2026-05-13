package com.go2super.database.entity;

import com.go2super.database.entity.sub.UserIPInfo;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.*;

@Document(collection = "game_users_ips")
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class UserIP {

    @Id
    private ObjectId id;

    private String email;
    private String accountId;
    private String accountName;

    private List<UserIPInfo> ips = new ArrayList<>();

}
