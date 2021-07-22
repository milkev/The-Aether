package com.aether.items.tools;

import com.aether.blocks.AetherBlocks;
import com.aether.items.AetherItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class VialItem extends Item {

    private final Fluid fluid;

    public VialItem(Fluid fluid, Properties settings) {
        super(settings);
        this.fluid = fluid;
    }

    public static ItemStack getEmptiedStack(ItemStack stack, Player player) {
        return !player.getAbilities().instabuild ? new ItemStack(AetherItems.QUICKSOIL_VIAL) : stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(world, user, this.fluid == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = hitResult.getBlockPos();
            Direction direction = hitResult.getDirection();
            BlockPos blockPos2 = blockPos.relative(direction);
            if (world.mayInteract(user, blockPos) && user.mayUseItemAt(blockPos2, direction, itemStack)) {
                BlockState blockState;
                // Originally, vials couldn't pick up dense aercloud. If this was intended, remove this if statement
                if (this.fluid == Fluids.EMPTY) {
                    blockState = world.getBlockState(blockPos);
                    if (blockState.getBlock() instanceof BucketPickup && blockState.getFluidState().getType().equals(AetherBlocks.DENSE_AERCLOUD_STILL)) {
                        BucketPickup fluidDrainable = (BucketPickup) blockState.getBlock();
                        ItemStack itemStack2 = fluidDrainable.pickupBlock(world, blockPos, blockState);
                        if (!itemStack2.isEmpty()) {
                            world.gameEvent(user, GameEvent.FLUID_PICKUP, blockPos);
                            ItemStack itemStack3 = ItemUtils.createFilledResult(itemStack, user, new ItemStack(AetherItems.AERCLOUD_VIAL));

                            return InteractionResultHolder.sidedSuccess(itemStack3, world.isClientSide());
                        }
                    }

                    return InteractionResultHolder.fail(itemStack);
                } else {
                    blockState = world.getBlockState(blockPos);
                    BlockPos blockPos3 = blockState.getBlock() instanceof LiquidBlockContainer && this.fluid == AetherBlocks.DENSE_AERCLOUD_STILL ? blockPos : blockPos2;
                    if (placeFluid(user, world, blockPos3, hitResult)) {
                        return InteractionResultHolder.sidedSuccess(getEmptiedStack(itemStack, user), world.isClientSide());
                    } else {
                        return InteractionResultHolder.fail(itemStack);
                    }
                }
            } else {
                return InteractionResultHolder.fail(itemStack);
            }
        }
        return InteractionResultHolder.fail(itemStack);
    }

    private boolean placeFluid(Player player, Level world, BlockPos pos, BlockHitResult hitResult) {
        if (!(this.fluid instanceof FlowingFluid)) {
            return false;
        } else {
            BlockState blockState = world.getBlockState(pos);
            Block block = blockState.getBlock();
            Material material = blockState.getMaterial();
            boolean bl = blockState.canBeReplaced(this.fluid);
            boolean bl2 = blockState.isAir() || bl || block instanceof LiquidBlockContainer && ((LiquidBlockContainer) block).canPlaceLiquid(world, pos, blockState, this.fluid);
            if (!bl2) {
                return hitResult != null && this.placeFluid(player, world, hitResult.getBlockPos().relative(hitResult.getDirection()), null);
            } else if (world.dimensionType().ultraWarm() && this.fluid.equals(AetherBlocks.DENSE_AERCLOUD_STILL)) {
                int i = pos.getX();
                int j = pos.getY();
                int k = pos.getZ();
                world.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

                for (int l = 0; l < 8; ++l) {
                    world.addParticle(ParticleTypes.LARGE_SMOKE, (double) i + Math.random(), (double) j + Math.random(), (double) k + Math.random(), 0.0D, 0.0D, 0.0D);
                }

                return true;
            } else if (block instanceof LiquidBlockContainer && this.fluid == AetherBlocks.DENSE_AERCLOUD_STILL) {
                ((LiquidBlockContainer) block).placeLiquid(world, pos, blockState, ((FlowingFluid) this.fluid).getSource(false));
//                this.playEmptyingSound(player, world, pos);
                return true;
            } else {
                if (!world.isClientSide && bl && !material.isLiquid()) {
                    world.destroyBlock(pos, true);
                }

                //                    this.playEmptyingSound(player, world, pos);
                return world.setBlock(pos, this.fluid.defaultFluidState().createLegacyBlock(), 11) || blockState.getFluidState().isSource();
            }
        }
    }
}
