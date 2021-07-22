package com.aether.client.rendering.entity;

import com.aether.Aether;
import com.aether.client.model.entity.CockatriceModel;
import com.aether.client.rendering.entity.layer.AetherModelLayers;
import com.aether.entities.hostile.CockatriceEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class CockatriceRenderer extends MobRenderer<CockatriceEntity, CockatriceModel> {

    private static final ResourceLocation TEXTURE = Aether.locate("textures/entity/cockatrice/cockatrice.png");

    public CockatriceRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CockatriceModel(renderManager.bakeLayer(AetherModelLayers.COCKATRICE)), 1.0F);
    }

    @Override
    protected float getBob(CockatriceEntity cockatrice, float f) {
        float f1 = cockatrice.prevWingRotation + (cockatrice.wingRotation - cockatrice.prevWingRotation) * f;
        float f2 = cockatrice.prevDestPos + (cockatrice.destPos - cockatrice.prevDestPos) * f;

        return (Mth.sin(f1) + 1.0F) * f2;
    }

    @Override
    protected void scale(CockatriceEntity cockatrice, PoseStack matrices, float f) {
        matrices.scale(1.8F, 1.8F, 1.8F);
    }

    @Override
    public ResourceLocation getTextureLocation(CockatriceEntity cockatrice) {
        return TEXTURE;
    }
}
