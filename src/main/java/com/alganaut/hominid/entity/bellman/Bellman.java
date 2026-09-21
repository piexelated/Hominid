package com.alganaut.hominid.entity.bellman;

import com.alganaut.hominid.entity.animation.IdleAnimationController;
import com.alganaut.hominid.entity.behavior.SunlightBurning;
import com.alganaut.hominid.entity.goal.AttackTurtleEggGoal;
import com.alganaut.hominid.registry.misc.HominidTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

public class Bellman extends Monster {
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState idleAnimationState = new AnimationState();
    private final IdleAnimationController idleAnimationController = new IdleAnimationController(250);

    int summonCooldown;

    public Bellman(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.15)
                .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SummonUndeadGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new AttackTurtleEggGoal(this, 1.0, 3, 1.14));
        this.goalSelector.addGoal(1, new FollowPlayerGoal(this, 1.0, 3.0F, 20.0F));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0, 0.0F));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
    }
    protected boolean isSunSensitive() {
        return true;
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) {
            this.idleAnimationController.tick(this, this.idleAnimationState);
        }
        if(summonCooldown >= -100){
            summonCooldown--;
        }
        super.tick();
    }

    @Override
    public void aiStep() {
        if (this.isAlive() && this.isSunSensitive() && this.isSunBurnTick()) {
            SunlightBurning.apply(this);
        }
        super.aiStep();
    }

    static EntityType<?>[] getSummonPool() {
        return BuiltInRegistries.ENTITY_TYPE
                .getTag(HominidTags.EntityType.BELLMAN_SPAWNABLE)
                .map(holders -> holders.stream()
                        .map(Holder::value)
                        .toArray(EntityType<?>[]::new))
                .orElseGet(Bellman::getDefaultSummonPool);
    }

    private static EntityType<?>[] getDefaultSummonPool() {
        return new EntityType<?>[]{
                EntityType.ZOMBIE,
                EntityType.HUSK,
                EntityType.DROWNED,
                EntityType.SKELETON,
                EntityType.STRAY,
                EntityType.BOGGED,
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("hominid", "incendiary")),
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("hominid", "mellified")),
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("hominid", "famished")),
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("hominid", "juggernaut")),
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("hominid", "fossilized")),
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("hominid", "vampire")),
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("galosphere", "preserved")),
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("minecraft", "parched")),
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("alexscaves", "boundroid")),
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("alexscaves", "caniac")),
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("alexscaves", "brainiac")),
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("netherexp", "vessel")),
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("caverns_and_chasms", "mime")),
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("spawn", "barbed")),
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("species", "quake")),
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("opposing_force", "frowzy")),
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("opposing_force", "rambler")),
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("quark", "forgotten")),
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("undead_unleashed", "wraith")),
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("undead_unleashed", "dreadknight")),
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("nomansland", "buried")),
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("nomansland", "remnant")),
                BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("windswept", "chilled"))
        };
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }
    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return level.getBlockState(pos).isAir() ? 10.0F : 0.0F;
    }

    @Override
    public boolean isInvertedHealAndHarm() {
        return true;
    }

    @Override
    public void handleEntityEvent(byte state) {
        if (state == 60){
            this.attackAnimationState.stop();
            this.attackAnimationState.startIfStopped(this.tickCount);
        }
        else super.handleEntityEvent(state);
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if(!level().isClientSide){
            this.level().broadcastEntityEvent(this, (byte) 60);
        }
        return super.doHurtTarget(entity);
    }

}
