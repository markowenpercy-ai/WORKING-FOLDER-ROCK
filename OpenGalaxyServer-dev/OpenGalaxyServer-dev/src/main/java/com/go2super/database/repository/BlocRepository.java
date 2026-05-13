package com.go2super.database.repository;

import com.go2super.database.entity.Bloc;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;
import java.util.*;

public interface BlocRepository extends MongoRepository<Bloc, ObjectId>, Serializable {

    Bloc findTopByOrderByIdDesc();

    List<Bloc> findAll();

    Optional<Bloc> findByName(String name);

    Optional<Bloc> findByOrganizer(int organizer);

    Optional<Bloc> findByCode(String code);

}