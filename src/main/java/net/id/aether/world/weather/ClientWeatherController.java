package net.id.aether.world.weather;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.*;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.id.aether.mixin.client.render.WorldRendererAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;

@Environment(EnvType.CLIENT)
public final class ClientWeatherController{
    private ClientWeatherController(){}
    
    private static Map<Biome, Map<WeatherController<Object>, Object>> BIOME_CONTROLLERS = Map.of();
    private static Map<Object, Optional<Object>> CLIENT_STATES = Map.of();
    
    //FIXME The server needs to send this info to the client before this gets merged.
    public static void init(DynamicRegistryManager manager, PacketSender sender){
        //noinspection unchecked
        BIOME_CONTROLLERS = (Map<Biome, Map<WeatherController<Object>, Object>>)(Object)AetherWeatherController.createControllerMap(manager);
        sender.sendPacket(AetherWeatherController.PACKET_WEATHER_REQUEST, PacketByteBufs.empty());
        CLIENT_STATES = BIOME_CONTROLLERS.values().stream()
            .flatMap((map)->map.entrySet().stream())
            .filter((entry)->entry.getKey().getRenderer() != null)
            .collect(Collectors.toUnmodifiableMap(
                Map.Entry::getValue, (entry)->{
                    var renderer = entry.getKey().getRenderer();
                    var state = entry.getValue();
                    return Optional.ofNullable(renderer.createState(state));
                }
            ));
    }
    
    public static void deinit(){
        BIOME_CONTROLLERS = Map.of();
    }
    
    public static <T> void updateController(Biome biome, WeatherController<T> controller, PacketByteBuf buffer){
        var controllers = BIOME_CONTROLLERS.get(biome);
        if(controllers == null){
            return;
        }
        @SuppressWarnings("unchecked")
        var state = (T)controllers.get(controller);
        if(state != null){
            controller.readDelta(state, buffer);
        }
    }
    
    public static <T> void setController(Biome biome, WeatherController<T> controller, PacketByteBuf buffer){
        var controllers = BIOME_CONTROLLERS.get(biome);
        if(controllers == null){
            return;
        }
        @SuppressWarnings("unchecked")
        var state = (T)controllers.get(controller);
        if(state != null){
            controller.read(state, buffer);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T> boolean has(Biome biome, WeatherController<T> controller){
        var controllers = BIOME_CONTROLLERS.get(biome);
        if(controllers == null){
            return false;
        }
        var state = controllers.get(controller);
        return state != null && controller.isActive((T)state);
    }
    
    public static <R extends WorldRenderer & WorldRendererAccessor> void render(R renderer, LightmapTextureManager manager, float tickDelta, double cameraX, double cameraY, double cameraZ){
        manager.enable();
        int x = MathHelper.floor(cameraX);
        int y = MathHelper.floor(cameraY);
        int z = MathHelper.floor(cameraZ);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        int weatherDistance = MinecraftClient.isFancyGraphicsOrBetter() ? 10 : 5;
    
        RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
        int m = -1;
        var rendererTicks = renderer.getTicks();
        float ticks = rendererTicks + tickDelta;
        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
    
        //TODO
        float rainGradient = 1;
    
        float[] field_20794 = renderer.getField_20794();
        float[] field_20795 = renderer.getField_20795();
        var world = renderer.getClient().world;
        var intCameraPos = new Vec3i(x, y, z);
        var floatCameraPos = new Vec3d(cameraX, cameraY, cameraZ);
        
        for(int blockZ = z - weatherDistance; blockZ <= z + weatherDistance; blockZ++){
            for(int blockX = x - weatherDistance; blockX <= x + weatherDistance; blockX++){
                int offsetIndex = (blockZ - z + 16) * 32 + blockX - x + 16;
                double xOffset = field_20794[offsetIndex] * 0.5D;
                double zOffset = field_20795[offsetIndex] * 0.5D;
                mutable.set(blockX, 0, blockZ);
                Biome biome = world.getBiome(mutable);
                var map = BIOME_CONTROLLERS.get(biome);
                if(map == null){
                    continue;
                }
                for(var entry : map.entrySet()){
                    var controller = entry.getKey();
                    var weatherRenderer = controller.getRenderer();
                    if(weatherRenderer != null){
                        var state = entry.getValue();
                        var clientState = CLIENT_STATES.get(state).orElse(null);
                        weatherRenderer.render(state, clientState, world, renderer, mutable, tickDelta, ticks, weatherDistance, floatCameraPos, intCameraPos);
                    }
                }
            
                // Yeah I know this is dead, just here so I can reference it in other places.
                boolean isRaining = false && ClientWeatherController.has(biome, AetherWeatherController.CONTROLLER_RAIN);
                boolean isSnowing = false && ClientWeatherController.has(biome, AetherWeatherController.CONTROLLER_SNOW);
                if(!(isRaining || isSnowing)){
                    continue;
                }
            
                int height = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, mutable).getY();
            
                int lowerY = y - weatherDistance;
                if(lowerY < height){
                    lowerY = height;
                }
            
                int upperY = y + weatherDistance;
                if(upperY < height){
                    upperY = height;
                }
            
                int w = Math.max(height, y);
            
                if(lowerY != upperY){
                    @SuppressWarnings("IntegerMultiplicationImplicitCastToLong")
                    Random random = new Random(blockX * blockX * 3121 + blockX * 45238971 ^ blockZ * blockZ * 418711 + blockZ * 13761);
                    mutable.set(blockX, lowerY, blockZ);
                    float vOffset;
                    float ad;
                
                    // Rain
                    if(isRaining){
                        if(m != 0){
                            if(m >= 0){
                                tessellator.draw();
                            }
                        
                            m = 0;
                            RenderSystem.setShaderTexture(0, WorldRendererAccessor.getRain());
                            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
                        }
                    
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
                    }
                    if(isSnowing){
                        // Snow
                        if(m != 1){
                            if(m >= 0){
                                tessellator.draw();
                            }
                        
                            m = 1;
                            RenderSystem.setShaderTexture(0, WorldRendererAccessor.getSnow());
                            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
                        }
                    
                        float af = -((float)(rendererTicks & 511) + tickDelta) / 512.0F;
                        vOffset = (float)(random.nextDouble() + (double)ticks * 0.01D * (double)((float)random.nextGaussian()));
                        float ah = (float)(random.nextDouble() + (double)(ticks * (float)random.nextGaussian()) * 0.001D);
                        double ai = (double)blockX + 0.5D - cameraX;
                        double aj = (double)blockZ + 0.5D - cameraZ;
                        ad = (float)Math.sqrt(ai * ai + aj * aj) / (float)weatherDistance;
                        float alpha = ((1.0F - ad * ad) * 0.3F + 0.5F) * rainGradient;
                        mutable.set(blockX, w, blockZ);
                        int am = WorldRenderer.getLightmapCoordinates(world, mutable);
                        int an = am >> 16 & 0xFFFF;
                        int ao = am & 0xFFFF;
                        int lightV = (an * 3 + 240) / 4;
                        int lightU = (ao * 3 + 240) / 4;
                        bufferBuilder.vertex(blockX - cameraX - xOffset + 0.5D, upperY - cameraY, blockZ - cameraZ - zOffset + 0.5D)
                            .texture(0.0F + vOffset, lowerY * 0.25F + af + ah)
                            .color(1.0F, 1.0F, 1.0F, alpha)
                            .light(lightU, lightV)
                            .next();
                        bufferBuilder.vertex(blockX - cameraX + xOffset + 0.5D, upperY - cameraY, blockZ - cameraZ + zOffset + 0.5D)
                            .texture(1.0F + vOffset, lowerY * 0.25F + af + ah)
                            .color(1.0F, 1.0F, 1.0F, alpha)
                            .light(lightU, lightV)
                            .next();
                        bufferBuilder.vertex(blockX - cameraX + xOffset + 0.5D, lowerY - cameraY, blockZ - cameraZ + zOffset + 0.5D)
                            .texture(1.0F + vOffset, upperY * 0.25F + af + ah)
                            .color(1.0F, 1.0F, 1.0F, alpha)
                            .light(lightU, lightV)
                            .next();
                        bufferBuilder.vertex(blockX - cameraX - xOffset + 0.5D, lowerY - cameraY, blockZ - cameraZ - zOffset + 0.5D)
                            .texture(0.0F + vOffset, upperY * 0.25F + af + ah)
                            .color(1.0F, 1.0F, 1.0F, alpha)
                            .light(lightU, lightV)
                            .next();
                    }
                }
            }
        }
    
        if (m >= 0) {
            tessellator.draw();
        }
    
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        manager.disable();
    }
}
