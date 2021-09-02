package net.id.aether.world.weather;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.biome.Biome;

@Environment(EnvType.CLIENT)
public final class ClientWeatherController{
    private ClientWeatherController(){}
    
    private static Map<Biome, BiomeWeatherController> BIOME_CONTROLLERS = Map.of();
    private static Map<Identifier, BiomeWeatherController> IDENTIFIER_CONTROLLERS = Map.of();
    
    //FIXME The server needs to send this info to the client before this gets merged.
    public static void init(DynamicRegistryManager manager, PacketSender sender){
        BIOME_CONTROLLERS = AetherWeatherController.createControllerMap(manager);
        IDENTIFIER_CONTROLLERS = BIOME_CONTROLLERS.values().stream()
            .collect(Collectors.toUnmodifiableMap(BiomeWeatherController::getId, Function.identity()));
        sender.sendPacket(AetherWeatherController.PACKET_WEATHER_REQUEST, PacketByteBufs.empty());
    }
    
    public static void deinit(){
        BIOME_CONTROLLERS = Map.of();
        IDENTIFIER_CONTROLLERS = Map.of();
    }
    
    public static void updateController(Identifier id, PacketByteBuf buffer){
        var controller = IDENTIFIER_CONTROLLERS.get(id);
        if(controller != null){
            controller.readDelta(buffer);
        }
    }
    
    public static void setController(Identifier id, PacketByteBuf buffer){
        var controller = IDENTIFIER_CONTROLLERS.get(id);
        if(controller != null){
            controller.read(buffer);
        }
    }
    
    public static Optional<BiomeWeatherController> getWeatherController(Biome biome){
        return Optional.ofNullable(BIOME_CONTROLLERS.get(biome));
    }
}
