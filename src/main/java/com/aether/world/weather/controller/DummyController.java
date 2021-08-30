package com.aether.world.weather.controller;

import com.aether.world.weather.AetherWeatherType;
import com.aether.world.weather.BiomeWeatherController;
import java.util.OptionalInt;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;

/**
 * A controller that doesn't support any weather, used as a fallback.
 */
public final class DummyController implements BiomeWeatherController{
    public static final BiomeWeatherController INSTANCE = new DummyController();
    
    @Override
    public void tick(ServerWorld world){}
    
    @Override
    public void load(NbtCompound tag){}
    
    @Override
    public NbtCompound save(){
        return new NbtCompound();
    }
    
    @Override
    public OptionalInt get(AetherWeatherType type){
        return OptionalInt.empty();
    }
    
    @Override
    public boolean set(AetherWeatherType type, int duration){
        return false;
    }
    
    @Override
    public boolean has(AetherWeatherType type){
        return false;
    }
    
    @Override
    public boolean hasRaw(AetherWeatherType type){
        return false;
    }
}
