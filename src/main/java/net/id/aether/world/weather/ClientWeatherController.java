package net.id.aether.world.weather;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.id.aether.world.weather.controller.VanillaWeatherController;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

@Environment(EnvType.CLIENT)
public final class ClientWeatherController{
    private ClientWeatherController(){}
    
    private static Map<Biome, Map<WeatherController<Object>, Object>> BIOME_CONTROLLERS = Map.of();
    private static Map<Map.Entry<Identifier, Identifier>, Map.Entry<WeatherController<Object>, Object>> IDENTIFIER_CONTROLLERS = Map.of();
    
    //FIXME The server needs to send this info to the client before this gets merged.
    public static void init(DynamicRegistryManager manager, PacketSender sender){
        var biomeRegistry = manager.get(Registry.BIOME_KEY);
        //noinspection unchecked
        BIOME_CONTROLLERS = (Map<Biome, Map<WeatherController<Object>, Object>>)(Object)AetherWeatherController.createControllerMap(manager);
        IDENTIFIER_CONTROLLERS = new HashMap<>();
        BIOME_CONTROLLERS.forEach((biome, map)->{
            map.forEach((controller, state)->{
                //noinspection ConstantConditions
                IDENTIFIER_CONTROLLERS.put(
                    Map.entry(biomeRegistry.getId(biome), controller.getIdentifier()),
                    Map.entry(controller, state)
                );
            });
        });
        sender.sendPacket(AetherWeatherController.PACKET_WEATHER_REQUEST, PacketByteBufs.empty());
    }
    
    public static void deinit(){
        BIOME_CONTROLLERS = Map.of();
        IDENTIFIER_CONTROLLERS = Map.of();
    }
    
    public static void updateController(Identifier biome, Identifier id, PacketByteBuf buffer){
        var entry = IDENTIFIER_CONTROLLERS.get(Map.entry(biome, id));
        var controller = entry.getKey();
        var state = entry.getValue();
        if(controller != null){
            controller.readDelta(state, buffer);
        }
    }
    
    public static void setController(Identifier biome, Identifier id, PacketByteBuf buffer){
        var entry = IDENTIFIER_CONTROLLERS.get(Map.entry(biome, id));
        var controller = entry.getKey();
        var state = entry.getValue();
        if(controller != null){
            controller.read(state, buffer);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T> boolean has(Identifier biome, WeatherController<T> controller){
        var entry = IDENTIFIER_CONTROLLERS.get(Map.entry(biome, controller.getIdentifier()));
        return entry == null ? false : controller.isActive((T)entry.getValue());
    }
}
