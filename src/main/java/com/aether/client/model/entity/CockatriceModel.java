package com.aether.client.model.entity;

import com.aether.entities.hostile.CockatriceEntity;
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

public class CockatriceModel extends EntityModel<CockatriceEntity> {

    public final ModelPart head, body;
    public final ModelPart legs, legs2;
    public final ModelPart wings, wings2;
    public final ModelPart jaw, neck;
    public final ModelPart feather1, feather2, feather3;

    public CockatriceModel(ModelPart root) {
        this.head = root.getChild("head");
        this.jaw = root.getChild("jaw");
        this.body = root.getChild("body");
        this.legs = root.getChild("legs");
        this.legs2 = root.getChild("legs2");
        this.wings = root.getChild("wings");
        this.wings2 = root.getChild("wings2");
        this.neck = root.getChild("neck");
        this.feather1 = root.getChild("feather1");
        this.feather2 = root.getChild("feather2");
        this.feather3 = root.getChild("feather3");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        modelPartData.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 13).addBox(-2.0F, -4.0F, -6.0F, 4, 4, 8), PartPose.offset(0.0F, (float) (-8 + 16), -4.0F));
        modelPartData.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(24, 13).addBox(-2.0F, -1.0F, -6.0F, 4, 1, 8), PartPose.offset(0.0F, (float) (-8 + 16), -4.0F));
        modelPartData.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, 0.0F, 6, 8, 5), PartPose.offset(0.0F, (float) (16), 0.0F));
        modelPartData.addOrReplaceChild("legs", CubeListBuilder.create().texOffs(22, 0).addBox(-1.0F, -1.0F, -1.0F, 2, 9, 2), PartPose.offset(-2.0F, (float) (16), 1.0F));
        modelPartData.addOrReplaceChild("legs2", CubeListBuilder.create().texOffs(22, 0).addBox(-1.0F, -1.0F, -1.0F, 2, 9, 2), PartPose.offset(2.0F, (float) (16), 1.0F));
        modelPartData.addOrReplaceChild("wings", CubeListBuilder.create().texOffs(52, 0).addBox(-1.0F, -0.0F, -1.0F, 1, 8, 4), PartPose.offset(-3.0F, (float) (16), 2.0F));
        modelPartData.addOrReplaceChild("wings2", CubeListBuilder.create().texOffs(52, 0).addBox(0.0F, -0.0F, -1.0F, 1, 8, 4), PartPose.offset(3.0F, (float) (-4 + 16), 0.0F));
        modelPartData.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(44, 0).addBox(-1.0F, -6.0F, -1.0F, 2, 6, 2), PartPose.offset(0.0F, (float) (-2 + 16), -4.0F));
        modelPartData.addOrReplaceChild("feather1", CubeListBuilder.create().texOffs(30, 0).addBox(-1.0F, -5.0F, 5.0F, 2, 1, 5), PartPose.offset(0.0F, (float) (1 + 16) + 0.5F, 1.0F));
        modelPartData.addOrReplaceChild("feather2", CubeListBuilder.create().texOffs(30, 0).addBox(-1.0F, -5.0F, 5.0F, 2, 1, 5), PartPose.offset(0.0F, (float) (1 + 16) + 0.5F, 1.0F));
        modelPartData.addOrReplaceChild("feather3", CubeListBuilder.create().texOffs(30, 0).addBox(-1.0F, -5.0F, 5.0F, 2, 1, 5), PartPose.offset(0.0F, (float) (1 + 16) + 0.5F, 1.0F));

        return LayerDefinition.create(modelData,64,64);
    }

    @Override
    public void renderToBuffer(PoseStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        this.head.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        this.jaw.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        this.body.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        this.legs.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        this.legs2.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        this.wings.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        this.wings2.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        this.neck.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        this.feather1.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        this.feather2.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        this.feather3.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
    }

    @Override
    public void setupAnim(CockatriceEntity cockatrice, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.xRot = headPitch / 57.29578F;
        this.head.yRot = netHeadYaw / 57.29578F;
        this.jaw.xRot = this.head.xRot;
        this.jaw.yRot = this.head.yRot;
        this.body.xRot = 1.570796F;
        this.legs.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.legs2.xRot = Mth.cos((float) (limbSwing * 0.6662F + Math.PI)) * 1.4F * limbSwingAmount;

        if (ageInTicks > 0.001F) {
            this.wings.z = -1F;
            this.wings2.z = -1F;
            this.wings.y = 12F;
            this.wings2.y = 12F;
            this.wings.xRot = 0.0F;
            this.wings2.xRot = 0.0F;
            this.wings.zRot = ageInTicks;
            this.wings2.zRot = -ageInTicks;
            this.legs.xRot = 0.6F;
            this.legs2.xRot = 0.6F;
        } else {
            this.wings.z = -3F;
            this.wings2.z = -3F;
            this.wings.y = 14F;
            this.wings2.y = 14F;
            this.wings.xRot = (float) (Math.PI / 2.0F);
            this.wings2.xRot = (float) (Math.PI / 2.0F);
            this.wings.zRot = 0.0F;
            this.wings2.zRot = 0.0F;
        }

        this.feather1.yRot = -0.375F;
        this.feather2.yRot = 0.0F;
        this.feather3.yRot = 0.375F;
        this.feather1.xRot = 0.25F;
        this.feather2.xRot = 0.25F;
        this.feather3.xRot = 0.25F;
        this.neck.xRot = 0.0F;
        this.neck.yRot = head.yRot;
        this.jaw.xRot += 0.35F;
    }
}
