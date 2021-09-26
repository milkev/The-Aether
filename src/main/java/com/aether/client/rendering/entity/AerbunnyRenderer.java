package com.aether.client.rendering.entity;

import com.aether.Aether;
import com.aether.client.model.entity.AerbunnyModel;
import com.aether.client.rendering.entity.layer.AetherModelLayers;
import com.aether.entities.passive.AerbunnyEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class AerbunnyRenderer extends MobEntityRenderer<AerbunnyEntity, AerbunnyModel> {

    private static final Identifier TEXTURE = Aether.locate("textures/entity/aerbunny.png");

    public AerbunnyRenderer(EntityRendererFactory.Context context) {
        super(context, new AerbunnyModel(context.getPart(AetherModelLayers.AERBUNNY)), 0.3F);
    }

    @Override
    public AerbunnyModel getModel() {
        return super.getModel();
    }

    @Override
    protected void setupTransforms(AerbunnyEntity entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta) {
        super.setupTransforms(entity, matrices, animationProgress, bodyYaw, tickDelta);
        if(entity.isBaby())
            matrices.scale(0.6F, 0.6F, 0.6F);
    }

    @Override
    public Identifier getTexture(AerbunnyEntity entity) {
        return TEXTURE;
    }
}
