package com.go2super.database.entity;

import com.go2super.database.entity.type.DashboardRank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Column;
import javax.persistence.Id;

@Document(collection = "game_dashboard_accounts")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAccount {

    @Id
    private ObjectId id;

    @Column(unique = true)
    private String email;
    private String password;

    private DashboardRank rank;

}
