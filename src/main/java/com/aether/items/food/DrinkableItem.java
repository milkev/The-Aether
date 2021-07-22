package com.aether.items.food;

import com.aether.items.AetherItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class DrinkableItem extends Item {

    public DrinkableItem(Properties settings) {
        super(settings);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if(!(user instanceof Player) || !((Player) user).isCreative()) {
            if (this == AetherItems.AETHER_MILK) {
                return new ItemStack(AetherItems.QUICKSOIL_VIAL);
            }
        }
        return super.finishUsingItem(stack, world, user);
    }
}
