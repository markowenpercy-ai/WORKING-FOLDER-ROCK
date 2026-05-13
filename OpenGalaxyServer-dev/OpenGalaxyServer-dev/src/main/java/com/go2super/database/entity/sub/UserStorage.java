package com.go2super.database.entity.sub;

import lombok.Builder;
import lombok.Data;

import java.util.*;

@Builder
@Data
public class UserStorage {

    private int gold;
    private int he3;
    private int metal;

    private int goldProduction;
    private int he3Production;
    private int metalProduction;

    private Date lastProductionCalculus;

    public void reset() {

        this.gold = 0;
        this.he3 = 0;
        this.metal = 0;

    }

    public void addGold(int gold, int max) {

        if (this.gold + gold > max) {
            this.gold = max;
            return;
        }

        this.gold += gold;

    }

    public void addHe3(int he3, int max) {

        if (this.he3 + he3 > max) {
            this.he3 = max;
            return;
        }

        this.he3 += he3;

    }

    public void addMetal(int metal, int max) {

        if (this.metal + metal > max) {
            this.metal = max;
            return;
        }

        this.metal += metal;

    }

}
