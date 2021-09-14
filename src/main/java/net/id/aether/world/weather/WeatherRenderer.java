package net.id.aether.world.weather;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.id.aether.mixin.client.render.WorldRendererAccessor;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public interface WeatherRenderer<T, C>{
    default @Nullable C createState(@Nullable T state){
        return null;
    }
    
    <R extends WorldRenderer & WorldRendererAccessor> void setupState(LightmapTextureManager manager, R renderer, float tickDelta, Vec3d cameraPos);
    <R extends WorldRenderer & WorldRendererAccessor> void render(BlockPos pos, T state, C clientState, R renderer, int renderDistance, float tickDelta, Vec3d cameraPos);
    <R extends WorldRenderer & WorldRendererAccessor> void teardownState(LightmapTextureManager manager, R renderer);
}
