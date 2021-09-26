package com.aether.entities.ai;

import com.aether.blocks.AetherBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.function.Predicate;

public class EatAetherGrassGoal extends Goal {
    private static final Predicate<BlockState> grass = BlockStatePredicate.forBlock(AetherBlocks.AETHER_GRASS_BLOCK);

    private final MobEntity owner;
    private final World world;
    private int timer;

    public EatAetherGrassGoal(MobEntity entity) {
        this.owner = entity;
        this.world = entity.world;

        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
    }

    @Override
    public boolean canStart() {
        if (this.owner.getRandom().nextInt(this.owner.isBaby() ? 50 : 1000) != 0) {
            return false;
        } else {
            BlockPos pos = new BlockPos(this.owner.getBlockPos());

            if (grass.test(this.world.getBlockState(pos))) return true;
            else return this.world.getBlockState(pos.down()).getBlock() == AetherBlocks.AETHER_GRASS_BLOCK;
        }
    }

    @Override
    public void start() {
        this.timer = 40;
        this.world.sendEntityStatus(this.owner, (byte) 10);
        this.owner.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.timer = 0;
    }

    @Override
    public boolean shouldContinue() {
        return this.timer > 0;
    }

    @Override
    public void tick() {
        this.timer = Math.max(0, this.timer - 1);

        if (this.timer == 4) {
            BlockPos pos = new BlockPos(this.owner.getBlockPos());

            if (grass.test(this.world.getBlockState(pos))) {
                if (this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) this.world.breakBlock(pos, false);

                this.owner.onEatingGrass();
            } else {
                BlockPos downPos = pos.down();

                if (this.world.getBlockState(downPos).getBlock() == AetherBlocks.AETHER_GRASS_BLOCK) {
                    if (this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                        this.world.syncGlobalEvent(2001, downPos, Block.getRawIdFromState(AetherBlocks.AETHER_GRASS_BLOCK.getDefaultState()));
                        this.world.setBlockState(downPos, AetherBlocks.AETHER_DIRT.getDefaultState(), 2);
                    }

                    this.owner.onEatingGrass();
                }
            }
        }
    }

    public int getTimer() {
        return this.timer;
    }
}