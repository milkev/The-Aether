package com.aether.mixin.village;

import com.aether.village.AetherVillagerProfessionExtensions;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VillagerProfession.class)
public abstract class VillagerProfessionMixin implements AetherVillagerProfessionExtensions {
    @Shadow
    @Mutable
    @Final
    private ImmutableSet<Block> secondaryPoi;

    @Override
    public void addSecondaryJobSite(Block jobSite) {
        secondaryPoi = ImmutableSet.<Block>builder().addAll(secondaryPoi).add(jobSite).build();
    }
}
