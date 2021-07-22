package com.aether.client.rendering.entity.layer;

import com.aether.Aether;
import com.aether.client.model.entity.*;
import com.google.common.collect.Maps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class AetherModelLayers {
    public static final Map<ModelLayerLocation, LayerDefinition> ENTRIES = Maps.newHashMap();

    public static final ModelLayerLocation AECHOR_PLANT = register("aechor_plant", "main", AechorPlantModel.getTexturedModelData());
    public static final ModelLayerLocation AERBUNNY = register("aerbunny", "main", AerbunnyModel.getTexturedModelData());
    public static final ModelLayerLocation AERWHALE = register("aerwhale", "main", AerwhaleModel.getTexturedModelData());
    public static final ModelLayerLocation COCKATRICE = register("cockatrice", "main", CockatriceModel.getTexturedModelData());
    public static final ModelLayerLocation MIMIC = register("mimic", "main", ChestMimicModel.getTexturedModelData());
    public static final ModelLayerLocation MOA = register("moa", "main", MoaModel.getTexturedModelData());

    public static ModelLayerLocation register(ResourceLocation id, String layer, LayerDefinition data) {
        ModelLayerLocation entityModelLayer = new ModelLayerLocation(id, layer);
        if (!ModelLayers.ALL_MODELS.add(entityModelLayer)) {
            throw new IllegalStateException("Duplicate registration for " + entityModelLayer);
        } else {
            ENTRIES.put(entityModelLayer, data);
            return entityModelLayer;
        }
    }

    public static ModelLayerLocation register(String id, String layer, LayerDefinition data) {
        return register(Aether.locate(id), layer, data);
    }

    public static void initClient() {
        // Endless void.
    }
}
