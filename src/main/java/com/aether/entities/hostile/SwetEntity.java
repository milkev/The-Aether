package com.aether.entities.hostile;

import com.aether.entities.AetherEntityTypes;
import com.aether.items.AetherItems;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class SwetEntity extends Slime {

    protected int initialSize = 2;
    protected float massStuck = 0;
    protected static final AttributeModifier knockbackResistanceModifier = new AttributeModifier(
            "Temporary swet knockback resistance",
            1,
            AttributeModifier.Operation.ADDITION);

    public SwetEntity(Level world) {
        this(AetherEntityTypes.WHITE_SWET, world);
    }

    public SwetEntity(EntityType<? extends SwetEntity> entityType, Level world) {
        super(entityType, world);
        init();
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if(stack.is(AetherItems.SWET_SPAWN_EGG)
                || stack.is(AetherItems.BLUE_SWET_SPAWN_EGG)
                || stack.is(AetherItems.PURPLE_SWET_SPAWN_EGG)
                || stack.is(AetherItems.GOLDEN_SWET_SPAWN_EGG)
                || stack.is(AetherItems.SWET_BALL)){
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            this.setSize(this.getSize() + 1, true);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void registerGoals() {
        // Replace the inherited slime target selectors with one that avoids chasing absorbed players, and ignores iron golems
        super.registerGoals();
        this.targetSelector.removeAllGoals();
        this.targetSelector.addGoal(1, new FollowUnabsorbedTargetGoal<>(
                this, Player.class, 10, true, false, (player) ->
                Math.abs(player.getY() - this.getY()) <= 4.0D &&
                        !(canAbsorb(this, player))
        ));
    }

    protected void init() {
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(25);
        setHealth(getMaxHealth());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    public static AttributeSupplier.Builder initAttributes() {
        return AetherEntityTypes.getDefaultAttributes()
                .add(Attributes.FOLLOW_RANGE, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28000000417232513D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.ATTACK_SPEED, 0.25D)
                .add(Attributes.MAX_HEALTH, 25.0D);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putBoolean("Oversize", this.getSize()>=20);
    }

    @Override
    public void tick() {
        // Entities don't have onEntityCollision, so this does that
        if (!this.isDeadOrDying()) {
            massStuck = 0;
            level.getEntities(this, this.getBoundingBox().expandTowards(0.9, 0.9, 0.9)).forEach((entity) -> {
                AABB box = entity.getBoundingBox();
                massStuck += box.getXsize() * box.getYsize() * box.getZsize();
            });
            level.getEntities(this, this.getBoundingBox()).forEach(this::onEntityCollision);
        }
        super.tick();
    }

    @Override
    public void playerTouch(Player player) {
        // Already taken care of in tick()
    }

    protected void onEntityCollision(Entity entity){
        if (entity instanceof SwetEntity swet) {
            if (this.getSize() >= swet.getSize() && !swet.isDeadOrDying()) {
                this.setSize(Mth.ceil(Mth.sqrt(this.getSize() * this.getSize() + swet.getSize() * swet.getSize())), true);
                swet.discard();
            }
            return;
        }
        if (entity.canBeCollidedWith()){
            return;
        }
        // vehicles
        if (entity instanceof Boat || entity instanceof Minecart){
            return;
        }
        // Move this to vermillion swets (?) once they are added.
        // Ask Azzy about it 🤷‍
//        if (entity instanceof TntMinecartEntity tnt){
//            if (!tnt.isPrimed() && this.getSize() >= 4){
//                tnt.prime();
//            }
//        }
        // Make items ride the swet. They often shake free with the jiggle physics
        if (entity instanceof ItemEntity item) {
            if (item.getItem().getItem() == AetherItems.SWET_BALL) {
                this.setSize(this.getSize() + 1, false);
                item.remove(RemovalReason.KILLED);
                return;
            }
            item.startRiding(this, true);
            return;
        }
        boolean canPickupNonPlayers = level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        boolean isPet = (entity instanceof TamableAnimal pet && pet.isTame());
        boolean isEligiblePet = isPet && level.getDifficulty() != Difficulty.EASY;
        boolean isEligibleNonPlayer = !(entity instanceof Player || isPet) && canPickupNonPlayers;
        boolean canBePickedUp = isAbsorbable(entity) && (entity instanceof Player || isEligiblePet || isEligibleNonPlayer);
        if (canBePickedUp) {
            // The higher the number this is multiplied by, the stiffer the wobble is
            // If the wobbles feel too sharp, try changing the clamp below
            if (massStuck < 1){
                massStuck = 1;
            }
            Vec3 suckVelocity = this.getBoundingBox().getCenter().subtract(entity.position()).scale(Mth.clamp(0.25 + massStuck/100,0,1))
                    .add(this.getDeltaMovement().subtract(entity.getDeltaMovement()).scale(0.45 / massStuck / this.getSize()));
            Vec3 newVelocity = entity.getDeltaMovement().add(suckVelocity);
            double velocityClamp = this.getSize() * 0.1 + 0.25;
            entity.setDeltaMovement(Mth.clamp(newVelocity.x(), -velocityClamp, velocityClamp),
                    Math.min(newVelocity.y(), 0.25),
                    Mth.clamp(newVelocity.z(), -velocityClamp, velocityClamp));
            entity.hasImpulse = true;
            entity.fallDistance = 0;
        }

        if (entity instanceof LivingEntity livingEntity) {
            // Hack to prevent knockback; TODO: find a better way to prevent knockback
            AttributeInstance knockbackResistance = livingEntity.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (canBePickedUp && knockbackResistance != null) {
                knockbackResistance.addTransientModifier(knockbackResistanceModifier);
                this.doHurtTarget(livingEntity);
                knockbackResistance.removeModifier(knockbackResistanceModifier);
            } else {
                this.doHurtTarget(livingEntity);
            }
        }
    }

    // Prevent pushing entities away, as this interferes with sucking them in
    @Override
    public boolean isPushable() {
        return false;
    }

    // Same as above
    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public void setSize(int size, boolean heal){
        super.setSize(size, heal);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData entityData, @Nullable CompoundTag entityNbt){
        setSize(initialSize, true);
        this.getAttribute(Attributes.FOLLOW_RANGE).addPermanentModifier(new AttributeModifier(
                "Random spawn bonus",
                this.random.nextGaussian() * 0.05D,
                AttributeModifier.Operation.MULTIPLY_BASE));
        this.setLeftHanded(this.random.nextFloat() < 0.05F);

        return entityData;
    }

    // Prevents duplicate entities
    @Override
    public void remove(RemovalReason reason) {
        this.setRemoved(reason);
        if (reason == Entity.RemovalReason.KILLED) {
            this.gameEvent(GameEvent.ENTITY_KILLED);
        }
    }

    @Override
    protected ParticleOptions getParticleType() {
        return ParticleTypes.SPLASH;
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return this.getType().getDefaultLootTable();
    }

    protected static boolean canAbsorb(Entity swet, Entity target) {
        return isAbsorbable(target) &&
                swet.getBoundingBox().inflate(0, 0.5, 0).move(0, 0.25, 0).intersects(target.getBoundingBox());
    }

    protected static boolean isAbsorbable(Entity entity) {
        return !(entity.isShiftKeyDown() || entity instanceof Player playerEntity && playerEntity.getAbilities().flying);
    }

    protected static class FollowUnabsorbedTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
        public FollowUnabsorbedTargetGoal(Mob mob, Class<T> targetClass, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, @Nullable Predicate<LivingEntity> targetPredicate) {
            super(mob, targetClass, reciprocalChance, checkVisibility, checkCanNavigate, targetPredicate);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.mob.getTarget();
            if (target == null) {
                target = this.targetMob;
            }
            return super.canContinueToUse() &&
                    !(canAbsorb(this.mob, this.mob.getTarget()));
        }
    }
}