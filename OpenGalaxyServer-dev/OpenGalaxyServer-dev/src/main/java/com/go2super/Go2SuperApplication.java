package com.go2super;

import com.go2super.resources.json.*;
import com.go2super.resources.json.storeevent.QuickstartEventJson;
import com.go2super.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
@SpringBootApplication
public class Go2SuperApplication implements ApplicationListener<ContextRefreshedEvent> {

    public static String VIRTUAL_VERSION = "0.2.0";
    public static Date UPTIME_DATE = new Date();

    public List<Object> RUNTIME_REFLECTION = Arrays.asList(

        new GalaxyMapJson(),
        new BuildsJson(),
        new CommandersJson(),
        new TasksJson(),

        new FarmLandsJson(),
        new FieldResourcesJson(),

        new ShipPartJson(),
        new ShipBodyJson(),

        new CorpsPirateJson(),
        new CorpsShopJson(),
        new CorpsLevelJson(),
        new InstancesJson(),
        new ShipModelsJson(),

        new LotteryJson(),

        new QuickstartEventJson()
    );

    public static void main(String[] args) {

        SpringApplication.run(Go2SuperApplication.class, args);
    }

    @Value("${spring.application.name}")
    String name;
    @Value("${spring.application.version}")
    String version;
    @Value("${spring.application.restPort}")
    String restPort;

    @Autowired
    private GalaxyService galaxyService;
    @Autowired
    private BattleService battleService;
    @Autowired
    private PacketService packetService;
    @Autowired
    private SocketService socketService;
    @Autowired
    private StoreEventService storeEventService;
    @Autowired
    private JobService jobService;

    private static boolean initialized = false;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (initialized) {
            return;
        }
        initialized = true;
        disableAccessWarnings();
        try {
            storeEventService.fetchFirst();
        } catch (IOException e) {
            // do nothing. we can fetch later if something goes wrong
            log.error(e.getMessage());
        }
        galaxyService.calculatePositions();
        battleService.setup();
        socketService.startSockets();

        log.info("SuperGO2 server has been initialized!");
    }

    public static void disableAccessWarnings() {

        try {

            Class unsafeClass = Class.forName("sun.misc.Unsafe");
            Field field = unsafeClass.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Object unsafe = field.get(null);

            Method putObjectVolatile = unsafeClass.getDeclaredMethod("putObjectVolatile", Object.class, long.class, Object.class);
            Method staticFieldOffset = unsafeClass.getDeclaredMethod("staticFieldOffset", Field.class);

            Class loggerClass = Class.forName("jdk.internal.module.IllegalAccessLogger");
            Field loggerField = loggerClass.getDeclaredField("logger");
            Long offset = (Long) staticFieldOffset.invoke(unsafe, loggerField);

            putObjectVolatile.invoke(unsafe, loggerClass, offset, null);

        } catch (Exception ignored) {
        }

    }

}
