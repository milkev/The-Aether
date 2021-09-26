package com.aether.client.rendering.entity;

import com.aether.Aether;
import com.aether.client.model.entity.AechorPlantModel;
import com.aether.client.rendering.entity.layer.AetherModelLayers;
import com.aether.entities.hostile.AechorPlantEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class AechorPlantRenderer extends MobEntityRenderer<AechorPlantEntity, AechorPlantModel> {
    private static final Identifier TEXTURE = Aether.locate("textures/entity/aechor_plant.png");

    public AechorPlantRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new AechorPlantModel(renderManager.getPart(AetherModelLayers.AECHOR_PLANT)), 0.3F);
    }

    @Override
    public AechorPlantModel getModel() {
        return super.getModel();
    }

    @Override
    public Identifier getTexture(AechorPlantEntity entity) {
        return TEXTURE;
    }

}
