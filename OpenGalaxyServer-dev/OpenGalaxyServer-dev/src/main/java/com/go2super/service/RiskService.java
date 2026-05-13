package com.go2super.service;

import com.go2super.database.repository.RiskIncidentRepository;
import com.go2super.database.repository.SanctionRepository;
import com.go2super.database.repository.UserIPRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Getter
@Service
public class RiskService {

    private static RiskService instance;

    @Autowired
    private UserIPRepository userIPRepository;

    @Autowired
    private RiskIncidentRepository riskIncidentRepository;

    @Autowired
    private SanctionRepository sanctionRepository;

    public RiskService() {

        instance = this;
    }

    public static RiskService getInstance() {

        return instance;
    }

}
