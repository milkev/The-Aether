// Made with Model Converter by Globox_Z
// Generate all required imports
// Made with Blockbench 3.8.3
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.aether.client.model.entity;
import com.aether.entities.passive.AerbunnyEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
public class AerbunnyModel extends EntityModel<AerbunnyEntity> {
    private final ModelPart body;
    private final ModelPart fluff;
    private final ModelPart head;
    private final ModelPart tail;
    private final ModelPart left_front_leg;
    private final ModelPart right_front_leg;
    private final ModelPart back_right_leg;
    private final ModelPart back_left_leg;

    private float fluff_scale = 1;

    public AerbunnyModel(ModelPart root) {
        this.body = root.getChild("body");
        this.tail = this.body.getChild("tail");
        this.back_left_leg = this.body.getChild("back_left_leg");
        this.back_right_leg = this.body.getChild("back_right_leg");
        this.right_front_leg = this.body.getChild("right_front_leg");
        this.left_front_leg = this.body.getChild("left_front_leg");
        this.head = this.body.getChild("head");
        this.fluff = root.getChild("fluff");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition modelPartData1 = modelPartData.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F,22.8889F,1.0F));
        modelPartData.addOrReplaceChild("fluff", CubeListBuilder.create().texOffs(0,0).addBox(-4.0F, -3.5F, -3.5F, 8.0F, 7.0F, 7.0F), PartPose.offset(0.0F,0F,0F));
        modelPartData1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(18,26).addBox(-2.0F, -1.5F, -3.0F, 4.0F, 3.0F, 3.0F).texOffs(0,0).addBox(-2.0F, -5.5F, -2.0F, 1.0F, 4.0F, 1.0F).texOffs(0,0).addBox(1.0F, -5.5F, -2.0F, 1.0F, 4.0F, 1.0F).texOffs(0,14).addBox(-4.0F, -0.5F, -2.0F, 2.0F, 2.0F, 0.0F).texOffs(0,14).addBox(2.0F, -0.5F, -2.0F, 2.0F, 2.0F, 0.0F, true), PartPose.offset(0.0F, -2.3889F, -4.5F));
        modelPartData1.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(0,21).addBox(-1.0F, -1.0F, -2.25F, 2.0F, 1.0F, 3.0F), PartPose.offset(-4.0F,1.1111F,-3.25F));
        modelPartData1.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(0,21).addBox(-1.0F, -1.0F, -2.5F, 2.0F, 1.0F, 3.0F), PartPose.offset(4.0F,1.1111F,-3.0F));
        modelPartData1.addOrReplaceChild("back_right_leg", CubeListBuilder.create().texOffs(10,20).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 1.0F, 4.0F).texOffs(22,20).addBox(-1.0F, -2.0F, -2.0F, 2.0F, 2.0F, 3.0F), PartPose.offset(4.0F,0.1111F,2.5F));
        modelPartData1.addOrReplaceChild("back_left_leg", CubeListBuilder.create().texOffs(22,20).addBox(-1.0F, -2.0F, -2.0F, 2.0F, 2.0F, 3.0F).texOffs(10,20).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 1.0F, 4.0F), PartPose.offset(-4.0F,0.1111F,2.5F));
        modelPartData1.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0,25).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 3.0F), PartPose.offset(0.0F,-2.8889F,2.5F));
        return LayerDefinition.create(modelData,32,32);
    }

    @Override
    public void setupAnim(AerbunnyEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        head.xRot = headPitch * 0.017453F;
        head.yRot = netHeadYaw * 0.017453292F;
        back_right_leg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        back_left_leg.xRot = Mth.cos(limbSwing * 0.6662F + 3.1415927F) * 1.4F * limbSwingAmount;
        right_front_leg.xRot = Mth.cos(limbSwing * 0.6662F + 3.1415927F) * 1.4F * limbSwingAmount;
        left_front_leg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        float targetFloof = entity.getPuffiness() / 2F;
        if (entity.floof < targetFloof) {
            entity.floof += 0.025F;
        } else if (entity.floof > targetFloof) {
            entity.floof -= 0.025F;
        }
        if (Math.abs(targetFloof - entity.floof) <= 0.03)
            entity.floof = targetFloof;

        fluff_scale = entity.floof + 1;
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        body.render(matrixStack, buffer, packedLight, packedOverlay);
        matrixStack.translate(0, 1.28, 0);
        matrixStack.scale(fluff_scale, fluff_scale, fluff_scale);
        fluff.render(matrixStack, buffer, packedLight, packedOverlay);
    }
}