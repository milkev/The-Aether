package com.aether.mixin.client.render;

import com.aether.client.rendering.particle.AetherParticles;
import com.aether.client.rendering.particle.GoldenOakLeafParticle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class ParticleManagerMixin {

    @Shadow
    protected abstract <T extends ParticleOptions> void register(ParticleType<T> type, ParticleEngine.SpriteParticleRegistration<T> factory);

    @Inject(method = "registerProviders", at = @At("TAIL"))
    private void extendDefaults(CallbackInfo ci) {
        this.register(AetherParticles.GOLDEN_OAK_LEAF, GoldenOakLeafParticle.DefaultFactory::new);
    }
}
