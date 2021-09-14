package net.id.aether.world.weather.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.id.aether.mixin.client.render.WorldRendererAccessor;
import net.id.aether.world.weather.WeatherRenderer;
import net.id.aether.world.weather.controller.VanillaWeatherController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.Heightmap;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.client.render.WorldRenderer.getLightmapCoordinates;

@Environment(EnvType.CLIENT)
public final class RainWeatherRenderer implements WeatherRenderer<VanillaWeatherController.State, RainWeatherRenderer.ClientState>{
    private ClientWorld world;
    private Tessellator tessellator;
    private BufferBuilder bufferBuilder;
    private Vec3i cameraPos;
    private float[] field_20794;
    private float[] field_20795;
    
    @Override
    public RainWeatherRenderer.ClientState createState(VanillaWeatherController.@Nullable State state){
        return new ClientState(state.isActive());
    }
    
    @Override
    public <R extends WorldRenderer & WorldRendererAccessor> void setupState(LightmapTextureManager manager, R renderer, float tickDelta, Vec3d cameraPos){
        manager.enable();
        world = renderer.getClient().world;
        tessellator = Tessellator.getInstance();
        bufferBuilder = tessellator.getBuffer();
    
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
    
        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    
        this.cameraPos = new Vec3i(
            MathHelper.floor(cameraPos.getX()),
            MathHelper.floor(cameraPos.getY()),
            MathHelper.floor(cameraPos.getZ())
        );
    
        field_20794 = renderer.getField_20794();
        field_20795 = renderer.getField_20795();
    }
    
    @Override
    public <R extends WorldRenderer & WorldRendererAccessor> void render(BlockPos weatherPos, VanillaWeatherController.State state, ClientState clientState, R renderer, int renderDistance, float tickDelta, Vec3d cameraPos){
        var cameraPosX = this.cameraPos.getX();
        var cameraPosY = this.cameraPos.getY();
        var cameraPosZ = this.cameraPos.getZ();
    
        var weatherX = weatherPos.getX();
        var weatherZ = weatherPos.getZ();
    
        int q = (weatherZ - cameraPosZ + 16) * 32 + weatherX - cameraPosX + 16;
        double r = field_20794[q] * 0.5D;
        double s = field_20795[q] * 0.5D;
    
        int heightmap = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, weatherPos).getY();
        int weatherBottom = Math.max(cameraPosY - renderDistance, heightmap);
        int weatherTop = Math.max(cameraPosY + renderDistance, heightmap);
        int w = Math.max(heightmap, cameraPosY);
    
        if(weatherBottom != weatherTop){
            @SuppressWarnings("IntegerMultiplicationImplicitCastToLong")
            Random random = new Random(weatherX * weatherX * 3121 + weatherX * 45238971 ^ weatherZ * weatherZ * 418711 + weatherZ * 13761);
            RenderSystem.setShaderTexture(0, WorldRendererAccessor.getRain());
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
            
            var d = cameraPos.getX();
            var e = cameraPos.getY();
            var g = cameraPos.getZ();
            
            int y = renderer.getTicks() + weatherX * weatherX * 3121 + weatherX * 45238971 + weatherZ * weatherZ * 418711 + weatherZ * 13761 & 31;
            float z = -(y + tickDelta) / 32.0F * (3.0F + random.nextFloat());
            double aa = weatherX + 0.5D - cameraPos.getX();
            double ab = weatherZ + 0.5D - cameraPos.getZ();
            float ac = (float)Math.sqrt(aa * aa + ab * ab) / renderDistance;
            float transparency = ((1.0F - ac * ac) * 0.5F + 0.5F) /*TODO rainGradient*/;
            int light = getLightmapCoordinates(world, weatherPos.withY(w));

            bufferBuilder.vertex((double)weatherX - d - r + 0.5D, (double)weatherTop - e, (double)weatherZ - g - s + 0.5D)
                .texture(0.0F, (float)weatherBottom * 0.25F + z)
                .color(1.0F, 1.0F, 1.0F, transparency)
                .light(light)
                .next();
            bufferBuilder .vertex((double)weatherX - d + r + 0.5D, (double)weatherTop - e, (double)weatherZ - g + s + 0.5D)
                .texture(1.0F, (float)weatherBottom * 0.25F + z).color(1.0F, 1.0F, 1.0F, transparency)
                .light(light)
                .next();
            bufferBuilder.vertex((double)weatherX - d + r + 0.5D, (double)weatherBottom - e, (double)weatherZ - g + s + 0.5D)
                .texture(1.0F, (float)weatherTop * 0.25F + z)
                .color(1.0F, 1.0F, 1.0F, transparency)
                .light(light)
                .next();
            bufferBuilder.vertex((double)weatherX - d - r + 0.5D, (double)weatherBottom - e, (double)weatherZ - g - s + 0.5D)
                .texture(0.0F, (float)weatherTop * 0.25F + z)
                .color(1.0F, 1.0F, 1.0F, transparency)
                .light(light)
                .next();
        }
    }
    
    @Override
    public <R extends WorldRenderer & WorldRendererAccessor> void teardownState(LightmapTextureManager manager, R renderer){
        tessellator.draw();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        manager.disable();
    
        world = null;
        tessellator = null;
        bufferBuilder = null;
        cameraPos = null;
        field_20794 = null;
        field_20795 = null;
    }
    
    public static final class ClientState{
        private ClientState(boolean active){
        
        }
    }
}
