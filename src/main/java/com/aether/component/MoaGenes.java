package com.aether.component;

import com.aether.api.MoaAPI;
import com.aether.api.MoaAttributes;
import com.aether.entities.passive.MoaEntity;
import com.aether.items.AetherItems;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MoaGenes /*implements AutoSyncedComponent*/ {

    private final Object2FloatOpenHashMap<MoaAttributes> attributeMap = new Object2FloatOpenHashMap<>();
    private MoaAPI.Race race = MoaAPI.FALLBACK_MOA;
    private MoaAttributes affinity;
    private boolean legendary, initialized;
    private UUID owner;
    private float hunger = 100F;

    public MoaGenes() {}

    public void initMoa(@NotNull MoaEntity moa) {
        Level world = moa.level;
        Random random = moa.getRandom();
        race = MoaAPI.getMoaForBiome(world.getBiomeName(moa.blockPosition()).get(), random);
        affinity = race.defaultAffinity();

        for (MoaAttributes attribute : MoaAttributes.values()) {
            attributeMap.addTo(attribute, race.statWeighting().configure(attribute, race, random));
        }
        initialized = true;
    }

    public ItemStack getEggForBreeding(MoaGenes otherParent, Level world, BlockPos pos) {
        MoaAPI.Race childRace = MoaAPI.getMoaForBreeding(this, otherParent, world, pos);

        ItemStack stack = new ItemStack(AetherItems.MOA_EGG);
        CompoundTag nbt = stack.getOrCreateTagElement("genes");
        Random random = world.getRandom();
        MoaGenes genes = new MoaGenes();

        float increaseChance = 1F;
        for (MoaAttributes attribute : MoaAttributes.values()) {
            boolean increase = random.nextFloat() <= increaseChance;
            genes.attributeMap.addTo(attribute, attribute.fromBreeding(this, otherParent, increase));
            if(increase) {
                increaseChance /= 2;
            }
        }
        genes.race = childRace;
        genes.affinity = random.nextBoolean() ? this.affinity : otherParent.affinity;
        genes.owner = random.nextBoolean() ? this.owner : otherParent.owner;
        genes.initialized = true;

        genes.writeToNbt(nbt);
        nbt.putBoolean("baby", true);
        return stack;
    }

    public static ItemStack getEggForCommand(MoaAPI.Race race, Level world, boolean baby) {
        ItemStack stack = new ItemStack(AetherItems.MOA_EGG);
        CompoundTag nbt = stack.getOrCreateTagElement("genes");
        Random random = world.getRandom();
        MoaGenes genes = new MoaGenes();

        for (MoaAttributes attribute : MoaAttributes.values()) {
            genes.attributeMap.addTo(attribute, race.statWeighting().configure(attribute, race, random));
        }
        genes.race = race;
        genes.affinity = race.defaultAffinity();
        genes.initialized = true;

        genes.writeToNbt(nbt);
        nbt.putBoolean("baby", baby);
        return stack;
    }

    public float getAttribute(MoaAttributes attribute) {
        return attributeMap.getOrDefault(attribute, attribute.min);
    }

    public void setAttribute(MoaAttributes attribute, float value) {
        attributeMap.put(attribute, value);
    }

    public MoaAttributes getAffinity() {
        return affinity;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public MoaAPI.Race getRace() {
        return race;
    }

    public ResourceLocation getTexture() {
        return race.texturePath();
    }

    public float getHunger() {
        return hunger;
    }

    public void setHunger(float hunger) {
        this.hunger = Math.max(Math.min(hunger, 100), 0);
    }

    public boolean isTamed() {
        return owner != null;
    }

    public void tame(UUID newOwner) {
        this.owner = newOwner;
    }

    public UUID getOwner() {
        return owner;
    }

    public static MoaGenes get(@NotNull MoaEntity moa) {
        return null;//AetherComponents.MOA_GENETICS_KEY.get(moa);
    }

    //@Override
    public void readFromNbt(CompoundTag tag) {
        initialized = tag.getBoolean("initialized");
        if(initialized) {
            race = MoaAPI.getRace(ResourceLocation.tryParse(tag.getString("raceId")));
            affinity = MoaAttributes.valueOf(tag.getString("affinity"));
            legendary = tag.getBoolean("legendary");
            hunger = tag.getFloat("hunger");
            if(tag.getBoolean("tamed")) {
                owner = tag.getUUID("owner");
            }
            Arrays.stream(MoaAttributes.values()).forEach(attribute -> attributeMap.put(attribute, tag.getFloat(attribute.name())));
        }
    }

    //@Override
    public void writeToNbt(CompoundTag tag) {
        tag.putBoolean("initialized", initialized);
        if(initialized) {
            tag.putString("raceId", race.id().toString());
            tag.putString("affinity", affinity.name());
            tag.putBoolean("legendary", legendary);
            tag.putFloat("hunger", hunger);
            tag.putBoolean("tamed", isTamed());
            if(isTamed()) {
                tag.putUUID("owner", owner);
            }
            Arrays.stream(MoaAttributes.values()).forEach(attribute -> tag.putFloat(attribute.name(), attributeMap.getFloat(attribute)));
        }
    }
}
