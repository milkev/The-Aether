package com.aether.commands;

import com.aether.duck.ServerWorldDuck;
import com.aether.world.dimension.AetherDimension;
import com.aether.world.weather.AetherWeatherType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Objects;
import java.util.OptionalInt;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import static com.aether.Aether.MOD_ID;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * The command to set weather in the Aether.<br>
 * <br>
 * This "overlaps" with vanilla on purpose, it is unlikely to conflict because of the required "aether" keyword.<br>
 * <br>
 * Usage:<br>
 * /weather aether set (biome) (weather) [time]<br>
 * /weather aether get (biome) (weather)
 */
final class WeatherCommand{
    /**
     * Suggests all the Aether biomes.
     */
    private static final SuggestionProvider<ServerCommandSource> BIOME_SUGGESTER = (context, builder)->{
        var registry = context.getSource().getServer().getRegistryManager().get(Registry.BIOME_KEY);
        registry.stream()
            .map(registry::getId)
            .filter(Objects::nonNull)
            .filter((biome)->biome.getNamespace().equals(MOD_ID))
            .map(Identifier::toString)
            .forEach(builder::suggest);
        return builder.buildFuture();
    };
    
    /**
     * Suggests all values of the weather.
     */
    private static final SuggestionProvider<ServerCommandSource> WEATHER_SUGGESTER = (context, builder)->{
        for(var type : AetherWeatherType.values()){
            builder.suggest(type.getName());
        }
        return builder.buildFuture();
    };
    
    static void register(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(
            literal("weather").requires((source)->source.hasPermissionLevel(2))
                .then(literal("aether")
                    .then(argument("biome", IdentifierArgumentType.identifier()).suggests(BIOME_SUGGESTER)
                        .then(literal("set")
                            .then(argument("type", StringArgumentType.word()).suggests(WEATHER_SUGGESTER)
                                .executes((context)->executeSet(context, false))
                                .then(argument("duration", IntegerArgumentType.integer(0, 1000000))
                                    .executes((context)->executeSet(context, true))
                                )
                            )
                        )
                        .then(literal("get")
                            .then(argument("type", StringArgumentType.word()).suggests(WEATHER_SUGGESTER)
                                .executes(WeatherCommand::executeGet)
                            )
                        )
                    )
                )
        );
    }
    
    /**
     * Gets an instance of the biome from the issued command.
     *
     * @param context The command context
     * @return The biome or null if it failed
     */
    private static Biome getBiome(CommandContext<ServerCommandSource> context){
        var id = IdentifierArgumentType.getIdentifier(context, "biome");
        if(!id.getNamespace().equals(MOD_ID)){
            context.getSource().sendError(new TranslatableText("commands.aether.weather.invalid_biome", id.toString()));
            return null;
        }
        var registry = context.getSource().getServer().getRegistryManager().get(Registry.BIOME_KEY);
        var biome = registry.get(id);
        if(biome == null){
            context.getSource().sendError(new TranslatableText("commands.aether.weather.invalid_biome", id.toString()));
            return null;
        }
        return biome;
    }
    
    /**
     * Gets the type of weather from the issued command.
     *
     * @param context The command context
     * @return The weather type or null if it failed
     */
    private static AetherWeatherType getWeatherType(CommandContext<ServerCommandSource> context){
        var string = StringArgumentType.getString(context, "type");
        var type = AetherWeatherType.getValue(string);
        if(type == null){
            context.getSource().sendError(new TranslatableText("commands.aether.weather.invalid_type", string));
        }
        return type;
    }
    
    /**
     * Gets the instance of the Aether Server world.
     *
     * @param context The command context
     * @return A bi-type world instance
     */
    @SuppressWarnings("unchecked")
    private static <T extends ServerWorld & ServerWorldDuck> T getAetherWorld(CommandContext<ServerCommandSource> context){
        return (T)context.getSource().getServer().getWorld(AetherDimension.AETHER_WORLD_KEY);
    }
    
    /**
     * Handles the `get` command.
     *
     * @param context The command context
     * @return The command status
     */
    private static int executeGet(CommandContext<ServerCommandSource> context){
        try{
            var world = getAetherWorld(context);
            var controller = world.the_aether$getWeatherController();
    
            var type = getWeatherType(context);
            if(type == null){
                // Status is already sent to the user
                return 0;
            }
    
            var biome = getBiome(context);
            if(biome == null){
                // Status is already sent to the user
                return 0;
            }
    
            var source = context.getSource();
    
            // Get the weather, empty if the biome doesn't support that weather type
            OptionalInt time = controller.getWeatherDuration(biome, type);
            if(time.isPresent()){
                // We know this is valid because the getWeather command didn't fail
                @SuppressWarnings("OptionalGetWithoutIsPresent")
                var biomeController = controller.getWeatherController(biome).get();
                var state = biomeController.hasRaw(type);
                // Return the amount of time the current state lasts
                source.sendFeedback(new TranslatableText(
                    state ? "commands.aether.weather.get.active" : "commands.aether.weather.get.inactive",
                    type.getName(),
                    time.getAsInt()
                ), false);
                return 1;
            }else{
                // The biome doesn't support that weather
                source.sendFeedback(new TranslatableText("commands.aether.weather.get.failed"), false);
                return 0;
            }
        }catch(Throwable t){
            t.printStackTrace();
            throw t;
        }
    }
    
    /**
     * Handles the set command.
     *
     * @param context The context of the command
     * @param durationSpecified Whether the time was specified
     * @return The command result
     */
    private static int executeSet(CommandContext<ServerCommandSource> context, boolean durationSpecified){
        try{
            var world = getAetherWorld(context);
            var controller = world.the_aether$getWeatherController();
    
            var type = getWeatherType(context);
            if(type == null){
                // Status is already sent to the user
                return 0;
            }
    
            var biome = getBiome(context);
            if(biome == null){
                // Status is already sent to the user
                return 0;
            }
    
            int duration;
            if(durationSpecified){
                duration = IntegerArgumentType.getInteger(context, "duration");
            }else{
                // Same as vanilla
                duration = 6000;
            }
            
            var source = context.getSource();
            if(controller.setWeather(biome, type, duration)){
                source.sendFeedback(new TranslatableText("commands.aether.weather.set"), false);
                return 1;
            }else{
                source.sendFeedback(new TranslatableText("commands.aether.weather.set.failed"), false);
                return 0;
            }
        }catch(Throwable t){
            t.printStackTrace();
            throw t;
        }
    }
}
