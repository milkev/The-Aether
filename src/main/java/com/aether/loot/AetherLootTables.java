package com.aether.loot;

import com.aether.Aether;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public class AetherLootTables {
    private static final Set<ResourceLocation> LOOT_TABLES = Sets.newHashSet();
    private static final Set<ResourceLocation> LOOT_TABLES_READ_ONLY;
    public static final ResourceLocation GOLDEN_OAK_STRIPPING;

    public AetherLootTables() {
    }

    private static ResourceLocation register(String id) {
        return registerLootTable(Aether.locate(id));
    }

    private static ResourceLocation registerLootTable(ResourceLocation id) {
        if (LOOT_TABLES.add(id)) {
            return id;
        } else {
            throw new IllegalArgumentException(id + " is already a registered built-in loot table");
        }
    }

    public static Set<ResourceLocation> getAll() {
        return LOOT_TABLES_READ_ONLY;
    }

    static {
        LOOT_TABLES_READ_ONLY = Collections.unmodifiableSet(LOOT_TABLES);
        GOLDEN_OAK_STRIPPING = register("gameplay/golden_oak_log_strip");
    }
}
