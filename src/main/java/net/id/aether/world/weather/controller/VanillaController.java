package net.id.aether.world.weather.controller;

import net.minecraft.util.Identifier;

/**
 * Mimics vanilla weather in warm biomes.
 */
public final class VanillaController extends AbstractVanillaController{
    public VanillaController(Identifier id){
        super(id, false);
    }
}
