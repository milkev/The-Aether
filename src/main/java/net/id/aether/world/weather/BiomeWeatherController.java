package net.id.aether.world.weather;

import net.id.aether.world.weather.controller.*;
import java.util.OptionalInt;
import java.util.function.Supplier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;

/**
 * Controls the weather of a biome.
 */
public interface BiomeWeatherController{
    /**
     * A dummy weather controller that supports no weather.
     */
    Supplier<BiomeWeatherController> DUMMY = ()->DummyController.INSTANCE;
    /**
     * A controller that has a lot of thunder.
     */
    Supplier<BiomeWeatherController> COMMON_THUNDER = CommonThunderController::new;
    /**
     * A controller that mimics warm biomes in vanilla.
     */
    Supplier<BiomeWeatherController> VANILLA = VanillaController::new;
    /**
     * A controller that mimics cold biomes in vanilla.
     */
    Supplier<BiomeWeatherController> SNOW = SnowController::new;
    
    /**
     * Update the weather for this controller.
     *
     * @param world The Aether
     */
    void tick(ServerWorld world);
    
    /**
     * Load the weather state from a tag.
     *
     * @param tag The tag to read from
     */
    void load(NbtCompound tag);
    
    /**
     * Save the weather state to a tag.
     *
     * @return The tag to save
     */
    NbtCompound save();
    
    /**
     * Get the amount of time before weather changes.
     *
     * @param type The type of weather
     * @return The amount of time before a transition, or empty if unsupported
     */
    OptionalInt get(AetherWeatherType type);
    
    /**
     * Sets the weather state and time before a transition.
     *
     * @param type The type of weather
     * @param duration The amount of time before a transition
     * @return true if it succeeded, false if unsupported
     */
    boolean set(AetherWeatherType type, int duration);
    
    /**
     * Checks if the controller is currently experiencing weather.<br>
     * <br>
     * This can return true if the weather is not truly there, I.E. if there is thunder rain will return true for some
     * controllers.
     *
     * @param type The type of weather to query
     * @return true if present, false if absent or unsupported
     */
    boolean has(AetherWeatherType type);
    
    /**
     * Checks if the controller is currently experiencing weather.<br>
     * <br>
     * Unlike {@link #has} this one should reflect the true state of the internal weather system.
     *
     * @param type The type of weather to query
     * @return true if present, false if absent or unsupported
     */
    boolean hasRaw(AetherWeatherType type);
}
