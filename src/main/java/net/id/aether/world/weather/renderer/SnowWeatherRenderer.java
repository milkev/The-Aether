package net.id.aether.world.weather.renderer;

import net.id.aether.mixin.client.render.WorldRendererAccessor;
import net.id.aether.world.weather.WeatherRenderer;
import net.id.aether.world.weather.controller.VanillaWeatherController;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class SnowWeatherRenderer implements WeatherRenderer<VanillaWeatherController.State, SnowWeatherRenderer.ClientState>{
    @Override
    public ClientState createState(VanillaWeatherController.@Nullable State state){
        return new ClientState(state);
    }
    
    @Override
    public <R extends WorldRenderer & WorldRendererAccessor> void setupState(LightmapTextureManager manager, R renderer, float tickDelta, Vec3d cameraPos){
    
    }
    
    @Override
    public <R extends WorldRenderer & WorldRendererAccessor> void render(BlockPos pos, VanillaWeatherController.State state, ClientState clientState, R renderer, int renderDistance, float tickDelta, Vec3d cameraPos){
    
    }
    
    @Override
    public <R extends WorldRenderer & WorldRendererAccessor> void teardownState(LightmapTextureManager manager, R renderer){
    
    }
    
    public static final class ClientState{
        private ClientState(VanillaWeatherController.State state){
        }
    }
}
