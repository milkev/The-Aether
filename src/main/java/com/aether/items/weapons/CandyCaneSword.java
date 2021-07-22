package com.aether.items.weapons;

import com.aether.items.AetherItems;
import com.aether.items.tools.AetherSword;
import com.aether.items.utils.AetherTiers;
import java.util.Random;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CandyCaneSword extends AetherSword {
    public CandyCaneSword(Properties settings) {
        super(AetherTiers.CANDY, 3, -2, settings);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.getItem() == AetherItems.CANDY_CANE;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target.deathTime > 0) {
            return true;
        } else {
            if ((new Random()).nextBoolean() && attacker instanceof Player && !attacker.level.isClientSide && target.hurtTime > 0)
                target.spawnAtLocation(AetherItems.CANDY_CANE, 1);
            stack.hurtAndBreak(1, attacker, null);
            return true;
        }
    }
}