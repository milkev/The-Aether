package com.aether.client.rendering.entity;

import com.aether.Aether;
import com.aether.client.model.entity.ChestMimicModel;
import com.aether.client.rendering.entity.layer.AetherModelLayers;
import com.aether.entities.hostile.ChestMimicEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ChestMimicRenderer extends MobRenderer<ChestMimicEntity, ChestMimicModel> {

    private static final ResourceLocation TEXTURE_HEAD = Aether.locate("textures/entity/mimic/mimic_head.png");
    private static final ResourceLocation TEXTURE_LEGS = Aether.locate("textures/entity/mimic/mimic_legs.png");
    private static final ResourceLocation TEXTURE_HEAD_XMAS = Aether.locate("textures/entity/mimic/mimic_head_christmas.png");
    private static final ResourceLocation TEXTURE_LEGS_XMAS = Aether.locate("textures/entity/mimic/mimic_legs_christmas.png");

    public ChestMimicRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ChestMimicModel(renderManager.bakeLayer(AetherModelLayers.MIMIC)), 0.0F);
    }

    @Override
    public void render(ChestMimicEntity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
        matrices.pushPose();
        matrices.translate(entity.getX(), entity.getY(), entity.getZ());
        // TODO: Fix rotate call (1.17)
        //GlStateManager.rotatef(180.0F - entity.getPitch(), 0.0F, 1.0F, 0.0F);
        matrices.scale(-1.0F, -1.0F, 1.0F);

        this.model.setupAnim(entity, 0, 0F, 0.0F, 0.0F, 0.0F);

        // TODO: FIXME Please!
        /*Calendar calendar = Calendar.getInstance();
        if (calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26)
        {
            this.bindTexture(TEXTURE_HEAD_XMAS);
            this.modelbase.renderHead(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, mimic);

            this.bindTexture(TEXTURE_LEGS_XMAS);
            this.modelbase.renderLegs(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, mimic);
        }
        else
        {
            this.bindTexture(TEXTURE_HEAD);
            this.model.render(matrices, null, light, 0);
            this.modelbase.renderHead(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, mimic);
            this.bindTexture(TEXTURE_LEGS);
            this.modelbase.renderLegs(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, mimic);
        }*/

        matrices.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ChestMimicEntity entity) {
        return null;
    }
}
