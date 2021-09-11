package net.id.aether.mixin.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.id.aether.world.dimension.AetherDimension;
import net.id.aether.world.weather.ClientWeatherController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// This entire thing needs to be replaced, I'm not documenting this for that reason.
@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin{
    @Shadow @Final private MinecraftClient client;
    
    @Shadow public abstract void tick();
    
    @Inject(
        method = "renderWeather",
        at = @At("HEAD"),
        cancellable = true
    )
    private void renderWeather(LightmapTextureManager manager, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo ci){
        var world = client.world;
        if(world == null){
            // Shouldn't happen...
            return;
        }
        
        if(!world.getRegistryKey().equals(AetherDimension.AETHER_WORLD_KEY)){
            return;
        }
    
        ci.cancel();
        
        ClientWeatherController.render((WorldRenderer & WorldRendererAccessor)(Object)this, manager, tickDelta, cameraX, cameraY, cameraZ);
    }
}
