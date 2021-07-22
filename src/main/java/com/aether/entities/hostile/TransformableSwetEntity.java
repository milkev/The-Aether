package com.aether.entities.hostile;

import com.aether.blocks.AetherBlocks;
import com.aether.blocks.natural.BlueberryBushBlock;
import com.aether.entities.AetherEntityTypes;
import com.aether.items.AetherItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public abstract class TransformableSwetEntity extends SwetEntity{
    public TransformableSwetEntity(Level world) {
        super(world);
    }

    public TransformableSwetEntity(EntityType<? extends SwetEntity> entityType, Level world) {
        super(entityType, world);
    }

    protected boolean changeType(EntityType<? extends SwetEntity> type){
        if(!this.getType().equals(type) && !this.isRemoved()) {
            SwetEntity swet = (this.convertTo(type, true));
            swet.setSize(this.getSize(), false);
            level.addFreshEntity(swet);
            return true;
        }
        return false;
    }

    @Override
    protected void onEntityCollision(Entity entity) {
        super.onEntityCollision(entity);
        if(entity.distanceToSqr(this) <= 1 && this.getSize() > 1){
            if (entity instanceof CockatriceEntity || entity instanceof AechorPlantEntity) {
                this.changeType(AetherEntityTypes.PURPLE_SWET);
            }
            if (entity instanceof ItemEntity item){
                if (item.getItem().getItem() == AetherItems.BLUEBERRY){
                    this.changeType(AetherEntityTypes.BLUE_SWET);
                    item.remove(RemovalReason.KILLED);
                }
                if (item.getItem().getItem() == AetherItems.GOLDEN_AMBER){
                    this.changeType(AetherEntityTypes.GOLDEN_SWET);
                    item.remove(RemovalReason.KILLED);
                }
            }
        }
    }

    public boolean suggestTypeChange(Level world, BlockPos blockPos, BlockState state){
        Block block = state.getBlock();
        if (block == AetherBlocks.GOLDEN_OAK_LOG ||
                block == AetherBlocks.GOLDEN_OAK_LEAVES ||
                block == AetherBlocks.GOLDEN_OAK_SAPLING ||
                block == AetherBlocks.STRIPPED_GOLDEN_OAK_LOG ||
                block == AetherBlocks.POTTED_GOLDEN_OAK_SAPLING) {
            return this.changeType(AetherEntityTypes.GOLDEN_SWET);
        }
        if (state.getBlock() == AetherBlocks.BLUEBERRY_BUSH) {
            return this.changeType(AetherEntityTypes.BLUE_SWET);
        }
        return false;
    }

    @Override
    protected void onInsideBlock(BlockState state) {
        super.onInsideBlock(state);
        if (state.getFluidState().getType() == Fluids.WATER ||
                state.getFluidState().getType() == Fluids.FLOWING_WATER) {
            this.changeType(AetherEntityTypes.BLUE_SWET);
        }
    }
}
