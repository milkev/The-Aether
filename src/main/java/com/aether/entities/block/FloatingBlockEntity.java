package com.aether.entities.block;

import com.aether.blocks.AetherBlocks;
import com.aether.blocks.FloatingBlock;
import com.aether.entities.AetherEntityTypes;
import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.List;
import java.util.function.Supplier;

public class FloatingBlockEntity extends Entity {
    protected static final EntityDataAccessor<BlockPos> ORIGIN = SynchedEntityData.defineId(FloatingBlockEntity.class, EntityDataSerializers.BLOCK_POS);
    public int floatTime;
    public boolean dropItem = true;
    public CompoundTag blockEntityData;
    private BlockState floatTile = AetherBlocks.GRAVITITE_ORE.defaultBlockState();
    private boolean dontSetBlock;
    private boolean hurtEntities;
    private int floatHurtMax = 40;
    private float floatHurtAmount = 2.0f;
    private Supplier<Boolean> dropState = () -> false;
    private boolean dropping = false;
    private boolean collides;

    public FloatingBlockEntity(EntityType<? extends FloatingBlockEntity> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
        setDropState(() -> {
            int distFromTop = worldIn.getMaxBuildHeight() - this.blockPosition().getY();
            return distFromTop <= 50;
        });
    }

    public FloatingBlockEntity(Level worldIn) {
        this(AetherEntityTypes.FLOATING_BLOCK, worldIn);
    }

    public FloatingBlockEntity(Level world, double x, double y, double z, BlockState floatingBlockState) {
        this(world);

        this.floatTile = floatingBlockState;
        this.blocksBuilding = true;
        this.setPos(x, y, z);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.setOrigin(new BlockPos(this.position()));
    }

    public FloatingBlockEntity(Level world, double x, double y, double z, BlockState floatingBlockState, Supplier<Boolean> dropSupplier) {
        this(world, x, y, z, floatingBlockState);
        this.setDropState(dropSupplier);
    }

    @Override
    public void setPos(double x, double y, double z) {
        if (entityData == null || floatTile == null) {
            super.setPos(x, y, z);
        } else {
            collides = true;
            BlockPos origin = entityData.get(ORIGIN);
            VoxelShape colShape = floatTile.getCollisionShape(level, origin);
            if (colShape.isEmpty()) {
                colShape = floatTile.getShape(level, origin);
                collides = false;
            }
            if (colShape.isEmpty()) {
                super.setPos(x, y, z);
            } else {
                this.setPosRaw(x, y, z);
                AABB box = colShape.bounds();
                this.setBoundingBox(box.move(position().subtract(new Vec3(Mth.lerp(0.5D, box.minX, box.maxX), 0, Mth.lerp(0.5D, box.minZ, box.maxZ)))));
            }
        }
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Environment(EnvType.CLIENT)
    public BlockPos getOrigin() {
        return this.entityData.get(ORIGIN);
    }

    public void setOrigin(BlockPos origin) {
        this.entityData.set(ORIGIN, origin);
        this.setPos(getX(), getY(), getZ());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ORIGIN, BlockPos.ZERO);
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved() && collides;
    }

    @Override
    public boolean canBeCollidedWith() {
        return this.isPickable();
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return !(other instanceof FloatingBlockEntity) && super.canCollideWith(other);
    }

    @Override
    public void tick() {}

    public void postTickEntities() {
        if (this.floatTile.isAir()) {
            this.discard();
        } else {
            this.xo = this.getX();
            this.yo = this.getY();
            this.zo = this.getZ();
            Block block = this.floatTile.getBlock();
            if (this.floatTime++ == 0) {
                BlockPos blockPos = this.blockPosition();
                if (this.level.getBlockState(blockPos).is(block)) {
                    this.level.removeBlock(blockPos, false);
                } else if (!this.level.isClientSide) {
                    this.discard();
                    return;
                }
            }

            boolean isFastFloater = (this.floatTile.getBlock() == AetherBlocks.GRAVITITE_ORE || this.floatTile.getBlock() == AetherBlocks.GRAVITITE_LEVITATOR || this.floatTile.getBlock() == AetherBlocks.BLOCK_OF_GRAVITITE);
            if (!this.isNoGravity()) {
                if (isFastFloater) {
                    this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.05D, 0.0D));
                } else {
                    if (!isDropping() && !shouldBeginDropping()) {
                        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.03D, 0.0D));
                    } else {
                        this.setDropping(true);
                        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.03D, 0.0D));
                    }
                }
            }

            this.move(MoverType.SELF, this.getDeltaMovement());

            // Take flight, my child!
            if (!FallingBlock.isFree(this.floatTile)) {
                List<Entity> otherEntities = this.level.getEntities(this, getBoundingBox());
                for (Entity entity : otherEntities) {
                    if (!(entity instanceof FloatingBlockEntity) && !entity.noPhysics && this.isPickable()) {
                        entity.setPos(entity.position().x, getBoundingBox().maxY, entity.position().z);
                        entity.fallDistance = 0F;
                    }
                    this.floatTile.getBlock().entityInside(floatTile, level, this.blockPosition(), entity);
                }
            }

            if (!this.level.isClientSide) {
                BlockPos blockPos = this.blockPosition();
                boolean isConcrete = this.floatTile.getBlock() instanceof ConcretePowderBlock;
                boolean shouldSolidify = isConcrete && this.level.getFluidState(blockPos).is(FluidTags.WATER);
                double speed = this.getDeltaMovement().lengthSqr();

                if (isConcrete && speed > 1.0D) {
                    BlockHitResult blockHitResult = this.level
                            .clip(new ClipContext(new Vec3(this.xo, this.yo, this.zo),
                                    new Vec3(this.getX(), this.getY(), this.getZ()), ClipContext.Block.COLLIDER,
                                    ClipContext.Fluid.SOURCE_ONLY, this));

                    if (blockHitResult.getType() != HitResult.Type.MISS
                            && this.level.getFluidState(blockHitResult.getBlockPos()).is(FluidTags.WATER)) {
                        blockPos = blockHitResult.getBlockPos();
                        shouldSolidify = true;
                    }
                }

                if ((!this.verticalCollision || this.onGround) && !shouldSolidify) {
                    if (!this.level.isClientSide && (blockPos.getY() < this.level.getMinBuildHeight() || blockPos.getY() > this.level.getMaxBuildHeight())) {
                        if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                            this.spawnAtLocation(block);
                        }
                        this.discard();
                    }
                } else {
                    this.land();
                }
            }

            if ((this.isDropping() || this.getDeltaMovement().y() == 0) && this.isOnGround()) {
                this.land();
            }

            this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
        }
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource damageSource) {
        if (this.hurtEntities) {
            int i = Mth.ceil(distance - 1.0F);
            if (i > 0) {
                List<Entity> list = Lists.newArrayList(this.level.getEntities(this, this.getBoundingBox()));
                boolean flag = this.floatTile.is(BlockTags.ANVIL);
                DamageSource damagesource = flag ? DamageSource.ANVIL : DamageSource.FALLING_BLOCK;

                for (Entity entity : list)
                    entity.hurt(damagesource, Math.min(Mth.floor(i * this.floatHurtAmount), this.floatHurtMax));

                if (flag && this.random.nextFloat() < 0.05F + i * 0.05F) {
                    BlockState blockstate = AnvilBlock.damage(this.floatTile);
                    if (blockstate == null) this.dontSetBlock = true;
                    else this.floatTile = blockstate;
                }
            }
        }
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.put("BlockState", NbtUtils.writeBlockState(this.floatTile));
        compound.putInt("Time", this.floatTime);
        compound.putBoolean("DropItem", this.dropItem);
        compound.putBoolean("Dropping", this.isDropping());
        compound.putBoolean("HurtEntities", this.hurtEntities);
        compound.putFloat("FallHurtAmount", this.floatHurtAmount);
        compound.putInt("FallHurtMax", this.floatHurtMax);
        if (this.blockEntityData != null) compound.put("TileEntityData", this.blockEntityData);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.floatTile = NbtUtils.readBlockState(compound.getCompound("BlockState"));
        this.floatTime = compound.getInt("Time");
        if (compound.contains("HurtEntities", 99)) {
            this.hurtEntities = compound.getBoolean("HurtEntities");
            this.floatHurtAmount = compound.getFloat("FallHurtAmount");
            this.floatHurtMax = compound.getInt("FallHurtMax");
        } else if (this.floatTile.is(BlockTags.ANVIL)) {
            this.hurtEntities = true;
        }

        if (compound.contains("DropItem", 99)) this.dropItem = compound.getBoolean("DropItem");

        if (compound.contains("Dropping", 99)) this.setDropping(compound.getBoolean("Dropping"));

        if (compound.contains("TileEntityData", 10)) this.blockEntityData = compound.getCompound("TileEntityData");

        if (this.floatTile.isAir()) this.floatTile = AetherBlocks.GRAVITITE_ORE.defaultBlockState();
    }

    @Environment(EnvType.CLIENT)
    public Level getWorldObj() {
        return this.level;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public void fillCrashReportCategory(CrashReportCategory section) {
        super.fillCrashReportCategory(section);
        section.setDetail("Immitating BlockState", this.floatTile.toString());
    }

    public BlockState getBlockState() {
        return this.floatTile;
    }

    public void setHurtEntities(boolean hurtEntitiesIn) {
        this.hurtEntities = hurtEntitiesIn;
    }

    public Supplier<Boolean> getDropState() {
        return dropState;
    }

    public boolean shouldBeginDropping() {
        return getDropState().get();
    }

    public void setDropState(Supplier<Boolean> supplier) {
        dropState = supplier;
    }

    public boolean isDropping() {
        return dropping;
    }

    public void setDropping(boolean dropping) {
        this.dropping = dropping;
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, Block.getId(this.getBlockState()));
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        this.floatTile = Block.stateById(packet.getData());
        this.blocksBuilding = true;
        double d = packet.getX();
        double e = packet.getY();
        double f = packet.getZ();
        this.setPos(d, e + (double)((1.0F - this.getBbHeight()) / 2.0F), f);
        this.setOrigin(this.blockPosition());
    }

    public interface ICPEM {
        void postTick();
    }

    private void land() {
        BlockPos blockPos = this.blockPosition();
        Block block = this.floatTile.getBlock();
        BlockState blockState = this.level.getBlockState(blockPos);
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, 0.5, 0.7));
        if (blockState.getBlock() != Blocks.MOVING_PISTON) {
            this.discard();
            if (!this.dontSetBlock) {
                boolean canReplace = blockState.canBeReplaced(new DirectionalPlaceContext(this.level, blockPos, Direction.UP, ItemStack.EMPTY, Direction.DOWN));
                boolean canPlace = this.floatTile.canSurvive(this.level, blockPos);

                if (canReplace && canPlace) {
                    if (this.floatTile.hasProperty(BlockStateProperties.WATERLOGGED) && this.level.getFluidState(blockPos).getType() == Fluids.WATER)
                        this.floatTile = this.floatTile.setValue(BlockStateProperties.WATERLOGGED, true);

                    if (this.level.setBlock(blockPos, this.floatTile, 3)) {
                        if (block instanceof FloatingBlock)
                            ((FloatingBlock) block).onEndFloating(this.level, blockPos, this.floatTile, blockState);

                        if (this.blockEntityData != null && this.floatTile.hasBlockEntity()) {
                            BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
                            if (blockEntity != null) {
                                CompoundTag compoundTag = blockEntity.save(new CompoundTag());

                                for (String keyName : this.blockEntityData.getAllKeys()) {
                                    Tag tag = this.blockEntityData.get(keyName);
                                    if (tag != null && !"x".equals(keyName) && !"y".equals(keyName) && !"z".equals(keyName)) {
                                        compoundTag.put(keyName, tag.copy());
                                    }
                                }

                                blockEntity.load(compoundTag);
                                blockEntity.setChanged();
                            }
                        }
                    } else if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                        this.spawnAtLocation(block);
                    }
                } else if (this.dropItem && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                    this.spawnAtLocation(block);
                }
            } else if (block instanceof FloatingBlock) {
                ((FloatingBlock) block).onBroken(this.level, blockPos);
            }
        }
    }
}