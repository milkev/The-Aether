package net.id.aether.mixin.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor{
    @Accessor("RAIN") static Identifier getRain(){return new Identifier("textures/environment/rain.png");}
    @Accessor("SNOW") static Identifier getSnow(){return new Identifier("textures/environment/snow.png");}
    
    @Accessor int getTicks();
    @Accessor float[] getField_20794();
    @Accessor float[] getField_20795();
    @Accessor MinecraftClient getClient();
}
