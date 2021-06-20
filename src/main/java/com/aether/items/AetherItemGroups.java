package com.aether.items;

import com.aether.Aether;
import com.aether.blocks.AetherBlocks;
import com.aether.items.utils.ItemGroupExpansions;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AetherItemGroups {
    public static final CreativeModeTab Blocks = build(
            Aether.locate("aether_blocks"),
            () -> new ItemStack(AetherBlocks.AETHER_GRASS_BLOCK));

    public static final CreativeModeTab Tools = build(
            Aether.locate("aether_tools"),
            () -> new ItemStack(AetherItems.GRAVITITE_PICKAXE));

    public static final CreativeModeTab Food = build(
            Aether.locate("aether_food"),
            () -> new ItemStack(AetherItems.BLUEBERRY));

    public static final CreativeModeTab Resources = build(
            Aether.locate("aether_resources"),
            () -> new ItemStack(AetherItems.AMBROSIUM_SHARD));

    public static final CreativeModeTab Misc = build(
            Aether.locate("aether_misc"),
            () -> new ItemStack(AetherItems.BRONZE_KEY));

    public static final CreativeModeTab Wearable = build(
            Aether.locate("aether_wearables"),
            () -> new ItemStack(AetherItems.ZANITE_CHESTPLATE));

    private static CreativeModeTab build(ResourceLocation id, @Nullable Supplier<ItemStack> stackSupplier, @Nullable Consumer<List<ItemStack>> stacksForDisplay) {
        ((ItemGroupExpansions) CreativeModeTab.TAB_BUILDING_BLOCKS).expandArray();
        return new CreativeModeTab(CreativeModeTab.TABS.length - 1, String.format("%s.%s", id.getNamespace(), id.getPath())) {
            @Override
            public ItemStack makeIcon() {
                if (stackSupplier != null) {
                    return stackSupplier.get();
                } else {
                    return ItemStack.EMPTY;
                }
            }

            @Override
            public void fillItemList(NonNullList<ItemStack> stacks) {
                if (stacksForDisplay != null) {
                    stacksForDisplay.accept(stacks);
                    return;
                }

                super.fillItemList(stacks);
            }
        };
    }

    private static CreativeModeTab build(ResourceLocation id, @Nullable Supplier<ItemStack> stackSupplier) {
        return build(id, stackSupplier, null);
    }
}
