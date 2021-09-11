package net.id.aether.world.weather.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.id.aether.mixin.client.render.WorldRendererAccessor;
import net.id.aether.world.weather.WeatherRenderer;
import net.id.aether.world.weather.controller.VanillaWeatherController;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.Heightmap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class RainWeatherRenderer implements WeatherRenderer<VanillaWeatherController.State, RainWeatherRenderer.ClientState>{
    @Override
    public RainWeatherRenderer.ClientState createState(VanillaWeatherController.@Nullable State state){
        return new ClientState(state.isActive());
    }
    
    @Override
    public <R extends WorldRenderer & WorldRendererAccessor> void render(@NotNull VanillaWeatherController.State state, @Nullable ClientState clientState, ClientWorld world, R renderer, BlockPos pos, float tickDelta, float ticks, int weatherDistance, Vec3d floatCameraPos, Vec3i intCameraPos){
        if(!state.isActive()){
            return;
        }
    
        var cameraX = floatCameraPos.getX();
        var cameraY = floatCameraPos.getY();
        var cameraZ = floatCameraPos.getZ();
        var x = intCameraPos.getX();
        var y = intCameraPos.getY();
        var z = intCameraPos.getZ();
        var blockX = pos.getX();
        var blockZ = pos.getZ();
        var rendererTicks = renderer.getTicks();
    
        int height = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, pos).getY();
        
        int lowerY = y - weatherDistance;
        if(lowerY < height){
            lowerY = height;
        }
    
        int upperY = y + weatherDistance;
        if(upperY < height){
            upperY = height;
        }
        
        if(lowerY == upperY){
            return;
        }
    
        int w = Math.max(height, y);
    
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
    
        //TODO
        var rainGradient = 1F;
    
        int offsetIndex = (blockZ - z + 16) * 32 + blockX - x + 16;
        double xOffset = renderer.getField_20794()[offsetIndex] * 0.5D;
        double zOffset = renderer.getField_20795()[offsetIndex] * 0.5D;
    
        @SuppressWarnings("IntegerMultiplicationImplicitCastToLong")
        Random random = new Random(blockX * blockX * 3121 + blockX * 45238971 ^ blockZ * blockZ * 418711 + blockZ * 13761);
        var mutable = pos.mutableCopy().set(blockX, lowerY, blockZ);
        float vOffset;
        float ad;
    
        // Rain
        RenderSystem.setShaderTexture(0, WorldRendererAccessor.getRain());
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
    
        int animationStep = rendererTicks + blockX * blockX * 3121 + blockX * 45238971 + blockZ * blockZ * 418711 + blockZ * 13761 & 31;
        vOffset = -((float)animationStep + tickDelta) / 32.0F * (3.0F + random.nextFloat());
        double aa = (double)blockX + 0.5D - cameraX;
        double ab = (double)blockZ + 0.5D - cameraZ;
        float ac = (float)Math.sqrt(aa * aa + ab * ab) / (float)weatherDistance;
        ad = ((1.0F - ac * ac) * 0.5F + 0.5F) * rainGradient;
        mutable.set(blockX, w, blockZ);
        int light = WorldRenderer.getLightmapCoordinates(world, mutable);
        bufferBuilder.vertex(blockX - cameraX - xOffset + 0.5D, upperY - cameraY, blockZ - cameraZ - zOffset + 0.5D)
            .texture(0.0F, lowerY * 0.25F + vOffset)
            .color(1.0F, 1.0F, 1.0F, ad)
            .light(light)
            .next();
        bufferBuilder.vertex(blockX - cameraX + xOffset + 0.5D, upperY - cameraY, blockZ - cameraZ + zOffset + 0.5D)
            .texture(1.0F, lowerY * 0.25F + vOffset)
            .color(1.0F, 1.0F, 1.0F, ad)
            .light(light)
            .next();
        bufferBuilder.vertex(blockX - cameraX + xOffset + 0.5D, lowerY - cameraY, blockZ - cameraZ + zOffset + 0.5D)
            .texture(1.0F, upperY * 0.25F + vOffset)
            .color(1.0F, 1.0F, 1.0F, ad)
            .light(light)
            .next();
        bufferBuilder.vertex(blockX - cameraX - xOffset + 0.5D, lowerY - cameraY, blockZ - cameraZ - zOffset + 0.5D)
            .texture(0.0F, upperY * 0.25F + vOffset)
            .color(1.0F, 1.0F, 1.0F, ad)
            .light(light)
            .next();
    
        tessellator.draw();
    }
    
    public static final class ClientState{
        private ClientState(boolean active){
        
        }
    }
}
