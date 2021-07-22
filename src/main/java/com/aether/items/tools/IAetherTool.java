package com.aether.items.tools;

import com.aether.blocks.AetherBlocks;
import com.aether.entities.AetherEntityExtensions;
import com.aether.entities.block.FloatingBlockEntity;
import com.aether.entities.block.FloatingBlockStructure;
import com.aether.items.utils.AetherTiers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;

public interface IAetherTool {
    float getDestroySpeed(ItemStack item, BlockState state);

    AetherTiers getItemMaterial();

    Logger log = LogManager.getLogger(IAetherTool.class);

    default boolean eligibleToFloat(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Level world = context.getLevel();
        BlockState state = world.getBlockState(pos);
        ItemStack heldItem = context.getItemInHand();
        Supplier<Boolean> dropState = () -> {
            int distFromTop = world.getMaxBuildHeight() - pos.getY();
            boolean isFastFloater = (
                    state.getBlock() == AetherBlocks.GRAVITITE_ORE ||
                    state.getBlock() == AetherBlocks.GRAVITITE_LEVITATOR ||
                    state.getBlock() == AetherBlocks.BLOCK_OF_GRAVITITE);
            return !isFastFloater && distFromTop <= 50;
        };

        return (!state.requiresCorrectToolForDrops() || heldItem.isCorrectToolForDrops(state))
                && FloatingBlockEntity.canMakeBlock(dropState, world.getBlockState(pos.below()),world.getBlockState(pos.above()));
    }

    default InteractionResult useOnBlock(UseOnContext context, @Nullable InteractionResult defaultResult) {
        if (this.getItemMaterial() == AetherTiers.GRAVITITE) {
            if (eligibleToFloat(context)) {
                return createFloatingBlockEntity(context);
            }
        }
        return defaultResult != null ? defaultResult : defaultItemUse(context);
    }

    private InteractionResult createFloatingBlockEntity(UseOnContext context){
        BlockPos pos = context.getClickedPos();
        Level world = context.getLevel();
        BlockState state = world.getBlockState(pos);

        if (world.getBlockEntity(pos) != null || state.getDestroySpeed(world, pos) == -1.0F) {
            return InteractionResult.FAIL;
        }
        if (state.getBlock() == Blocks.FIRE || state.getBlock() == Blocks.SOUL_FIRE) {
            world.destroyBlock(pos, false);
            return InteractionResult.SUCCESS;
        }

        if (!world.isClientSide()) {
            if(state.getProperties().contains(BlockStateProperties.DOUBLE_BLOCK_HALF)){ // doors and tall grass
                if(state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER){
                    pos = pos.below();
                    state = world.getBlockState(pos);
                }
                BlockState upperState = world.getBlockState(pos.above());
                FloatingBlockEntity upper = new FloatingBlockEntity(world, pos.getX() + 0.5, pos.getY()+1, pos.getZ() + 0.5, upperState);
                FloatingBlockEntity lower = new FloatingBlockEntity(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, state);
                FloatingBlockStructure structure = new FloatingBlockStructure(lower, upper, Vec3i.ZERO.above());
                structure.spawn(world);
            } else { // everything else
                FloatingBlockEntity entity = new FloatingBlockEntity(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, state);
                entity.floatTime = 0;
                if (state.getBlock() == Blocks.TNT){
                    System.out.println("boomer says");
                    entity.setOnEndFloating((impact, landed) -> {
                        System.out.println("boom " + impact);
                        if (impact >= 0.8) {
                            BlockPos landingPos = entity.blockPosition();
                            System.out.println("yea");
                            world.destroyBlock(landingPos, false);
                            world.explode(entity, landingPos.getX(), landingPos.getY(), landingPos.getZ(), (float) Mth.clamp(impact*5.5, 0, 10), Explosion.BlockInteraction.BREAK);
                        }
                    });
                }
                if (state.getBlock() == Blocks.LIGHTNING_ROD){
                    entity.setOnEndFloating((impact, landed) -> {
                        if (world.isThundering() && landed && impact >= 1.1){
                            LightningBolt lightning = new LightningBolt(EntityType.LIGHTNING_BOLT, world);
                            lightning.setPos(Vec3.atCenterOf(entity.blockPosition()));
                            world.addFreshEntity(lightning);
                        }
                    });
                }
                world.addFreshEntity(entity);
            }
        }

        if (context.getPlayer() != null && !context.getPlayer().isCreative()) {
            context.getItemInHand().hurtAndBreak(4, context.getPlayer(), (p) -> p.broadcastBreakEvent(context.getHand()));
        }

        return InteractionResult.SUCCESS;
    }

    default float calculateIncrease(ItemStack tool) {
        int current = tool.getDamageValue();
        int maxDamage = tool.getMaxDamage();

        if (maxDamage - 50 <= current) {
            return 7.0F;
        } else if (maxDamage - 110 <= current) {
            return 6.0F;
        } else if (maxDamage - 200 <= current) {
            return 5.0F;
        } else if (maxDamage - 239 <= current) {
            return 4.0F;
        } else {
            return 3.0F;
        }
    }

    default InteractionResult useOnEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand){
        if(this.getItemMaterial() == AetherTiers.GRAVITITE){
            ((AetherEntityExtensions)entity).setFlipped();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    default InteractionResult defaultItemUse(UseOnContext context) {
        return InteractionResult.SUCCESS;
    }
}