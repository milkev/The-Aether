package com.aether.items.tools;

import com.aether.items.utils.AetherTiers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.state.BlockState;

public class AetherSword extends SwordItem implements IAetherTool {
    private final AetherTiers material;

    public AetherSword(AetherTiers material, float attackSpeed, int damageVsEntity, Properties settings) {
        super(material.getDefaultTier(), damageVsEntity, attackSpeed, settings);
        this.material = material;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        float original = super.getDestroySpeed(stack, state);
        if (material == AetherTiers.ZANITE) return original + this.calculateIncrease(stack);
        return original;
    }

    @Override
    public AetherTiers getItemMaterial() {
        return this.material;
    }
}