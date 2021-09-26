package com.aether.client.model;

import com.aether.items.AetherItems;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

public class AetherModelPredicates {

    public static void init() {
        ModelPredicateProviderRegistry.register(AetherItems.PHOENIX_BOW, new Identifier("pull"), ((stack, world, entity, seed) -> {
            if (entity == null) {
                return 0F;
            }
            return entity.getActiveItem() != stack ? 0F : (stack.getMaxUseTime() - entity.getItemUseTimeLeft()) / 20F;
        }));

        ModelPredicateProviderRegistry.register(AetherItems.PHOENIX_BOW, new Identifier("pulling"), (itemStack, clientWorld, livingEntity, seed) -> {
            if (livingEntity == null) {
                return 0.0F;
            }
            return livingEntity.isUsingItem() && livingEntity.getActiveItem() == itemStack ? 1.0F : 0.0F;
        });
    }
}
