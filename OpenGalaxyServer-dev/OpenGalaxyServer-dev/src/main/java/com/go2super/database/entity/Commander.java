package com.go2super.database.entity;

import com.go2super.database.entity.sub.BattleCommander;
import com.go2super.database.entity.sub.BionicChip;
import com.go2super.database.entity.sub.CommanderExpertise;
import com.go2super.database.entity.sub.UserPlanet;
import com.go2super.database.entity.type.ExpertiseType;
import com.go2super.obj.game.CommanderAttributes;
import com.go2super.obj.game.CommanderLevel;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.CommanderLevelsData;
import com.go2super.resources.data.PropData;
import com.go2super.resources.data.meta.ChipMeta;
import com.go2super.resources.data.meta.EnemyStatsMeta;
import com.go2super.resources.data.meta.GemMeta;
import com.go2super.resources.data.props.PropChipData;
import com.go2super.resources.data.props.PropGemData;
import com.go2super.resources.localization.Localization;
import com.go2super.service.BattleService;
import com.go2super.service.CommanderService;
import com.go2super.service.GalaxyService;
import com.go2super.service.PacketService;
import com.go2super.socket.util.MathUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.*;

@Document(collection = "game_commanders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commander {

    @Id
    private ObjectId id;

    @Column(unique = true)
    private int commanderId;

    private long userId;
    private int shipTeamId = -1;

    private String name;

    private int skill;
    private int stars;
    private int level;
    private int experience;
    private int variance;

    private Date untilRest;
    private boolean dead;
    private String injuredMatch;

    private int growthAim;
    private int growthDodge;
    private int growthSpeed;
    private int growthElectron;

    private List<Integer> gems = new ArrayList<>();
    private List<BionicChip> chips = new ArrayList<>();

    private int commonBaseAim;
    private int commonBaseDodge;
    private int commonBaseSpeed;
    private int commonBaseElectron;

    private CommanderExpertise commonExpertise;

    public int addExp(int exp) {

        if (isVinna()) {
            exp = (int) (exp * (1.3));
        }
        if ((this.experience + exp) >= CommanderService.MAXIMUM_EXPERIENCE) {
            this.experience = CommanderService.MAXIMUM_EXPERIENCE;
        } else {
            this.experience += exp;
        }

        return exp;

    }

    public int getNextChipHole() {

        int holeId;
        hole:
        for (holeId = 0; holeId < 5; holeId++) {
            for (BionicChip chip : chips) {
                if (chip.getHoleId() == holeId) {
                    continue hole;
                }
            }
            return holeId;
        }
        return -1;
    }

    public void save() {

        CommanderService.getInstance().getCommanderCache().save(this);
    }

    public void setVariance(int variance) {
        this.variance = Math.min(Math.max(variance, -CommanderService.MAX_VARIANCE), CommanderService.MAX_VARIANCE);
    }

    public void setGrowthAim(int growthAim) {
        this.growthAim = Math.min(Math.max(growthAim, 0), CommanderService.MAX_GROWTH);
    }

    public void setGrowthDodge(int growthDodge) {
        this.growthDodge = Math.min(Math.max(growthDodge, 0), CommanderService.MAX_GROWTH);
    }

    public void setGrowthSpeed(int growthSpeed) {
        this.growthSpeed = Math.min(Math.max(growthSpeed, 0), CommanderService.MAX_GROWTH);
    }

    public void setGrowthElectron(int growthElectron) {
        this.growthElectron = Math.min(Math.max(growthElectron, 0), CommanderService.MAX_GROWTH);
    }

    public void reset() {

        if (isCommon()) {

            setCommonBaseAim(MathUtil.random(0, 11));
            setCommonBaseElectron(MathUtil.random(0, 11));
            setCommonBaseDodge(MathUtil.random(0, 11));
            setCommonBaseSpeed(MathUtil.random(0, 11));

            setCommonExpertise(CommanderExpertise.common());

        }

        this.experience = 0;

        randomizeGrowth();
        randomizeVariance();

        this.growthAim = Math.min(Math.max(this.growthAim, 0), CommanderService.MAX_GROWTH);
        this.growthDodge = Math.min(Math.max(this.growthDodge, 0), CommanderService.MAX_GROWTH);
        this.growthSpeed = Math.min(Math.max(this.growthSpeed, 0), CommanderService.MAX_GROWTH);
        this.growthElectron = Math.min(Math.max(this.growthElectron, 0), CommanderService.MAX_GROWTH);

    }

    @Transient
    public BattleCommander createBattleCommander(double additionalGrowth, EnemyStatsMeta stats) {

        BattleCommander commander = new BattleCommander();

        CommanderAttributes baseAttributes = CommanderService.getInstance().getBaseAttributes(this);

        commander.setCommanderId(commanderId);
        commander.setSkillId(skill);
        commander.setExpertise(getExpertise());
        commander.setNameId(name);
        commander.setName(getName());
        commander.setStars(stars);

        double totalAim;
        double totalDodge;
        double totalSpeed;
        double totalElectron;

        if (stats == null) {

            totalAim = getTotalAim(baseAttributes);
            totalDodge = getTotalDodge(baseAttributes);
            totalSpeed = getTotalSpeed(baseAttributes);
            totalElectron = getTotalElectron(baseAttributes);

        } else {

            totalAim = stats.getAccuracy();
            totalDodge = stats.getDodge();
            totalSpeed = stats.getSpeed();
            totalElectron = stats.getElectron();

        }

        commander.setTotalAccuracy(totalAim);
        commander.setTotalDodge(totalDodge);
        commander.setTotalSpeed(totalSpeed);
        commander.setTotalElectron(totalElectron);

        double attackIncrement = getStoneAssault();
        double structureIncrement = getStoneEndure();
        double shieldIncrement = getStoneShield();
        double criticalAttackIncrement = getStoneBlastHurt();
        double criticalAttackRateIncrement = getStoneBlastHurtRate();
        double doubleAttackRateIncrement = getStoneDoubleHit();
        double shieldHealIncrement = getStoneRepairShield();
        double experienceRateIncrement = getStoneExp();

        commander.setAttackPowerIncrement(attackIncrement);
        commander.setStructureIncrement(structureIncrement);
        commander.setShieldIncrement(shieldIncrement);
        commander.setCriticalAttackDamageIncrement(criticalAttackIncrement);
        commander.setCriticalAttackRateIncrement(criticalAttackRateIncrement);
        commander.setDoubleAttackRateIncrement(doubleAttackRateIncrement);
        commander.setShieldHealIncrement(shieldHealIncrement);
        commander.setExperienceRateIncrement(experienceRateIncrement);

        double additionalArmor = getChipAdditionalArmor();
        double additionalShield = getChipAdditionalShield();

        double additionalDaedalus = getChipAdditionalDaedalus();
        double additionalStability = getChipAdditionalStability();
        double additionalAbsorption = getChipAdditionalAbsorption();
        double additionalArmorRegeneration = getChipAdditionalArmorRegen();
        double additionalShieldRegeneration = getChipAdditionalShieldRegen();

        List<Pair<String, Double>> additionalMinAttack = getChipAdditionalMinAttack();
        List<Pair<String, Double>> additionalMaxAttack = getChipAdditionalMaxAttack();

        commander.setAdditionalArmor(additionalArmor);
        commander.setAdditionalShield(additionalShield);

        commander.setAdditionalDaedalus(additionalDaedalus);
        commander.setAdditionalStability(additionalStability);
        commander.setAdditionalAbsorption(additionalAbsorption);
        commander.setAdditionalArmorRegeneration(additionalArmorRegeneration);
        commander.setAdditionalShieldRegeneration(additionalShieldRegeneration);

        commander.setAdditionalMinAttack(additionalMinAttack);
        commander.setAdditionalMaxAttack(additionalMaxAttack);

        commander.setLevel(getLevel().getLevel());
        commander.setTrigger(CommanderService.getInstance().createTrigger(commander));

        return commander;

    }

    public Fleet getFleet() {

        return PacketService.getInstance().getFleetCache().findByCommanderId(commanderId);
    }

    public boolean hasFleet() {

        return getFleet() != null;
    }

    public double getTotalAim(CommanderAttributes commanderAttributes) {

        return commanderAttributes.getAim() + getStoneAim();
    }

    public double getTotalDodge(CommanderAttributes commanderAttributes) {

        return commanderAttributes.getDodge() + getStoneDodge();
    }

    public double getTotalSpeed(CommanderAttributes commanderAttributes) {

        return commanderAttributes.getSpeed() + getStonePriority();
    }

    public double getTotalElectron(CommanderAttributes commanderAttributes) {

        return commanderAttributes.getElectron() + getStoneElectron();
    }

    public double getChipAdditionalArmor() {

        return getChipAttribute("armor");
    }

    public double getChipAdditionalShield() {

        return getChipAttribute("shield");
    }

    public double getChipAdditionalDaedalus() {

        return getChipAttribute("daedalus");
    }

    public double getChipAdditionalStability() {

        return getChipAttribute("stability");
    }

    public double getChipAdditionalAbsorption() {

        return getChipAttribute("negation");
    }

    public double getChipAdditionalArmorRegen() {

        return getChipAttribute("armorRepair");
    }

    public double getChipAdditionalShieldRegen() {

        return getChipAttribute("shieldRepair");
    }

    public double getChipAdditionalTransmission() {
        return getChipAttribute("transmission");
    }

    public List<Pair<String, Double>> getChipAdditionalMinAttack() {

        return getChipAttackAttribute("minAssault");
    }

    public List<Pair<String, Double>> getChipAdditionalMaxAttack() {

        return getChipAttackAttribute("maxAssault");
    }

    public double getStoneAim() {

        return getGemAttribute("accuracy");
    }

    public double getStoneDodge() {

        return getGemAttribute("dodge");
    }

    public double getStoneElectron() {

        return getGemAttribute("electron");
    }

    public double getStonePriority() {

        return getGemAttribute("speed");
    }

    public double getStoneAssault() {

        return getGemAttribute("attack");
    }

    public double getStoneEndure() {

        return getGemAttribute("armor");
    }

    public double getStoneShield() {

        return getGemAttribute("shield");
    }

    public double getStoneBlastHurt() {

        return getGemAttribute("critAttack");
    }

    public double getStoneBlastHurtRate() {

        return getGemAttribute("critRate");
    }

    public double getStoneDoubleHit() {

        return getGemAttribute("doubleRate");
    }

    public double getStoneRepairShield() {

        return getGemAttribute("shieldHeal");
    }

    public double getStoneExp() {

        return getGemAttribute("expRate");
    }

    public ExpertiseType getStoneBallisticExpertise() {

        return getGemExpertise("expertiseBallistic");
    }

    public ExpertiseType getStoneMissileExpertise() {

        return getGemExpertise("expertiseMissile");
    }

    public ExpertiseType getStoneDirectionalExpertise() {

        return getGemExpertise("expertiseDirectional");
    }

    public ExpertiseType getStoneShipBasedExpertise() {

        return getGemExpertise("expertiseShipBased");
    }

    public ExpertiseType getStoneDefendExpertise() {

        return getGemExpertise("expertiseBuilding");
    }

    public ExpertiseType getStoneFrigateExpertise() {

        return getGemExpertise("expertiseFrigate");
    }

    public ExpertiseType getStoneCruiserExpertise() {

        return getGemExpertise("expertiseCruiser");
    }

    public ExpertiseType getStoneBattleshipExpertise() {

        return getGemExpertise("expertiseBattleship");
    }

    public double getChipAttribute(String name) {

        double result = 0.0d;
        List<BionicChip> chips = getChips();

        for (BionicChip chip : chips) {

            ChipMeta chipMeta = chip.getChipData().getEffect();
            if (chipMeta.getType().equals(name)) {
                result += chipMeta.getValue();
            }

        }

        return result;

    }

    public List<Pair<String, Double>> getChipAttackAttribute(String name) {

        List<Pair<String, Double>> result = new ArrayList<>();
        List<BionicChip> chips = getChips();

        for (BionicChip chip : chips) {

            PropChipData chipData = chip.getChipData();
            ChipMeta chipMeta = chipData.getEffect();

            if (name.equals(chipMeta.getType())) {
                result.add(Pair.of(chipMeta.getTag(), chipMeta.getValue()));
            }

        }

        return result;

    }

    public ExpertiseType getGemExpertise(String name) {

        List<Integer> gems = getGems();
        ExpertiseType result = ExpertiseType.D;

        for (int i = 0; i < getLevel().getLevelData().getGem(); i++) {

            int gemId = gems.get(i);
            if (gemId == -1) {
                continue;
            }

            PropData gem = ResourceManager.getProps().getGemData(gemId);
            if (gem == null) {
                continue;
            }

            PropGemData data = gem.getGemData();

            if (data.getEffects() != null && data.getEffects().size() > 0) {
                for (GemMeta meta : data.getEffects()) {
                    if (meta.getType().equals(name)) {
                        String expertise = meta.getExpertise();
                        if (expertise == null) {
                            continue;
                        }
                        ExpertiseType type = ExpertiseType.valueOf(expertise);
                        if (type.isBetterThan(result)) {
                            result = type;
                        }
                    }
                }
            }

        }

        return result;

    }

    public double getGemAttribute(String name) {

        List<Integer> gems = getGems();
        double result = 0;

        for (int i = 0; i < getLevel().getLevelData().getGem(); i++) {

            int gemId = gems.get(i);
            if (gemId == -1) {
                continue;
            }

            PropData gem = ResourceManager.getProps().getGemData(gemId);
            if (gem == null) {
                continue;
            }

            PropGemData data = gem.getGemData();

            if (data.getEffects() != null && data.getEffects().size() > 0) {
                for (GemMeta meta : data.getEffects()) {
                    if (meta.getType().equals(name)) {
                        result += meta.getValue();
                    }
                }
            }

        }

        return result;

    }

    public int getInheritedLevel() {

        return level;
    }

    public void delete() {

        CommanderService.getInstance().getCommanderCache().delete(this);
    }

    public boolean isPirate() {

        return commanderId == -1;
    }

    public boolean isCommon() {

        return skill == -1;
    }

    public boolean isAngla() {

        return skill == 0;
    }

    public boolean isVinna() {

        return skill == 34;
    }

    public boolean isTitan() {

        return Arrays.asList(85, 86, 87, 88, 89, 90, 94, 105, 106).contains(skill);
    }

    public void randomizeGrowth() {

        CommanderService.getInstance().randomizeGrowth(this);
    }

    public void randomizeVariance() {

        CommanderService.getInstance().randomizeVariance(this);
    }
    @Transient
    public CommanderLevel getLevel() {
        if (experience == -1) {
            return CommanderLevel.builder()
                    .levelData(CommanderLevelsData.builder()
                            .exp(-1)
                            .gem(-1)
                            .build())
                    .level(level)
                    .levelExperience(-1)
                    .build();
        }
        return CommanderService.getInstance().getLevel(this);
    }

    public int getBaseAim() {

        return isCommon() ? commonBaseAim : CommanderService.getInstance().getStats(this).getBaseStats().getAccuracy();
    }

    public int getBaseElectron() {

        return isCommon() ? commonBaseElectron : CommanderService.getInstance().getStats(this).getBaseStats().getElectron();
    }

    public int getBaseDodge() {

        return isCommon() ? commonBaseDodge : CommanderService.getInstance().getStats(this).getBaseStats().getDodge();
    }

    public int getBaseSpeed() {

        return isCommon() ? commonBaseSpeed : CommanderService.getInstance().getStats(this).getBaseStats().getSpeed();
    }

    public String getName() {

        return isCommon() ? name : Localization.EN_US.get(CommanderService.getInstance().getStats(this).getName());
    }

    public int getType() {

        return isCommon() ? 1 : CommanderService.getInstance().getStats(this).typeCode();
    }

    public User getUser() {

        UserPlanet userPlanet = getUserPlanet();
        if (userPlanet == null) {
            return null;
        }
        Optional<User> optionalUser = userPlanet.getUser();
        if (optionalUser.isEmpty()) {
            return null;
        }
        return optionalUser.get();
    }

    public UserPlanet getUserPlanet() {

        return GalaxyService.getInstance().getUserPlanet(userId);
    }

    public CommanderExpertise getExpertise() {

        CommanderExpertise expertise = isCommon() ? commonExpertise : CommanderService.getInstance().getStats(this).getExpertise();

        ExpertiseType ballisticExpertise = getStoneBallisticExpertise();
        ExpertiseType directionalExpertise = getStoneDirectionalExpertise();
        ExpertiseType missileExpertise = getStoneMissileExpertise();
        ExpertiseType carrierExpertise = getStoneShipBasedExpertise();
        ExpertiseType defendExpertise = getStoneDefendExpertise();

        ExpertiseType frigateExpertise = getStoneFrigateExpertise();
        ExpertiseType cruiserExpertise = getStoneCruiserExpertise();
        ExpertiseType battleShipExpertise = getStoneBattleshipExpertise();

        if (ballisticExpertise.isBetterThan(expertise.getBallistic())) {
            expertise.setBallistic(ballisticExpertise);
        }
        if (directionalExpertise.isBetterThan(expertise.getDirectional())) {
            expertise.setDirectional(directionalExpertise);
        }
        if (missileExpertise.isBetterThan(expertise.getMissile())) {
            expertise.setMissile(missileExpertise);
        }
        if (carrierExpertise.isBetterThan(expertise.getCarrier())) {
            expertise.setCarrier(carrierExpertise);
        }
        if (defendExpertise.isBetterThan(expertise.getDefend())) {
            expertise.setDefend(defendExpertise);
        }

        if (frigateExpertise.isBetterThan(expertise.getFrigate())) {
            expertise.setFrigate(frigateExpertise);
        }
        if (cruiserExpertise.isBetterThan(expertise.getCruiser())) {
            expertise.setCruiser(cruiserExpertise);
        }
        if (battleShipExpertise.isBetterThan(expertise.getBattleShip())) {
            expertise.setBattleShip(battleShipExpertise);
        }

        return expertise;

    }

    public List<Integer> getGems() {

        if (gems == null || gems.size() != 12) {
            for (int i = 0; i < 12; i++) {
                gems.add(-1);
            }
        }
        return gems;
    }

    public boolean hasGems() {

        for (int gem : getGems()) {
            if (gem != -1) {
                return true;
            }
        }
        return false;
    }
    // state is:
    // 0 = None
    // 1 = Wounded
    // 2 = Dead
    // 3 = Receiving
    // 4 = In Battle
    // 5 = In Transition
    public int getState() {

        if (BattleService.getInstance().getCurrent(this) != null) {
            return 4;
        }

        Fleet fleet = PacketService.getInstance().getFleetCache().findByCommanderId(this.getCommanderId());
        if (fleet != null && fleet.isInTransmission()) {
            return 5;
        }

        if (injuredMatch != null && BattleService.getInstance().getBattle(injuredMatch) != null) {
            return 4;
        }
        if (isDead()) {
            return 2;
        }
        if (untilRest != null && untilRest.after(new Date())) {
            return 1;
        }
        return 0;
    }

    public boolean canPilot() {

        return !isDead() && getState() != 4;
    }

    public boolean hasChips() {

        return !getChips().isEmpty();
    }

    public CommanderAttributes getBaseAttributes() {

        return CommanderService.getInstance().getBaseAttributes(this);
    }

}
