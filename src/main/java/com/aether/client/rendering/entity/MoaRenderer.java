package com.aether.client.rendering.entity;

import com.aether.client.model.entity.MoaModel;
import com.aether.client.rendering.entity.layer.AetherModelLayers;
import com.aether.entities.passive.MoaEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MoaRenderer extends MobRenderer<MoaEntity, MoaModel> {

    public MoaRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MoaModel(renderManager.bakeLayer(AetherModelLayers.MOA)), 0.7f);
    }

    @Override
    protected void scale(MoaEntity moa, PoseStack matrixStack, float partialTicks) {
        float moaScale = moa.isBaby() ? 0.3334F : 1.0F;
        matrixStack.scale(moaScale, moaScale, moaScale);
    }

    @Override
    public void render(MoaEntity moa, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i) {
        super.render(moa, f, g, matrixStack, vertexConsumerProvider, moa.getGenes().getRace().glowing() ? LightTexture.FULL_BRIGHT : i);
    }

    @Override
    public ResourceLocation getTextureLocation(MoaEntity entity) {
        return entity.getGenes().getTexture();
    }
}
