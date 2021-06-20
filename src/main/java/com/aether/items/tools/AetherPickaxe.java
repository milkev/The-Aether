package com.aether.items.tools;

import com.aether.items.utils.AetherTiers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

public class AetherPickaxe extends PickaxeItem implements IAetherTool {

    private final AetherTiers material;

    public AetherPickaxe(AetherTiers material, Properties settings, int damageVsEntity, float attackSpeed) {
        super(material.getDefaultTier(), damageVsEntity, attackSpeed, settings);
        this.material = material;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        float original = super.getDestroySpeed(stack, state);
        if (this.getItemMaterial() == AetherTiers.Zanite) return original + this.calculateIncrease(stack);
        return original;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult defaultResult = super.useOn(context);
        return IAetherTool.super.useOnBlock(context, defaultResult);
    }

    private float calculateIncrease(ItemStack tool) {
        return (float) tool.getMaxDamage() / tool.getDamageValue() / 50;
    }


    @Override
    public AetherTiers getItemMaterial() {
        return this.material;
    }
}