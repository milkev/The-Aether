package com.aether.world.dimension;

import com.aether.Aether;
import com.aether.blocks.AetherBlocks;
import com.aether.world.gen.AetherSurfaceBuilder;
import com.aether.world.gen.AetherSurfaceBuilderConfig;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;

public class AetherDimension {
    public static final ResourceKey<Level> AETHER_WORLD_KEY = ResourceKey.create(Registry.DIMENSION_REGISTRY, Aether.locate(Aether.MOD_ID));
    public static final ResourceKey<DimensionType> TYPE = ResourceKey.create(Registry.DIMENSION_TYPE_REGISTRY, Aether.locate("the_aether"));

    public static final ResourceKey<Biome> HIGHLANDS_PLAINS = ResourceKey.create(Registry.BIOME_REGISTRY, Aether.locate("aether_highlands"));
    public static final ResourceKey<Biome> HIGHLANDS_FOREST = ResourceKey.create(Registry.BIOME_REGISTRY, Aether.locate("aether_highlands_forest"));
    public static final ResourceKey<Biome> HIGHLANDS_THICKET = ResourceKey.create(Registry.BIOME_REGISTRY, Aether.locate("aether_highlands_thicket"));
    public static final ResourceKey<Biome> WISTERIA_WOODS = ResourceKey.create(Registry.BIOME_REGISTRY, Aether.locate("aether_wisteria_woods"));

    public static final SurfaceBuilder<AetherSurfaceBuilderConfig> AETHER_SURFACE_BUILDER =
            Registry.register(Registry.SURFACE_BUILDER, Aether.locate("surface_builder"), new AetherSurfaceBuilder());

    public static void setupDimension() {
        //CustomPortalApiRegistry.addPortal(Blocks.GLOWSTONE, PortalIgnitionSource.WATER, (CustomPortalBlock) AetherBlocks.BLUE_PORTAL, Aether.locate(Aether.MOD_ID), 55, 89, 195);
    }
}