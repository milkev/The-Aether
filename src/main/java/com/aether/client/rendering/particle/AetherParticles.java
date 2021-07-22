package com.aether.client.rendering.particle;

import com.aether.Aether;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;

public class AetherParticles {

    public static final SimpleParticleType GOLDEN_OAK_LEAF, FALLING_ORANGE_PETAL;

    static {
        GOLDEN_OAK_LEAF = Registry.register(Registry.PARTICLE_TYPE, Aether.locate("golden_leaf"), new SimpleParticleType(true));
        FALLING_ORANGE_PETAL  = Registry.register(Registry.PARTICLE_TYPE, Aether.locate("falling_orange_petal"), new SimpleParticleType(true));
    }
}
