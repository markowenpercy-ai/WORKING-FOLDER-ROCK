package com.go2super.service;

import com.go2super.database.cache.CorpCache;
import com.go2super.database.cache.UserCache;
import com.go2super.database.entity.Corp;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.CorpHistory;
import com.go2super.database.entity.sub.CorpInventory;
import com.go2super.database.entity.sub.CorpMember;
import com.go2super.database.entity.sub.CorpMembers;
import com.go2super.database.repository.BlocRepository;
import com.go2super.obj.game.ConsortiaJobName;
import com.go2super.obj.utility.SmartString;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.CorpsLevelData;
import com.go2super.resources.data.meta.CorpsLevelEffectMeta;
import com.go2super.resources.data.meta.CorpsLevelMeta;
import com.go2super.resources.json.CorpsLevelJson;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Getter
@Service
public class CorpService {

    public static final String COUNCIL_CORP_NAME = "Administrator";

    private static CorpService instance;

    @Autowired
    private BlocRepository blocRepository;

    @Getter
    UserCache userCache;
    @Getter
    CorpCache corpCache;

    public CorpService(UserCache userCache, CorpCache corpCache) {

        instance = this;

        this.userCache = userCache;
        this.corpCache = corpCache;

    }

    public List<CorpMember> findRecruitsByPage(int page, int max, int guid) {

        Corp corp = corpCache.findByGuid(guid);

        List<CorpMember> corpRecruits = corp.getMembers().getRecruits();
        List<CorpMember> corpRecruitsList = new ArrayList<>();

        for (int i = page * max; i < max; i++) {

            if (i < 0) {
                continue;
            }
            if (corpRecruits.size() > i) {
                corpRecruitsList.add(corpRecruits.get(i));
            }

        }

        return corpRecruitsList;

    }

    public static String getCorpName(int guid) {

        Corp corp = instance.getCorpByUser(guid);

        SmartString corpName = SmartString.of("", 32);

        if (corp != null) {
            corpName.setValue(corp.getName());
        }

        return corpName.noSpaces();

    }

    public Corp getCorpByUser(int guid) {

        return corpCache.findByGuid(guid);
    }

    public Corp createCorp(SmartString name, String proclaim, char icon) {

        String nameString = name.noSpaces();

        Corp corp = Corp.builder()
            .corpId(AutoIncrementService.getInstance().getNextCorpId())
            .name(nameString)
            .members(new CorpMembers())
            .history(new CorpHistory())
            .consortiaJobName(new ConsortiaJobName("Recruit", "Colonel", "Commandant", "Captain", "Soldier"))
            .maxMembers(30)
            .rbpLimit(1)
            .resourceBonus(0.05)
            .mergeBonus(0)
            .wealth(0)
            .level(0)
            .mallLevel(-1)
            .mergingLevel(-1)
            .warehouseLevel(-1)
            .piratesLevel(0)
            .philosophy(proclaim)
            .bulletin("")
            .icon(icon)
            .build();

        return corp;
    }

    public CorpMember createCorpMember(int guid, int rank) {

        User user = UserService.getInstance().getUserCache().findByGuid(guid);

        if (user == null) {
            return null;
        }

        CorpMember corpMember = CorpMember.builder()
            .guid(guid)
            .rank(rank)
            .contribution(0)
            .donateResources(0)
            .donateMallPoints(0)
            .build();

        return corpMember;

    }

    public static CorpsLevelData getCorpsLevelData(int index) {

        CorpsLevelJson corpsLevelJson = ResourceManager.getCorpsLevelJson();

        if (index < 0 || index >= corpsLevelJson.getCorpsUpgrades().size()) {
            return null;
        }

        List<CorpsLevelData> corpsLevelData = corpsLevelJson.getCorpsUpgrades();
        CorpsLevelData data = corpsLevelData.get(index);

        return data;

    }

    public static CorpsLevelMeta getCorpsLevelMeta(CorpsLevelData corpsLevelData, int index) {

        if (index < 0 || index >= corpsLevelData.getLevels().size()) {
            return null;
        }

        List<CorpsLevelMeta> corpsLevelMeta = corpsLevelData.getLevels();
        CorpsLevelMeta data = corpsLevelMeta.get(index);

        return data;

    }

    public static List<CorpsLevelEffectMeta> getCorpsLevelEffectMeta(CorpsLevelMeta corpsLevelMeta) {

        List<CorpsLevelEffectMeta> data = corpsLevelMeta.getEffects();

        return data;

    }

    public static CorpInventory createNewCorpInventory() {

        CorpInventory inventory = CorpInventory.builder()
            .maxStacks(10)
            .stackPrice(1000)
            .corpPropList(new ArrayList<>())
            .build();

        return inventory;

    }


    public static CorpService getInstance() {

        return instance;
    }

}
