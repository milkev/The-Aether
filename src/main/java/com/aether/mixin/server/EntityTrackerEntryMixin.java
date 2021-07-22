package com.aether.mixin.server;

import com.aether.entities.AetherEntityExtensions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Mixin(ServerEntity.class)
public class EntityTrackerEntryMixin {

    private boolean flipped = false;
    private int gravFlippedTime = 0;
    @Final
    @Shadow
    private
    Entity entity;

    @Inject(method = "sendPairingData", at = @At("HEAD"))
    private void sendPackets(Consumer<Packet<?>> sender, CallbackInfo ci){
        if (this.entity instanceof LivingEntity) {
            this.flipped = ((AetherEntityExtensions)this.entity).getFlipped();
            this.gravFlippedTime = ((AetherEntityExtensions)this.entity).getFlipTime();
        }
    }
}
