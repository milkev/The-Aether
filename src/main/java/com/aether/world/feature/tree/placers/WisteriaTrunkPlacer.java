package com.aether.world.feature.tree.placers;

import com.aether.world.feature.tree.AetherTreeHell;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class WisteriaTrunkPlacer extends TrunkPlacer {

    public static final Codec<WisteriaTrunkPlacer> CODEC = RecordCodecBuilder.create(instance ->
            trunkPlacerParts(instance).apply(instance, WisteriaTrunkPlacer::new));

    public WisteriaTrunkPlacer(int baseHeight, int firstRandomHeight, int secondRandomHeight) {
        super(baseHeight, firstRandomHeight, secondRandomHeight);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return AetherTreeHell.WISTERIA_TRUNK;
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader world, BiConsumer<BlockPos, BlockState> replacer, Random random, int height, BlockPos startPos, TreeConfiguration config) {
        List<FoliagePlacer.FoliageAttachment> nodes = new ArrayList<>();

        for (int i = 0; i < baseHeight; i++) {
            placeLog(world, replacer, random, startPos, config);
            startPos = startPos.above();
        }

        for (Direction dir : new Direction[] {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
            int vOffset = random.nextInt(3) - 1;
            int branchSize = 2 + random.nextInt(2);

            BlockPos tempPos = startPos.below(2);
            tempPos = tempPos.above(vOffset);

            for(int i = 0; i < branchSize; i++) {
                tempPos = tempPos.relative(dir);
                if (random.nextBoolean()) tempPos = tempPos.above();
                placeLog(world, replacer, random, tempPos, config);
            }
            nodes.add(new FoliagePlacer.FoliageAttachment(tempPos, 0, false));
        }

        return nodes;
    }
}
