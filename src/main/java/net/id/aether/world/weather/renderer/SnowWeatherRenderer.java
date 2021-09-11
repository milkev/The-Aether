package net.id.aether.world.weather.renderer;

import net.id.aether.mixin.client.render.WorldRendererAccessor;
import net.id.aether.world.weather.WeatherRenderer;
import net.id.aether.world.weather.controller.VanillaWeatherController;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SnowWeatherRenderer implements WeatherRenderer<VanillaWeatherController.State, SnowWeatherRenderer.State>{
    @Override
    public SnowWeatherRenderer.State createState(VanillaWeatherController.@Nullable State state){
        return new State(state);
    }
    
    @Override
    public <R extends WorldRenderer & WorldRendererAccessor> void render(VanillaWeatherController.@NotNull State state, @Nullable State clientState, ClientWorld world, R renderer, BlockPos pos, float tickDelta, float ticks, int weatherDistance, Vec3d floatCameraPos, Vec3i intCameraPos){
    
    }
    
    public static final class State{
        private State(VanillaWeatherController.State state){
        }
    }
}
