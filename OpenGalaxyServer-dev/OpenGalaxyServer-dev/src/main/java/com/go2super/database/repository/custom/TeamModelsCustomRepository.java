package com.go2super.database.repository.custom;

import com.go2super.database.entity.TeamModelSlot;

public interface TeamModelsCustomRepository {

    TeamModelSlot findByGuidAndIndexId(int guid, int indexId);


}
