package com.go2super.database.entity.sub;

import java.util.*;

public class CorpHistory {

    private List<CorpIncident> incidents = new ArrayList<>();

    public List<CorpIncident> getIncidents() {

        return incidents;
    }

    public void addIncident(CorpIncident incident) {

        if (incidents == null) {
            incidents = new ArrayList<>();
        }

        incidents.add(incident);

    }

}
