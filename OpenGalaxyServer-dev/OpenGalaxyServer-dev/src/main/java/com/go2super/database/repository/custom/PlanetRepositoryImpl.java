package com.go2super.database.repository.custom;

import com.go2super.database.entity.Planet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.HumaroidPlanet;
import com.go2super.database.entity.sub.ResourcePlanet;
import com.go2super.database.entity.sub.UserPlanet;
import com.go2super.obj.utility.GalaxyRegion;
import com.go2super.obj.utility.GalaxyTile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;
import java.util.stream.*;

public class PlanetRepositoryImpl implements PlanetRepositoryCustom {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public UserPlanet getUserPlanet(User user) {

        return getUserPlanet(user.getUserId());
    }

    @Override
    public UserPlanet getUserPlanet(long userId) {

        Criteria criteria = Criteria.where("userId").is(userId);

        Query query = new Query(criteria).restrict(UserPlanet.class);
        return mongoTemplate.findOne(query, UserPlanet.class);

    }

    @Override
    public List<GalaxyTile> getTakenPositions() {

        Criteria criteria = new Criteria();

        Query query = new Query(criteria).restrict(UserPlanet.class);
        query.fields().include("position");

        return mongoTemplate.find(query, UserPlanet.class).stream().map(UserPlanet::getPosition).collect(Collectors.toList());

    }

    @Override
    public List<Planet> getPlanets(GalaxyRegion galaxyRegion) {

        GalaxyTile min = galaxyRegion.getMinBoundingTile();
        GalaxyTile max = galaxyRegion.getMaxBoundingTile();

        Criteria criteria = new Criteria();
        criteria.andOperator(
            Criteria.where("position.x")
                .gte(min.getX())
                .lte(max.getX()),
            Criteria.where("position.y")
                .gte(min.getY())
                .lte(max.getY()));

        Query query = new Query(criteria);
        return mongoTemplate.find(query, Planet.class);

    }

    @Override
    public List<ResourcePlanet> findResourcePlanets() {

        Criteria criteria = new Criteria();

        Query query = new Query(criteria).restrict(ResourcePlanet.class);
        return mongoTemplate.find(query, ResourcePlanet.class);

    }

    @Override
    public List<UserPlanet> getUserPlanets(GalaxyRegion galaxyRegion) {

        GalaxyTile min = galaxyRegion.getMinBoundingTile();
        GalaxyTile max = galaxyRegion.getMaxBoundingTile();

        Criteria criteria = new Criteria();
        criteria.andOperator(
            Criteria.where("position.x")
                .gte(min.getX())
                .lte(max.getX()),
            Criteria.where("position.y")
                .gte(min.getY())
                .lte(max.getY()));

        Query query = new Query(criteria);
        query.restrict(UserPlanet.class);
        return mongoTemplate.find(query, UserPlanet.class);

    }

    @Override
    public List<HumaroidPlanet> getHumaroidPlanet() {
        Criteria criteria = Criteria.where("type").is("HUMAROID_PLANET");
        Query query = new Query(criteria);
        query.restrict(HumaroidPlanet.class);
        return mongoTemplate.find(query, HumaroidPlanet.class);
    }

    @Override
    public List<ResourcePlanet> findResourcePlanets(int corpId) {

        Criteria criteria = Criteria.where("currentCorp").is(corpId);

        Query query = new Query(criteria);
        query.restrict(ResourcePlanet.class);
        return mongoTemplate.find(query, ResourcePlanet.class);

    }

}
