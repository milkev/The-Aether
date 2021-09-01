package net.id.aether.world.weather;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.id.aether.Aether.MOD_ID;

/**
 * The controller for weather in the Aether.
 */
public final class AetherWeatherController{
    static final Logger LOGGER = LogManager.getLogger("Aether Weather");
    
    private final ServerWorld world;
    private final Set<Biome> biomes;
    private final Map<Biome, BiomeWeatherController> controllers;
    private final Path savePath;
    
    public static ServerWorld WORLD;
    
    public AetherWeatherController(ServerWorld world, Path savePath){
        WORLD = world;
        this.world = world;
        this.savePath = savePath;
        biomes = getBiomes();
        controllers = createControllerMap();
        load();
    }
    
    /**
     * Gets all biomes that use `the_aether` as a namespace.
     *
     * @return A set of all Aether biomes
     */
    private Set<Biome> getBiomes(){
        var registry = world.getRegistryManager().get(Registry.BIOME_KEY);
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
        var registry = world.getRegistryManager().get(Registry.BIOME_KEY);
        return biomes.stream()
            .map((biome)->{
                //TODO Figure out a better way to handle this, biome duck + weather category?
                var key = registry.getKey(biome).orElse(null);
                // Should never happen
                if(key == null){
                    return null;
                }
                var controller = (switch(key.getValue().getPath()){
                    case "aether_highlands", "aether_highlands_forest", "aether_highlands_thicket" -> BiomeWeatherController.COMMON_THUNDER;
                    case "aether_wisteria_woods", "continental_plato", "highlands_shield" -> BiomeWeatherController.VANILLA;
                    case "autumnal_tundra" -> BiomeWeatherController.SNOW;
                    
                    default -> {
                        LOGGER.warn("Biome \"%s\" isn't handled in AetherWeatherController.createControllerMap".formatted(key.toString()));
                        yield BiomeWeatherController.DUMMY;
                    }
                }).get();
                return Map.entry(biome, controller);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    /**
     * Ticks all the weather in the Aether. Replaces the vanilla weather ticking.
     */
    public void tick(){
        controllers.values().forEach(biomeWeatherController->biomeWeatherController.tick(world));
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
        
        // We need this for names.
        var registry = world.getRegistryManager().get(Registry.BIOME_KEY);
        for(var entry : controllers.entrySet()){
            var id = registry.getId(entry.getKey());
            if(id == null){
                continue;
            }
            var tag = compound.getCompound(id.toString());
            if(tag.isEmpty()){
                continue;
            }
            entry.getValue().load(tag);
        }
    }
    
    /**
     * Saves the weather state to disk.
     */
    public void save(){
        var compound = new NbtCompound();
        var registry = world.getRegistryManager().get(Registry.BIOME_KEY);
        for(var entry : controllers.entrySet()){
            var id = registry.getId(entry.getKey());
            if(id == null){
                continue;
            }
            var tag = entry.getValue().save();
            if(tag.isEmpty()){
                continue;
            }
            compound.put(id.toString(), tag);
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
