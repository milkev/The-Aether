package net.id.aether.world.weather;

import java.util.OptionalInt;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.id.aether.mixin.client.render.WorldRendererAccessor;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface WeatherController<T>{
    @NotNull Identifier getIdentifier();
    
    @Nullable T createState(@NotNull Biome biome);
    
    void tick(@NotNull Random random, @NotNull T state);
    
    void write(@NotNull T state, @NotNull PacketByteBuf buffer);
    
    @Environment(EnvType.CLIENT)
    void read(@NotNull T state, @NotNull PacketByteBuf buffer);
    
    void writeDelta(@NotNull T state, @NotNull PacketByteBuf buffer);
    
    @Environment(EnvType.CLIENT)
    void readDelta(@NotNull T state, @NotNull PacketByteBuf buffer);
    
    @Nullable NbtCompound writeNbt(@NotNull T state);
    
    void readNbt(@NotNull T state, @NotNull NbtCompound compound);
    
    int getDuration(@NotNull T state);
    
    boolean set(@NotNull T state, NbtCompound data);
    
    boolean isActive(@NotNull T state);
    
    @Environment(EnvType.CLIENT)
    default @Nullable <C> WeatherRenderer<T, C> getRenderer(){
        return null;
    }
}
