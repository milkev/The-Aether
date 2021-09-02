package net.id.aether.world.weather.controller;

import net.minecraft.util.Identifier;

/**
 * Mimics vanilla biomes that have snow in them.
 */
public final class SnowController extends AbstractVanillaController{
    public SnowController(Identifier id){
        super(id, true);
    }
}
