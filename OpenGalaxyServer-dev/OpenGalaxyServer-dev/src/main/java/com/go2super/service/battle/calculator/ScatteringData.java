package com.go2super.service.battle.calculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScatteringData {

    private List<ScatteringHit> hits = new ArrayList<>();

    public void addHit(ScatteringHit hit) {

        Optional<ScatteringHit> optionalScatteringHit = hits.stream().filter(oldHit -> oldHit.getPosition().equals(hit.getPosition())).findFirst();

        if (optionalScatteringHit.isPresent()) {

            ScatteringHit scatteringHit = optionalScatteringHit.get();
            scatteringHit.setGeneralDamage(scatteringHit.getGeneralDamage() + hit.getGeneralDamage());
            return;

        }

        ScatteringHit scatteringHit = ScatteringHit.builder()
            .position(hit.getPosition())
            .generalDamage(hit.getGeneralDamage())
            .build();

        hits.add(scatteringHit);

    }

}
