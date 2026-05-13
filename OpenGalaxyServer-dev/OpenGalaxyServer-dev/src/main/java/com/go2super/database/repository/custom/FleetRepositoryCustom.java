package com.go2super.database.repository.custom;

import com.go2super.database.entity.Fleet;

import java.util.*;

public interface FleetRepositoryCustom {

    List<Fleet> getInWarFleets();

    List<Fleet> getInTransmissionFleets();

    List<Fleet> getInTransmissionFleets(int guid);

    List<Fleet> getRadarFleets(int guid, int toGalaxyId);

}
