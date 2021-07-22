package com.aether.blocks.blockentity;

import com.aether.blocks.AetherBlocks;
import com.aether.blocks.mechanical.FoodBowlBlock;
import com.aether.util.InventoryWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FoodBowlBlockEntity extends BlockEntity implements InventoryWrapper, WorldlyContainer {

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);

    public FoodBowlBlockEntity(BlockPos pos, BlockState state) {
        super(AetherBlocks.FOOD_BOWL_BLOCK_ENTITY_TYPE, pos, state);
    }

    @SuppressWarnings("ConstantConditions")
    public boolean handleUse(Player player, InteractionHand hand, ItemStack handStack) {
        ItemStack storedFood = inventory.get(0);
        if(!storedFood.isEmpty() && (handStack.isEmpty() || !handStack.sameItemStackIgnoreDurability(storedFood))) {
            if(!player.getInventory().add(storedFood))
                level.addFreshEntity(new ItemEntity(level, worldPosition.getX(), worldPosition.getY() + 0.75, worldPosition.getZ(), storedFood, 0, 0, 0));
            inventory.clear();
            updateState();
            return true;
        }

        Item food = handStack.getItem();

        if(food.isEdible() && food.getFoodProperties().isMeat()) {
            if(storedFood.isEmpty()) {
                inventory.set(0, handStack);
                player.setItemInHand(hand, ItemStack.EMPTY);
            }
            else {
                int overflow = (storedFood.getCount() + handStack.getCount()) - 64;
                storedFood.setCount(Math.min(64 + overflow, 64));
                handStack.setCount(Math.max(overflow, 0));
            }
            updateState();
            return true;
        }
        return false;
    }

    @SuppressWarnings("ConstantConditions")
    private void updateState() {
        level.setBlockAndUpdate(worldPosition, getBlockState().setValue(FoodBowlBlock.FULL, !inventory.get(0).isEmpty()));
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        ContainerHelper.saveAllItems(nbt, inventory);
        return super.save(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        ContainerHelper.loadAllItems(nbt, inventory);
        super.load(nbt);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[1];
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        return stack.isEdible() && stack.getItem().getFoodProperties().isMeat();
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return false;
    }
}
