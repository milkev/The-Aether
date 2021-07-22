package com.aether.blocks.natural;

import com.aether.blocks.AetherBlocks;
import com.aether.entities.hostile.TransformableSwetEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

public class AetherPillarBlock extends RotatedPillarBlock {
    public AetherPillarBlock(Properties settings) {
        super(settings);
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
        super.entityInside(state, world, pos, entity);
        if ((this == AetherBlocks.GOLDEN_OAK_LOG)
                && entity instanceof TransformableSwetEntity swet) {
            swet.suggestTypeChange(world, pos, state);
        }
    }
}
