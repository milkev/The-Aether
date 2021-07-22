package com.aether.entities.hostile;

import com.aether.entities.AetherEntityTypes;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class PurpleSwetEntity extends SwetEntity{
    public PurpleSwetEntity(Level world){
        super(AetherEntityTypes.PURPLE_SWET, world);
        setSize(2, true);
    }

    protected void onEntityCollision(Entity entity){
        if (entity instanceof LivingEntity livingEntity)
            livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 4, 1));
        super.onEntityCollision(entity);
    }
}
