package net.id.aether.world.weather;

import java.util.Locale;
import java.util.Random;
import java.util.Set;

public enum AetherWeatherType{
    // onTime, randomOnTime, offTime, randomOffTime
    CLEAR(0, 0, 0, 0),
    RAIN(12000, 12000, 12000, 168000),
    SNOW(12000, 12000, 12000, 168000),
    THUNDER(2600, 12000, 12000, 168000, RAIN),
    THUNDER_SNOW(2600, 12000, 24000, 336000, THUNDER),
    ;
    
    private final int onTime;
    private final int randomOnTime;
    private final int offTime;
    private final int randomOffTime;
    private final String name;
    private final Set<AetherWeatherType> implies;
    
    AetherWeatherType(int onTime, int randomOnTime, int offTime, int randomOffTime, AetherWeatherType... implies){
        this.onTime = onTime;
        this.randomOnTime = randomOnTime;
        this.offTime = offTime;
        this.randomOffTime = randomOffTime;
        this.implies = Set.of(implies);
        name = name().toLowerCase(Locale.ROOT);
    }
    
    public static AetherWeatherType getValue(String name){
        return switch(name){
            case "clear" -> CLEAR;
            case "rain" -> RAIN;
            case "snow" -> SNOW;
            case "thunder" -> THUNDER;
            case "thunder_snow" -> THUNDER_SNOW;
            default -> null;
        };
    }
    
    public String getName(){
        return name;
    }
    
    public int getRandomTime(Random random, boolean hasWeather){
        if(hasWeather){
            return random.nextInt(randomOnTime) + onTime;
        }else{
            return random.nextInt(randomOffTime) + offTime;
        }
    }
}
