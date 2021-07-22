package com.aether.items.tools;

import com.aether.blocks.AetherBlocks;
import com.aether.items.utils.AetherTiers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class AetherHoe extends HoeItem implements IAetherTool {

    protected static final Map<Block, BlockState> convertibleBlocks = Maps.newHashMap(ImmutableMap.of(
            Blocks.GRASS_BLOCK, Blocks.FARMLAND.defaultBlockState(),
            Blocks.DIRT_PATH, Blocks.FARMLAND.defaultBlockState(),
            Blocks.DIRT, Blocks.FARMLAND.defaultBlockState(),
            Blocks.COARSE_DIRT, Blocks.DIRT.defaultBlockState()
    ));
    private final AetherTiers material;

    public AetherHoe(AetherTiers material, Properties settings, float attackSpeed) {
        super(material.getDefaultTier(), 1, attackSpeed, settings);
        this.material = material;
        setupConvertibleData();
    }

    private void setupConvertibleData() {
        final Map<Block, BlockState> modifiedConvertibles = Maps.newHashMap(ImmutableMap.of(
                AetherBlocks.AETHER_GRASS_BLOCK, AetherBlocks.AETHER_FARMLAND.defaultBlockState(),
                AetherBlocks.AETHER_DIRT_PATH, AetherBlocks.AETHER_FARMLAND.defaultBlockState(),
                AetherBlocks.AETHER_DIRT, AetherBlocks.AETHER_FARMLAND.defaultBlockState()
        ));
        convertibleBlocks.putAll(modifiedConvertibles);
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