package com.aether.mixin.client.render;

import com.aether.world.dimension.AetherDimension;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public final class CloudRendererMixin {
    @Final
    @Shadow
    private static ResourceLocation CLOUDS_LOCATION;
    @Shadow
    @NotNull
    private final ClientLevel level;
    @Shadow
    private final int ticks;
    @Final
    @Shadow
    @NotNull
    private final Minecraft minecraft;
    @Shadow
    private int prevCloudX;
    @Shadow
    private int prevCloudY;
    @Shadow
    private int prevCloudZ;
    @Shadow
    @NotNull
    private Vec3 prevCloudColor;
    @Shadow
    @NotNull
    private CloudStatus prevCloudsType;
    @Shadow
    private boolean generateClouds;
    @Shadow
    @NotNull
    private VertexBuffer cloudBuffer;

    public CloudRendererMixin() {
        throw new NullPointerException("null cannot be cast to non-null type net.minecraft.client.world.ClientWorld");
    }

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    public void renderClouds(PoseStack matrices, Matrix4f model, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci) {
        if (level.dimension() == AetherDimension.AETHER_WORLD_KEY) {
            internalCloudRender(matrices, model, tickDelta, cameraX, cameraY, cameraZ, 96, 1f, 1f);
            internalCloudRender(matrices, model, tickDelta, cameraX, cameraY, cameraZ, 32, 1.25f, -2f);
            internalCloudRender(matrices, model, tickDelta, cameraX, cameraY, cameraZ, -128, 2f, 1.5f);
            ci.cancel();
        }
    }

    private void internalCloudRender(PoseStack matrices, Matrix4f model, float tickDelta, double cameraX, double cameraY, double cameraZ, float cloudOffset, float cloudScale, float speedMod) {
        DimensionSpecialEffects properties = this.level.effects();
        float cloudHeight = properties.getCloudHeight();
        if (!Float.isNaN(cloudHeight)) {
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.depthMask(true);
            double speed = ((this.ticks + tickDelta) * (0.03F * speedMod));
            double posX = (cameraX + speed) / 12.0D;
            double posY = cloudHeight - (float) cameraY + cloudOffset;
            double posZ = cameraZ / 12.0D + 0.33000001311302185D;
            posX -= Mth.floor(posX / 2048.0D) * 2048;
            posZ -= Mth.floor(posZ / 2048.0D) * 2048;
            float adjustedX = (float) (posX - (double) Mth.floor(posX));
            float adjustedY = (float) (posY / 4.0D - (double) Mth.floor(posY / 4.0D)) * 4.0F;
            float adjustedZ = (float) (posZ - (double) Mth.floor(posZ));
            Vec3 cloudColor = this.level.getCloudColor(tickDelta);
            int floorX = (int) Math.floor(posX);
            int floorY = (int) Math.floor(posY / 4.0D);
            int floorZ = (int) Math.floor(posZ);
            if (floorX != this.prevCloudX || floorY != this.prevCloudY || floorZ != this.prevCloudZ || this.minecraft.options.getCloudsType() != this.prevCloudsType || this.prevCloudColor.distanceToSqr(cloudColor) > 2.0E-4D) {
                this.prevCloudX = floorX;
                this.prevCloudY = floorY;
                this.prevCloudZ = floorZ;
                this.prevCloudColor = cloudColor;
                this.prevCloudsType = this.minecraft.options.getCloudsType();
                this.generateClouds = true;
            }

            if (this.generateClouds) {
                this.generateClouds = false;
                Tesselator tessellator = Tesselator.getInstance();
                BufferBuilder bufferBuilder = tessellator.getBuilder();
                if (this.cloudBuffer != null) this.cloudBuffer.close();

                this.cloudBuffer = new VertexBuffer();
                this.buildClouds(bufferBuilder, posX, posY, posZ, cloudColor);
                bufferBuilder.end();
                this.cloudBuffer.upload(bufferBuilder);
            }

            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
            RenderSystem.setShaderTexture(0, CLOUDS_LOCATION);
            FogRenderer.levelFogColor();
            matrices.pushPose();
            matrices.scale(12.0F, 1.0F, 12.0F);
            matrices.scale(cloudScale, cloudScale, cloudScale);
            matrices.translate(-adjustedX, adjustedY, -adjustedZ);
            if (this.cloudBuffer != null) {
                int cloudMainIndex = this.prevCloudsType == CloudStatus.FANCY ? 0 : 1;

                for (int cloudIndex = 1; cloudMainIndex <= cloudIndex; ++cloudMainIndex) {
                    if (cloudMainIndex == 0) {
                        RenderSystem.colorMask(false, false, false, false);
                    } else {
                        RenderSystem.colorMask(true, true, true, true);
                    }

                    ShaderInstance shader = RenderSystem.getShader();
                    this.cloudBuffer.drawWithShader(matrices.last().pose(), model, shader);
                }
            }

            matrices.popPose();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
    }

    @Shadow
    private void buildClouds(BufferBuilder builder, double x, double y, double z, Vec3 color) {
    }
}