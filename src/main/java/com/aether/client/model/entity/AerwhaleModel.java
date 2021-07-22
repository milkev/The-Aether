// Made with Model Converter by Globox_Z
// Generate all required imports
// Made with Blockbench 3.8.3
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.aether.client.model.entity;
import com.aether.entities.passive.AerwhaleEntity;
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
public class AerwhaleModel extends EntityModel<AerwhaleEntity> {
    private final ModelPart aerwhale_head;
    private final ModelPart aerwhale_body1;
    private final ModelPart aerwhale_body2;
    private final ModelPart l_tail;
    private final ModelPart r_tail;
    private final ModelPart l_fin;
    private final ModelPart r_fin;

    public AerwhaleModel(ModelPart root) {
        this.aerwhale_head = root.getChild("aerwhale_head");
        this.r_fin = this.aerwhale_head.getChild("r_fin");
        this.l_fin = this.aerwhale_head.getChild("l_fin");
        this.aerwhale_body1 = this.aerwhale_head.getChild("aerwhale_body1");
        this.aerwhale_body2 = this.aerwhale_body1.getChild("aerwhale_body2");
        this.r_tail = this.aerwhale_body2.getChild("r_tail");
        this.l_tail = this.aerwhale_body2.getChild("l_tail");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition modelPartData1 = modelPartData.addOrReplaceChild("aerwhale_head", CubeListBuilder.create().texOffs(60,0).addBox(-10.5F, -18.0F, -29.0F, 21.0F, 18.0F, 30.0F), PartPose.offset(0.0F,24.0F,0.0F));
        PartDefinition modelPartData2 = modelPartData1.addOrReplaceChild("aerwhale_body1", CubeListBuilder.create().texOffs(0,0).addBox(-7.5F, -7.5F, 0.0F, 15.0F, 15.0F, 15.0F), PartPose.offset(0.0F,-7.5F,-1.0F));
        PartDefinition modelPartData3 = modelPartData2.addOrReplaceChild("aerwhale_body2", CubeListBuilder.create().texOffs(0,30).addBox(-4.5F, -4.5F, 0.0F, 9.0F, 9.0F, 12.0F), PartPose.offset(0.0F,0.0F,13.0F));
        modelPartData3.addOrReplaceChild("l_tail", CubeListBuilder.create().texOffs(0,51).addBox(0.0F, 0.0F, 0.0F, 24.0F, 3.0F, 12.0F), PartPose.offset(4.5F,0.5F,2.0F));
        modelPartData3.addOrReplaceChild("r_tail", CubeListBuilder.create().texOffs(0,66).addBox(0.0F, 0.0F, -12.0F, 24.0F, 3.0F, 12.0F), PartPose.offsetAndRotation(-4.5F,0.5F,2.0F,0.0F,3.1416F,0.0F));
        modelPartData1.addOrReplaceChild("l_fin", CubeListBuilder.create().texOffs(72,48).addBox(0.0F, -2.0F, 0.0F, 12.0F, 3.0F, 6.0F), PartPose.offset(10.5F,-2.0F,-13.0F));
        modelPartData1.addOrReplaceChild("r_fin", CubeListBuilder.create().texOffs(72,57).addBox(0.0F, -2.0F, -6.0F, 12.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(-10.5F,-2.0F,-12.0F,0.0F,3.1416F,0.0F));

        return LayerDefinition.create(modelData,192,96);
    }

    final float pi = 3.1415927F;

    @Override
    public void setupAnim(AerwhaleEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float time = ageInTicks / 20;
        //aerwhale_body1.yaw = MathHelper.cos(time)/4;
        //aerwhale_head.pitch = MathHelper.sin(time)/36;
        aerwhale_body1.xRot = Mth.cos(time - 1) / 10;
        aerwhale_body2.xRot = Mth.cos(time - 2) / 10;
        l_fin.yRot = Mth.cos(limbSwing) / 4;
        r_fin.yRot = pi + -Mth.cos(limbSwing) / 4;
        l_tail.yRot = Mth.cos(limbSwing + 10) / 6;
        r_tail.yRot = pi + -Mth.cos(limbSwing + 10) / 6;
        l_tail.xRot = Mth.cos(time + 1.5F) / 5;
        r_tail.xRot = Mth.cos(time - 1.5F) / 5;
        //previously the render function, render code was moved to a method below
    }

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        aerwhale_head.render(matrixStack, buffer, packedLight, packedOverlay);
    }
}