package com.aether.mixin.world;

import com.aether.util.SeedSupplier;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseChunkGeneratorMixin extends ChunkGenerator {
	@Mutable
	@Final
	@Shadow
	private long seed;

	@Unique private static long LAST_SEED = SeedSupplier.MARKER;

	public NoiseChunkGeneratorMixin(BiomeSource biomeSource, StructureSettings structuresConfig) {
		super(biomeSource, structuresConfig);
	}

	@Redirect(method = "lambda$static$3(Lcom/mojang/serialization/codecs/RecordCodecBuilder$Instance;)Lcom/mojang/datafixers/kinds/App;", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/PrimitiveCodec;fieldOf(Ljava/lang/String;)Lcom/mojang/serialization/MapCodec;", ordinal = 0))
	private static MapCodec<Long> giveUsRandomSeeds(PrimitiveCodec<Long> codec, final String name) {
		return codec.fieldOf(name).orElseGet(SeedSupplier::getSeed);
	}

	@Inject(method = "<init>(Lnet/minecraft/world/level/biome/BiomeSource;Lnet/minecraft/world/level/biome/BiomeSource;JLjava/util/function/Supplier;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/levelgen/NoiseBasedChunkGenerator;seed:J", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
	private void replaceSeed(BiomeSource populationSource, BiomeSource biomeSource, long seed, Supplier<NoiseGeneratorSettings> settings, CallbackInfo ci) {
		if (seed == SeedSupplier.MARKER) {
			this.seed = LAST_SEED;
			this.strongholdSeed = LAST_SEED;
		} else {
			LAST_SEED = seed;
		}
	}

	@Redirect(method = "<init>(Lnet/minecraft/world/level/biome/BiomeSource;Lnet/minecraft/world/level/biome/BiomeSource;JLjava/util/function/Supplier;)V", at = @At(value = "NEW", target = "net/minecraft/world/level/levelgen/WorldgenRandom"))
	private WorldgenRandom useOurSeed(long seed) {
		return new WorldgenRandom(this.seed);
	}
}
