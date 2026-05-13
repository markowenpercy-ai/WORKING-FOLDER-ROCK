package com.go2super.database.entity;

import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ShipBodyData;
import com.go2super.resources.data.ShipPartData;
import com.go2super.resources.data.meta.*;
import com.go2super.service.PacketService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.*;

@Document(collection = "game_models")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipModel {

    @Id
    private ObjectId id;

    @Column(unique = true)
    private int shipModelId;
    private int guid;

    private String name;
    private int bodyId;

    private boolean deleted;

    private List<Integer> parts = new ArrayList<>();

    public List<PartLevelMeta> getPartsMeta() {

        List<PartLevelMeta> partLevelMetas = new ArrayList<>();
        for (int partId : parts) {
            ShipPartData partData = ResourceManager.getShipParts().findByPartId(partId);
            partLevelMetas.add(partData.getLevel(partId));
        }
        return partLevelMetas;
    }

    public int getTransmissionStart() {

        if (PacketService.getInstance().isFastTransmission()) {
            return 2;
        }

        int result = 0;

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);
        if (body != null) {
            result += body.getTransmission().getStart();
        }

        return result;

    }

    public int getTransmissionRate() {

        if (PacketService.getInstance().isFastTransmission()) {
            return 2;
        }

        int result = 0;

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);
        if (body != null) {
            result += body.getTransmission().getRate();
        }

        return result;

    }

    public int getBuildTime() {

        int result = 0;

        for (int part : parts) {

            PartLevelMeta meta = ResourceManager.getShipParts().getMeta(part);

            if (meta == null) {
                continue;
            }

            result += meta.getBuildCost().getTime();

        }

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);

        if (body != null) {
            result += body.getBuildCost().getTime();
        }

        return result;

    }

    public int getMetalBuildCost() {

        int result = 0;

        for (int part : parts) {

            PartLevelMeta meta = ResourceManager.getShipParts().getMeta(part);

            if (meta == null) {
                continue;
            }

            result += meta.getBuildCost().getMetal();

        }

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);

        if (body != null) {
            result += body.getBuildCost().getMetal();
        }

        return result;

    }

    public int getHe3BuildCost() {

        int result = 0;

        for (int part : parts) {

            PartLevelMeta meta = ResourceManager.getShipParts().getMeta(part);

            if (meta == null) {
                continue;
            }

            result += meta.getBuildCost().getFuel();

        }

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);

        if (body != null) {
            result += body.getBuildCost().getFuel();
        }

        return result;

    }

    public int getGoldBuildCost() {

        int result = 0;

        for (int part : parts) {

            PartLevelMeta meta = ResourceManager.getShipParts().getMeta(part);

            if (meta == null) {
                continue;
            }

            result += meta.getBuildCost().getGold();

        }

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);

        if (body != null) {
            result += body.getBuildCost().getGold();
        }

        return result;

    }

    public int getDurability() {

        return getStructure() + getShields();
    }

    public int getStructure() {

        int result = 0;
        PartEffectMeta effect;

        for (int part : parts) {
            effect = ResourceManager.getShipParts().getEffect(part, "structure");
            result += effect != null ? (double) effect.getValue() : 0;
            effect = ResourceManager.getShipParts().getEffect(part, "armor");
            result += effect != null ? (double) effect.getValue() : 0;
        }

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);

        if (body != null) {
            result += body.getArmor();
        }

        return result;

    }

    public int getFuelStorage() {

        int result = 0;

        for (int part : parts) {
            PartEffectMeta effect = ResourceManager.getShipParts().getEffect(part, "fuelStorage");
            result += effect != null ? (double) effect.getValue() : 0;
        }

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);

        if (body != null) {
            result += body.getFuelStorage();
        }

        return result;

    }

    public double getFuelUsage() {

        double result = 0;

        for (int part : parts) {
            double effect = ResourceManager.getShipParts().getMeta(part).getFuelUsage();
            result += effect;
        }

        return result;

    }

    public int getShields() {

        int result = 0;

        for (int part : parts) {
            PartEffectMeta effect = ResourceManager.getShipParts().getEffect(part, "shield");
            result += effect != null ? (double) effect.getValue() : 0;
        }

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);

        if (body != null) {
            result += body.getShield();
        }

        return result;

    }

    public int getMinRange() {

        int result = 0;

        for (int part : parts) {

            PartEffectMeta effect = ResourceManager.getShipParts().getEffect(part, "range");

            if (effect == null) {
                continue;
            }

            int range = (int) effect.getMin();

            if (result == 0) {
                result = range;
            } else if (result > range) {
                result = range;
            }

        }

        return result;

    }

    public int getMaxRange() {

        int result = 0;

        for (int part : parts) {

            PartEffectMeta effect = ResourceManager.getShipParts().getEffect(part, "range");

            if (effect == null) {
                continue;
            }

            int range = (int) effect.getMax();

            if (result == 0) {
                result = range;
            } else if (result < range) {
                result = range;
            }

        }

        return result;

    }

    public int getMinAttack() {
        return getMinAttack(Collections.emptyList());
    }

    public int getMinAttack(List<Pair<String, Double>> additionalAttack) {
        int result = 0;

        for (int part : parts) {
            PartEffectMeta effect = ResourceManager.getShipParts().getEffect(part, "attack");
            ShipPartData data = ResourceManager.getShipParts().findByPartId(part);
            if (effect == null) continue;
            double atk = effect.getMin();
            for (Pair<String, Double> pair : additionalAttack) {
                if (pair.getKey().equals(data.getPartSubType())) {
                    atk = effect.getMin() + pair.getValue();
                    break;
                }
            }
            result += (int) atk;
        }
        return result;
    }

    public int getMaxAttack() {
        return getMaxAttack(Collections.emptyList());
    }

    public int getMaxAttack(List<Pair<String, Double>> additionalAttack) {

        int result = 0;

        for (int part : parts) {
            PartEffectMeta effect = ResourceManager.getShipParts().getEffect(part, "attack");
            ShipPartData data = ResourceManager.getShipParts().findByPartId(part);
            if (effect == null) continue;
            double atk = effect.getMax();

            for (Pair<String, Double> pair : additionalAttack) {
                if (pair.getKey().equals(data.getPartSubType())) {
                    atk = effect.getMax() + pair.getValue();
                    break;
                }
            }
            result += (int) atk;
        }
        return result;

    }

    public int getMovement() {

        int result = 0;

        for (int part : parts) {
            PartEffectMeta effect = ResourceManager.getShipParts().getEffect(part, "movement");
            result += effect != null ? (double) effect.getValue() : 0;
        }

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);

        if (body != null) {
            result += body.getMovement();
        }

        return result;

    }

    public double getDoubleRate() {

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);
        PartEffectMeta effect = body.getEffect("doubleRate");

        if (effect == null) {
            return 0;
        }

        return (double) effect.getValue();

    }

    public double getCritRate() {

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);
        PartEffectMeta effect = body.getEffect("critRate");

        if (effect == null) {
            return 0;
        }

        return (double) effect.getValue();

    }

    public double getCriticalRateReduce() {

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);
        PartEffectMeta effect = body.getEffect("critRateReduce");

        if (effect == null) {
            return 0;
        }

        return (double) effect.getValue();

    }

    public boolean getFlankIgnore() {

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);
        PartEffectMeta effect = body.getEffect("flankIgnore");

        if (effect == null) {
            return false;
        }

        return (double) effect.getValue() == 1.0;
    }

    public double getDamageReduce() {

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);
        PartEffectMeta effect = body.getEffect("damageReduce");

        if (effect == null) {
            return 0;
        }

        return (double) effect.getValue();

    }

    public double getFlankRate() {

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);
        PartEffectMeta effect = body.getEffect("flankBonus");

        if (effect == null) {
            return 0;
        }

        return (double) effect.getValue();

    }

    public double getHitRate() {

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);
        PartEffectMeta effect = body.getEffect("hitRate");

        if (effect == null) {
            return 0;
        }

        return (double) effect.getValue();

    }

    public double getPierceShield() {

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);
        PartEffectMeta effect = body.getEffect("pierceShield");

        if (effect == null) {
            return 0;
        }

        return (double) effect.getValue();

    }

    public double getDamageBonus() {

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);
        PartEffectMeta effect = body.getEffect("damageBonus");

        if (effect == null) {
            return 0;
        }

        return (double) effect.getValue();

    }

    public Map<String, Double> getArmorBonus() {

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);
        PartSpecialMeta[] effect = body.getSpecialEffect("armorBonus");

        Map<String, Double> result = new HashMap<>();

        if (effect == null) {
            return result;
        }

        for (PartSpecialMeta meta : effect) {
            result.put(meta.getArmor(), meta.getAmount());
        }

        return result;

    }

    public Map<String, Double> getPartBonus() {
        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);
        PartBonusMeta[] effect = body.getBonusEffect("partBonus");
        Map<String, Double> result = new HashMap<>();

        if (effect == null) {
            return result;
        }

        for (PartBonusMeta meta : effect) {
            result.put(meta.getPart(), meta.getAmount());
        }
        return result;
    }

    public int getArmorHeal() {

        int result = 0;

        for (int part : parts) {
            PartEffectMeta effect = ResourceManager.getShipParts().getEffect(part, "armorHeal");
            result += effect != null ? effect.getMax() : 0;
        }

        return result;

    }

    public int getShieldHeal() {

        int result = 0;

        for (int part : parts) {
            PartEffectMeta effect = ResourceManager.getShipParts().getEffect(part, "shieldHeal");
            result += effect != null ? effect.getMax() : 0;
        }

        return result;

    }

    public double getCritRateBonus() {

        String effectName = "critRateBonus";
        double result = 0;

        for (int part : parts) {
            ShipPartData partData = ResourceManager.getShipParts().findByPartId(part);
            if (partData.getPartOperation() == null) {
                PartEffectMeta effect = partData.getLevel(part).getEffect(effectName);
                result += effect != null ? (double) effect.getValue() : 0.00;
            }
        }

        return result;

    }

    public double getAgility() {

        String effectName = "agility";
        double result = 0;

        for (int part : parts) {
            ShipPartData partData = ResourceManager.getShipParts().findByPartId(part);
            if (partData.getPartOperation() == null) {
                PartEffectMeta effect = partData.getLevel(part).getEffect(effectName);
                result += effect != null ? (double) effect.getValue() : 0.00;
            }
        }

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);

        if (body != null) {
            result += body.getAgility();
        }
        return result;

    }

    public double getSteeringBonus() {

        String effectName = "steeringBonus";
        double result = 0;

        for (int part : parts) {
            ShipPartData partData = ResourceManager.getShipParts().findByPartId(part);
            if (partData.getPartOperation() == null) {
                PartEffectMeta effect = partData.getLevel(part).getEffect(effectName);
                result += effect != null ? (double) effect.getValue() : 0.00;
            }
        }

        return result;

    }

    public double getHitRateBonus() {

        String effectName = "hitRateBonus";
        double result = 0;

        for (int part : parts) {
            ShipPartData partData = ResourceManager.getShipParts().findByPartId(part);
            if (partData.getPartOperation() == null) {
                PartEffectMeta effect = partData.getLevel(part).getEffect(effectName);
                result += effect != null ? (double) effect.getValue() : 0.00;
            }
        }

        return result;

    }

    public double getStability() {

        double result = 0;

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);

        if (body != null) {
            result += body.getStability();
        }

        return result;

    }

    public double getStabilityBonus() {

        String effectName = "stability";
        double result = 0;

        for (int part : parts) {
            ShipPartData partData = ResourceManager.getShipParts().findByPartId(part);
            if (partData.getPartOperation() == null) {
                PartEffectMeta effect = partData.getLevel(part).getEffect(effectName);
                result += effect != null ? (double) effect.getValue() : 0.00;
            }
        }

        return result;

    }

    public double getDaedalus() {

        String effectName = "daedalus";
        double result = 0;

        for (int part : parts) {
            ShipPartData partData = ResourceManager.getShipParts().findByPartId(part);
            if (partData.getPartOperation() == null) {
                PartEffectMeta effect = partData.getLevel(part).getEffect(effectName);
                result += effect != null ? (double) effect.getValue() : 0.00;
            }
        }

        BodyLevelMeta body = ResourceManager.getShipBodies().getMeta(bodyId);

        if (body != null) {
            result += body.getDaedalus();
        }

        return result;

    }

    public ShipBodyData getBodyData() {

        return ResourceManager.getShipBodies().findByBodyId(bodyId);
    }

    public BodyLevelMeta getBodyLevelMeta() {

        return ResourceManager.getShipBodies().getMeta(bodyId);
    }

    public boolean isFlagship() {

        ShipBodyData body = ResourceManager.getShipBodies().findByBodyId(bodyId);
        return body != null && body.isFlagship();
    }

    public int[] partArray() {

        int[] array = new int[50];
        for (int i = 0; i < parts.size(); i++) {
            array[i] = parts.get(i);
        }
        return array;
    }

    public int partNum() {

        int count = 0;
        for (int part : parts) {
            if (part > 0) {
                count++;
            }
        }
        return count;
    }

}
