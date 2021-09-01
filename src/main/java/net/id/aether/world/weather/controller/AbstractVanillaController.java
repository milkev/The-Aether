package net.id.aether.world.weather.controller;

import java.util.OptionalInt;
import net.id.aether.world.weather.AetherWeatherType;
import net.id.aether.world.weather.BiomeWeatherController;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;

@SuppressWarnings("PointlessBitwiseExpression")
abstract class AbstractVanillaController implements BiomeWeatherController{
    private final boolean isSnowy;
    
    protected int clearTime;
    protected int precipitatingTime;
    protected int thunderTime;
    
    protected boolean isPrecipitating = false;
    protected boolean isThundering = false;
    protected boolean wasPrecipitating = false;
    protected boolean wasThundering = false;
    
    protected AbstractVanillaController(boolean isSnowy){
        this.isSnowy = isSnowy;
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
        }else{
            thunderTime = AetherWeatherType.THUNDER.getRandomTime(random, isThundering);
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
    
    @Override
    public void writeDelta(PacketByteBuf buffer){
        if((isPrecipitating != wasPrecipitating) || (isThundering != wasThundering)){
            buffer.writeByte(
                (isPrecipitating ? 1 << 0 : 0) |
                (isThundering ? 1 << 1 : 0)
            );
        }
    }
    
    @Override
    public void readDelta(PacketByteBuf buffer){
        var state = buffer.readByte();
        isPrecipitating = (state & 1 << 0) != 0;
        isThundering = (state & 1 << 1) != 0;
    }
    
    @Override
    public void load(NbtCompound tag){
        clearTime = tag.getInt("clear");
        precipitatingTime = tag.getInt(isSnowy ? "snow" : "rain");
        thunderTime = tag.getInt("thunder");
        
        isPrecipitating = tag.getBoolean(isSnowy ? "snowing" : "raining");
        isThundering = tag.getBoolean("thundering");
    }
    
    @Override
    public NbtCompound save(){
        var tag = new NbtCompound();
        
        tag.putInt("clear", clearTime);
        tag.putInt(isSnowy ? "snow" : "rain", precipitatingTime);
        tag.putInt("thunder", thunderTime);
        
        tag.putBoolean(isSnowy ? "snowing" : "raining", isPrecipitating);
        tag.putBoolean("thundering", isThundering);
        
        return tag;
    }
    
    @Override
    public OptionalInt get(AetherWeatherType type){
        if(isSnowy){
            return switch(type){
                case CLEAR -> OptionalInt.of(clearTime);
                case SNOW -> OptionalInt.of(precipitatingTime);
                case THUNDER_SNOW -> OptionalInt.of(thunderTime);
                default -> OptionalInt.empty();
            };
        }else{
            return switch(type){
                case CLEAR -> OptionalInt.of(clearTime);
                case RAIN -> OptionalInt.of(precipitatingTime);
                case THUNDER -> OptionalInt.of(thunderTime);
                default -> OptionalInt.empty();
            };
        }
    }
    
    @Override
    public boolean set(AetherWeatherType type, int duration){
        if(isSnowy){
            return switch(type){
                case CLEAR -> {
                    clearTime = duration;
                    isPrecipitating = false;
                    isThundering = false;
                    yield true;
                }
                case SNOW -> {
                    precipitatingTime = duration;
                    isPrecipitating = true;
                    isThundering = false;
                    yield true;
                }
                case THUNDER_SNOW -> {
                    thunderTime = duration;
                    isThundering = true;
                    isPrecipitating = false;
                    yield true;
                }
                default -> false;
            };
        }else{
            return switch(type){
                case CLEAR -> {
                    clearTime = duration;
                    isPrecipitating = false;
                    isThundering = false;
                    yield true;
                }
                case RAIN -> {
                    precipitatingTime = duration;
                    isPrecipitating = true;
                    isThundering = false;
                    yield true;
                }
                case THUNDER -> {
                    thunderTime = duration;
                    isThundering = true;
                    isPrecipitating = false;
                    yield true;
                }
                default -> false;
            };
        }
    }
    
    @Override
    public boolean has(AetherWeatherType type){
        if(isSnowy){
            return switch(type){
                case SNOW -> isPrecipitating | isThundering;
                case THUNDER_SNOW -> isThundering;
                default -> false;
            };
        }else{
            return switch(type){
                case RAIN -> isPrecipitating | isThundering;
                case THUNDER -> isThundering;
                default -> false;
            };
        }
    }
    
    @Override
    public boolean hasRaw(AetherWeatherType type){
        if(isSnowy){
            return switch(type){
                case SNOW -> isPrecipitating;
                case THUNDER_SNOW -> isThundering;
                default -> false;
            };
        }else{
            return switch(type){
                case RAIN -> isPrecipitating;
                case THUNDER -> isThundering;
                default -> false;
            };
        }
    }
}
