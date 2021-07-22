package com.aether.world.feature;

import com.aether.Aether;
import com.aether.world.feature.generator.SkyrootTowerGenerator;
import com.aether.world.feature.generator.WellGenerator;
import com.aether.world.feature.structure.SkyrootTowerFeature;
import com.aether.world.feature.structure.WellFeature;
import com.aether.world.gen.decorator.CrystalTreeIslandDecorator;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class AetherFeatures {
    public static final StructurePieceType WELL_PIECE = register(WellGenerator.Piece::new, "well");
    public static final StructurePieceType SKYROOT_TOWER_PIECE = register(SkyrootTowerGenerator.Piece::new, "skyroot_tower");

    public static void registerFeatures() {
        register("lake", new AetherLakeFeature());
        register("aercloud", new AercloudFeature());
        register("quicksoil", new QuicksoilFeature());
        register("crystal_tree_island", new CrystalTreeIslandFeature(NoneFeatureConfiguration.CODEC));

        // Decorators
        register("crystal_tree_island", new CrystalTreeIslandDecorator(NoneDecoratorConfiguration.CODEC));

        register("well", new WellFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
        register("skyroot_tower", new SkyrootTowerFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
    }

    private static <T extends FeatureConfiguration> void register(String id, StructureFeature<T> structure, GenerationStep.Decoration genStep) {
        StructureFeature.register(Aether.locate(id).toString(), structure, genStep);
    }

    static StructurePieceType register(StructurePieceType pieceType, String id) {
        return Registry.register(Registry.STRUCTURE_PIECE, Aether.locate(id), pieceType);
    }


    @SuppressWarnings("UnusedReturnValue")
    private static <C extends FeatureConfiguration, F extends Feature<C>> F register(String id, F feature) {
        return Registry.register(Registry.FEATURE, Aether.locate(id), feature);
    }

    private static <T extends DecoratorConfiguration, G extends FeatureDecorator<T>> G register(String id, G decorator) {
        return Registry.register(Registry.DECORATOR, Aether.locate(id), decorator);
    }
}