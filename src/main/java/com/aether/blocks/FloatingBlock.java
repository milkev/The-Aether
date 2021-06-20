package com.aether.blocks;

import com.aether.entities.block.FloatingBlockEntity;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.OreBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
public class FloatingBlock extends OreBlock {
    private final boolean powered;

    public FloatingBlock(boolean powered, BlockBehaviour.Properties properties, UniformInt experienceDropped) {
        super(properties, experienceDropped);
        this.powered = powered;
    }

    public FloatingBlock(boolean powered, BlockBehaviour.Properties properties) {
        this(powered, properties, UniformInt.of(0, 0));
    }

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos posIn, BlockState oldState, boolean notify) {
        worldIn.getBlockTicks().scheduleTick(posIn, this, this.getFallDelay());
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facingIn, BlockState facingState, LevelAccessor worldIn, BlockPos posIn, BlockPos facingPosIn) {
        worldIn.getBlockTicks().scheduleTick(posIn, this, this.getFallDelay());
        return super.updateShape(stateIn, facingIn, facingState, worldIn, posIn, facingPosIn);
    }

    @Override
    public void tick(BlockState stateIn, ServerLevel worldIn, BlockPos posIn, Random randIn) {
        this.checkFloatable(worldIn, posIn);
    }

    private void checkFloatable(Level worldIn, BlockPos pos) {
        if ((worldIn.isEmptyBlock(pos.above()) || FallingBlock.isFree(worldIn.getBlockState(pos.above()))) && (!this.powered || worldIn.hasNeighborSignal(pos))) {
            if (!worldIn.isClientSide) {
                FloatingBlockEntity floatingblockentity = new FloatingBlockEntity(worldIn, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, worldIn.getBlockState(pos));
                this.onStartFloating(floatingblockentity);
                worldIn.addFreshEntity(floatingblockentity);
            }
        }
    }

    protected void onStartFloating(FloatingBlockEntity entityIn) {}

    public void onEndFloating(Level worldIn, BlockPos posIn, BlockState floatingState, BlockState hitState) {}

    public void onBroken(Level worldIn, BlockPos pos) {}

    protected int getFallDelay() {
        return 2;
    }
}