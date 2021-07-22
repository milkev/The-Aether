package com.aether.mixin.client.render;

import com.aether.world.dimension.AetherDimension;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LiquidBlockRenderer.class)
@Environment(EnvType.CLIENT)
public class FluidRendererMixin {

    @Unique
    private float fadeAlpha;

    @Inject(method = "tesselate", at = @At("HEAD"))
    private void render(BlockAndTintGetter world, BlockPos pos, VertexConsumer builder, FluidState state, CallbackInfoReturnable<Boolean> info) {
        fadeAlpha = 1F;
        if (state.getType().isSame(Fluids.WATER)) {
            if (Minecraft.getInstance().level.dimension() == AetherDimension.AETHER_WORLD_KEY) {
                fadeAlpha = Math.min((pos.getY() - world.getMinBuildHeight()) / 32F, 1);
            }

        }
    }

    @ModifyArg(
            method = "vertex",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;color(FFFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"
            ),
            index = 3
    )
    private float adjustAlphaForUplandsFadeOut(float a) {
        return fadeAlpha;
    }
}
