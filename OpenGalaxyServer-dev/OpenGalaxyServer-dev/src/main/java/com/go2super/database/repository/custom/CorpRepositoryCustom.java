package com.go2super.database.repository.custom;

import com.go2super.database.entity.Corp;

import java.util.*;

public interface CorpRepositoryCustom {

    Corp findByGuid(int guid);

    List<Corp> findByCorpUpgrade();

    Corp findByName(String name);

    List<Corp> findRecruitsByGuid(int guid);

    List<Corp> findByStartWithName(String name);

}