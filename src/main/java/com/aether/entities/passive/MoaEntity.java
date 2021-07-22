package com.aether.entities.passive;

import com.aether.api.MoaAPI;
import com.aether.api.MoaAttributes;
import com.aether.blocks.blockentity.FoodBowlBlockEntity;
import com.aether.component.MoaGenes;
import com.aether.entities.AetherEntityTypes;
import com.aether.entities.util.SaddleMountEntity;
import com.aether.items.AetherItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

//import com.aether.world.storage.loot.AetherLootTableList;

public class MoaEntity extends SaddleMountEntity implements PlayerRideableJumping, OwnableEntity {

    public static final EntityDataAccessor<Integer> AIR_TICKS = SynchedEntityData.defineId(MoaEntity.class, EntityDataSerializers.INT);
    public float curWingRoll, curWingYaw, curLegPitch;
    public float jumpStrength;
    public boolean isInAir;
    protected int secsUntilEgg;
    private MoaGenes genes;

    public MoaEntity(Level world) {
        super(AetherEntityTypes.MOA, world);

        this.maxUpStep = 1.0F;
        this.secsUntilEgg = this.getRandomEggTime();
    }

    public static AttributeSupplier.Builder initAttributes() {
        return AetherEntityTypes.getDefaultAttributes()
                .add(Attributes.MAX_HEALTH, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MoaEscapeDangerGoal(this, 2));
        this.goalSelector.addGoal(2, new EatFromBowlGoal(1, 24, 16));
        this.goalSelector.addGoal(3, new BreedGoal(this, 0.25F));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0D, Ingredient.of(AetherItems.NATURE_STAFF), false));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.65F, 0.1F)); //WanderGoal
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 4.5F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this)); //LookGoal
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData entityData, @Nullable CompoundTag entityNbt) {
        if(!genes.isInitialized()) {
            genes.initMoa(this);
            setHealth(genes.getAttribute(MoaAttributes.MAX_HEALTH));
        }
        return super.finalizeSpawn(world, difficulty, spawnReason, entityData, entityNbt);
    }

    @Override
    public void move(MoverType movement, Vec3 motion) {
        super.move(movement, motion);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(AIR_TICKS, 0);
    }

    public float getWingRoll() {
        if(!isGliding()) {
            float baseWingRoll = 1.39626F;

            float lDif = -baseWingRoll - curWingRoll;
            if(Math.abs(lDif) > 0.005F) {
                curWingRoll += lDif / 6;
            }
        }
        else {
            curWingRoll = (Mth.sin(tickCount / 1.75F) * 0.725F + 0.1F);
        }
        return curWingRoll;
    }

    public float getWingYaw() {
        float baseWingYaw = isGliding() ? 0.95626F : 0.174533F;

        float lDif = -baseWingYaw - curWingYaw;
        if(Math.abs(lDif) > 0.005F) {
            curWingYaw += lDif / 12.75;
        }
        return curWingYaw;
    }

    public float getLegPitch() {
        float baseLegPitch = isGliding() ? -1.5708F : 0.0174533F;

        float lDif = -baseLegPitch - curLegPitch;
        if(Math.abs(lDif) > 0.005F) {
            curLegPitch += lDif / 6;
        }
        return curLegPitch;
    }

    public int getRandomEggTime() {
        return 775 + this.random.nextInt(50);
    }

    @Override
    protected void playHurtSound(DamageSource source) {
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BAT_DEATH, SoundSource.NEUTRAL, 0.225F, Mth.clamp(this.random.nextFloat(), 0.5f, 0.7f) + Mth.clamp(this.random.nextFloat(), 0f, 0.15f));
    }

    @Override
    public void tick() {
        super.tick();

        isInAir = !onGround;

        if(isInAir)
            entityData.set(AIR_TICKS, entityData.get(AIR_TICKS) + 1);
        else
            entityData.set(AIR_TICKS, 0);

        if (tickCount % 15 == 0) {
            if(isGliding()) {
                this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PHANTOM_FLAP, SoundSource.NEUTRAL, 4.5F, Mth.clamp(this.random.nextFloat(), 0.85f, 1.2f) + Mth.clamp(this.random.nextFloat(), 0f, 0.35f));
            }
            else if(random.nextFloat() < 0.057334F) {
                this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PARROT_AMBIENT, SoundSource.NEUTRAL, 1.5F + random.nextFloat() * 2, Mth.clamp(this.random.nextFloat(), 0.55f, 0.7f) + Mth.clamp(this.random.nextFloat(), 0f, 0.25f));
            }
        }

        if (this.jumping) this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.05D, 0.0D));

        this.fall();

        if (!this.level.isClientSide && !this.isBaby() && this.getPassengers().isEmpty()) {
            if (this.secsUntilEgg > 0) {
                if (this.tickCount % 20 == 0) this.secsUntilEgg--;
            } else {
                this.playSound(SoundEvents.CHICKEN_EGG, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);

                this.secsUntilEgg = this.getRandomEggTime();
            }
        }
        MoaGenes genes = getGenes();
        float hunger = genes.getHunger();
        if(genes.isTamed()) {
            if(random.nextBoolean()) {
                genes.setHunger(hunger - (1F / 12000F));
            }
        }
        if(getHealth() < getMaxHealth() && hunger > 65F && level.getGameTime() % 20 == 0 && random.nextBoolean()) {
            heal(1);
            genes.setHunger(hunger - 0.5F);
        }
        if(hunger < 20F && level.getGameTime() % 10 + random.nextInt(4) == 0) {
            produceParticles(ParticleTypes.ANGRY_VILLAGER);
            if(hunger < 10F) {
                ejectPassengers();
            }
        }
    }

    protected void produceParticles(ParticleOptions parameters) {
        for(int i = 0; i < 5; ++i) {
            double d = this.random.nextGaussian() * 0.02D;
            double e = this.random.nextGaussian() * 0.02D;
            double f = this.random.nextGaussian() * 0.02D;
            this.level.addParticle(parameters, this.getRandomX(1.0D), this.getRandomY() + 1.0D, this.getRandomZ(1.0D), d, e, f);
        }

    }

    @Override
    @Environment(EnvType.CLIENT)
    protected Component getTypeName() {
        return new TranslatableComponent(MoaAPI.formatForTranslation(getGenes().getRace().id()));
    }

    @Override
    protected int calculateFallDamage(float fallDistance, float damageMultiplier) {
        return 0;
    }

    public boolean isGliding() {
        return !isInWater() && entityData.get(AIR_TICKS) > 20;
    }

    public boolean isFood(ItemStack stack) {
        return stack.getItem() == AetherItems.ORANGE;
    }

    @Override
    public boolean isSaddleable() {
        return getGenes().isTamed();
    }

    public void travel(Vec3 movementInput) {
        if (this.isAlive()) {
            if (this.isVehicle() && this.canBeControlledByRider() && this.isSaddled()) {
                LivingEntity livingEntity = (LivingEntity)this.getControllingPassenger();
                this.yRotO = this.getYRot();
                this.setYRot(livingEntity.getYRot());
                this.setXRot(livingEntity.getXRot() * 0.5F);
                this.setRot(this.getYRot(), this.getXRot());
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.yBodyRot;
                float f = livingEntity.xxa * 0.5F;
                float g = livingEntity.zza;
                if (g <= 0.0F) {
                    g *= 0.25F;
                }

                if(isControlledByLocalInstance()) {
                    if (this.jumpStrength > 0.0F && !this.isInAir) {
                        double d = 0.1F * (double)this.jumpStrength * (double)this.getBlockJumpFactor();
                        double h;
                        if (this.hasEffect(MobEffects.JUMP)) {
                            h = d + (double)((float)(this.getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.1F);
                        } else {
                            h = d;
                        }

                        Vec3 vec3d = this.getDeltaMovement();
                        this.setDeltaMovement(vec3d.x, h, vec3d.z);
                        this.hasImpulse = true;
                        if (g > 0.0F) {
                            float adjVel = jumpStrength / 2F;
                            float i = Mth.sin(this.getYRot() * 0.017453292F);
                            float j = Mth.cos(this.getYRot() * 0.017453292F);
                            this.setDeltaMovement(this.getDeltaMovement().add(-0.4F * i * adjVel, 0.0D, 0.4F * j * adjVel));
                        }

                        this.jumpStrength = 0.0F;
                    }
                }

                if(jumpStrength <= 0.01F && onGround)
                    isInAir = false;

                this.flyingSpeed = getFlyingSpeed();
                if (this.isControlledByLocalInstance()) {
                    this.setSpeed(getMountedMoveSpeed());
                    super.travel(new Vec3(f, movementInput.y, g));
                } else if (livingEntity instanceof Player) {
                    this.setDeltaMovement(Vec3.ZERO);
                }

                this.calculateEntityAnimation(this, false);
            } else {
                this.flyingSpeed = getFlyingSpeed();
                super.travel(movementInput);
            }
        }
    }

    @Override
    public float getMountedMoveSpeed() {
        return getSpeed() * 0.75F;
    }

    @Override
    public float getSpeed() {
        return getGenes().getAttribute(MoaAttributes.GROUND_SPEED) * 0.65F;
    }

    public float getFlyingSpeed() {
        return isGliding() ? getGenes().getAttribute(MoaAttributes.GLIDING_SPEED) * 0.8F : getSpeed() * 0.1F;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        Item heldItem = heldStack.getItem();
        if(heldItem.isEdible() && heldItem.getFoodProperties().isMeat()) {
            if(!getGenes().isTamed()) {
                if(random.nextFloat() < 0.15F) {
                    getGenes().tame(player.getUUID());
                    produceParticles(ParticleTypes.HEART);
                    playSound(SoundEvents.PARROT_AMBIENT, 2F, 2F);
                }
                heldStack.shrink(1);
                playSound(SoundEvents.PARROT_EAT, 1F, 0.8F);
            }
            else {
                float hungerRestored = heldItem.getFoodProperties().getNutrition() * 4;
                float satiation = getGenes().getHunger();
                float hunger = 100 - satiation;
                if(hunger > 1) {
                    int consumption = Math.min((int) Math.ceil(hunger / hungerRestored), heldStack.getCount());
                    triggerItemUseEffects(heldStack, 10 + random.nextInt(consumption * 2 + 1));
                    heldStack.shrink(consumption);
                    getGenes().setHunger(satiation + (consumption * hungerRestored));
                    playSound(SoundEvents.PARROT_EAT, 1.5F, 0.8F);
                    produceParticles(ParticleTypes.HAPPY_VILLAGER);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("airTicks", entityData.get(AIR_TICKS));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        entityData.set(AIR_TICKS, compound.getInt("airTicks"));
    }

    @Override
    public boolean canSpawnSprintParticle() {
        return Math.abs(getDeltaMovement().multiply(1, 0, 1).length()) > 0 && !isInWater() && !isGliding();
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel world, Animal other) {
        MoaGenes genes = getGenes();
        if(genes.getHunger() > 80F && genes.isTamed() && other instanceof MoaEntity moa && moa.getGenes().isTamed()) {
            ItemStack egg = genes.getEggForBreeding(((MoaEntity) other).genes, world, blockPosition());
            playSound(SoundEvents.TURTLE_LAY_EGG, 0.8F, 1.5F);

            Containers.dropItemStack(world, getX(), getY(), getZ(), egg);
            this.setAge(6000);
            other.setAge(6000);
            this.resetLove();
            other.resetLove();
            world.broadcastEntityEvent(this, (byte)18);
            if (world.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                world.addFreshEntity(new ExperienceOrb(world, this.getX(), this.getY(), this.getZ(), this.getRandom().nextInt(16) + 4));
            }
        }
    }

    @Override
    protected void playStepSound(BlockPos posIn, BlockState stateIn) {
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PIG_STEP, SoundSource.NEUTRAL, 0.15F, 1F);
    }

    public void fall() {
        if (this.getDeltaMovement().y < 0.0D && !this.isShiftKeyDown())
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D,  isGliding() ? getGenes().getAttribute(MoaAttributes.GLIDING_DECAY) * 1.4 : 1D, 1.0D));
    }

    @Override
    public void setJumping(boolean jump) {
        super.setJumping(jump);
    }

    @Override
    public double getPassengersRidingOffset() {
        return 1.03;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel world, AgeableMob matingAnimal) {
        return new MoaEntity(this.level);
    }

    @Override
    public ResourceLocation getDefaultLootTable() {
        return null;
    }

    @Override
    public void onPlayerJump(int strength) {
        jumpStrength = strength * getGenes().getAttribute(MoaAttributes.JUMPING_STRENGTH) * 0.95F;
    }

    @Override
    public boolean canJump() {
        return true;
    }

    @Override
    public void handleStartJump(int height) {
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PHANTOM_FLAP, SoundSource.NEUTRAL, 7.5F, Mth.clamp(this.random.nextFloat(), 0.55f, 0.8f));
    }

    @Override
    public void handleStopJump() {
    }

    public MoaGenes getGenes() {
        if(genes == null) {
            genes = MoaGenes.get(this);
        }
        return genes;
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return getGenes().getOwner();
    }

    @Nullable
    @Override
    public Entity getOwner() {
        return Optional.ofNullable(getOwnerUUID()).map(level::getPlayerByUUID).orElse(null);
    }

    private class MoaEscapeDangerGoal extends PanicGoal {

        public MoaEscapeDangerGoal(PathfinderMob mob, double speed) {
            super(mob, speed);
        }

        @Override
        public boolean canUse() {
            boolean ownerNear = MoaEntity.this.getLastHurtByMob() != MoaEntity.this.getOwner();
            return ownerNear && super.canUse();
        }
    }

    public class EatFromBowlGoal extends MoveToBlockGoal {
        protected int timer;

        public EatFromBowlGoal(double speed, int range, int maxYDifference) {
            super(MoaEntity.this, speed, range, maxYDifference);
        }

        public double acceptedDistance() {
            return 2.0D;
        }

        public boolean shouldRecalculatePath() {
            return this.tryTicks % 100 == 0;
        }

        protected boolean isValidTarget(LevelReader world, BlockPos pos) {
            if(world.getBlockEntity(pos) instanceof FoodBowlBlockEntity foodBowl) {
                ItemStack foodStack = foodBowl.getItem(0);
                Item foodItem = foodStack.getItem();
                return foodItem.isEdible() && foodItem.getFoodProperties().isMeat();
            }
            return false;
        }

        public void tick() {
            if (this.isReachedTarget()) {
                if (this.timer >= 20) {
                    this.tryEat();
                } else {
                    ++this.timer;
                }
            } else if (!this.isReachedTarget() && MoaEntity.this.random.nextFloat() < 0.025F) {
                MoaEntity.this.playSound(SoundEvents.PARROT_DEATH, 0.5F, 2.0F);
            }

            super.tick();
        }

        protected void tryEat() {
            if(level.getBlockEntity(blockPos) instanceof FoodBowlBlockEntity foodBowl) {
                ItemStack foodStack = foodBowl.getItem(0);
                Item foodItem = foodStack.getItem();
                if(foodItem.isEdible() && foodItem.getFoodProperties().isMeat()) {
                    float hungerRestored = foodItem.getFoodProperties().getNutrition() * 4;
                    float satiation = getGenes().getHunger();
                    float hunger = 100 - satiation;
                    if(hunger > 1) {
                        int consumption = Math.min((int) Math.ceil(hunger / hungerRestored), foodStack.getCount());
                        triggerItemUseEffects(foodStack, 10 + random.nextInt(consumption * 2 + 1));
                        foodStack.shrink(consumption);
                        getGenes().setHunger(satiation + (consumption * hungerRestored));
                        playSound(SoundEvents.PARROT_EAT, 1.5F, 0.8F);
                        produceParticles(ParticleTypes.HAPPY_VILLAGER);
                    }
                }
            }
        }

        @Override
        public boolean canUse() {
            return getGenes().getHunger() < 80F && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return MoaEntity.this.getGenes().getHunger() < 98F && super.canContinueToUse();
        }

        public void start() {
            this.timer = 0;
            super.start();
        }
    }
}