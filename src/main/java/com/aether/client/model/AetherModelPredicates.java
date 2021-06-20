package com.aether.client.model;

import com.aether.items.AetherItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public class AetherModelPredicates {

    public static void init() {
        ItemProperties.register(AetherItems.PHOENIX_BOW, new ResourceLocation("pull"), ((stack, world, entity, seed) -> {
            if (entity == null) {
                return 0F;
            }
            return entity.getUseItem() != stack ? 0F : (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 20F;
        }));

        ItemProperties.register(AetherItems.PHOENIX_BOW, new ResourceLocation("pulling"), (itemStack, clientWorld, livingEntity, seed) -> {
            if (livingEntity == null) {
                return 0.0F;
            }
            return livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0F : 0.0F;
        });
    }
}
