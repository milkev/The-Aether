package com.aether.world.feature;

import com.aether.Aether;
import com.aether.blocks.AetherBlocks;
import com.aether.blocks.natural.AetherFruitingLeaves;
import com.aether.world.feature.tree.placers.WisteriaFoliagePlacer;
import com.aether.world.feature.tree.placers.WisteriaTrunkPlacer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.Features;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.blockplacers.SimpleBlockPlacer;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.RandomSpreadFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.SpruceFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.BendingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.placement.FrequencyWithExtraChanceDecoratorConfiguration;
import java.util.OptionalInt;

@SuppressWarnings("unchecked")
public class AetherConfiguredFeatures {

    public static Feature<BlockStateConfiguration> BOULDER;

    public static ConfiguredFeature<TreeConfiguration, ?> GOLDEN_OAK_TREE, CRYSTAL_TREE, SKYROOT_TREE, ORANGE_TREE, ROSE_WISTERIA_TREE, LAVENDER_WISTERIA_TREE, FROST_WISTERIA_TREE, FANCY_ROSE_WISTERIA_TREE, FANCY_LAVENDER_WISTERIA_TREE, FANCY_FROST_WISTERIA_TREE, FANCY_SKYROOT_TREE, BOREAL_WISTERIA_TREE, FANCY_BOREAL_WISTERIA_TREE;
    public static ConfiguredFeature<?, ?> SCATTERED_TREES, SPARSE_TREES, THICKET_TREES, RAINBOW_FOREST_TREES;
    public static ConfiguredFeature<?, ?> HOLYSTONE_BOULDER, MOSSY_HOLYSTONE_BOULDER;
    public static ConfiguredFeature<?, ?> AETHER_GRASS, AETHER_TALL_GRASS, ALT_AETHER_GRASS, ALT_AETHER_TALL_GRASS, DENSE_TALL_GRASS, AETHER_FERN, DENSE_AETHER_FERN, AETHER_BUSH, FLUTEGRASS;
    public static ConfiguredFeature<?, ?> FALLEN_LEAVES, FALLEN_RAINBOW_LEAVES, ALT_FALLEN_LEAVES;

    public static void registerFeatures() {

        BOULDER = Registry.register(Registry.FEATURE, Aether.locate("boulder"), new AetherBoulderFeature(BlockStateConfiguration.CODEC));

        AETHER_BUSH = register("aether_bush", Feature.RANDOM_PATCH.configured(Configs.AETHER_BUSH_CONFIG).decorated(Features.Decorators.ADD_32).decorated(Features.Decorators.HEIGHTMAP_SQUARE).decorated(FeatureDecorator.CHANCE.configured(new ChanceDecoratorConfiguration(5))).count(3));
        SKYROOT_TREE = register("skyroot_tree", Feature.TREE.configured(Configs.SKYROOT_CONFIG));
        GOLDEN_OAK_TREE = (ConfiguredFeature<TreeConfiguration, ?>) register("golden_oak_tree", Feature.TREE.configured(Configs.GOLDEN_OAK_CONFIG).decorated(Features.Decorators.HEIGHTMAP));
        CRYSTAL_TREE = (ConfiguredFeature<TreeConfiguration, ?>) register("crystal_tree", Feature.TREE.configured(Configs.CRYSTAL_TREE_CONFIG).decorated(Features.Decorators.HEIGHTMAP));
        ORANGE_TREE = (ConfiguredFeature<TreeConfiguration, ?>) register("orange_tree", Feature.TREE.configured(Configs.ORANGE_TREE_SAPLING_CONFIG).decorated(Features.Decorators.HEIGHTMAP));
        ROSE_WISTERIA_TREE = (ConfiguredFeature<TreeConfiguration, ?>) register("rose_wisteria_tree", Feature.TREE.configured(Configs.ROSE_WISTERIA_CONFIG).decorated(Features.Decorators.HEIGHTMAP));
        LAVENDER_WISTERIA_TREE = (ConfiguredFeature<TreeConfiguration, ?>) register("lavender_wisteria_tree", Feature.TREE.configured(Configs.LAVENDER_WISTERIA_CONFIG).decorated(Features.Decorators.HEIGHTMAP));
        FROST_WISTERIA_TREE = (ConfiguredFeature<TreeConfiguration, ?>) register("frost_wisteria_tree", Feature.TREE.configured(Configs.FROST_WISTERIA_CONFIG).decorated(Features.Decorators.HEIGHTMAP));
        BOREAL_WISTERIA_TREE = (ConfiguredFeature<TreeConfiguration, ?>) register("boreal_wisteria_tree", Feature.TREE.configured(Configs.BOREAL_WISTERIA_CONFIG).decorated(Features.Decorators.HEIGHTMAP));
        FANCY_ROSE_WISTERIA_TREE = (ConfiguredFeature<TreeConfiguration, ?>) register("fancy_rose_wisteria_tree", Feature.TREE.configured(Configs.FANCY_ROSE_WISTERIA_CONFIG).decorated(Features.Decorators.HEIGHTMAP));
        FANCY_LAVENDER_WISTERIA_TREE = (ConfiguredFeature<TreeConfiguration, ?>) register("fancy_lavender_wisteria_tree", Feature.TREE.configured(Configs.FANCY_LAVENDER_WISTERIA_CONFIG).decorated(Features.Decorators.HEIGHTMAP));
        FANCY_FROST_WISTERIA_TREE = (ConfiguredFeature<TreeConfiguration, ?>) register("fancy_frost_wisteria_tree", Feature.TREE.configured(Configs.FANCY_FROST_WISTERIA_CONFIG).decorated(Features.Decorators.HEIGHTMAP));
        FANCY_BOREAL_WISTERIA_TREE = (ConfiguredFeature<TreeConfiguration, ?>) register("fancy_boreal_wisteria_tree", Feature.TREE.configured(Configs.FANCY_BOREAL_WISTERIA_CONFIG).decorated(Features.Decorators.HEIGHTMAP));
        FANCY_SKYROOT_TREE = (ConfiguredFeature<TreeConfiguration, ?>) register("fancy_skyroot_tree", Feature.TREE.configured(Configs.FANCY_SKYROOT_CONFIG).decorated(Features.Decorators.HEIGHTMAP));
        SCATTERED_TREES = register("scattered_trees", Feature.RANDOM_SELECTOR.configured(Configs.SCATTERED_TREES_CONFIG).decorated(Features.Decorators.HEIGHTMAP_SQUARE).decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(7, 0.1F, 2))));
        SPARSE_TREES = register("sparse_trees", Feature.RANDOM_SELECTOR.configured(Configs.SPARSE_TREES_CONFIG).decorated(Features.Decorators.HEIGHTMAP_SQUARE).decorated(FeatureDecorator.CHANCE.configured(new ChanceDecoratorConfiguration(5))));
        THICKET_TREES = register("thicket_trees", Feature.RANDOM_SELECTOR.configured(Configs.THICKET_TREES_CONFIG).decorated(Features.Decorators.HEIGHTMAP_SQUARE).decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(20, 0.25F, 12)))).squared().countRandom(3);
        RAINBOW_FOREST_TREES = register("wisteria_woods_trees", Feature.RANDOM_SELECTOR.configured(Configs.RAINBOW_FOREST_CONFIG).decorated(Features.Decorators.HEIGHTMAP_SQUARE).decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(16, 0.25F, 16)))).squared().countRandom(4);

        // Used in json
        HOLYSTONE_BOULDER = register("holystone_boulder", BOULDER.configured(new BlockStateConfiguration(AetherBlocks.COBBLED_HOLYSTONE.defaultBlockState()))).decorated(Features.Decorators.HEIGHTMAP_SQUARE).rarity(5);
        MOSSY_HOLYSTONE_BOULDER = register("mossy_holystone_boulder", BOULDER.configured(new BlockStateConfiguration(AetherBlocks.MOSSY_HOLYSTONE.defaultBlockState()))).decorated(Features.Decorators.HEIGHTMAP_SQUARE).decorated(FeatureDecorator.CHANCE.configured(new ChanceDecoratorConfiguration(3))).squared().countRandom(2);

        FALLEN_LEAVES = register("fallen_leaves", Feature.RANDOM_PATCH.configured(Configs.FALLEN_LEAVES_CONFIG).decorated(Features.Decorators.ADD_32).decorated(Features.Decorators.HEIGHTMAP_SQUARE).decorated(FeatureDecorator.CHANCE.configured(new ChanceDecoratorConfiguration(5))).count(3));
        ALT_FALLEN_LEAVES = register("alt_fallen_leaves", Feature.RANDOM_PATCH.configured(Configs.FALLEN_LEAVES_CONFIG).decorated(Features.Decorators.ADD_32).decorated(Features.Decorators.HEIGHTMAP_SQUARE).decorated(FeatureDecorator.CHANCE.configured(new ChanceDecoratorConfiguration(5))).count(3));
        FALLEN_RAINBOW_LEAVES = register("rainbow_fallen_leaves", Feature.RANDOM_PATCH.configured(Configs.RAINBOW_LEAVES_CONFIG).decorated(Features.Decorators.ADD_32).decorated(Features.Decorators.HEIGHTMAP_SQUARE).decorated(FeatureDecorator.CHANCE.configured(new ChanceDecoratorConfiguration(5))).count(3));
    }

    private static <FC extends FeatureConfiguration> ConfiguredFeature<FC, ?> register(String id, ConfiguredFeature<FC, ?> configuredFeature) {
        return Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, Aether.locate(id), configuredFeature);
    }

    public static class Configs {

        public static final BlockState ORANGE_LEAVES = AetherBlocks.ORANGE_LEAVES.defaultBlockState().setValue(AetherFruitingLeaves.CAPPED, true).setValue(AetherFruitingLeaves.NATURAL, true);
        public static final BlockState ORANGE_LEAVES_FLOWERING = AetherBlocks.ORANGE_LEAVES.defaultBlockState().setValue(AetherFruitingLeaves.CAPPED, true).setValue(AetherFruitingLeaves.NATURAL, true).setValue(AetherFruitingLeaves.GROWTH, 1);
        public static final BlockState ORANGE_LEAVES_FRUITING = AetherBlocks.ORANGE_LEAVES.defaultBlockState().setValue(AetherFruitingLeaves.CAPPED, true).setValue(AetherFruitingLeaves.NATURAL, true).setValue(AetherFruitingLeaves.GROWTH, 2);

        //public static final RandomPatchFeatureConfig FLOWER_CONFIG = (new RandomPatchFeatureConfig.Builder((new WeightedBlockStateProvider(ConfiguredFeatures.pool().add(AetherBlocks.PURPLE_FLOWER.getDefaultState(), 2).add(AetherBlocks.WHITE_FLOWER.getDefaultState(), 1)), new SimpleBlockPlacer())).tries(64).build();
        public static final RandomPatchConfiguration AETHER_BUSH_CONFIG = (new RandomPatchConfiguration.GrassConfigurationBuilder(new SimpleStateProvider(AetherBlocks.AETHER_BUSH.defaultBlockState()), SimpleBlockPlacer.INSTANCE)).xspread(16).yspread(7).zspread(16).tries(256).build();
        public static final RandomPatchConfiguration FALLEN_LEAVES_CONFIG = (new RandomPatchConfiguration.GrassConfigurationBuilder(new WeightedStateProvider(Features.weightedBlockStateBuilder().add(AetherBlocks.SKYROOT_LEAF_PILE.defaultBlockState(), 8).add(AetherBlocks.SKYROOT_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true), 1)), new SimpleBlockPlacer())).whitelist(ImmutableSet.of(AetherBlocks.AETHER_GRASS_BLOCK, AetherBlocks.HOLYSTONE, AetherBlocks.MOSSY_HOLYSTONE)).noProjection().xspread(10).yspread(7).zspread(10).tries(96).build();
        public static final RandomPatchConfiguration RAINBOW_LEAVES_CONFIG = (new RandomPatchConfiguration.GrassConfigurationBuilder(new WeightedStateProvider(Features.weightedBlockStateBuilder().add(AetherBlocks.ROSE_WISTERIA_LEAF_PILE.defaultBlockState(), 10).add(AetherBlocks.LAVENDER_WISTERIA_LEAF_PILE.defaultBlockState(), 9).add(AetherBlocks.ROSE_WISTERIA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true), 2).add(AetherBlocks.LAVENDER_WISTERIA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true), 1)), new SimpleBlockPlacer())).whitelist(ImmutableSet.of(AetherBlocks.AETHER_GRASS_BLOCK, AetherBlocks.HOLYSTONE, AetherBlocks.MOSSY_HOLYSTONE)).noProjection().xspread(10).yspread(7).zspread(10).tries(256).build();
        public static final TreeConfiguration SKYROOT_CONFIG = (new TreeConfiguration.TreeConfigurationBuilder(new SimpleStateProvider(AetherBlocks.SKYROOT_LOG.defaultBlockState()), new StraightTrunkPlacer(4, 2, 0), new SimpleStateProvider(AetherBlocks.SKYROOT_LEAVES.defaultBlockState()), new SimpleStateProvider(AetherBlocks.SKYROOT_SAPLING.defaultBlockState()), new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3), new TwoLayersFeatureSize(1, 0, 1))).ignoreVines().build();
        public static final TreeConfiguration CRYSTAL_TREE_CONFIG = (new TreeConfiguration.TreeConfigurationBuilder(new SimpleStateProvider(AetherBlocks.CRYSTAL_LOG.defaultBlockState()), new StraightTrunkPlacer(5, 2, 2), new SimpleStateProvider(AetherBlocks.CRYSTAL_LEAVES.defaultBlockState()), new SimpleStateProvider(AetherBlocks.CRYSTAL_SAPLING.defaultBlockState()), new SpruceFoliagePlacer(UniformInt.of(1, 2), UniformInt.of(0, 2), UniformInt.of(1, 1)), new TwoLayersFeatureSize(2, 0, 2))).ignoreVines().build();
        public static final TreeConfiguration ORANGE_TREE_SAPLING_CONFIG = (new TreeConfiguration.TreeConfigurationBuilder(new SimpleStateProvider(AetherBlocks.ORANGE_LOG.defaultBlockState()), new BendingTrunkPlacer(3, 2, 1, 3, UniformInt.of(1, 2)), new SimpleStateProvider(AetherBlocks.ORANGE_LEAVES.defaultBlockState()), new SimpleStateProvider(AetherBlocks.ORANGE_SAPLING.defaultBlockState()), new RandomSpreadFoliagePlacer(UniformInt.of(3, 4), ConstantInt.of(0), ConstantInt.of(3), 63), new TwoLayersFeatureSize(1, 0, 1))).build();
        public static final TreeConfiguration ORANGE_TREE_WILD_CONFIG = (new TreeConfiguration.TreeConfigurationBuilder(new SimpleStateProvider(AetherBlocks.ORANGE_LOG.defaultBlockState()), new BendingTrunkPlacer(3, 2, 1, 3, UniformInt.of(1, 2)), new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder().add(ORANGE_LEAVES, 2).add(ORANGE_LEAVES_FLOWERING, 2).add(ORANGE_LEAVES_FRUITING, 1)), new SimpleStateProvider(AetherBlocks.ORANGE_SAPLING.defaultBlockState()), new RandomSpreadFoliagePlacer(UniformInt.of(3, 4), ConstantInt.of(0), ConstantInt.of(3), 63), new TwoLayersFeatureSize(1, 0, 1))).build();
        public static final TreeConfiguration ROSE_WISTERIA_CONFIG = (new TreeConfiguration.TreeConfigurationBuilder(new SimpleStateProvider(AetherBlocks.WISTERIA_LOG.defaultBlockState()), new WisteriaTrunkPlacer(6, 3, 2), new SimpleStateProvider(AetherBlocks.ROSE_WISTERIA_LEAVES.defaultBlockState()), new SimpleStateProvider(AetherBlocks.ROSE_WISTERIA_SAPLING.defaultBlockState()), new WisteriaFoliagePlacer(UniformInt.of(1, 2), UniformInt.of(0, 1)), new TwoLayersFeatureSize(3, 0, 3))).ignoreVines().build();
        public static final TreeConfiguration LAVENDER_WISTERIA_CONFIG = (new TreeConfiguration.TreeConfigurationBuilder(new SimpleStateProvider(AetherBlocks.WISTERIA_LOG.defaultBlockState()), new WisteriaTrunkPlacer(6, 3, 2), new SimpleStateProvider(AetherBlocks.LAVENDER_WISTERIA_LEAVES.defaultBlockState()), new SimpleStateProvider(AetherBlocks.LAVENDER_WISTERIA_SAPLING.defaultBlockState()), new WisteriaFoliagePlacer(UniformInt.of(1, 2), UniformInt.of(0, 1)), new TwoLayersFeatureSize(3, 0, 3))).ignoreVines().build();
        public static final TreeConfiguration FROST_WISTERIA_CONFIG = (new TreeConfiguration.TreeConfigurationBuilder(new SimpleStateProvider(AetherBlocks.WISTERIA_LOG.defaultBlockState()), new WisteriaTrunkPlacer(6, 3, 2), new SimpleStateProvider(AetherBlocks.FROST_WISTERIA_LEAVES.defaultBlockState()), new SimpleStateProvider(AetherBlocks.FROST_WISTERIA_SAPLING.defaultBlockState()), new WisteriaFoliagePlacer(UniformInt.of(1, 2), UniformInt.of(0, 1)), new TwoLayersFeatureSize(3, 0, 3))).ignoreVines().build();
        public static final TreeConfiguration BOREAL_WISTERIA_CONFIG = (new TreeConfiguration.TreeConfigurationBuilder(new SimpleStateProvider(AetherBlocks.WISTERIA_LOG.defaultBlockState()), new WisteriaTrunkPlacer(6, 3, 2), new SimpleStateProvider(AetherBlocks.BOREAL_WISTERIA_LEAVES.defaultBlockState()), new SimpleStateProvider(AetherBlocks.BOREAL_WISTERIA_SAPLING.defaultBlockState()), new WisteriaFoliagePlacer(UniformInt.of(1, 3), UniformInt.of(0, 1)), new TwoLayersFeatureSize(3, 0, 3))).ignoreVines().build();
        public static final TreeConfiguration FANCY_ROSE_WISTERIA_CONFIG = (new TreeConfiguration.TreeConfigurationBuilder(new SimpleStateProvider(AetherBlocks.WISTERIA_LOG.defaultBlockState()), new WisteriaTrunkPlacer(9, 4, 2), new SimpleStateProvider(AetherBlocks.ROSE_WISTERIA_LEAVES.defaultBlockState()), new SimpleStateProvider(AetherBlocks.ROSE_WISTERIA_SAPLING.defaultBlockState()), new WisteriaFoliagePlacer(UniformInt.of(3, 7), UniformInt.of(0, 1)), new TwoLayersFeatureSize(3, 0, 3))).ignoreVines().build();
        public static final TreeConfiguration FANCY_LAVENDER_WISTERIA_CONFIG = (new TreeConfiguration.TreeConfigurationBuilder(new SimpleStateProvider(AetherBlocks.WISTERIA_LOG.defaultBlockState()), new WisteriaTrunkPlacer(9, 4, 2), new SimpleStateProvider(AetherBlocks.LAVENDER_WISTERIA_LEAVES.defaultBlockState()), new SimpleStateProvider(AetherBlocks.LAVENDER_WISTERIA_SAPLING.defaultBlockState()), new WisteriaFoliagePlacer(UniformInt.of(3, 7), UniformInt.of(0, 1)), new TwoLayersFeatureSize(3, 0, 3))).ignoreVines().build();
        public static final TreeConfiguration FANCY_FROST_WISTERIA_CONFIG = (new TreeConfiguration.TreeConfigurationBuilder(new SimpleStateProvider(AetherBlocks.WISTERIA_LOG.defaultBlockState()), new WisteriaTrunkPlacer(11, 6, 3), new SimpleStateProvider(AetherBlocks.FROST_WISTERIA_LEAVES.defaultBlockState()), new SimpleStateProvider(AetherBlocks.FROST_WISTERIA_SAPLING.defaultBlockState()), new WisteriaFoliagePlacer(UniformInt.of(4, 9), UniformInt.of(0, 1)), new TwoLayersFeatureSize(3, 0, 3))).ignoreVines().build();
        public static final TreeConfiguration FANCY_BOREAL_WISTERIA_CONFIG = (new TreeConfiguration.TreeConfigurationBuilder(new SimpleStateProvider(AetherBlocks.WISTERIA_LOG.defaultBlockState()), new WisteriaTrunkPlacer(11, 6, 3), new SimpleStateProvider(AetherBlocks.BOREAL_WISTERIA_LEAVES.defaultBlockState()), new SimpleStateProvider(AetherBlocks.BOREAL_WISTERIA_SAPLING.defaultBlockState()), new WisteriaFoliagePlacer(UniformInt.of(4, 9), UniformInt.of(0, 1)), new TwoLayersFeatureSize(3, 0, 3))).ignoreVines().build();
        public static final TreeConfiguration GOLDEN_OAK_CONFIG = (new TreeConfiguration.TreeConfigurationBuilder(new SimpleStateProvider(AetherBlocks.GOLDEN_OAK_LOG.defaultBlockState()), new FancyTrunkPlacer(4, 8, 0), new SimpleStateProvider(AetherBlocks.GOLDEN_OAK_LEAVES.defaultBlockState()), new SimpleStateProvider(AetherBlocks.GOLDEN_OAK_SAPLING.defaultBlockState()), new BlobFoliagePlacer(ConstantInt.of(4), ConstantInt.of(3), 3), new TwoLayersFeatureSize(3, 0, 3, OptionalInt.of(2)))).ignoreVines().build();
        public static final TreeConfiguration FANCY_SKYROOT_CONFIG = (new TreeConfiguration.TreeConfigurationBuilder(new SimpleStateProvider(AetherBlocks.SKYROOT_LOG.defaultBlockState()), new FancyTrunkPlacer(4, 11, 0), new SimpleStateProvider(AetherBlocks.SKYROOT_LEAVES.defaultBlockState()), new SimpleStateProvider(AetherBlocks.SKYROOT_SAPLING.defaultBlockState()), new FancyFoliagePlacer(ConstantInt.of(2), ConstantInt.of(4), 4), new TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4)))).ignoreVines().build();

        public static final RandomFeatureConfiguration SCATTERED_TREES_CONFIG = new RandomFeatureConfiguration(
                ImmutableList.of(Feature.TREE.configured(FANCY_SKYROOT_CONFIG).weighted(0.05F), Feature.TREE.configured(ROSE_WISTERIA_CONFIG).weighted(0.002F)),
                Feature.TREE.configured(Configs.SKYROOT_CONFIG)
        );

        public static final RandomFeatureConfiguration THICKET_TREES_CONFIG = new RandomFeatureConfiguration(
                ImmutableList.of(Feature.TREE.configured(ROSE_WISTERIA_CONFIG).weighted(0.0001F), Feature.TREE.configured(LAVENDER_WISTERIA_CONFIG).weighted(0.0001F), Feature.TREE.configured(GOLDEN_OAK_CONFIG).weighted(0.0025F), Feature.TREE.configured(SKYROOT_CONFIG).weighted(0.1F)),
                Feature.TREE.configured(Configs.FANCY_SKYROOT_CONFIG)
        );

        public static final RandomFeatureConfiguration RAINBOW_FOREST_CONFIG = new RandomFeatureConfiguration(
                ImmutableList.of(Feature.TREE.configured(LAVENDER_WISTERIA_CONFIG).weighted(0.33F), Feature.TREE.configured(ROSE_WISTERIA_CONFIG).weighted(0.075F), Feature.TREE.configured(FANCY_LAVENDER_WISTERIA_CONFIG).weighted(0.025F), Feature.TREE.configured(FANCY_ROSE_WISTERIA_CONFIG).weighted(0.075F), Feature.TREE.configured(FROST_WISTERIA_CONFIG).weighted(0.0001F), Feature.TREE.configured(SKYROOT_CONFIG).weighted(0.2F), Feature.TREE.configured(ORANGE_TREE_WILD_CONFIG).weighted(0.0125F)),
                Feature.TREE.configured(Configs.ROSE_WISTERIA_CONFIG)
        );

        public static final RandomFeatureConfiguration SPARSE_TREES_CONFIG = new RandomFeatureConfiguration(
                ImmutableList.of(
                        Feature.TREE.configured(FANCY_SKYROOT_CONFIG).weighted(0.1F),
                        Feature.TREE.configured(ORANGE_TREE_WILD_CONFIG).weighted(0.02F)
                ),
                Feature.TREE.configured(Configs.SKYROOT_CONFIG)
        );
    }
}