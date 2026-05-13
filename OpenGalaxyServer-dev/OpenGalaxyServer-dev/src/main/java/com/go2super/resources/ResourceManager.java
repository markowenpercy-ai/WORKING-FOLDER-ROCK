package com.go2super.resources;

import com.go2super.database.entity.type.UserRank;
import com.go2super.obj.type.InstanceType;
import com.go2super.obj.utility.GameInstance;
import com.go2super.resources.data.LayoutData;
import com.go2super.resources.data.PropData;
import com.go2super.resources.json.*;
import com.go2super.resources.json.storeevent.CommanderEventJson;
import com.go2super.resources.json.storeevent.QuickstartEventJson;
import com.go2super.resources.serialization.LevelsJsonDeserializer;
import com.go2super.resources.serialization.PropsJsonDeserializer;
import com.go2super.service.PacketService;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class ResourceManager {

    private static ResourceManager instance;

    private final String otpHtml;

    private final PropsJson propsJson;
    private final LevelsJson levelsJson;
    private final BuildsJson buildsJson;
    private final TasksJson tasksJson;
    private final DailyTaskJson dailyTaskJson;
    private final ScienceJson scienceJson;
    private final ChipsJson chipsJson;
    private final FlagshipsJson flagshipsJson;

    private final TeslaJson teslaJson;

    private final FarmLandsJson farmLandsJson;
    private final FieldResourcesJson fieldResourcesJson;
    private final FortificationJson fortificationJson;
    private final RBPFortificationJson rbpFortificationJson;

    private final ShipPartJson shipPartJson;
    private final ShipBodyJson shipBodyJson;

    private final GalaxyMapJson galaxyMapJson;
    private final CommandersJson commandersJson;
    private final CorpsShopJson corpsShopJson;
    private final CorpsLevelJson corpsLevelJson;
    private final InstancesJson instancesJson;
    private final RestrictedJson restrictedJson;
    private final TrialsJson trialsJson;
    private final ConstellationsJson constellationsJson;
    private final RaidRewardJson raidRewardJson;
    private final ShipModelsJson shipModelsJson;
    private final HumaroidsJson humaroidsJson;
    private final LeaguesJson leagueJson;

    private final RBPSSJson rbpssJson;
    private final RBPJson rbpJson;

    private final LotteryJson lotteryJson;
    private final RewardsJson viprewardsJson;
    private final RewardsJson rewardsJson;
    private final RewardsJson altsRewardsJson;
    private final RewardsJson paltsRewardsJson;
    private final RewardsJson mainRewardsJson;
    private final RewardsJson secondaryRewardsJson;
    private final PiratesJson piratesJson;

    private final QuickstartEventJson quickstartEventJson;

    private final CommanderEventJson commanderEventJson;
    private final List<String> compactedDictionary;

    private final Set<GameInstance> instances;
    private final Set<LayoutData> layouts;

    public ResourceManager() {

        this.otpHtml = getString("email/otp.html");

        this.propsJson = getJson("props.tmp.json", PropsJson.class, new PropsJsonDeserializer());
        this.levelsJson = getJson("levels.json", LevelsJson.class, new LevelsJsonDeserializer());
        this.galaxyMapJson = getJson("galaxyMap.json", GalaxyMapJson.class);
        this.buildsJson = getJson("builds.tmp.json", BuildsJson.class);
        this.commandersJson = getJson("commanders.tmp.json", CommandersJson.class);
        this.tasksJson = getJson("tasks.json", TasksJson.class);
        this.dailyTaskJson = getJson("dailyTask.json", DailyTaskJson.class);
        this.scienceJson = getJson("science.json", ScienceJson.class);
        this.chipsJson = getJson("chips.json", ChipsJson.class);
        this.flagshipsJson = getJson("flagships.json", FlagshipsJson.class);
        this.teslaJson = getJson("teslas.json", TeslaJson.class);
        this.leagueJson = getJson("leagues.json", LeaguesJson.class);

        this.rewardsJson = getJson("rewards.json", RewardsJson.class);
        this.altsRewardsJson = getJson("altrewards.json", RewardsJson.class);
        this.paltsRewardsJson = getJson("paltrewards.json", RewardsJson.class);
        this.viprewardsJson = getJson("viprewards.json", RewardsJson.class);
        this.mainRewardsJson = getJson("mainrewards.json", RewardsJson.class);
        this.secondaryRewardsJson = getJson("secondaryrewards.json", RewardsJson.class);
        this.piratesJson = getJson("pirates.json", PiratesJson.class);

        this.farmLandsJson = getJson("farmLands.json", FarmLandsJson.class);
        this.fieldResourcesJson = getJson("fieldResources.json", FieldResourcesJson.class);
        this.fortificationJson = getJson("fortification.json", FortificationJson.class);
        this.rbpFortificationJson = getJson("rbpFortification.json", RBPFortificationJson.class);

        this.rbpssJson = getJson("rbpSS.json", RBPSSJson.class);
        this.rbpJson = getJson("rbp.json", RBPJson.class);

        this.shipPartJson = getJson("shipPart.json", ShipPartJson.class);
        this.shipBodyJson = getJson("shipBody.json", ShipBodyJson.class);

        this.corpsShopJson = getJson("corpsShop.json", CorpsShopJson.class);
        this.corpsLevelJson = getJson("corpsLevel.json", CorpsLevelJson.class);
        this.instancesJson = getJson("instances.json", InstancesJson.class);
        this.restrictedJson = getJson("restricted.json", RestrictedJson.class);
        this.raidRewardJson = getJson("raidReward.json", RaidRewardJson.class);

        this.trialsJson = getJson("trials.json", TrialsJson.class);
        this.humaroidsJson = getJson("humaroids.json", HumaroidsJson.class);
        this.constellationsJson = getJson("constellations.json", ConstellationsJson.class);
        this.shipModelsJson = getJson("shipModels.json", ShipModelsJson.class);
        this.lotteryJson = getJson("lottery.json", LotteryJson.class);

        this.quickstartEventJson = getJson("storeevent/quickstart.json", QuickstartEventJson.class);
        this.commanderEventJson = getJson("storeevent/pack.json", CommanderEventJson.class);

        this.compactedDictionary = new ArrayList<>();

        this.instances = new HashSet<>();
        this.layouts = new HashSet<>();

        Reader dictionaryReader = getReader("others/dictionary.txt");
        Scanner scanner = new Scanner(dictionaryReader);

        while (scanner.hasNextLine()) {

            String word = scanner.nextLine();
            if (word.length() < 5 || !StringUtils.isAlphanumeric(word)) {
                continue;
            }

            compactedDictionary.add(word);

        }

        layouts.addAll(instancesJson.getLayout().stream().toList());
        layouts.addAll(restrictedJson.getLayout().stream().toList());
        layouts.addAll(trialsJson.getLayout().stream().toList());
        layouts.addAll(humaroidsJson.getLayout().stream().toList());
        layouts.addAll(constellationsJson.getLayout().stream().toList());
        layouts.addAll(piratesJson.getLayout().stream().toList());

        instances.addAll(instancesJson.getInstances().stream().map(data -> new GameInstance(data, InstanceType.INSTANCE)).toList());
        instances.addAll(restrictedJson.getInstances().stream().map(data -> new GameInstance(data, InstanceType.RESTRICTED)).toList());
        instances.addAll(trialsJson.getInstances().stream().map(data -> new GameInstance(data, InstanceType.TRIALS)).toList());
        instances.addAll(constellationsJson.getInstances().stream().map(data -> new GameInstance(data, InstanceType.CONSTELLATION)).toList());

    }


    private <T> T getJson(String jsonFile, Class<T> clazz, JsonDeserializer<T> deserializer) {

        return new GsonBuilder().registerTypeAdapter(clazz, deserializer).create().fromJson(getReader(jsonFile), clazz);
    }


    private <T> T getJson(String jsonFile, Class<T> clazz) {

        return new GsonBuilder().create().fromJson(getReader(jsonFile), clazz);
    }

    @SneakyThrows
    private Reader getReader(String file) {

        return new InputStreamReader(getInputStream(file), StandardCharsets.UTF_8);
    }

    @SneakyThrows
    private InputStream getInputStream(String file) {

        return new ClassPathResource("data/" + file).getInputStream();
    }

    @SneakyThrows
    private String getString(String file) {

        return CharStreams.toString(new InputStreamReader(new ClassPathResource(file).getInputStream(), Charsets.UTF_8));
    }

    public static Optional<LayoutData> fetchLayout(String layout) {

        return getInstance().layouts.stream().filter(layoutData -> layoutData.getName().equals(layout)).findFirst();
    }

    public static String getOtpHtml() {

        return getInstance().otpHtml;
    }

    public static PropsJson getProps() {

        return getInstance().propsJson;
    }

    public static BuildsJson getBuilds() {

        return getInstance().buildsJson;
    }

    public static LotteryJson getLottery() {

        return getInstance().lotteryJson;
    }

    public static RewardsJson getRewards(UserRank rank) {
        switch (rank) {
            case ALT -> {
                return getInstance().altsRewardsJson;
            }
            case PALT -> {
                return getInstance().paltsRewardsJson;
            }
            case VIP -> {
                return getInstance().viprewardsJson;
            }
            default -> {
                return getInstance().rewardsJson;
            }
        }
    }

    public static RewardsJson getRewards(boolean isMain) {
        return isMain ? getInstance().mainRewardsJson : getInstance().secondaryRewardsJson;
    }

    public static TasksJson getTasks() {

        return getInstance().tasksJson;
    }

    public static ScienceJson getScience() {

        return getInstance().scienceJson;
    }

    public static FarmLandsJson getFarmLands() {

        return getInstance().farmLandsJson;
    }

    public static FieldResourcesJson getFieldResources() {

        return getInstance().fieldResourcesJson;
    }

    public static FortificationJson getFortification() {

        return getInstance().fortificationJson;
    }

    public static RBPFortificationJson getRBPFortification() {

        return getInstance().rbpFortificationJson;
    }

    public static CorpsShopJson getCorpsShopJson() {

        return getInstance().corpsShopJson;
    }

    public static CorpsLevelJson getCorpsLevelJson() {

        return getInstance().corpsLevelJson;
    }

    public static LeaguesJson getLeaguesJson() {

        return getInstance().leagueJson;
    }

    public static LevelsJson getLevels() {

        return getInstance().levelsJson;
    }

    public static ChipsJson getChips() {

        return getInstance().chipsJson;
    }

    public static FlagshipsJson getFlagships() {

        return getInstance().flagshipsJson;
    }

    public static TeslaJson getTesla() {

        return getInstance().teslaJson;
    }

    public static ShipPartJson getShipParts() {

        return getInstance().shipPartJson;
    }

    public static ShipBodyJson getShipBodies() {

        return getInstance().shipBodyJson;
    }

    public static ShipModelsJson getShipModels() {

        return getInstance().shipModelsJson;
    }

    public static CommandersJson getCommanders() {

        return getInstance().commandersJson;
    }

    public static InstancesJson getInstances() {

        return getInstance().instancesJson;
    }

    public static RestrictedJson getRestricted() {

        return getInstance().restrictedJson;
    }

    public static HumaroidsJson getHumaroids() {

        return getInstance().humaroidsJson;
    }

    public static TrialsJson getTrials() {

        return getInstance().trialsJson;
    }

    public static RBPSSJson getRBPss() {

        return getInstance().rbpssJson;
    }

    public static RBPJson getRBP() {

        return getInstance().rbpJson;
    }

    public static PiratesJson getPirates() {

        return getInstance().piratesJson;
    }

    public static QuickstartEventJson getQuickstartEvent() {
        return getInstance().quickstartEventJson;
    }

    public static CommanderEventJson getCommanderEvent() {
        return getInstance().commanderEventJson;
    }

    public static GalaxyMapJson getGalaxyMaps() {

        return getInstance().galaxyMapJson;
    }

    public static GameInstance getGameInstance(int id) {

        return getInstance().instances.stream().filter(instance -> instance.getData().getId() == id).findFirst().orElse(null);
    }

    public static List<String> getCompactedWords() {

        return getInstance().compactedDictionary;
    }

    public static PropData getProp(String key){
        return getInstance().propsJson.getData(key);
    }
    public static RaidRewardJson getRewardJson(){
        return getInstance().raidRewardJson;
    }
    public static ResourceManager getInstance() {
        if(instance == null){
            instance = new ResourceManager();
        }
        return instance;
    }

}