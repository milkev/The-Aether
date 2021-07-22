package com.aether.items.tools;

import com.aether.items.utils.AetherTiers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
        if (this.getItemMaterial() == AetherTiers.ZANITE) return original + this.calculateIncrease(stack);
        return original;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult defaultResult = super.useOn(context);
        return defaultResult != InteractionResult.PASS ? defaultResult : IAetherTool.super.useOnBlock(context, defaultResult);
    }

    @Override
    public AetherTiers getItemMaterial() {
        return this.material;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand){
        return IAetherTool.super.useOnEntity(stack, player, entity, hand);
    }
}