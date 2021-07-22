package com.aether.mixin.entity;

import com.aether.blocks.AetherBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net/minecraft/world/entity/animal/Rabbit$RaidGardenGoal")
public abstract class EatCarrotCropGoalMixin {
    @Redirect(method = "isValidTarget(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    public boolean isThisOrAetherFarmland(BlockState self, Block block) {
        return self.is(block) || self.is(AetherBlocks.AETHER_FARMLAND);
    }
}
