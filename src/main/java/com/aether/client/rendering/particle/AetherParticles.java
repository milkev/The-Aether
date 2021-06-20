package com.aether.client.rendering.particle;

import com.aether.Aether;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;

public class AetherParticles {

    public static final SimpleParticleType GOLDEN_OAK_LEAF;

    static {
        GOLDEN_OAK_LEAF = Registry.register(Registry.PARTICLE_TYPE, Aether.locate("golden_leaf"), new SimpleParticleType(true));
    }
}
