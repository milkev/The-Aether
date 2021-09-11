package net.id.aether.mixin.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.id.aether.world.dimension.AetherDimension;
import net.id.aether.world.weather.AetherWeatherController;
import net.id.aether.world.weather.ClientWeatherController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
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
    @Shadow private int ticks;
    @Shadow @Final private float[] field_20794;
    @Shadow @Final private float[] field_20795;
    @Shadow @Final private static Identifier SNOW;
    @Shadow @Final private static Identifier RAIN;
    
    @Shadow public abstract void tick();
    @Shadow public static int getLightmapCoordinates(BlockRenderView world, BlockPos pos){return 0;}
    
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
        int weatherDistance = 5;
        if(MinecraftClient.isFancyGraphicsOrBetter()){
            weatherDistance = 10;
        }
    
        RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
        int m = -1;
        float ticks = this.ticks + tickDelta;
        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        
        //TODO
        float rainGradient = 1;
        
        var biomeRegistry = world.getRegistryManager().get(Registry.BIOME_KEY);
        
        for(int blockY = z - weatherDistance; blockY <= z + weatherDistance; blockY++){
            for(int blockX = x - weatherDistance; blockX <= x + weatherDistance; blockX++){
                int offsetIndex = (blockY - z + 16) * 32 + blockX - x + 16;
                double xOffset = field_20794[offsetIndex] * 0.5D;
                double zOffset = field_20795[offsetIndex] * 0.5D;
                mutable.set(blockX, 0, blockY);
                Biome biome = world.getBiome(mutable);
                var biomeId = biomeRegistry.getId(biome);
                if(biomeId == null){
                    //How?
                    continue;
                }
                
                boolean isRaining = ClientWeatherController.has(biomeId, AetherWeatherController.CONTROLLER_RAIN);
                boolean isSnowing = ClientWeatherController.has(biomeId, AetherWeatherController.CONTROLLER_SNOW);
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
                    Random random = new Random(blockX * blockX * 3121 + blockX * 45238971 ^ blockY * blockY * 418711 + blockY * 13761);
                    mutable.set(blockX, lowerY, blockY);
                    float vOffset;
                    float ad;
                    
                    // Rain
                    if(isRaining){
                        if(m != 0){
                            if(m >= 0){
                                tessellator.draw();
                            }
                        
                            m = 0;
                            RenderSystem.setShaderTexture(0, RAIN);
                            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
                        }
                    
                        int animationStep = this.ticks + blockX * blockX * 3121 + blockX * 45238971 + blockY * blockY * 418711 + blockY * 13761 & 31;
                        vOffset = -((float)animationStep + tickDelta) / 32.0F * (3.0F + random.nextFloat());
                        double aa = (double)blockX + 0.5D - cameraX;
                        double ab = (double)blockY + 0.5D - cameraZ;
                        float ac = (float)Math.sqrt(aa * aa + ab * ab) / (float)weatherDistance;
                        ad = ((1.0F - ac * ac) * 0.5F + 0.5F) * rainGradient;
                        mutable.set(blockX, w, blockY);
                        int light = getLightmapCoordinates(world, mutable);
                        bufferBuilder.vertex(blockX - cameraX - xOffset + 0.5D, upperY - cameraY, blockY - cameraZ - zOffset + 0.5D)
                            .texture(0.0F, lowerY * 0.25F + vOffset)
                            .color(1.0F, 1.0F, 1.0F, ad)
                            .light(light)
                            .next();
                        bufferBuilder.vertex(blockX - cameraX + xOffset + 0.5D, upperY - cameraY, blockY - cameraZ + zOffset + 0.5D)
                            .texture(1.0F, lowerY * 0.25F + vOffset)
                            .color(1.0F, 1.0F, 1.0F, ad)
                            .light(light)
                            .next();
                        bufferBuilder.vertex(blockX - cameraX + xOffset + 0.5D, lowerY - cameraY, blockY - cameraZ + zOffset + 0.5D)
                            .texture(1.0F, upperY * 0.25F + vOffset)
                            .color(1.0F, 1.0F, 1.0F, ad)
                            .light(light)
                            .next();
                        bufferBuilder.vertex(blockX - cameraX - xOffset + 0.5D, lowerY - cameraY, blockY - cameraZ - zOffset + 0.5D)
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
                            RenderSystem.setShaderTexture(0, SNOW);
                            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
                        }
                    
                        float af = -((float)(this.ticks & 511) + tickDelta) / 512.0F;
                        vOffset = (float)(random.nextDouble() + (double)ticks * 0.01D * (double)((float)random.nextGaussian()));
                        float ah = (float)(random.nextDouble() + (double)(ticks * (float)random.nextGaussian()) * 0.001D);
                        double ai = (double)blockX + 0.5D - cameraX;
                        double aj = (double)blockY + 0.5D - cameraZ;
                        ad = (float)Math.sqrt(ai * ai + aj * aj) / (float)weatherDistance;
                        float alpha = ((1.0F - ad * ad) * 0.3F + 0.5F) * rainGradient;
                        mutable.set(blockX, w, blockY);
                        int am = getLightmapCoordinates(world, mutable);
                        int an = am >> 16 & 0xFFFF;
                        int ao = am & 0xFFFF;
                        int lightV = (an * 3 + 240) / 4;
                        int lightU = (ao * 3 + 240) / 4;
                        bufferBuilder.vertex(blockX - cameraX - xOffset + 0.5D, upperY - cameraY, blockY - cameraZ - zOffset + 0.5D)
                            .texture(0.0F + vOffset, lowerY * 0.25F + af + ah)
                            .color(1.0F, 1.0F, 1.0F, alpha)
                            .light(lightU, lightV)
                            .next();
                        bufferBuilder.vertex(blockX - cameraX + xOffset + 0.5D, upperY - cameraY, blockY - cameraZ + zOffset + 0.5D)
                            .texture(1.0F + vOffset, lowerY * 0.25F + af + ah)
                            .color(1.0F, 1.0F, 1.0F, alpha)
                            .light(lightU, lightV)
                            .next();
                        bufferBuilder.vertex(blockX - cameraX + xOffset + 0.5D, lowerY - cameraY, blockY - cameraZ + zOffset + 0.5D)
                            .texture(1.0F + vOffset, upperY * 0.25F + af + ah)
                            .color(1.0F, 1.0F, 1.0F, alpha)
                            .light(lightU, lightV)
                            .next();
                        bufferBuilder.vertex(blockX - cameraX - xOffset + 0.5D, lowerY - cameraY, blockY - cameraZ - zOffset + 0.5D)
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
