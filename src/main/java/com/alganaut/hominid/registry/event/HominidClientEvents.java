package com.alganaut.hominid.registry.event;

import com.alganaut.hominid.entity.bellman.Bellman;
import com.alganaut.hominid.entity.juggernaut.Juggernaut;
import com.alganaut.hominid.entity.vampire.Vampire;
import com.alganaut.hominid.registry.HominidEntityCreator;
import com.alganaut.hominid.registry.item.HominidItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

public class HominidClientEvents {
    private HominidClientEvents() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, HominidClientEvents::onEntityJoinWorld);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, HominidClientEvents::onEntityDie);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, HominidClientEvents::onLivingDrops);
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(FinalizeSpawnEvent event) {
        if (event.getEntity().getClass() == Zombie.class && Math.random() < 0.1) {
            Zombie wolf = (Zombie) event.getEntity();
            event.getEntity().discard();

            Juggernaut customMob = new Juggernaut(HominidEntityCreator.JUGGERNAUT.get(), wolf.level());
            customMob.setPos(wolf.position().x, wolf.position().y, wolf.position().z);
            wolf.level().addFreshEntity(customMob);

        }
        if (event.getEntity().getClass() == Zombie.class && Math.random() < 0.1) {
            Zombie wolf = (Zombie) event.getEntity();
            event.getEntity().discard();

            Bellman customMob = new Bellman(HominidEntityCreator.BELLMAN.get(), wolf.level());
            customMob.setPos(wolf.position().x, wolf.position().y, wolf.position().z);
            wolf.level().addFreshEntity(customMob);

        }
        if (event.getEntity() != null && event.getEntity() instanceof AbstractIllager illager) {
            illager.targetSelector.addGoal(3, new AvoidEntityGoal<>(illager, Vampire.class, 6.0F, 1.0D, 1.2D));
        }
    }

    private static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }

        if (event.getEntity().getPersistentData().getBoolean("BellmanSummon")) {
            event.getDrops().clear();
        }
    }

    @SubscribeEvent
    public static void onEntityDie(LivingDeathEvent event){
        if (event.getEntity().level().isClientSide) {
            return;
        }

        LivingEntity deadEntity = event.getEntity();

        if (!(deadEntity instanceof Creeper creeper)) {
            return;
        }

        LivingEntity killer = deadEntity.getLastAttacker();

        if (killer instanceof Vampire) {

            Level level = deadEntity.level();

            ItemStack drop = new ItemStack(HominidItems.MUSIC_DISC_HEMATOMA.get());

            ItemEntity itemEntity = new ItemEntity(
                    level,
                    creeper.getX(),
                    creeper.getY(),
                    creeper.getZ(),
                    drop
            );

            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
    }

}
