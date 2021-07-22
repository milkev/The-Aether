package com.aether.mixin.server;

import com.aether.entities.AetherEntityExtensions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Entity implements AetherEntityExtensions {
    public ServerPlayerEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }
    
    private boolean flipped = false;

    private int gravFlipTime;

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci){
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