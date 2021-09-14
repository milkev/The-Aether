package net.id.aether.world.weather.controller;

import java.util.Objects;
import java.util.Random;
import net.id.aether.world.weather.WeatherController;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.NotNull;

public final class ThunderWeatherController implements WeatherController<ThunderWeatherController.State>{
    private final Identifier identifier;
    
    public ThunderWeatherController(@NotNull Identifier identifier){
        Objects.requireNonNull(identifier, "identifier was null");
        this.identifier = identifier;
    }
    
    @Override
    public @NotNull Identifier getIdentifier(){
        return identifier;
    }
    
    @Override
    public State createState(@NotNull Biome biome){
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
    public boolean set(@NotNull State state, NbtCompound data){
        if(data.contains("time", NbtElement.INT_TYPE) && data.contains("active", NbtElement.BYTE_TYPE)){
            readNbt(state, data);
            state.wasSet = true;
            return true;
        }else{
            return false;
        }
    }
    
    @Override
    public boolean isActive(@NotNull State state){
        return state.active;
    }
    
    public static final class State{
        private int time;
        private boolean active;
        private boolean wasActive;
        private boolean wasSet;
    }
}
