package net.id.aether.world.weather;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.id.aether.Aether;
import net.id.aether.duck.ServerWorldDuck;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.id.aether.Aether.MOD_ID;

/**
 * The controller for weather in the Aether.
 */
public final class AetherWeatherController{
    private static final Logger LOGGER = LogManager.getLogger("Aether Weather");
    
    static final Identifier PACKET_WEATHER_REQUEST = Aether.locate("weather_request");
    private static final Identifier PACKET_WEATHER_SEND = Aether.locate("weather_send");
    private static final Identifier PACKET_WEATHER_UPDATE = Aether.locate("weather_update");
    
    private final ServerWorld world;
    private final Map<Biome, BiomeWeatherController> controllers;
    private final Path savePath;
    
    public static ServerWorld WORLD;
    
    public AetherWeatherController(ServerWorld world, Path savePath){
        WORLD = world;
        this.world = world;
        this.savePath = savePath;
        controllers = createControllerMap();
        load();
    }
    
    public static void init(){
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination)->{
            handleWorldSwitch(destination, player);
        });
        ServerPlayNetworking.registerGlobalReceiver(PACKET_WEATHER_REQUEST, (server, player, handler, buffer, responseSender)->{
            handleWorldSwitch(player.world, player);
        });
    }
    
    private static void handleWorldSwitch(World world, ServerPlayerEntity player){
        var controller = ((ServerWorldDuck)world).the_aether$getWeatherController();
        if(controller != null){
            controller.sendWeatherInfo(player);
        }
    }
    
    private void sendWeatherInfo(ServerPlayerEntity player){
        var buffer = PacketByteBufs.create();
        writeControllerState(buffer, BiomeWeatherController::write);
        ServerPlayNetworking.send(player, PACKET_WEATHER_SEND, buffer);
    }
    
    @Environment(EnvType.CLIENT)
    public static void initClient(){
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client)->
            ClientWeatherController.init(handler.getRegistryManager(), sender)
        );
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client)->
            ClientWeatherController.deinit()
        );
        ClientPlayNetworking.registerGlobalReceiver(PACKET_WEATHER_UPDATE, (client, handler, buffer, sender)->{
            updateClientController(buffer, ClientWeatherController::updateController);
        });
        ClientPlayNetworking.registerGlobalReceiver(PACKET_WEATHER_SEND, (client, handler, buffer, sender)->{
            updateClientController(buffer, ClientWeatherController::setController);
        });
    }
    
    @Environment(EnvType.CLIENT)
    private static void updateClientController(PacketByteBuf buffer, BiConsumer<Identifier, PacketByteBuf> callback){
        while(buffer.readableBytes() > 0){
            var id = buffer.readIdentifier();
            var size = buffer.readByte();
            var nextId = buffer.readerIndex() + size;
            callback.accept(id, buffer);
            buffer.readerIndex(nextId);
        }
    }
    
    /**
     * Gets all biomes that use `the_aether` as a namespace.
     *
     * @param manager The registry manager
     * @return A set of all Aether biomes
     */
    static Set<Biome> getBiomes(DynamicRegistryManager manager){
        var registry = manager.get(Registry.BIOME_KEY);
        return registry.stream()
            .filter((biome)->{
                var id = registry.getId(biome);
                if(id == null){
                    // Should never happen but you never know...
                    return false;
                }
                return id.getNamespace().equals(MOD_ID);
            })
            .collect(Collectors.toUnmodifiableSet());
    }
    
    /**
     * Creates a map of controllers for all Aether biomes.
     *
     * @return A map of controllers
     */
    private Map<Biome, BiomeWeatherController> createControllerMap(){
        return createControllerMap(world.getRegistryManager());
    }
    
    static Map<Biome, BiomeWeatherController> createControllerMap(DynamicRegistryManager registryManager){
        var registry = registryManager.get(Registry.BIOME_KEY);
        return getBiomes(registryManager).stream()
            .map((biome)->{
                //TODO Figure out a better way to handle this, biome duck + weather category?
                //Wind, if implemented should go to highlands plains and continental plateau
                var key = registry.getKey(biome).orElse(null);
                // Should never happen
                if(key == null){
                    return null;
                }
                var id = key.getValue();
                var controller = (switch(id.getPath()){
                    case "aether_highlands", "aether_highlands_forest", "aether_highlands_thicket" -> BiomeWeatherController.COMMON_THUNDER;
                    case "aether_wisteria_woods", "continental_plato", "highlands_shield" -> BiomeWeatherController.VANILLA;
                    case "autumnal_tundra" -> BiomeWeatherController.SNOW;
                    
                    default -> {
                        LOGGER.warn("Biome \"%s\" isn't handled in AetherWeatherController.createControllerMap".formatted(key.toString()));
                        yield BiomeWeatherController.DUMMY;
                    }
                }).apply(id);
                return Map.entry(biome, controller);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    /**
     * Ticks all the weather in the Aether. Replaces the vanilla weather ticking.
     */
    public void tick(){
        controllers.values().forEach((controller)->controller.tick(world));
    
        var players = world.getPlayers();
        // No players, don't bother calculating updates
        if(players.isEmpty()){
            return;
        }
        
        var buffer = PacketByteBufs.create();
        writeControllerState(buffer, BiomeWeatherController::writeDelta);
        // No delta
        if(buffer.writerIndex() == 0){
            buffer.release();
            return;
        }
        
        var packet = ServerPlayNetworking.createS2CPacket(PACKET_WEATHER_UPDATE, buffer);
        players.forEach((player)->
            ServerPlayNetworking.getSender(player).sendPacket(packet)
        );
    }
    
    private void writeControllerState(PacketByteBuf buffer, BiConsumer<BiomeWeatherController, PacketByteBuf> callback){
        controllers.values().forEach((controller)->{
            buffer.markWriterIndex();
            buffer.writeIdentifier(controller.getId());
            buffer.writeByte(0);
            var index = buffer.writerIndex();
            callback.accept(controller, buffer);
            var index2 = buffer.writerIndex();
            if(index == index2){
                // Data was not written, no change;
                buffer.resetWriterIndex();
                return;
            }
            buffer.writerIndex(index - 1);
            buffer.writeByte(index2 - index);
            buffer.writerIndex(index2);
        });
    }
    
    /**
     * Loads the weather info from disk.
     */
    private void load(){
        // No save, nothing to load.
        if(!Files.isRegularFile(savePath)){
            return;
        }
    
        NbtCompound compound;
        try(var stream = Files.newInputStream(savePath)){
            compound = NbtIo.readCompressed(stream);
        }catch(IOException e){
            LOGGER.error("Failed to load weather state!", e);
            return;
        }
        
        for(var controller : controllers.values()){
            var tag = compound.getCompound(controller.getId().toString());
            if(tag.isEmpty()){
                continue;
            }
            controller.load(tag);
        }
    }
    
    /**
     * Saves the weather state to disk.
     */
    public void save(){
        var compound = new NbtCompound();
        for(var controller : controllers.values()){
            var tag = controller.save();
            if(tag.isEmpty()){
                continue;
            }
            compound.put(controller.getId().toString(), tag);
        }

        try{
            Files.createDirectories(savePath.getParent());
            try(var stream = Files.newOutputStream(savePath)){
                NbtIo.writeCompressed(compound, stream);
            }
        }catch(IOException e){
            LOGGER.error("Failed to save weather state!", e);
        }
    }
    
    /**
     * Gets the amount of time before weather changes in a biome.
     *
     * @param biome The biome to query
     * @param type The weather to query
     * @return Time in ticks or empty if failed
     */
    public OptionalInt getWeatherDuration(Biome biome, AetherWeatherType type){
        var controller = getWeatherController(biome);
        if(controller.isPresent()){
            return controller.get().get(type);
        }else{
            return OptionalInt.empty();
        }
    }
    
    /**
     * Sets the weather in a biome.
     *
     * @param biome The biome to set
     * @param type The weather to set
     * @param duration The duration of the weather
     * @return True if it succeeded, false otherwise
     */
    public boolean setWeather(Biome biome, AetherWeatherType type, int duration){
        return getWeatherController(biome).map((controller)->controller.set(type, duration)).orElse(false);
    }
    
    /**
     * Gets a controller for a specified biome.
     *
     * @param biome The biome to query
     * @return The controller or empty on error
     */
    public Optional<BiomeWeatherController> getWeatherController(Biome biome){
        return Optional.ofNullable(controllers.get(biome));
    }
}
