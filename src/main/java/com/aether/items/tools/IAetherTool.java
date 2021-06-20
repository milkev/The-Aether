package com.aether.items.tools;

import com.aether.entities.block.FloatingBlockEntity;
import com.aether.items.utils.AetherTiers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public interface IAetherTool {
    float getDestroySpeed(ItemStack item, BlockState state);

    AetherTiers getItemMaterial();

    Logger log = LogManager.getLogger(IAetherTool.class);

    default InteractionResult useOnBlock(UseOnContext context, @Nullable InteractionResult defaultResult) {
        if (this.getItemMaterial() == AetherTiers.Gravitite) {
            BlockPos pos = context.getClickedPos();
            Level world = context.getLevel();
            BlockState state = world.getBlockState(pos);
            ItemStack heldItem = context.getItemInHand();

            if ((!state.requiresCorrectToolForDrops() || heldItem.isCorrectToolForDrops(state)) && FallingBlock.isFree(world.getBlockState(pos.above()))) {
                if (world.getBlockEntity(pos) != null || state.getDestroySpeed(world, pos) == -1.0F) {
                    return InteractionResult.FAIL;
                }

                if (!world.isClientSide()) {
                    FloatingBlockEntity entity = new FloatingBlockEntity(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, state);
                    entity.floatTime = 0;
                    world.addFreshEntity(entity);
                }

                if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
                    context.getItemInHand().hurtAndBreak(4, context.getPlayer(), (p) -> p.broadcastBreakEvent(context.getHand()));
                }

                return InteractionResult.SUCCESS;
            }
        }

        return defaultResult != null ? defaultResult : defaultItemUse(context);
    }

    default InteractionResult defaultItemUse(UseOnContext context) {
        return InteractionResult.SUCCESS;
    }
}