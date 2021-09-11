package net.id.aether.world.weather.controller;

import java.util.Random;
import net.id.aether.world.weather.WeatherController;
import net.id.aether.world.weather.WeatherRenderer;
import net.id.aether.world.weather.renderer.RainWeatherRenderer;
import net.id.aether.world.weather.renderer.SnowWeatherRenderer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.NotNull;

import static net.id.aether.Aether.locate;

public final class VanillaWeatherController implements WeatherController<VanillaWeatherController.State>{
    private static final Identifier IDENTIFIER_RAIN = locate("rain");
    private static final Identifier IDENTIFIER_SNOW = locate("snow");
    
    private static final RainWeatherRenderer RENDERER_RAIN = new RainWeatherRenderer();
    private static final SnowWeatherRenderer RENDERER_SNOW = new SnowWeatherRenderer();
    
    private final boolean isSnow;
    
    public VanillaWeatherController(boolean isSnow){
        this.isSnow = isSnow;
    }
    
    @Override
    public @NotNull Identifier getIdentifier(){
        return isSnow ? IDENTIFIER_RAIN : IDENTIFIER_SNOW;
    }
    
    @Override
    public @NotNull State createState(@NotNull Biome biome){
        var state = new State();
        state.active = true;
        return state;
    }
    
    @Override
    public void tick(@NotNull Random random, @NotNull State state){
        state.wasActive = state.active;
        // 12000, 12000, 12000, 168000
        if(state.time-- == 0){
            var active = state.active = !state.active;
            state.time = 12000 + random.nextInt(active ? 12000 : 168000);
        }
    }
    
    @Override
    public void write(@NotNull State state, @NotNull PacketByteBuf buffer){
        buffer.writeBoolean(state.active);
    }
    
    @Override
    public void read(@NotNull State state, @NotNull PacketByteBuf buffer){
        state.active = buffer.readBoolean();
    }
    
    @Override
    public void writeDelta(@NotNull State state, @NotNull PacketByteBuf buffer){
        if(state.active != state.wasActive || state.wasSet){
            state.wasSet = false;
            write(state, buffer);
        }
    }
    
    @Override
    public void readDelta(@NotNull State state, @NotNull PacketByteBuf buffer){
        read(state, buffer);
    }
    
    @Override
    public @NotNull NbtCompound writeNbt(@NotNull State state){
        var tag = new NbtCompound();
        tag.putInt("time", state.time);
        tag.putBoolean("active", state.active);
        return tag;
    }
    
    @Override
    public void readNbt(@NotNull State state, @NotNull NbtCompound compound){
        state.time = compound.getInt("time");
        state.wasActive = state.active = compound.getBoolean("active");
    }
    
    @Override
    public int getDuration(@NotNull State state){
        return state.time;
    }
    
    @Override
    public boolean set(@NotNull State state, int duration){
        state.time = duration;
        state.active = true;
        state.wasSet = true;
        return true;
    }
    
    @Override
    public boolean isActive(@NotNull State state){
        return state.active;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public @NotNull WeatherRenderer<State, ?> getRenderer(){
        return isSnow ? RENDERER_SNOW : RENDERER_RAIN;
    }
    
    public static final class State{
        private int time;
        private boolean active;
        private boolean wasActive;
        private boolean wasSet;
    
        public boolean isActive(){
            return active;
        }
    }
}
