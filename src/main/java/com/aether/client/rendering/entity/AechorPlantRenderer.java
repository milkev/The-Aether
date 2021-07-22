package com.aether.client.rendering.entity;

import com.aether.Aether;
import com.aether.client.model.entity.AechorPlantModel;
import com.aether.client.rendering.entity.layer.AetherModelLayers;
import com.aether.entities.hostile.AechorPlantEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AechorPlantRenderer extends MobRenderer<AechorPlantEntity, AechorPlantModel> {
    private static final ResourceLocation TEXTURE = Aether.locate("textures/entity/aechor_plant.png");

    public AechorPlantRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AechorPlantModel(renderManager.bakeLayer(AetherModelLayers.AECHOR_PLANT)), 0.3F);
    }

    @Override
    public AechorPlantModel getModel() {
        return super.getModel();
    }

    @Override
    public ResourceLocation getTextureLocation(AechorPlantEntity entity) {
        return TEXTURE;
    }

}
