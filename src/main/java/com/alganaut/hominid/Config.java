package com.alganaut.hominid;

import java.util.*;
import java.util.stream.Collectors;

import com.alganaut.hominid.entity.bellman.Bellman;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = Hominid.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<List<? extends String>> BELLMAN_SUMMONS = BUILDER
            .comment("Entities the bellman can summon.")
            .defineListAllowEmpty("Bellman Summons", Bellman.SUPPORTED_SUMMONS, Config::validateSummon);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean validateSummon(final Object obj) {
        return obj instanceof String entity && ResourceLocation.tryParse(entity) != null;
    }

    public static final EntityType<?>[] getBellmanSummons = Bellman.SUPPORTED_SUMMONS.stream()
            .map(ResourceLocation::tryParse)
            .filter(Objects::nonNull)
            .map(BuiltInRegistries.ENTITY_TYPE::getOptional)
            .flatMap(Optional::stream)
            .distinct()
            .toArray(EntityType<?>[]::new);
}
