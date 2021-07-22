package com.aether.world.feature.structure;

import com.aether.world.feature.generator.WellGenerator;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class WellFeature extends StructureFeature<NoneFeatureConfiguration> {
    public WellFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return Start::new;
    }

    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.SURFACE_STRUCTURES;
    }

    public static class Start extends StructureStart<NoneFeatureConfiguration> {

        public Start(StructureFeature<NoneFeatureConfiguration> feature, ChunkPos pos, int references, long seed) {
            super(feature, pos, references, seed);
        }

        @Override
        public void generatePieces(RegistryAccess registryManager, ChunkGenerator chunkGenerator, StructureManager manager, ChunkPos pos, Biome biome, NoneFeatureConfiguration config, LevelHeightAccessor world) {
            int x = pos.x * 16;
            int z = pos.z * 16;
            int y = chunkGenerator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, world);
            BlockPos newPos = new BlockPos(x, y, z);
            WellGenerator.addPieces(manager, this, random, newPos);
            this.getBoundingBox();
        }
    }
}
