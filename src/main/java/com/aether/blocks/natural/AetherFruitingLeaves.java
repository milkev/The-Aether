package com.aether.blocks.natural;

import com.aether.client.rendering.particle.AetherParticles;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class AetherFruitingLeaves extends AetherLeavesBlock {

    public static final IntegerProperty GROWTH = IntegerProperty.create("growth", 0, 2);
    public static final BooleanProperty CAPPED = BooleanProperty.create("capped");
    public static final BooleanProperty NATURAL = BooleanProperty.create("natural");
    private final Item fruit;

    public AetherFruitingLeaves(Properties settings, Item fruit) {
        super(settings, true);
        this.fruit = fruit;
        registerDefaultState(defaultBlockState().setValue(GROWTH, 0).setValue(CAPPED, false).setValue(NATURAL, false));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, Random random) {
        if(!state.getValue(NATURAL)) {
            int growth = state.getValue(GROWTH);
            if(!state.getValue(CAPPED)) {
                if(random.nextInt(60) == 0 && growth < 2) {
                    if(growth == 1) {
                        spawnPetalBurst(world, random, pos);
                    }
                    state = state.setValue(GROWTH, growth + 1).setValue(CAPPED, random.nextDouble() < 0.45 || growth + 1 == 2);
                    world.playSound(null, pos, SoundEvents.MOSS_BREAK, SoundSource.BLOCKS, 1.25F, 1.5F);
                    world.setBlockAndUpdate(pos, state);
                }
            }
            else {
                if(random.nextInt(240) == 0) {
                    if(growth == 1) {
                        spawnPetalBurst(world, random, pos);
                    }
                    else {
                        int dropBlocks = 0;
                        while (!world.isEmptyBlock(pos.below(dropBlocks + 1)) && dropBlocks < 16 && world.getBlockState(pos.below(dropBlocks)).is(this)) {
                            dropBlocks++;
                        }
                        world.addFreshEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() - (dropBlocks + 0.25), pos.getZ() + 0.5, new ItemStack(fruit), 0, 0, 0));
                        world.playSound(null, pos, SoundEvents.CANDLE_BREAK, SoundSource.BLOCKS, 1F, 1F);
                    }
                    world.setBlockAndUpdate(pos, defaultBlockState().setValue(DISTANCE, state.getValue(DISTANCE)));
                }
            }
        }
        super.randomTick(state, world, pos, random);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        Random random = world.getRandom();
        int growth = state.getValue(GROWTH);

        if(growth > 0) {
            world.setBlockAndUpdate(pos, state.setValue(GROWTH, 0).setValue(CAPPED, false));
            world.playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 1F, 2F);

            if(growth == 1) {
                spawnPetalBurst(world, random, pos);
            }
            else {
                int fortune = EnchantmentHelper.getEnchantments(player.getItemInHand(hand)).getOrDefault(Enchantments.BLOCK_FORTUNE, 0);
                ItemStack drops = new ItemStack(fruit, random.nextInt(fortune + 1 + random.nextInt(1)) + 1);
                if(!player.addItem(drops)) {
                    Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), drops);
                }
            }
            return InteractionResult.sidedSuccess(world.isClientSide());
        }

        return super.use(state, world, pos, player, hand, hit);
    }

    private void spawnPetalBurst(Level world, Random random, BlockPos pos) {
        for (int i = 0; i < random.nextInt(9) + 5; i++) {
            world.addParticle(AetherParticles.FALLING_ORANGE_PETAL, (double) pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), (double) pos.getZ() + random.nextDouble(), speed, world.getRandom().nextDouble() / -20.0, 0);
        }
    }

    @Override
    public void animateTick(BlockState state, Level world, BlockPos pos, Random random) {
        if (state.getValue(GROWTH) == 1 && random.nextInt(50) == 0) {
            Direction direction = Direction.DOWN;
            BlockPos blockPos = pos.relative(direction);
            BlockState blockState = world.getBlockState(blockPos);
            if (!(!blockState.isFaceSturdy(world, blockPos, direction.getOpposite()) && !blockState.propagatesSkylightDown(world, blockPos))) {

                if (speed == 0 || world.getGameTime() % 3000 == 0) {
                    speed = world.getRandom().nextInt(4);
                    if (world.isRaining()) speed += 1;
                    else if (world.isThundering()) speed += 2;
                }

                for (int leaf = 0; leaf < random.nextInt(2) + 3; leaf++) {
                    if (world.random.nextInt(3) == 0) {
                        double d = direction.getStepX() == 0 ? random.nextDouble() : 0.5D + (double) direction.getStepX() * 0.6D;
                        double f = direction.getStepZ() == 0 ? random.nextDouble() : 0.5D + (double) direction.getStepZ() * 0.6D;
                        world.addParticle(AetherParticles.FALLING_ORANGE_PETAL, (double) pos.getX() + d, pos.getY(), (double) pos.getZ() + f, speed, world.getRandom().nextDouble() / -20.0, 0);
                    }
                }
            }
        }
        super.animateTick(state, world, pos, random);
    }

    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(GROWTH, CAPPED, NATURAL);
    }
}
