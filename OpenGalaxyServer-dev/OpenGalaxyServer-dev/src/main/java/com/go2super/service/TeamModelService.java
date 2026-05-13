package com.go2super.service;

import com.go2super.database.repository.TeamModelsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Getter
@Service
public class TeamModelService {

    private static TeamModelService instance;

    @Autowired
    private TeamModelsRepository teamModelsRepository;

    public TeamModelService() {

        instance = this;
    }

    public static TeamModelService getInstance() {

        return instance;
    }

}
