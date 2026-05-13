package com.go2super.service;

import com.go2super.Go2SuperApplication;
import com.go2super.database.entity.Planet;
import com.go2super.dto.metric.LastPlanetDTO;
import com.go2super.dto.metric.OnlineDTO;
import com.go2super.dto.metric.PatchDTO;
import com.go2super.dto.response.BasicResponse;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.utility.GalaxyTile;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class MetricService {

    @Value("${application.game.patch}")
    @Getter
    private String currentPatch;

    public MetricService() {

    }

    public BasicResponse onlinePlayers(HttpServletRequest request) {

        List<LoggedGameUser> loggedGameUsers = LoginService.getInstance().getGameUsers();
        OnlineDTO onlineDTO = OnlineDTO.builder()
            .online(loggedGameUsers.size())
            .build();

        return BasicResponse
            .builder()
            .code(HttpStatus.OK.value())
            .message("OK")
            .data(onlineDTO)
            .build();

    }

    public BasicResponse lastPlanet(HttpServletRequest request) {

        Planet last = PacketService.getInstance().getPlanetCache().findTopByOrderByIdDesc();

        if (last == null) {
            return BasicResponse
                .builder()
                .code(HttpStatus.OK.value())
                .message("EMPTY")
                .build();
        }

        GalaxyTile galaxyTile = last.getPosition();
        LastPlanetDTO dto = LastPlanetDTO.builder()
            .id(last.getUserId())
            .x(galaxyTile.getX())
            .y(galaxyTile.getY())
            .build();

        return BasicResponse
            .builder()
            .code(HttpStatus.OK.value())
            .message("OK")
            .data(dto)
            .build();

    }

    public BasicResponse patch(HttpServletRequest request) {

        PatchDTO patchDTO = PatchDTO.builder()
            .patch(Go2SuperApplication.VIRTUAL_VERSION)
            .build();

        return BasicResponse
            .builder()
            .code(HttpStatus.OK.value())
            .message("OK")
            .data(patchDTO)
            .build();

    }

}
