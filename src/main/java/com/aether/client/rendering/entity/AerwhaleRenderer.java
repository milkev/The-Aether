package com.aether.client.rendering.entity;

import com.aether.Aether;
import com.aether.client.model.entity.AerwhaleModel;
import com.aether.client.rendering.entity.layer.AetherModelLayers;
import com.aether.entities.passive.AerwhaleEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AerwhaleRenderer extends MobRenderer<AerwhaleEntity, AerwhaleModel> {

    private static final ResourceLocation TEXTURE = Aether.locate("textures/entity/aerwhale/aerwhale.png");

    public AerwhaleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AerwhaleModel(renderManager.bakeLayer(AetherModelLayers.AERWHALE)), 0.3F);
    }

    @Override
    public AerwhaleModel getModel() {
        return super.getModel();
    }

    @Override
    public ResourceLocation getTextureLocation(AerwhaleEntity entity) {
        return TEXTURE;
    }

}
