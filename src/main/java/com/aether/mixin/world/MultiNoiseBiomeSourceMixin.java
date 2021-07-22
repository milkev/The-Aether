package com.aether.mixin.world;

import com.aether.util.SeedSupplier;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Mixin(MultiNoiseBiomeSource.class)
public class MultiNoiseBiomeSourceMixin {
	@Mutable
	@Shadow
	@Final
	private long seed;

	@Unique private static long LAST_SEED = SeedSupplier.MARKER;

	@Redirect(method = "lambda$static$7(Lcom/mojang/serialization/codecs/RecordCodecBuilder$Instance;)Lcom/mojang/datafixers/kinds/App;", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/PrimitiveCodec;fieldOf(Ljava/lang/String;)Lcom/mojang/serialization/MapCodec;", ordinal = 0))
	private static MapCodec<Long> giveUsRandomSeeds(PrimitiveCodec<Long> codec, final String name) {
		return codec.fieldOf(name).orElseGet(SeedSupplier::getSeed);
	}

	@Inject(method = "<init>(JLjava/util/List;Lnet/minecraft/world/level/biome/MultiNoiseBiomeSource$NoiseParameters;Lnet/minecraft/world/level/biome/MultiNoiseBiomeSource$NoiseParameters;Lnet/minecraft/world/level/biome/MultiNoiseBiomeSource$NoiseParameters;Lnet/minecraft/world/level/biome/MultiNoiseBiomeSource$NoiseParameters;Ljava/util/Optional;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/biome/MultiNoiseBiomeSource;seed:J", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
	private void replaceSeed(long seed, List<Pair<Biome.ClimateParameters, Supplier<Biome>>> biomePoints, MultiNoiseBiomeSource.NoiseParameters temperatureNoiseParameters, MultiNoiseBiomeSource.NoiseParameters humidityNoiseParameters, MultiNoiseBiomeSource.NoiseParameters altitudeNoiseParameters, MultiNoiseBiomeSource.NoiseParameters weirdnessNoiseParameters, Optional<Pair<Registry<Biome>, MultiNoiseBiomeSource.Preset>> instance, CallbackInfo ci) {
		if (seed == SeedSupplier.MARKER) {
			this.seed = LAST_SEED;
		} else {
			LAST_SEED = seed;
		}
	}

	@Redirect(method = "<init>(JLjava/util/List;Lnet/minecraft/world/level/biome/MultiNoiseBiomeSource$NoiseParameters;Lnet/minecraft/world/level/biome/MultiNoiseBiomeSource$NoiseParameters;Lnet/minecraft/world/level/biome/MultiNoiseBiomeSource$NoiseParameters;Lnet/minecraft/world/level/biome/MultiNoiseBiomeSource$NoiseParameters;Ljava/util/Optional;)V", at = @At(value = "NEW", target = "net/minecraft/world/level/levelgen/WorldgenRandom"))
	private WorldgenRandom useOurSeed(long seed) {
		return new WorldgenRandom(this.seed);
	}
}
