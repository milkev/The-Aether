package com.aether.blocks;

import com.aether.entities.hostile.SwetEntity;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;

public class SwetDropBlock extends EntityBlockEgg{

    private static final DirectionProperty FACING = BlockStateProperties.FACING;

    public SwetDropBlock(Properties settings, BiFunction<Level, BlockPos, ? extends LivingEntity> func) {
        super(settings, func);
    }

    public SwetDropBlock(Properties settings, EntityType<? extends SwetEntity> type) {
        super(settings, (world, pos) -> {
            SwetEntity swet = type.create(world);
            if (swet != null) {
                swet.setSize(1, true);
                swet.setPos(Vec3.atLowerCornerOf(pos));
            }
            return swet;
        });
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

}
