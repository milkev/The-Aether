package com.aether.world.weather.controller;

import com.aether.world.weather.AetherWeatherType;
import com.aether.world.weather.BiomeWeatherController;
import java.util.OptionalInt;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;

/**
 * Mimics vanilla biomes that have snow in them.
 */
public final class SnowController implements BiomeWeatherController{
    private int clearTime;
    private int snowTime;
    private int thunderTime;
    
    private boolean isSnowing = false;
    private boolean isThundering = false;
    
    @Override
    public void tick(ServerWorld world){
        if(clearTime > 0){
            clearTime--;
            isSnowing = false;
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
            thunderTime = AetherWeatherType.THUNDER_SNOW.getRandomTime(random, isThundering);
        }
    
        if(snowTime > 0){
            snowTime--;
            if(snowTime == 0){
                isSnowing = !isSnowing;
            }
        }else{
            snowTime = AetherWeatherType.SNOW.getRandomTime(random, isSnowing);
        }
    }
    
    @Override
    public void load(NbtCompound tag){
        clearTime = tag.getInt("clear");
        snowTime = tag.getInt("snow");
        thunderTime = tag.getInt("thunder");
        
        isSnowing = tag.getBoolean("snowing");
        isThundering = tag.getBoolean("thundering");
    }
    
    @Override
    public NbtCompound save(){
        var tag = new NbtCompound();
        
        tag.putInt("clear", clearTime);
        tag.putInt("snow", snowTime);
        tag.putInt("thunder", thunderTime);
        
        tag.putBoolean("snowing", isSnowing);
        tag.putBoolean("thundering", isThundering);
        
        return tag;
    }
    
    @Override
    public OptionalInt get(AetherWeatherType type){
        return switch(type){
            case CLEAR -> OptionalInt.of(clearTime);
            case SNOW -> OptionalInt.of(snowTime);
            case THUNDER_SNOW -> OptionalInt.of(thunderTime);
            default -> OptionalInt.empty();
        };
    }
    
    @Override
    public boolean set(AetherWeatherType type, int duration){
        return switch(type){
            case CLEAR -> {
                clearTime = duration;
                isSnowing = false;
                isThundering = false;
                yield true;
            }
            case SNOW -> {
                snowTime = duration;
                isSnowing = true;
                isThundering = false;
                yield true;
            }
            case THUNDER_SNOW -> {
                thunderTime = duration;
                isThundering = true;
                isSnowing = false;
                yield true;
            }
            default -> false;
        };
    }
    
    @Override
    public boolean has(AetherWeatherType type){
        return switch(type){
            case SNOW -> isSnowing | isThundering;
            case THUNDER_SNOW -> isThundering;
            default -> false;
        };
    }
    
    @Override
    public boolean hasRaw(AetherWeatherType type){
        return switch(type){
            case SNOW -> isSnowing;
            case THUNDER_SNOW -> isThundering;
            default -> false;
        };
    }
}
