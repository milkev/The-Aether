package com.aether.items.food;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class AetherFood {
    public static final FoodProperties GENERIC;
    public static final FoodProperties GUMMY_SWET;
    public static final FoodProperties BLUEBERRY, ENCHANTED_BLUEBERRY, ORANGE;
    public static final FoodProperties WHITE_APPLE;
    public static final FoodProperties HEALING_STONE;
    public static final FoodProperties MILK;

    static {
        GENERIC = new FoodProperties.Builder().nutrition(2).saturationMod(1.5F).build();
        GUMMY_SWET = new FoodProperties.Builder().nutrition(8).saturationMod(0.5F).build();
        BLUEBERRY = new FoodProperties.Builder().nutrition(2).saturationMod(0.5F).fast().build();
        ENCHANTED_BLUEBERRY = new FoodProperties.Builder().nutrition(8).saturationMod(1.0F).fast().build();
        ORANGE = new FoodProperties.Builder().nutrition(5).saturationMod(0.8F).fast().build();
        WHITE_APPLE = new FoodProperties.Builder().saturationMod(5.0F).alwaysEat().build();
        HEALING_STONE = new FoodProperties.Builder().saturationMod(2.5F).alwaysEat().fast().effect(new MobEffectInstance(MobEffects.REGENERATION, 610, 0), 1.0F).build();
        MILK = new FoodProperties.Builder().nutrition(12).saturationMod(2F).fast().alwaysEat().effect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 2), 1F).effect(new MobEffectInstance(MobEffects.ABSORPTION, 3600, 4), 1F).effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3600, 1), 1F).build();
    }
}