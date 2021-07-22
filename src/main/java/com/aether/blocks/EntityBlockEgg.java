package com.aether.blocks;

import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class EntityBlockEgg extends Block {
    private final BiFunction<Level, BlockPos, ? extends LivingEntity> function;

    public EntityBlockEgg(Properties settings, BiFunction<Level, BlockPos, ? extends LivingEntity> func) {
        super(settings);
        this.function = func;
    }

    public EntityBlockEgg(Properties settings, EntityType<? extends LivingEntity> type) {
        super(settings);
        this.function = (world, pos) -> {
            LivingEntity entity = type.create(world);
            if (entity!=null) entity.setPos(Vec3.atLowerCornerOf(pos));
            return entity;
        };
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (!player.isCreative()) {
            world.addFreshEntity(function.apply(world, pos));
        }
        world.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
    }
}
