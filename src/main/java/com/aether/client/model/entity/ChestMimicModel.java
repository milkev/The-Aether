package com.aether.client.model.entity;

import com.aether.entities.hostile.ChestMimicEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ChestMimicModel extends EntityModel<ChestMimicEntity> {

    public final ModelPart box, boxLid, leftLeg, rightLeg;

    public ChestMimicModel(ModelPart root) {
        this.box = root.getChild("box");
        this.boxLid = root.getChild("boxLid");
        this.leftLeg = root.getChild("leftLeg");
        this.rightLeg = root.getChild("rightLeg");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        modelPartData.addOrReplaceChild("box", CubeListBuilder.create().texOffs(0, 0).addBox(-8F, 0F, -8F, 16, 10, 16), PartPose.offset(0F, -24F, 0F));
        modelPartData.addOrReplaceChild("boxLid", CubeListBuilder.create().texOffs(16, 10).addBox(0F, 0F, 0F, 16, 6, 16), PartPose.offset(-8F, -24F, 8F));
        modelPartData.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(0, 0).addBox(-3F, 0F, -3F, 6, 15, 6), PartPose.offset(-4F, -15F, 0F));
        modelPartData.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(0, 0).addBox(-3F, 0F, -3F, 6, 15, 6), PartPose.offset(4F, -15F, 0F));

        return LayerDefinition.create(modelData,64,64);
    }

    @Override
    public void setupAnim(ChestMimicEntity entityIn, float f, float f1, float f2, float f3, float f4) {
        this.boxLid.x = 3.14159265F - entityIn.mouth;
        this.rightLeg.x = entityIn.legs;
        this.leftLeg.x = -entityIn.legs;
    }

    @Override
    public void renderToBuffer(PoseStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        box.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        boxLid.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        leftLeg.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
        rightLeg.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
    }
}