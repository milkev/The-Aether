package com.aether.entities.hostile;

import com.aether.blocks.AetherBlocks;
import com.aether.entities.AetherEntityTypes;
import com.aether.items.AetherItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class WhiteSwetEntity extends TransformableSwetEntity{

    public WhiteSwetEntity(Level world){
        super(AetherEntityTypes.WHITE_SWET, world);
        setSize(2, true);
    }

    @Override
    protected void onEntityCollision(Entity entity){
        if (getSize() > 1 && entity instanceof LivingEntity livingEntity) {
            MobEffectInstance[] effects = livingEntity.getActiveEffects().toArray(new MobEffectInstance[0]);
            for (MobEffectInstance effect : effects) {
                this.addEffect(effect, livingEntity);
                livingEntity.removeEffect(effect.getEffect());
            }
        }
        super.onEntityCollision(entity);
    }
}
