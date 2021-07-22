package com.aether.util;

import net.minecraft.world.damagesource.DamageSource;

public class AetherDamageSources extends DamageSource {

    protected AetherDamageSources(String name) {
        super(name);
    }

    public static final DamageSource AETHER_FALL = (new AetherDamageSources("aether_fall")).bypassArmor().setIsFall();
}
