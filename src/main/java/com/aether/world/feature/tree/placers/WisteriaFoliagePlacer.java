package com.aether.world.feature.tree.placers;

import com.aether.blocks.natural.AetherLeavesBlock;
import com.aether.blocks.natural.AuralLeavesBlock;
import com.aether.world.feature.tree.AetherTreeHell;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

import static com.aether.blocks.natural.AetherHangerBlock.TIP;

public class WisteriaFoliagePlacer extends FoliagePlacer {

    public static final Codec<WisteriaFoliagePlacer> CODEC = RecordCodecBuilder.create((instance) ->
            foliagePlacerParts(instance).apply(instance, WisteriaFoliagePlacer::new));

    public WisteriaFoliagePlacer(IntProvider radius, IntProvider offset) {
        super(radius, offset);
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return AetherTreeHell.WISTERIA_FOLIAGE;
    }

    @Override
    protected void createFoliage(LevelSimulatedReader world, BiConsumer<BlockPos, BlockState> replacer, Random random, TreeConfiguration config, int trunkHeight, FoliageAttachment treeNode, int foliageHeight, int radius, int offset) {
        Set<BlockPos> leaves = Sets.newHashSet();
        if(radius <= 3)
            radius = 3;

        radius -= treeNode.radiusOffset();
        BlockPos nodePos = treeNode.pos();
        BlockPos altNodePos = nodePos.above(offset);

        BlockState leafBlock = config.foliageProvider.getState(random, nodePos);
        BlockState hanger = Blocks.AIR.defaultBlockState();

        if(leafBlock.getBlock() instanceof AetherLeavesBlock || leafBlock.getBlock() instanceof AuralLeavesBlock) {
            hanger = AetherLeavesBlock.getHanger(leafBlock);
        }

        for(int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                for (int k = 0; k < radius; k++) {
                    BlockPos offPos = nodePos.offset(Math.signum(i) * Math.abs(i)-k, k, Math.signum(j) * Math.abs(j)-k);
                    if((world.isStateAtPosition(offPos, BlockBehaviour.BlockStateBase::isAir) || TreeFeature.validTreePos(world, offPos)) && offPos.closerThan(random.nextBoolean() ? nodePos : altNodePos, radius)) {
                        replacer.accept(offPos, leafBlock);
                        leaves.add(offPos);
                    }
                }
            }
        }
        for (int i = -radius; i < radius; i++) {
            for (int j = -radius; j < radius; j++) {
                BlockPos offPos = nodePos.offset(i, 0, j);
                if(leaves.contains(offPos) && random.nextBoolean()) {
                    offPos = offPos.below();
                    int hangerLength = random.nextInt(3);
                    int step = 0;
                    while (step <= hangerLength && world.isStateAtPosition(offPos, BlockBehaviour.BlockStateBase::isAir)) {
                        replacer.accept(offPos, hanger.setValue(TIP, false));
                        offPos = offPos.below();
                        step++;
                    }
                    replacer.accept(offPos.above(), hanger.setValue(TIP, true));
                }
            }
        }
    }

    @Override
    public int foliageHeight(Random random, int trunkHeight, TreeConfiguration config) {
        return 0;
    }

    @Override
    protected boolean shouldSkipLocation(Random random, int baseHeight, int dx, int y, int dz, boolean giantTrunk) {
        return false;
    }
}
