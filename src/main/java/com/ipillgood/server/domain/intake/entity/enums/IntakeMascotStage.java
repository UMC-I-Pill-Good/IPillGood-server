package com.ipillgood.server.domain.intake.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IntakeMascotStage {
    SEED(0, 6, "씨앗", 7),
    SPROUT(7, 14, "새싹", 15),
    FLOWER(15, 29, "꽃", 30),
    FRUIT(30, 30, "열매", 31),
    TREE(31, Integer.MAX_VALUE, "나무", null);

    private final int minDays;
    private final int maxDays;
    private final String label;
    private final Integer nextStageThresholdDays;

    public static IntakeMascotStage fromStreakDays(int streakDays) {
        for (IntakeMascotStage stage : values()) {
            if (streakDays >= stage.minDays && streakDays <= stage.maxDays) {
                return stage;
            }
        }
        return TREE;
    }
}
