package com.go2super.service.jobs.corp;

import com.go2super.database.entity.Corp;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.CorpMember;
import com.go2super.logger.BotLogger;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.corp.ResponseConsortiaUpgradeCompletePacket;
import com.go2super.resources.data.CorpsLevelData;
import com.go2super.resources.data.meta.CorpsLevelEffectMeta;
import com.go2super.resources.data.meta.CorpsLevelMeta;
import com.go2super.service.CorpService;
import com.go2super.service.UserService;
import com.go2super.service.jobs.OfflineJob;
import com.go2super.socket.util.DateUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CorpUpgradeJob implements OfflineJob {

    private long lastExecution = 0L;

    @Override
    public void setup() {

    }

    @Override
    public void run() {

        if (DateUtil.millis() - lastExecution < getInterval()) {
            return;
        }
        lastExecution = DateUtil.millis();

        List<Corp> corps = CorpService.getInstance().getCorpCache().findByCorpUpgrade();
        CopyOnWriteArrayList<Integer> toUpdate = new CopyOnWriteArrayList<>();

        for (Corp corp : corps) {

            if (corp.getCorpUpgrade() == null) {
                continue;
            }
            if (DateUtil.remains(corp.getCorpUpgrade().getUntil()).intValue() <= 0) {
                toUpdate.add(corp.getCorpId());
            }

        }

        if (!toUpdate.isEmpty()) {

            List<Corp> toUpdateCorps = CorpService.getInstance().getCorpCache().findByCorpId(toUpdate);

            for (Corp corp : toUpdateCorps) {

                if (corp == null || corp.getCorpUpgrade() == null) {
                    continue;
                }
                if (DateUtil.remains(corp.getCorpUpgrade().getUntil()).intValue() <= 0) {

                    switch (corp.getCorpUpgrade().getTypeUpgrade()) {

                        case 0:

                            int nextCorpLevel = corp.getLevel() + 1;

                            CorpsLevelData corpsLevelData = CorpService.getCorpsLevelData(0);
                            CorpsLevelMeta corpsLevelMeta = CorpService.getCorpsLevelMeta(corpsLevelData, nextCorpLevel);
                            List<CorpsLevelEffectMeta> corpsLevelEffectMeta = CorpService.getCorpsLevelEffectMeta(corpsLevelMeta);

                            corp.setLevel(corp.getLevel() + 1);

                            corp.setMaxMembers((int) corpsLevelEffectMeta.get(1).getValue());
                            corp.setRbpLimit((int) corpsLevelEffectMeta.get(2).getValue());
                            corp.setResourceBonus(corpsLevelEffectMeta.get(3).getValue());

                            corp.save();

                            break;

                        case 1:

                            int nextWareHouseLevel = corp.getWarehouseLevel() + 1;

                            CorpsLevelData wareHouseLevelData = CorpService.getCorpsLevelData(1);
                            CorpsLevelMeta wareHouseLevelMeta = CorpService.getCorpsLevelMeta(wareHouseLevelData, nextWareHouseLevel);
                            List<CorpsLevelEffectMeta> wareHouseLevelEffectMeta = CorpService.getCorpsLevelEffectMeta(wareHouseLevelMeta);


                            for (CorpMember corpMember : corp.getMembers().getMembers()) {

                                User user = UserService.getInstance().getUserCache().findByGuid(corpMember.getGuid());

                                if (user.getCorpInventory() == null) {

                                    user.setCorpInventory(CorpService.createNewCorpInventory());

                                }

                                user.getCorpInventory().setMaxStacks((int) wareHouseLevelEffectMeta.get(0).getValue());
                                user.save();

                            }

                            corp.setWarehouseLevel(corp.getWarehouseLevel() + 1);

                            corp.save();

                            break;

                        case 2:

                            int nextMergeLevel = corp.getMergingLevel() + 1;

                            CorpsLevelData mergeLevelData = CorpService.getCorpsLevelData(2);
                            CorpsLevelMeta mergeLevelMeta = CorpService.getCorpsLevelMeta(mergeLevelData, nextMergeLevel);
                            List<CorpsLevelEffectMeta> mergeEffectMeta = CorpService.getCorpsLevelEffectMeta(mergeLevelMeta);

                            corp.setMergingLevel(corp.getMergingLevel() + 1);
                            corp.setMergeBonus(mergeEffectMeta.get(0).getValue());

                            corp.save();

                            break;

                        case 3:

                            corp.setMallLevel(corp.getMallLevel() + 1);

                            corp.save();

                            break;

                        default:

                            BotLogger.log("Invalid Upgrade");

                            break;
                    }

                    try {
                        ResponseConsortiaUpgradeCompletePacket response = ResponseConsortiaUpgradeCompletePacket.builder()
                                .kind(corp.getCorpUpgrade().getTypeUpgrade())
                                .consortiaId(corp.getCorpId())
                                .level(UnsignedChar.of(corp.getLevel()))
                                .propsCorpsPack(0)
                                .shopLevel((byte) corp.getMallLevel())
                                .storageLevel((byte) corp.getWarehouseLevel())
                                .unionLevel((byte) corp.getMergingLevel())
                                .build();

                        List<CorpMember> corpMembers = corp.getMembers().getMembers();

                        for (CorpMember corpMember : corpMembers) {

                            User user = UserService.getInstance().getUserCache().findByGuid(corpMember.getGuid());
                            if (!user.isOnline()) {
                                continue;
                            }

                            if (user.getCorpInventory() != null) {
                                response.setPropsCorpsPack(user.getCorpInventory().getMaxStacks());
                            }

                            user.getLoggedGameUser().get().getSmartServer().send(response);
                            BotLogger.log("test");

                        }
                    } catch (Exception ex) {
                        //unable to response, ignore basically
                    }


                    corp.setCorpUpgrade(null);
                    corp.save();

                }

            }


        }

    }

    @Override
    public long getInterval() {

        return 2000L;
    }

}
