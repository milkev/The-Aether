package com.aether.world.feature.tree.placers;

import com.aether.blocks.natural.AetherLeavesBlock;
import com.aether.blocks.natural.AuralLeavesBlock;
import com.aether.world.feature.tree.AetherTreeHell;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.TestableWorld;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;
import net.minecraft.world.gen.foliage.FoliagePlacer;
import net.minecraft.world.gen.foliage.FoliagePlacerType;

import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;

import static com.aether.blocks.natural.AetherHangerBlock.TIP;

public class WisteriaFoliagePlacer extends FoliagePlacer {

    public static final Codec<WisteriaFoliagePlacer> CODEC = RecordCodecBuilder.create((instance) ->
            fillFoliagePlacerFields(instance).apply(instance, WisteriaFoliagePlacer::new));

    public WisteriaFoliagePlacer(IntProvider radius, IntProvider offset) {
        super(radius, offset);
    }

    @Override
    protected FoliagePlacerType<?> getType() {
        return AetherTreeHell.WISTERIA_FOLIAGE;
    }

    @Override
    protected void generate(TestableWorld world, BiConsumer<BlockPos, BlockState> replacer, Random random, TreeFeatureConfig config, int trunkHeight, TreeNode treeNode, int foliageHeight, int radius, int offset) {
        Set<BlockPos> leaves = Sets.newHashSet();
        if(radius <= 3)
            radius = 3;

        radius -= treeNode.getFoliageRadius();
        BlockPos nodePos = treeNode.getCenter();
        BlockPos altNodePos = nodePos.up(offset);

        BlockState leafBlock = config.foliageProvider.getBlockState(random, nodePos);
        BlockState hanger = Blocks.AIR.getDefaultState();

        if(leafBlock.getBlock() instanceof AetherLeavesBlock || leafBlock.getBlock() instanceof AuralLeavesBlock) {
            hanger = AetherLeavesBlock.getHanger(leafBlock);
        }

        for(int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                for (int k = 0; k < radius; k++) {
                    BlockPos offPos = nodePos.add(Math.signum(i) * Math.abs(i)-k, k, Math.signum(j) * Math.abs(j)-k);
                    if((world.testBlockState(offPos, AbstractBlock.AbstractBlockState::isAir) || TreeFeature.canReplace(world, offPos)) && offPos.isWithinDistance(random.nextBoolean() ? nodePos : altNodePos, radius)) {
                        replacer.accept(offPos, leafBlock);
                        leaves.add(offPos);
                    }
                }
            }
        }
        for (int i = -radius; i < radius; i++) {
            for (int j = -radius; j < radius; j++) {
                BlockPos offPos = nodePos.add(i, 0, j);
                if(leaves.contains(offPos) && random.nextBoolean()) {
                    offPos = offPos.down();
                    int hangerLength = random.nextInt(3);
                    int step = 0;
                    while (step <= hangerLength && world.testBlockState(offPos, AbstractBlock.AbstractBlockState::isAir)) {
                        replacer.accept(offPos, hanger.with(TIP, false));
                        offPos = offPos.down();
                        step++;
                    }
                    replacer.accept(offPos.up(), hanger.with(TIP, true));
                }
            }
        }
    }

    @Override
    public int getRandomHeight(Random random, int trunkHeight, TreeFeatureConfig config) {
        return 0;
    }

    @Override
    protected boolean isInvalidForLeaves(Random random, int baseHeight, int dx, int y, int dz, boolean giantTrunk) {
        return false;
    }
}
