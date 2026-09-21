package com.alganaut.hominid.entity.vampire;

import com.alganaut.hominid.entity.animation.IdleAnimationController;
import com.alganaut.hominid.registry.sound.HominidSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

public class Vampire extends Monster {
    private final IdleAnimationController idleAnimationController = new IdleAnimationController(120);
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState dieAnimationState = new AnimationState();

    public Vampire(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.FOLLOW_RANGE, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 12.0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return HominidSounds.VAMPIRE_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return HominidSounds.VAMPIRE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return HominidSounds.VAMPIRE_DEATH.get();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0, 0.0F));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, false));
        this.targetSelector.addGoal(2, new TargetDamagedEntityGoal(this));
        this.targetSelector.addGoal(2, new BurnInSunGoal(this));
        this.targetSelector.addGoal(2, new FollowPlayerGoal(this) {
            @Override
            public boolean canUse() {
                return !Vampire.this.isAggressive() && super.canUse();
            }
        });
        this.goalSelector.addGoal(9, new AvoidEntityGoal<>(this, Player.class, 6.0F, 1.0, 1.0) {
            @Override
            public boolean canUse() {
                return !Vampire.this.isAggressive() && super.canUse();
            }
        });
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) {
            this.idleAnimationController.tick(this, this.idleAnimationState);
        }
        super.tick();
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (!level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte) 65);
        }
        return super.doHurtTarget(entity);
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return level.getBlockState(pos).isAir() ? 10.0F : 0.0F;
    }

    @Override
    public boolean isInvertedHealAndHarm() {
        return true;
    }

    void dieAndPerish() {
        if (!this.isAlive()) return;

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0, this.getZ(), 20, 0.5, 0.5, 0.5, 0.1);
        }
        this.scream();
    }

    @Override
    public void handleEntityEvent(byte state) {
        if (state == 65) {
            this.attackAnimationState.stop();
            this.attackAnimationState.startIfStopped(this.tickCount);
        }
        if (state == 85) {
            this.dieAnimationState.stop();
            this.dieAnimationState.startIfStopped(this.tickCount);
        } else super.handleEntityEvent(state);
    }

    public void scream() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), HominidSounds.VAMPIRE_SCREAM.get(), this.getSoundSource(), 0.5F, 1.0F);
    }
}