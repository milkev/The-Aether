package com.aether.mixin.entity;

import com.aether.api.MoaAttributes;
import com.aether.entities.AetherEntityExtensions;
import com.aether.entities.passive.MoaEntity;
import com.aether.items.AetherItems;
import com.aether.items.utils.AetherTiers;
import com.google.common.collect.Sets;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.Set;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity implements AetherEntityExtensions {
    public MixinLivingEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    private boolean flipped = false;

    private int gravFlipTime;

    @Override
    public boolean getFlipped(){
        return flipped;
    }

    @Override
    public void setFlipped(){
        flipped = true;
        gravFlipTime = 0;
    }

    @Shadow
    public abstract boolean hasEffect(MobEffect effect);

    @ModifyVariable(method = "travel", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/entity/LivingEntity;isInWater()Z"))
    private double changeGravity(double gravity) {
        boolean isFalling = this.getDeltaMovement().y <= 0.0D;

        if ((Object) this instanceof Player) {
            Player playerEntity = (Player) (Object) this;
            Optional<TrinketComponent> componentOptional = TrinketsApi.getTrinketComponent(playerEntity);

            if (componentOptional.isPresent()) {
                // Get parachutes from trinket slots
                final Set<Item> validItems = Sets.newHashSet(AetherItems.CLOUD_PARACHUTE, AetherItems.GOLDEN_CLOUD_PARACHUTE);
                for (Item item : validItems) {
                    if (componentOptional.get().isEquipped(item)) {
                        if (isFalling && !this.hasEffect(MobEffects.SLOW_FALLING) && !isInWater() && !playerEntity.isShiftKeyDown()) {
                            gravity -= 0.07;
                            this.fallDistance = 0;
                        }
                        break;
                    }
                }
            }
        }

        return gravity;
    }

    @Inject(at = @At("RETURN"), method = "hurt")
    void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity attacker = source.getEntity();
        if (cir.getReturnValue() && attacker instanceof LivingEntity) {
            Item item = ((LivingEntity) attacker).getMainHandItem().getItem();
            if (item instanceof TieredItem && ((TieredItem) item).getTier() == AetherTiers.GRAVITITE.getDefaultTier()) {
                this.push(0, amount / 20 + 0.1, 0);
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "tick")
    public void tick(CallbackInfo ci){
        if(flipped){
            gravFlipTime++;
            if(gravFlipTime > 20){
                flipped = false;
                this.fallDistance = 0;
            }
            if(!this.isNoGravity()) {
                Vec3 antiGravity = new Vec3(0, 0.12D, 0);
                this.setDeltaMovement(this.getDeltaMovement().add(antiGravity));
            }
        }
    }

    @Inject(method = "getMaxHealth", at = @At("HEAD"), cancellable = true)
    public void getMoaMaxHealth(CallbackInfoReturnable<Float> cir) {
        if((Object) this instanceof MoaEntity moa) {
            var genes = moa.getGenes();
            cir.setReturnValue(genes.isInitialized() ? genes.getAttribute(MoaAttributes.MAX_HEALTH) : 40F);
            cir.cancel();
        }
    }
}
