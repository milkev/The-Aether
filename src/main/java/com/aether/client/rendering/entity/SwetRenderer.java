package com.aether.client.rendering.entity;

import com.aether.Aether;
import com.aether.entities.hostile.BlueSwetEntity;
import com.aether.entities.hostile.GoldenSwetEntity;
import com.aether.entities.hostile.PurpleSwetEntity;
import com.aether.entities.hostile.SwetEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SwetRenderer extends MobRenderer<SwetEntity, SlimeModel<SwetEntity>> {

    public SwetRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SlimeModel<>(renderManager.bakeLayer(ModelLayers.SLIME)), 0.25F);
        this.addLayer(new SlimeOuterLayer<>(this, renderManager.getModelSet()));
    }

    public void render(SwetEntity slimeEntity, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i) {
        this.shadowRadius = 0.25F * (float)slimeEntity.getSize();
        super.render(slimeEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }

    protected void scale(SwetEntity slimeEntity, PoseStack matrixStack, float f) {
        float g = 0.999F;
        matrixStack.scale(0.999F, 0.999F, 0.999F);
        matrixStack.translate(0.0D, 0.0010000000474974513D, 0.0D);
        float h = (float)slimeEntity.getSize();
        float i = Mth.lerp(f, slimeEntity.oSquish, slimeEntity.squish) / (h * 0.5F + 1.0F);
        float j = 1.0F / (i + 1.0F);
        matrixStack.scale(j * h, 1.0F / j * h, j * h);
    }

    public ResourceLocation getTextureLocation(SwetEntity slimeEntity) {
        if (slimeEntity instanceof BlueSwetEntity)
            return Aether.locate("textures/entity/swet/blue_swet.png");
        if (slimeEntity instanceof PurpleSwetEntity)
            return Aether.locate("textures/entity/swet/purple_swet.png");
        if (slimeEntity instanceof GoldenSwetEntity)
            return Aether.locate("textures/entity/swet/golden_swet.png");
        return Aether.locate("textures/entity/swet/white_swet.png");
    }
}
