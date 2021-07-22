package com.aether.mixin.client;

import com.aether.blocks.aercloud.DenseAercloudFluid;
import com.aether.util.RegistryUtil;
import com.aether.world.dimension.AetherDimension;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class BackgroundRendererMixin {
    @Redirect(method = "setupColor", at = @At(value = "FIELD", target = "Lnet/minecraft/world/phys/Vec3;y:D", opcode = Opcodes.GETFIELD, ordinal = 1))
    private static double adjustVoidVector(Vec3 vec3d) {
        return RegistryUtil.dimensionMatches(Minecraft.getInstance().level, AetherDimension.TYPE) ? Double.MAX_VALUE : vec3d.y;
    }

    @Shadow private static float fogRed;
    @Shadow private static float fogGreen;
    @Shadow private static float fogBlue;

    @Environment(EnvType.CLIENT)
    @Inject(method = "setupColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getPosition()Lnet/minecraft/world/phys/Vec3;"))
    private static void denseAercloudRenderColor(Camera camera, float tickDelta, ClientLevel world, int i, float f, CallbackInfo ci){
        BlockPos playerPos = new BlockPos(Minecraft.getInstance().player.getEyePosition());
        if(camera.getFluidInCamera() == FogType.WATER
                && world.getFluidState(playerPos).getType() instanceof DenseAercloudFluid) {
            fogRed = 0.323F;
            fogGreen = 0.434F;
            fogBlue = 0.485F;
        }
    }
}
