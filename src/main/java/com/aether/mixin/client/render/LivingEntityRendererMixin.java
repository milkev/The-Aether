package com.aether.mixin.client.render;

import com.aether.entities.AetherEntityExtensions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Inject(method = "setupRotations", at = @At("TAIL"))
    private void setupTransforms(LivingEntity entity, PoseStack matrices, float animationProgress, float bodyYaw, float tickDelta, CallbackInfo ci){
        if(((AetherEntityExtensions)entity).getFlipped()) {
            matrices.translate(0.0D, (double) (entity.getBbHeight() + 0.1F), 0.0D);
            matrices.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
        }
    }
}
