package com.aether.mixin.client;

import com.aether.blocks.aercloud.BaseAercloudBlock;
import com.aether.blocks.aercloud.DenseAercloudFluid;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenEffectRenderer.class)
@Environment(EnvType.CLIENT)
public abstract class InGameOverlayRendererMixin {

    @Shadow @Nullable protected static BlockState getViewBlockingState(Player player) { return null; }

    @Inject(method = "renderTex", at = @At("HEAD"), cancellable = true)
    private static void renderAercloudOverlay(TextureAtlasSprite sprite, PoseStack matrices, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        BlockState overlayState = getViewBlockingState(client.player);
        if(overlayState != null && overlayState.getBlock() instanceof BaseAercloudBlock) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.enableTexture();
            RenderSystem.setShaderTexture(0, new ResourceLocation("the_aether","textures/block/" + Registry.BLOCK.getKey(overlayState.getBlock()).getPath() + ".png"));
            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            float f = client.player.getBrightness();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(f, f, f, 0.775F);
            float yaw = client.player.getYRot() / 192.0F;
            float pitch = client.player.getXRot() / 192.0F;
            Matrix4f matrix4f = matrices.last().pose();
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferBuilder.vertex(matrix4f, -1.0F, -1.0F, -0.5F).uv(1.0F - yaw, 1.0F + pitch).endVertex();
            bufferBuilder.vertex(matrix4f, 1.0F, -1.0F, -0.5F).uv(0.0F - yaw, 1.0F + pitch).endVertex();
            bufferBuilder.vertex(matrix4f, 1.0F, 1.0F, -0.5F).uv(0.0F - yaw, 0.0F + pitch).endVertex();
            bufferBuilder.vertex(matrix4f, -1.0F, 1.0F, -0.5F).uv(1.0F - yaw, 0.0F + pitch).endVertex();
            bufferBuilder.end();
            BufferUploader.end(bufferBuilder);
            RenderSystem.disableBlend();
            ci.cancel();
        }
    }

    @Inject(method = "getViewBlockingState", at = @At("HEAD"), cancellable = true)
    private static void getInWallBlockState(Player player, CallbackInfoReturnable<BlockState> cir) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for(int i = 0; i < 8; ++i) {
            double d = player.getX() + (double)(((float)((i) % 2) - 0.5F) * player.getBbWidth() * 0.8F);
            double e = player.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F);
            double f = player.getZ() + (double)(((float)((i >> 2) % 2) - 0.5F) * player.getBbWidth() * 0.8F);
            mutable.set(d, e, f);
            BlockState blockState = player.level.getBlockState(mutable);
            if (blockState.getBlock() instanceof BaseAercloudBlock) {
                cir.setReturnValue(blockState);
                cir.cancel();
            }
        }
    }

    @Inject(method = "renderWater", at = @At("HEAD"), cancellable = true)
    private static void renderDenseAercloudOverlay(Minecraft client, PoseStack matrices, CallbackInfo ci){
        BlockPos pos = new BlockPos(client.player.getEyePosition());
        Level world = client.player.level;
        if(world.getFluidState(pos).getType() instanceof DenseAercloudFluid){
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.enableTexture();
            RenderSystem.setShaderTexture(0, new ResourceLocation("the_aether","textures/block/dense_aercloud_still.png"));
            BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
            float f = client.player.getBrightness();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(f, f, f, 0.8F);
            float m = -client.player.getYRot() / 64.0F;
            float n = client.player.getXRot() / 64.0F;
            Matrix4f matrix4f = matrices.last().pose();
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferBuilder.vertex(matrix4f, -1.0F, -1.0F, -0.5F).uv(4.0F + m, 4.0F + n).endVertex();
            bufferBuilder.vertex(matrix4f, 1.0F, -1.0F, -0.5F).uv(0.0F + m, 4.0F + n).endVertex();
            bufferBuilder.vertex(matrix4f, 1.0F, 1.0F, -0.5F).uv(0.0F + m, 0.0F + n).endVertex();
            bufferBuilder.vertex(matrix4f, -1.0F, 1.0F, -0.5F).uv(4.0F + m, 0.0F + n).endVertex();
            bufferBuilder.end();
            BufferUploader.end(bufferBuilder);
            RenderSystem.disableBlend();
            ci.cancel();
        }
    }
}
