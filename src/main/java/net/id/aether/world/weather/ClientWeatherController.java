package net.id.aether.world.weather;

import java.util.*;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.id.aether.mixin.client.render.WorldRendererAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.ImmutableTriple;

@Environment(EnvType.CLIENT)
public final class ClientWeatherController{
    private ClientWeatherController(){}
    
    private static Map<Biome, Map<WeatherController<Object>, Object>> BIOME_CONTROLLERS = Map.of();
    private static Map<Object, Optional<Object>> CLIENT_STATES = Map.of();
    
    //FIXME The server needs to send this info to the client before this gets merged.
    public static void init(DynamicRegistryManager manager, PacketSender sender){
        //noinspection unchecked
        BIOME_CONTROLLERS = (Map<Biome, Map<WeatherController<Object>, Object>>)(Object)AetherWeatherController.createControllerMap(manager);
        sender.sendPacket(AetherWeatherController.PACKET_WEATHER_REQUEST, PacketByteBufs.empty());
        CLIENT_STATES = BIOME_CONTROLLERS.values().stream()
            .flatMap((map)->map.entrySet().stream())
            .filter((entry)->entry.getKey().getRenderer() != null)
            .collect(Collectors.toUnmodifiableMap(
                Map.Entry::getValue, (entry)->{
                    var renderer = entry.getKey().getRenderer();
                    var state = entry.getValue();
                    return Optional.ofNullable(renderer.createState(state));
                }
            ));
    }
    
    public static void deinit(){
        BIOME_CONTROLLERS = Map.of();
    }
    
    public static <T> void updateController(Biome biome, WeatherController<T> controller, PacketByteBuf buffer){
        var controllers = BIOME_CONTROLLERS.get(biome);
        if(controllers == null){
            return;
        }
        @SuppressWarnings("unchecked")
        var state = (T)controllers.get(controller);
        if(state != null){
            controller.readDelta(state, buffer);
        }
    }
    
    public static <T> void setController(Biome biome, WeatherController<T> controller, PacketByteBuf buffer){
        var controllers = BIOME_CONTROLLERS.get(biome);
        if(controllers == null){
            return;
        }
        @SuppressWarnings("unchecked")
        var state = (T)controllers.get(controller);
        if(state != null){
            controller.read(state, buffer);
        }
    }
    
    public static <R extends WorldRenderer & WorldRendererAccessor> void render(R renderer, LightmapTextureManager manager, float tickDelta, double cameraX, double cameraY, double cameraZ){
        manager.enable();
        int x = MathHelper.floor(cameraX);
        int z = MathHelper.floor(cameraZ);
        int weatherDistance = MinecraftClient.isFancyGraphicsOrBetter() ? 10 : 5;
    
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        
        var world = renderer.getClient().world;
        var floatCameraPos = new Vec3d(cameraX, cameraY, cameraZ);
        
        // This kinda sucks, but in theory this should make weather rendering a bit faster
        Map<WeatherRenderer<Object, Object>, Set<ImmutableTriple<BlockPos, Object, Object>>> renderers = new HashMap<>();
        
        for(int blockZ = z - weatherDistance; blockZ <= z + weatherDistance; blockZ++){
            for(int blockX = x - weatherDistance; blockX <= x + weatherDistance; blockX++){
                mutable.set(blockX, 0, blockZ);
                Biome biome = world.getBiome(mutable);
                var map = BIOME_CONTROLLERS.get(biome);
                if(map == null){
                    continue;
                }
                for(var entry : map.entrySet()){
                    var immutablePos = mutable.toImmutable();
                    var controller = entry.getKey();
                    var weatherRenderer = controller.getRenderer();
                    if(weatherRenderer != null){
                        var state = entry.getValue();
                        if(state != null && controller.isActive(state)){
                            var clientState = CLIENT_STATES.get(state).orElse(null);
                            renderers.computeIfAbsent(weatherRenderer, (value)->new HashSet<>()).add(new ImmutableTriple<>(
                                immutablePos, state, clientState
                            ));
                        }
                    }
                }
            }
        }

        renderers.forEach((weatherRenderer, set)->{
            weatherRenderer.setupState(manager, renderer, tickDelta, floatCameraPos);
            try{
                set.forEach((triple)->{
                    var pos = triple.getLeft();
                    Object state = triple.getMiddle();
                    Object clientState = triple.getRight();
                    weatherRenderer.render(pos, state, clientState, renderer, weatherDistance, tickDelta, floatCameraPos);
                });
            }finally{
                weatherRenderer.teardownState(manager, renderer);
            }
        });
    }
}
