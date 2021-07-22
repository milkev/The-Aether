package com.aether.client.rendering.entity;

import com.aether.Aether;
import com.aether.client.model.entity.AerbunnyModel;
import com.aether.client.rendering.entity.layer.AetherModelLayers;
import com.aether.entities.passive.AerbunnyEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AerbunnyRenderer extends MobRenderer<AerbunnyEntity, AerbunnyModel> {

    private static final ResourceLocation TEXTURE = Aether.locate("textures/entity/aerbunny.png");

    public AerbunnyRenderer(EntityRendererProvider.Context context) {
        super(context, new AerbunnyModel(context.bakeLayer(AetherModelLayers.AERBUNNY)), 0.3F);
    }

    @Override
    public AerbunnyModel getModel() {
        return super.getModel();
    }

    @Override
    protected void setupRotations(AerbunnyEntity entity, PoseStack matrices, float animationProgress, float bodyYaw, float tickDelta) {
        super.setupRotations(entity, matrices, animationProgress, bodyYaw, tickDelta);
        if(entity.isBaby())
            matrices.scale(0.6F, 0.6F, 0.6F);
    }

    @Override
    public ResourceLocation getTextureLocation(AerbunnyEntity entity) {
        return TEXTURE;
    }
}
