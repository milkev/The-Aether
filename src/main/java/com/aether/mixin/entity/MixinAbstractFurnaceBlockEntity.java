package com.aether.mixin.entity;

import com.aether.items.AetherItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class MixinAbstractFurnaceBlockEntity {
    @Inject(method = "getFuel", at = @At("RETURN"), cancellable = true)
    private static void fuelTimeMapHook(CallbackInfoReturnable<Map<Item, Integer>> info) {
        // Format: item, fuelTime
        final Map<Item, Integer> returnValue = info.getReturnValue();

        returnValue.put(AetherItems.AMBROSIUM_SHARD, 500);

        info.setReturnValue(returnValue);
    }
}
