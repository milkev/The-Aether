package net.id.aether.world.weather;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.id.aether.Aether;
import net.id.aether.duck.ServerWorldDuck;
import net.id.aether.world.dimension.AetherDimension;
import net.id.aether.world.weather.controller.ThunderWeatherController;
import net.id.aether.world.weather.controller.VanillaWeatherController;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;

import static net.id.aether.Aether.MOD_ID;
import static net.id.aether.Aether.locate;

/**
 * The controller for weather in the Aether.
 */
public final class AetherWeatherController{
    private static final Logger LOGGER = LogManager.getLogger("Aether Weather");
    
    static final Identifier PACKET_WEATHER_REQUEST = Aether.locate("weather_request");
    private static final Identifier PACKET_WEATHER_SEND = Aether.locate("weather_send");
    private static final Identifier PACKET_WEATHER_UPDATE = Aether.locate("weather_update");
    
    @SuppressWarnings("unchecked")
    private static final Registry<WeatherController<?>> WEATHER_REGISTRY = (Registry<WeatherController<?>>)(Object)FabricRegistryBuilder.createSimple(WeatherController.class, locate("weather"))
        .attribute(RegistryAttribute.SYNCED)
        .buildAndRegister();
    
    /*
    FIXME This is not a real tag thing, vanilla doesn't like to have a duplicate tag type so it looks like we will
    need to make a bare-bones version for us to use.
     */
    private static final Map<WeatherController<?>, Set<RegistryKey<Biome>>> WEATHER_TAGS = new Object2ObjectOpenHashMap<>();
    
    public static final VanillaWeatherController CONTROLLER_RAIN = registerWeatherController(new VanillaWeatherController(locate("rain")));
    public static final VanillaWeatherController CONTROLLER_SNOW = registerWeatherController(new VanillaWeatherController(locate("snow")));
    public static final ThunderWeatherController CONTROLLER_THUNDER = registerWeatherController(new ThunderWeatherController(locate("thunder")));
    public static final ThunderWeatherController CONTROLLER_THUNDER_SNOW = registerWeatherController(new ThunderWeatherController(locate("thunder_snow")));
    
    private final ServerWorld world;
    private final Map<Biome, Map<WeatherController<?>, ?>> controllers;
    private final Path savePath;
    
    public static ServerWorld WORLD;
    
    public AetherWeatherController(ServerWorld world, Path savePath){
        WORLD = world;
        this.world = world;
        this.savePath = savePath;
        controllers = createControllerMap();
        load(world.getRegistryManager());
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
    
    public static <S, T extends WeatherController<S>> T registerWeatherController(T controller){
        var identifier = controller.getIdentifier();
        Registry.register(WEATHER_REGISTRY, identifier, controller);
        
        var biomes = switch(controller.getIdentifier().toString()){
            case "the_aether:rain", "the_aether:thunder" -> Set.of(
                AetherDimension.HIGHLANDS_PLAINS,
                AetherDimension.HIGHLANDS_FOREST,
                AetherDimension.HIGHLANDS_THICKET,
                AetherDimension.WISTERIA_WOODS,
                AetherDimension.CONTINENTAL_PLATO,
                AetherDimension.HIGHLANDS_SHIELD
            );
            case "the_aether:snow", "the_aether:thunder_snow" -> Set.of(
                AetherDimension.AUTUMNAL_TUNDRA
            );
            default -> throw new IllegalArgumentException("Unknown controller: " + controller.getIdentifier());
        };
        
        if(WEATHER_TAGS.putIfAbsent(controller, biomes) != null){
            throw new IllegalStateException("Weather controller \"" + identifier + "\" was already registered");
        }
        return controller;
    }
    
    public static Set<WeatherController<?>> getControllers(){
        return WEATHER_REGISTRY.stream().collect(Collectors.toUnmodifiableSet());
    }
    
    private void sendWeatherInfo(ServerPlayerEntity player){
        var buffer = PacketByteBufs.create();
        writeControllerState(player.world.getRegistryManager(), buffer, WeatherController::write);
        if(buffer.writerIndex() == 0){
            return;
        }
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
            updateClientController(handler.getRegistryManager(), buffer, ClientWeatherController::updateController);
        });
        ClientPlayNetworking.registerGlobalReceiver(PACKET_WEATHER_SEND, (client, handler, buffer, sender)->{
            updateClientController(handler.getRegistryManager(), buffer, ClientWeatherController::setController);
        });
    }
    
    @Environment(EnvType.CLIENT)
    private static void updateClientController(DynamicRegistryManager registryManager, PacketByteBuf buffer, TriConsumer<Biome, WeatherController<?>, PacketByteBuf> callback){
        var biomeRegistry = registryManager.get(Registry.BIOME_KEY);
        
        while(buffer.readableBytes() > 0){
            var controllerCount = Byte.toUnsignedInt(buffer.readByte());
            var biome = biomeRegistry.get(buffer.readVarInt());
            for(int i = 0; i < controllerCount; i++){
                var controller = WEATHER_REGISTRY.get(buffer.readVarInt());
                var size = buffer.readByte();
                var nextId = buffer.readerIndex() + size;
                callback.accept(biome, controller, buffer);
                buffer.readerIndex(nextId);
            }
        }
    }
    
    private void writeControllerState(DynamicRegistryManager registryManager, PacketByteBuf buffer, TriConsumer<WeatherController<Object>, Object, PacketByteBuf> callback){
        var biomeRegistry = registryManager.get(Registry.BIOME_KEY);
        
        controllers.forEach((biome, controllerMap)->{
            var mark = buffer.writerIndex();
            buffer.writeByte(0);
            buffer.writeVarInt(biomeRegistry.getRawId(biome));
            var position = buffer.writerIndex();
            
            var count = new Object(){
                int value = 0;
            };
            
            controllerMap.forEach((controller, state)->{
                buffer.markWriterIndex();
                buffer.writeVarInt(WEATHER_REGISTRY.getRawId(controller));
                buffer.writeByte(0);
                var index = buffer.writerIndex();
                //noinspection unchecked
                callback.accept((WeatherController<Object>)controller, state, buffer);
                var index2 = buffer.writerIndex();
                if(index == index2){
                    // Data was not written, no change;
                    buffer.resetWriterIndex();
                    return;
                }
                buffer.writerIndex(index - 1);
                buffer.writeByte(index2 - index);
                buffer.writerIndex(index2);
                count.value++;
            });
            
            if(position == buffer.writerIndex()){
                buffer.writerIndex(mark);
            }else{
                buffer.markWriterIndex();
                buffer.writerIndex(mark);
                buffer.writeByte(count.value);
                buffer.resetWriterIndex();
            }
        });
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
    private Map<Biome, Map<WeatherController<?>, ?>> createControllerMap(){
        return createControllerMap(world.getRegistryManager());
    }
    
    static Map<Biome, Map<WeatherController<?>, ?>> createControllerMap(DynamicRegistryManager registryManager){
        var biomes = getBiomes(registryManager);
        var biomeRegistry = registryManager.get(Registry.BIOME_KEY);
        
        Map<Biome, Map<WeatherController<?>, Object>> map = new Object2ObjectOpenHashMap<>();
        biomes.forEach((biome)->map.put(biome, new HashMap<>()));
        WEATHER_TAGS.forEach((controller, tag)->{
            for(var biomeKey : tag){
                var biome = biomeRegistry.get(biomeKey);
                var state = controller.createState(biome);
                if(state == null){
                    continue;
                }
                var controllers = map.get(biome);
                if(controllers == null){
                    var key = biomeRegistry.getId(biome);
                    throw new RuntimeException("Can not register weather for the non Aether biome: " + key);
                }
                controllers.put(controller, state);
            }
        });
        map.replaceAll((biome, set)->set.isEmpty() ? Map.of() : Collections.unmodifiableMap(set));
        return Collections.unmodifiableMap(map);
    }
    
    /**
     * Ticks all the weather in the Aether. Replaces the vanilla weather ticking.
     */
    public void tick(){
        var random = world.getRandom();
        //noinspection unchecked
        controllers.values().forEach((map)->map.forEach((controller, state)->((WeatherController<Object>)controller).tick(random, state)));
    
        var players = world.getPlayers();
        // No players, don't bother calculating updates
        if(players.isEmpty()){
            return;
        }
        
        var buffer = PacketByteBufs.create();
        writeControllerState(world.getRegistryManager(), buffer, WeatherController::writeDelta);
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
    
    /**
     * Loads the weather info from disk.
     *
     * @param registryManager The registry manager
     */
    private void load(DynamicRegistryManager registryManager){
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
        
        var biomeRegistry = registryManager.get(Registry.BIOME_KEY);
        controllers.forEach((biome, controllerMap)->{
            var biomeId = biomeRegistry.getId(biome);
            if(biomeId == null){
                //How?
                throw new RuntimeException("Biome ID was null for biome " + biome);
            }
            if(!compound.contains(biomeId.toString(), NbtElement.COMPOUND_TYPE)){
                return;
            }
    
            var biomeTag = compound.getCompound(biomeId.toString());
            if(biomeTag.isEmpty()){
                return;
            }
            
            controllerMap.forEach((controller, state)->{
                var controllerId = controller.getIdentifier().toString();
                if(!biomeTag.contains(controllerId, NbtElement.COMPOUND_TYPE)){
                    return;
                }
                var controllerTag = biomeTag.getCompound(controllerId);
                if(controllerTag.isEmpty()){
                    return;
                }
                //noinspection unchecked
                ((WeatherController<Object>)controller).readNbt(state, controllerTag);
            });
        });
    }
    
    /**
     * Saves the weather state to disk.
     */
    public void save(DynamicRegistryManager registryManager){
        var biomeRegistry = registryManager.get(Registry.BIOME_KEY);
        
        var compound = new NbtCompound();
        controllers.forEach((biome, controllerMap)->{
            var biomeId = biomeRegistry.getId(biome);
            if(biomeId == null){
                // How?
                throw new RuntimeException("Biome ID was null for biome " + biome);
            }
            
            var biomeTag = new NbtCompound();
            controllerMap.forEach((controller, state)->{
                @SuppressWarnings("unchecked")
                var controllerTag = ((WeatherController<Object>)controller).writeNbt(state);
                if(controllerTag == null || controllerTag.isEmpty()){
                    return;
                }
                var controllerId = controller.getIdentifier().toString();
                biomeTag.put(controllerId, controllerTag);
            });
            
            if(biomeTag.isEmpty()){
                return;
            }
            
            compound.put(biomeId.toString(), biomeTag);
        });

        try{
            Files.createDirectories(savePath.getParent());
            try(var stream = Files.newOutputStream(savePath)){
                NbtIo.writeCompressed(compound, stream);
            }
        }catch(IOException e){
            LOGGER.error("Failed to save weather state!", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> Optional<T> getControllerState(Biome biome, WeatherController<T> controller){
        var controllerMap = controllers.get(biome);
        if(controllerMap == null){
            return Optional.empty();
        }
        var state = controllerMap.get(controller);
        return Optional.ofNullable((T)state);
    }
    
    /**
     * Gets the amount of time before weather changes in a biome.
     *
     * @param biome The biome to query
     * @param type The weather to query
     * @return Time in ticks or empty if failed
     */
    public <T> OptionalInt getWeatherDuration(Biome biome, WeatherController<T> type){
        var state = getControllerState(biome, type);
        return state.map((value)->OptionalInt.of(type.getDuration(value))).orElseGet(OptionalInt::empty);
    }
    
    /**
     * Sets the weather in a biome.
     *
     * @param biome The biome to set
     * @param type The weather to set
     * @param duration The duration of the weather
     * @return True if it succeeded, false otherwise
     */
    public <T> boolean setWeather(Biome biome, WeatherController<T> type, int duration){
        return getControllerState(biome, type)
            .map((value)->type.set(value, duration))
            .orElse(false);
    }
    
    public <T> boolean isWeatherActive(Biome biome, WeatherController<T> controller){
        return getControllerState(biome, controller).filter(controller::isActive).isPresent();
    }
    
    /**
     * Gets a controller for a specified biome.
     *
     * @param biome The biome to query
     * @return The controller or empty on error
     */
    public Optional<Set<WeatherController<?>>> getWeatherControllers(Biome biome){
        var controllers = this.controllers.get(biome);
        if(controllers == null || controllers.isEmpty()){
            return Optional.empty();
        }else{
            return Optional.of(controllers.keySet());
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> Optional<WeatherController<T>> getController(Identifier identifier){
        return Optional.ofNullable((WeatherController<T>)WEATHER_REGISTRY.get(identifier));
    }
}
