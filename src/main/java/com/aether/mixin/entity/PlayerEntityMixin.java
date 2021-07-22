package com.aether.mixin.entity;

import com.aether.entities.AetherEntityExtensions;
import com.aether.util.AetherDamageSources;
import com.aether.util.CustomStatusEffectInstance;
import com.aether.world.dimension.AetherDimension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends Entity implements AetherEntityExtensions {

    @Shadow public abstract void awardStat(ResourceLocation stat, int amount);

    @Shadow @Final private Abilities abilities;

    public PlayerEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    private boolean flipped = false;
    private boolean aetherFallen = false;

    private int gravFlipTime;

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.isBypassInvul() && getY() < level.getMinBuildHeight() - 1 && level.dimension() == AetherDimension.AETHER_WORLD_KEY) {
            if (!level.isClientSide()) {
                setAetherFallen(true);
                ((ServerPlayer) (Object) this).teleportTo(getServer().getLevel(Level.OVERWORLD), this.getX() * 16, level.getMaxBuildHeight(), this.getZ() * 16, this.getYRot(), this.getXRot());
                CustomStatusEffectInstance ef = new CustomStatusEffectInstance(MobEffect.byId(9), 160, 2);
                ef.ShowParticles = false;
                ((ServerPlayer) (Object) this).addEffect(ef);
            }
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    public void onDeath(DamageSource source, CallbackInfo ci) {
    }

    @Override
    public void setAetherFallen(boolean aetherFallen) {
        this.aetherFallen = aetherFallen;
    }

    @Override
    public boolean isAetherFallen() {
        return aetherFallen;
    }

    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    public void handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if(isAetherFallen()) {

            aetherFallen = false;

            if (abilities.mayfly) {
                cir.setReturnValue(false);
            } else {
                if (fallDistance >= 2.0F) {
                    awardStat(Stats.FALL_ONE_CM, (int)Math.round((double)fallDistance * 100.0D));
                }
                cir.setReturnValue(super.causeFallDamage(fallDistance, damageMultiplier, AetherDamageSources.AETHER_FALL));
            }
            cir.cancel();
        }


    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci){
        if(flipped){
            gravFlipTime++;
            if(gravFlipTime > 20){
                flipped = false;
                this.fallDistance = 0;
            }
            if(!this.isNoGravity()) {
                Vec3 antiGravity = new Vec3(0, 0.12D, 0);
                this.setDeltaMovement(this.getDeltaMovement().add(antiGravity));
            }
        }
    }
}