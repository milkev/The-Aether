package com.aether.client.model.entity;

import com.aether.entities.hostile.AechorPlantEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class AechorPlantModel extends EntityModel<AechorPlantEntity> {

    public AechorPlantModel(ModelPart root) {
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();

        return LayerDefinition.create(modelData,48,32);
    }

    @Override
    public void setupAnim(AechorPlantEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {

    }
}
