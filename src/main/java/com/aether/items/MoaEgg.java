package com.aether.items;

import com.aether.api.MoaAPI;
import com.aether.api.MoaAttributes;
import com.aether.entities.passive.MoaEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MoaEgg extends Item {
    public MoaEgg(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext contextIn) {
        Level world = contextIn.getLevel();
        Player player = contextIn.getPlayer();
        ItemStack stack = contextIn.getItemInHand();
        if (player != null && stack.getOrCreateTag().contains("genes") && player.isCreative()) {
            MoaEntity moa = new MoaEntity(world);
            CompoundTag geneTag = stack.getTagElement("genes");
            boolean baby = geneTag.getBoolean("baby");
            moa.getGenes().readFromNbt(geneTag);
            if(baby) {
                moa.setAge(-43200);
            }
            moa.moveTo(contextIn.getClickedPos().above(), 0, 0);
            moa.setHealth(moa.getGenes().getAttribute(MoaAttributes.MAX_HEALTH));
            world.addFreshEntity(moa);
            return InteractionResult.sidedSuccess(world.isClientSide());
        }
        return super.useOn(contextIn);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        if(stack.getOrCreateTag().contains("genes")) {
            CompoundTag geneTag = stack.getTagElement("genes");
            ResourceLocation raceId = ResourceLocation.tryParse(geneTag.getString("raceId"));
            MoaAPI.Race race = MoaAPI.getRace(raceId);
            if(raceId != null) {
                tooltip.add(new TranslatableComponent(MoaAPI.formatForTranslation(raceId)).withStyle(race.legendary() ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_AQUA));
            }
            if(!geneTag.getBoolean("baby")) {
                tooltip.add(new TranslatableComponent("moa.egg.adult").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            }
        }
        super.appendHoverText(stack, world, tooltip, context);
    }
}