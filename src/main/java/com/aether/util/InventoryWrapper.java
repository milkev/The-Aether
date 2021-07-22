package com.aether.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A simple {@code Inventory} implementation with only default methods + an item list getter.
 *
 * Originally by Juuz
 */
public interface InventoryWrapper extends Container {
    /**
     * Gets the item list of this inventory.
     * Must return the same instance every time it's called.
     */
    NonNullList<ItemStack> getItems();
    // Creation
    /**
     * Creates an inventory from the item list.
     */
    static InventoryWrapper of(NonNullList<ItemStack> items) {
        return () -> items;
    }
    /**
     * Creates a new inventory with the size.
     */
    static InventoryWrapper ofSize(int size) {
        return of(NonNullList.withSize(size, ItemStack.EMPTY));
    }
    // Inventory
    /**
     * Returns the inventory size.
     */
    @Override
    default int getContainerSize() {
        return getItems().size();
    }
    /**
     * @return true if this inventory has only empty stacks, false otherwise
     */
    @Override
    default boolean isEmpty() {
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack stack = getItem(i);
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    /**
     * Gets the item in the slot.
     */
    @Override
    default ItemStack getItem(int slot) {
        return getItems().get(slot);
    }

    default int getItemSlot(ItemStack stack) {
        NonNullList<ItemStack> inv = getItems();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack item = inv.get(i);
            if(item.getMaxStackSize() > item.getCount() && item.sameItemStackIgnoreDurability(stack)) {
                return i;
            }
        }
        return -1;
    }
    /**
     * Takes a stack of the size from the slot.
     * <p>(default implementation) If there are less items in the slot than what are requested,
     * takes all items in that slot.
     */
    @Override
    default ItemStack removeItem(int slot, int count) {
        ItemStack result = ContainerHelper.removeItem(getItems(), slot, count);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }
    /**
     * Removes the current stack in the {@code slot} and returns it.
     */
    @Override
    default ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(getItems(), slot);
    }
    @Override
    default void setItem(int slot, ItemStack stack) {
        getItems().set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
    }
    /**
     * Clears {@linkplain #getItems() the item list}}.
     */
    @Override
    default void clearContent() {
        getItems().clear();
    }
    @Override
    default void setChanged() {
        // Override if you want behavior.
    }
    @Override
    default boolean stillValid(Player player) {
        return true;
    }
}


