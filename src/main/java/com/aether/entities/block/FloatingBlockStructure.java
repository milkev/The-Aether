package com.aether.entities.block;

import java.util.ArrayList;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FloatingBlockStructure {
    private static ArrayList<FloatingBlockStructure> allStructures = new ArrayList<>(0);
    protected ArrayList<FloatingBlockInfoWrapper> blockInfos = new ArrayList<>(0);

    public FloatingBlockStructure(FloatingBlockEntity entity1, FloatingBlockEntity entity2, Vec3i offset) {
        this.blockInfos.add(new FloatingBlockInfoWrapper(entity1, Vec3i.ZERO));
        this.blockInfos.add(new FloatingBlockInfoWrapper(entity2, offset));
        init();
    }

    public FloatingBlockStructure(ArrayList<FloatingBlockInfoWrapper> arr){
        this.blockInfos = arr;
        init();
    }

    public void spawn(Level world){
        blockInfos.forEach(blockInfo -> {
            blockInfo.block.markPartOfStructure();
            if(!blockInfo.equals(blockInfos.get(0))){
                blockInfo.block.dropItem = false;
            }
            blockInfo.block.floatTime = 0;
            world.addFreshEntity(blockInfo.block);
        });
    }

    public void postTick(){
        FloatingBlockInfoWrapper master = blockInfos.get(0);
        for(FloatingBlockInfoWrapper blockInfo : blockInfos){
            if (!blockInfo.equals(master)) {
                this.alignToMaster(blockInfo);
            }
            if(blockInfo.block.isRemoved()){
                land(blockInfo);
                break;
            }
        }
    }

    protected void alignToMaster(FloatingBlockInfoWrapper blockInfo){
        FloatingBlockInfoWrapper master = blockInfos.get(0);
        Vec3 newPos = master.block.position().add(Vec3.atLowerCornerOf(blockInfo.offset));
        blockInfo.block.setPosRaw(newPos.x, newPos.y, newPos.z);
        blockInfo.block.setDeltaMovement(master.block.getDeltaMovement());
        blockInfo.block.setDropping(master.block.isDropping());
    }

    public void land(FloatingBlockInfoWrapper lander){
        for(FloatingBlockInfoWrapper blockInfo : blockInfos){
            alignToMaster(blockInfo);
            if (!blockInfo.equals(lander)) {
                blockInfo.block.land((float) blockInfos.get(0).block.getDeltaMovement().length());
            }
        }
        allStructures.remove(this);
    }

    public static ArrayList<FloatingBlockStructure> getAllStructures(){
        return allStructures;
    }

    protected void init(){
        allStructures.add(this);
    }

    public static class FloatingBlockInfoWrapper {
        FloatingBlockEntity block;
        Vec3i offset;
        public FloatingBlockInfoWrapper(FloatingBlockEntity block, Vec3i offset){
            this.block = block;
            this.offset = offset;
        }
    }
}
