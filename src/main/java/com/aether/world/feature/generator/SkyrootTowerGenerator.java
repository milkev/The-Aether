package com.aether.world.feature.generator;

import com.aether.Aether;
import com.aether.world.feature.AetherFeatures;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

public class SkyrootTowerGenerator {
    private static final ResourceLocation SKYROOT_TOWER = Aether.locate("skyroot_tower");

    public static void addPieces(StructureManager manager, StructurePieceAccessor structurePiecesHolder, Random random, BlockPos pos) {
        Rotation blockRotation = Rotation.getRandom(random);
        structurePiecesHolder.addPiece(new SkyrootTowerGenerator.Piece(manager, SKYROOT_TOWER, pos, blockRotation));
    }

    public static class Piece extends TemplateStructurePiece {
        private boolean shifted = false;

        public Piece(StructureManager manager, ResourceLocation template, BlockPos pos, Rotation rotation) {
            super(AetherFeatures.SKYROOT_TOWER_PIECE, 0, manager, template, template.toString(), createPlacementData(rotation), pos);
        }

        public Piece(ServerLevel world, CompoundTag nbt) {
            super(AetherFeatures.SKYROOT_TOWER_PIECE, nbt, world, (identifier) -> createPlacementData(Rotation.valueOf(nbt.getString("Rot"))));
        }

        private static StructurePlaceSettings createPlacementData(Rotation rotation) {
            return (new StructurePlaceSettings()).setRotation(rotation).setMirror(Mirror.NONE).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        }

        protected void addAdditionalSaveData(ServerLevel world, CompoundTag nbt) {
            super.addAdditionalSaveData(world, nbt);
            nbt.putString("Rot", this.placeSettings.getRotation().name());
        }

        protected void handleDataMarker(String metadata, BlockPos pos, ServerLevelAccessor world, Random random, BoundingBox boundingBox) {
        }

        public boolean postProcess(WorldGenLevel world, StructureFeatureManager structureAccessor, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos pos) {
            if (this.templatePosition.getY() > 2) {
                if (!shifted) {
                    this.templatePosition = this.templatePosition.below(1);
                    shifted = true;
                }
                boundingBox.encapsulate(this.template.getBoundingBox(this.placeSettings, this.templatePosition));
                return super.postProcess(world, structureAccessor, chunkGenerator, random, boundingBox, chunkPos, pos);
            }
            return false;
        }
    }
}
