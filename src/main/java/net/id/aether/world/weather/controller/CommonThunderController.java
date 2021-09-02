package net.id.aether.world.weather.controller;

import net.id.aether.world.weather.AetherWeatherType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

/**
 * A controller that mimics Vanilla behavior but makes thunderstorms twice as common.
 */
public final class CommonThunderController extends AbstractVanillaController{
    public CommonThunderController(Identifier id){
        super(id, false);
    }
    
    @Override
    public void tick(ServerWorld world){
        wasPrecipitating = isPrecipitating;
        wasThundering = isThundering;
        
        if(clearTime > 0){
            clearTime--;
            isPrecipitating = false;
            isThundering = false;
            return;
        }
        
        var random = world.getRandom();
        
        if(thunderTime > 0){
            thunderTime--;
            if(thunderTime == 0){
                isThundering = !isThundering;
            }
        }else if(isThundering){
            thunderTime = AetherWeatherType.THUNDER.getRandomTime(random, true);
        }else{
            thunderTime = AetherWeatherType.THUNDER.getRandomTime(random, false) >> 1;
        }
        
        if(precipitatingTime > 0){
            precipitatingTime--;
            if(precipitatingTime == 0){
                isPrecipitating = !isPrecipitating;
            }
        }else{
            precipitatingTime = AetherWeatherType.RAIN.getRandomTime(random, isPrecipitating);
        }
    }
}
