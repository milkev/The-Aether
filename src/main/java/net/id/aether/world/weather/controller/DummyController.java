package net.id.aether.world.weather.controller;

import net.id.aether.world.weather.AetherWeatherType;
import net.id.aether.world.weather.BiomeWeatherController;
import java.util.OptionalInt;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

/**
 * A controller that doesn't support any weather, used as a fallback.
 */
public final class DummyController implements BiomeWeatherController{
    private final Identifier id;
    
    public DummyController(Identifier id){
        this.id = id;
    }
    
    @Override
    public Identifier getId(){
        return id;
    }
    
    @Override
    public void tick(ServerWorld world){}
    
    @Override
    public void write(PacketByteBuf buffer){}
    
    @Override
    public void read(PacketByteBuf buffer){}
    
    @Override
    public void writeDelta(PacketByteBuf buffer){}
    
    @Override
    public void readDelta(PacketByteBuf buffer){}
    
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
