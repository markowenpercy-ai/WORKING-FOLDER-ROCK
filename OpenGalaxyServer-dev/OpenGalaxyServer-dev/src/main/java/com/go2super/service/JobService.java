package com.go2super.service;

import com.go2super.database.cache.*;
import com.go2super.database.entity.AutoIncrement;
import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.TradeShip;
import com.go2super.database.entity.type.TradeType;
import com.go2super.logger.BotLogger;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.packet.reward.ResponseOnlineAwardPacket;
import com.go2super.service.jobs.GalaxyUserJob;
import com.go2super.service.jobs.OfflineJob;
import com.go2super.service.jobs.corp.CorpUpgradeJob;
import com.go2super.service.jobs.other.HumaroidJob;
import com.go2super.service.jobs.other.RBPJob;
import com.go2super.service.jobs.other.TransitionJob;
import com.go2super.service.jobs.trade.TradeJob;
import com.go2super.service.jobs.user.*;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Data
@Getter
@Service
@EnableScheduling
public class JobService {

    private static JobService instance;

    private LinkedList<GalaxyUserJob> jobs;
    private LinkedList<OfflineJob> offlineJobs;

    private RankJob rankJob;

    @Autowired
    @Qualifier("jobTaskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private TaskScheduler taskScheduler;
    @Autowired
    private RankService rankService;

    private AutoIncrementCache autoIncrementCache;
    private ShipModelCache shipModelCache;
    private CommanderCache commanderCache;
    private PlanetCache planetCache;
    private FleetCache fleetCache;
    private TradeCache tradeCache;
    private CorpCache corpCache;
    private UserCache userCache;

    @Autowired
    public JobService(AutoIncrementCache autoIncrementCache, CommanderCache commanderCache, PlanetCache planetCache, FleetCache fleetCache, UserCache userCache, CorpCache corpCache, ShipModelCache shipModelCache, TradeCache tradeCache) {
        instance = this;

        this.autoIncrementCache = autoIncrementCache;
        this.shipModelCache = shipModelCache;
        this.commanderCache = commanderCache;
        this.planetCache = planetCache;
        this.fleetCache = fleetCache;
        this.corpCache = corpCache;
        this.userCache = userCache;
        this.tradeCache = tradeCache;

        jobs = new LinkedList<>();
        offlineJobs = new LinkedList<>();

        jobs.add(new BuilderJob());
        jobs.add(new UpgradeJob());

        offlineJobs.add(new HumaroidJob());
        offlineJobs.add(new RBPJob());
        offlineJobs.add(new TechJob());
        offlineJobs.add(new TradeJob());
        offlineJobs.add(new TransitionJob());
        offlineJobs.add(new CorpUpgradeJob());
        offlineJobs.add(new ShipConstructionJob());
        offlineJobs.add(new ShipRepairConstructionJob());
        rankJob = new RankJob();
        for (OfflineJob offlineJob : offlineJobs) {
            offlineJob.setup();
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void refresh() {
        for (LoggedGameUser gameUser : LoginService.getInstance().getGameUsers()) {

            User user = gameUser.getUpdatedUser();
            if (user == null) {
                continue;
            }

            user.update();
            user.save();

            ResponseOnlineAwardPacket onlineAwardPacket = ResourcesService.getInstance().getOnlineAwardPacket(user);
            if (onlineAwardPacket != null) {
                gameUser.getSmartServer().send(onlineAwardPacket);
            }

            gameUser.getSmartServer().send(ResourcesService.getInstance().getPlayerResourcePacket(user));

        }
    }
    @Scheduled(cron = "0 0 * * * *")
    public void cleanUp(){
        //This will take quite some time... skip first one since it is default Wikes
        for(var models : shipModelCache.findAll().stream().skip(1).filter(ShipModel::isDeleted).toList()){
            try{
                var exist = userCache.findAll().stream().anyMatch(x ->
                        x.getShips().ships.stream().anyMatch(y -> y.getShipModelId() == models.getShipModelId()) ||
                                (x.getShips().repairFactory != null && x.getShips().repairFactory.getShipModelId() == models.getShipModelId()) ||
                                x.getShips().factory.stream().anyMatch(y -> y.getShipModelId() == models.getShipModelId()) ||
                                x.getShips().repair.stream().anyMatch(y -> y.getShipModelId() == models.getShipModelId())
                );
                var fleetExist = fleetCache.findAll().stream().anyMatch(x -> x.getFleetBody().cells.stream().anyMatch(y -> y.getShipModelId() == models.getShipModelId()));
                var tradeExist = tradeCache.findAll().stream().filter(x -> x.getTradeType() == TradeType.SHIP).map(x -> (TradeShip)x).anyMatch(x -> x.getShipModelId() == models.getShipModelId());
                var battleExist = BattleService.getInstance().getBattles().stream().anyMatch(x -> x.getMatch().getFleets().stream().anyMatch(y -> y.getCells().stream().anyMatch(z -> z.getShipModelId() == models.getShipModelId())));
                if(!exist && !fleetExist && !tradeExist && !battleExist){
                    shipModelCache.delete(models);
                    BotLogger.info("Deleting ship model " + models.getShipModelId() + ": ");
                    BotLogger.info  (models.toString());
                }
            }
            catch (Exception ex){
                BotLogger.error("Failed to clean up design! Design ID:" + models.getShipModelId());
                BotLogger.error(ex.getMessage());
            }

        }
    }

    @Scheduled(fixedDelay = 200L)
    public void saveUsersTask() {
        userCache.saveChanged();
    }

    @Scheduled(fixedDelay = 200L)
    public void saveIncrementsTask() {
        autoIncrementCache.saveChanged();
    }

    @Scheduled(fixedDelay = 3000000L)
    public void performanceLog() {
        MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long usedMemory = heapMemoryUsage.getUsed();
        long maxMemory = heapMemoryUsage.getMax();
        ChatService.getInstance().broadcastMessage("RAM: " + humanReadableByteCountSI(usedMemory) + "/" + humanReadableByteCountSI(maxMemory));
    }

    private String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    @Scheduled(fixedDelay = 10000L)
    public void rankTask() {

        rankJob.run();
    }

    @Scheduled(fixedDelay = 500L)
    public void offlineTasks() {

        for (OfflineJob job : offlineJobs) {
            job.run();
        }

    }

    @Scheduled(fixedDelay = 500L)
    public void userTasks() {

        CopyOnWriteArrayList<LoggedGameUser> users = new CopyOnWriteArrayList<>(LoginService.getInstance().getGameUsers());
        if (users.isEmpty()) {
            return;
        }

        CopyOnWriteArrayList<Integer> toUpdateGuid = new CopyOnWriteArrayList<>();
        List<Integer> guids = users.stream().map(LoggedGameUser::getGuid).collect(Collectors.toList());

        for (User updated : UserService.getInstance().getUserCache().findByGuid(guids)) {
            for (GalaxyUserJob galaxyUserJob : jobs) {
                if (galaxyUserJob.needUpdate(updated)) {
                    toUpdateGuid.add(updated.getGuid());
                }
            }
        }

        if (toUpdateGuid.isEmpty()) {
            return;
        }
        List<User> usersToUpdate = UserService.getInstance().getUserCache().findByGuid(toUpdateGuid);
        List<CompletableFuture> futures = new ArrayList<>();

        for (LoggedGameUser gameUser : users) {
            futures.add(CompletableFuture.runAsync(() -> {
                boolean save = false;
                User updatedUser = usersToUpdate.stream().filter(user -> user.getGuid() == gameUser.getGuid()).findFirst().orElse(null);
                if (updatedUser == null) {
                    return;
                }
                for (GalaxyUserJob galaxyJob : jobs) {
                    if (galaxyJob.needUpdate(updatedUser) && galaxyJob.run(gameUser, updatedUser)) {
                        save = true;
                    }
                }
                if (save) {
                    updatedUser.update();
                    updatedUser.save();
                }
            }));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();


    }


    public static <E extends OfflineJob> E getOfflineJob(Class<E> clazz) {

        for (OfflineJob job : getInstance().getOfflineJobs()) {
            if (job.getClass().isAssignableFrom(clazz)) {
                return (E) job;
            }
        }
        return null;
    }

    @SneakyThrows
    public static void submit(Runnable runnable, String taskName) {

        runnable.run();
    }


    public static JobService getInstance() {

        return instance;
    }

}
