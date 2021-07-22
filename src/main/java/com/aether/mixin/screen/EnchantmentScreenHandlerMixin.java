package com.aether.mixin.screen;

import com.aether.blocks.AetherBlocks;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentScreenHandlerMixin {
    @Redirect(method = "slotsChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    public boolean isThisOrSkyrootBookshelf(BlockState target, Block block) {
        return target.is(block) || target.is(AetherBlocks.SKYROOT_BOOKSHELF);
    }
}