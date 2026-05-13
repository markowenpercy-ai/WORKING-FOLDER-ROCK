package com.go2super.database.entity;

import com.go2super.obj.game.TeamModel;
import com.go2super.service.TeamModelService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

@Document(collection = "game_team_models")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamModelSlot {

    @Id
    private ObjectId id;

    private int guid;
    private int indexId;

    private TeamModel teamModel;

    public void save() {

        TeamModelService.getInstance().getTeamModelsRepository().save(this);
    }


}
