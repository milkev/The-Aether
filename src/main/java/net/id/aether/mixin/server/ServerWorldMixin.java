package net.id.aether.mixin.server;

import net.id.aether.Aether;
import net.id.aether.duck.ServerWorldDuck;
import net.id.aether.entities.block.FloatingBlockEntity;
import net.id.aether.entities.util.floatingblock.FloatingBlockStructure;
import net.id.aether.world.dimension.AetherDimension;
import net.id.aether.world.weather.AetherWeatherController;
import java.util.List;
import java.util.concurrent.Executor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.*;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Spawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World implements ServerWorldDuck{
    @SuppressWarnings("ConstantConditions")
    private ServerWorldMixin(){
        super(null, null, null, null, false, false, -1L);
    }

    @Shadow private int idleTimeout;

    @Shadow @Final EntityList entityList;
    
    // Will only be non-null on the Aether world.
    @Unique private AetherWeatherController the_aether$weatherController;
    
    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void init(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionType dimensionType, WorldGenerationProgressListener worldGenerationProgressListener, ChunkGenerator chunkGenerator, boolean debugWorld, long seed, List<Spawner> spawners, boolean shouldTickTime, CallbackInfo ci){
        if(getRegistryKey().equals(AetherDimension.AETHER_WORLD_KEY)){
            // I would do this in the controller, but we lose access to the path
            var weatherPath = session.getWorldDirectory(getRegistryKey()).toPath().resolve("weather.nbt.gz");
            the_aether$weatherController = new AetherWeatherController((ServerWorld)(Object)this, weatherPath);
        }
    }
    
    @Inject(method = "tick", at = @At(value = "RETURN"))
    void postEntityTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci){
        if (this.idleTimeout < 300) {
            entityList.forEach(entityObj -> {
                if (entityObj instanceof FloatingBlockEntity entity) {
                    entity.postTickEntities();
                } else if (entityObj == null) {
                    Aether.LOG.error("Started checking null entities in ServerWorldMixin::postEntityTick");
                }
            });
            FloatingBlockStructure[] structures = FloatingBlockStructure.getAllStructures().toArray(FloatingBlockStructure[]::new);
            for(FloatingBlockStructure structure : structures){
                structure.postTick();
            }
        }
    }
    
    /**
     * Override the vanilla weather ticking stuff to enable us to tick the Aether weather systems.<br>
     * <br>
     * FIXME Find a better way to do this.
     *
     * @param gameRules The game rules
     * @param rule The weather rule
     * @return Whether the vanilla weather should tick
     */
    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z"
        ),
        slice = @Slice(
            from = @At("HEAD"),
            to = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/level/ServerWorldProperties;getClearWeatherTime()I"
            )
        )
    )
    private boolean shouldTickWeather(GameRules gameRules, GameRules.Key<GameRules.BooleanRule> rule){
        var original = gameRules.getBoolean(rule);
        if(original && the_aether$weatherController != null){
            the_aether$weatherController.tick();
            // We never want vanilla weather ticking here
            return false;
        }
        return original;
    }
    
    @Inject(
        method = "saveLevel",
        at = @At("TAIL")
    )
    private void saveLevel(CallbackInfo ci){
        if(the_aether$weatherController != null){
            the_aether$weatherController.save();
        }
    }
    
    @Override
    @Unique
    public final AetherWeatherController the_aether$getWeatherController(){
        return the_aether$weatherController;
    }
}
