package com.aether.world.weather.controller;

import com.aether.world.weather.AetherWeatherType;
import com.aether.world.weather.BiomeWeatherController;
import java.util.OptionalInt;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;

/**
 * Mimics vanilla weather in warm biomes.
 */
public final class VanillaController implements BiomeWeatherController{
    private int clearTime;
    private int rainTime;
    private int thunderTime;
    
    private boolean isRaining = false;
    private boolean isThundering = false;
    
    @Override
    public void tick(ServerWorld world){
        if(clearTime > 0){
            clearTime--;
            isRaining = false;
            isThundering = false;
            return;
        }
        
        var random = world.getRandom();
        
        if(thunderTime > 0){
            thunderTime--;
            if(thunderTime == 0){
                isThundering = !isThundering;
            }
        }else{
            thunderTime = AetherWeatherType.THUNDER.getRandomTime(random, isThundering);
        }
        
        if(rainTime > 0){
            rainTime--;
            if(rainTime == 0){
                isRaining = !isRaining;
            }
        }else{
            rainTime = AetherWeatherType.RAIN.getRandomTime(random, isRaining);
        }
    }
    
    @Override
    public void load(NbtCompound tag){
        clearTime = tag.getInt("clear");
        rainTime = tag.getInt("rain");
        thunderTime = tag.getInt("thunder");
        
        isRaining = tag.getBoolean("raining");
        isThundering = tag.getBoolean("thundering");
    }
    
    @Override
    public NbtCompound save(){
        var tag = new NbtCompound();
        
        tag.putInt("clear", clearTime);
        tag.putInt("rain", rainTime);
        tag.putInt("thunder", thunderTime);
        
        tag.putBoolean("raining", isRaining);
        tag.putBoolean("thundering", isThundering);
        
        return tag;
    }
    
    @Override
    public OptionalInt get(AetherWeatherType type){
        return switch(type){
            case CLEAR -> OptionalInt.of(clearTime);
            case RAIN -> OptionalInt.of(rainTime);
            case THUNDER -> OptionalInt.of(thunderTime);
            default -> OptionalInt.empty();
        };
    }
    
    @Override
    public boolean set(AetherWeatherType type, int duration){
        return switch(type){
            case CLEAR -> {
                clearTime = duration;
                isRaining = false;
                isThundering = false;
                yield true;
            }
            case RAIN -> {
                rainTime = duration;
                isRaining = true;
                isThundering = false;
                yield true;
            }
            case THUNDER -> {
                thunderTime = duration;
                isThundering = true;
                isRaining = false;
                yield true;
            }
            default -> false;
        };
    }
    
    @Override
    public boolean has(AetherWeatherType type){
        return switch(type){
            case RAIN -> isRaining | isThundering;
            case THUNDER -> isThundering;
            default -> false;
        };
    }
    
    @Override
    public boolean hasRaw(AetherWeatherType type){
        return switch(type){
            case RAIN -> isRaining;
            case THUNDER -> isThundering;
            default -> false;
        };
    }
}
