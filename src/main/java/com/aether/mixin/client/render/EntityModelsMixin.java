package com.aether.mixin.client.render;

import com.aether.client.rendering.entity.layer.AetherModelLayers;
import com.google.common.collect.ImmutableMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Map;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

@Mixin(LayerDefinitions.class)
public class EntityModelsMixin {
    @Inject(method = "createRoots", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;", remap = false), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void registerExtraModelData(CallbackInfoReturnable<Map<ModelLayerLocation, LayerDefinition>> info, ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> builder) {
        for (Map.Entry<ModelLayerLocation, LayerDefinition> entry : AetherModelLayers.ENTRIES.entrySet()) {
            builder.put(entry.getKey(), entry.getValue());
        }
    }
}
