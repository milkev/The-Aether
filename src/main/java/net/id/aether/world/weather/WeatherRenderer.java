package net.id.aether.world.weather;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.id.aether.mixin.client.render.WorldRendererAccessor;
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
    
    <R extends WorldRenderer & WorldRendererAccessor> void render(@NotNull T state, @Nullable C clientState, ClientWorld world, R renderer, BlockPos pos, float tickDelta, float ticks, int weatherDistance, Vec3d floatCameraPos, Vec3i intCameraPos);
}
