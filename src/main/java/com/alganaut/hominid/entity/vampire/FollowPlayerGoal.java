package com.alganaut.hominid.entity.vampire;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

class FollowPlayerGoal extends Goal {
    private final Vampire entity;
    private final double followDistance = 120.0;
    private final double stopDistance = 25.0;
    private final double fleeDistance = 15.0;
    private Player targetPlayer;

    public FollowPlayerGoal(Vampire entity) {
        this.entity = entity;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        Player player = entity.level().getNearestPlayer(entity, followDistance);
        if (player == null || entity.getTarget() == player || player.isCreative() || entity.getTarget() != null) {
            return false;
        }

        this.targetPlayer = player;
        return true;
    }

    @Override
    public void start(){
        double distanceToPlayer = entity.distanceTo(targetPlayer);
        if (distanceToPlayer > stopDistance) {
            entity.getNavigation().moveTo(targetPlayer, 1.0);
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (targetPlayer == null || targetPlayer.isCreative() || entity.getTarget() != null) {
            return false;
        }

        double distanceToPlayer = entity.distanceTo(targetPlayer);
        return distanceToPlayer <= followDistance;
    }

    @Override
    public void tick() {
        double distanceToPlayer = entity.distanceTo(targetPlayer);

        if (distanceToPlayer < fleeDistance) {
            moveAwayFromPlayer();
        } else if (distanceToPlayer >= fleeDistance && distanceToPlayer <= stopDistance) {
            entity.getNavigation().stop();
            entity.getLookControl().setLookAt(targetPlayer, 30.0F, 30.0F);
        } else if (distanceToPlayer > stopDistance && distanceToPlayer <= followDistance) {
            entity.getNavigation().moveTo(targetPlayer, 1.0);
        }
    }

    private void moveAwayFromPlayer() {
        if (targetPlayer == null) return;

        double deltaX = entity.getX() - targetPlayer.getX();
        double deltaZ = entity.getZ() - targetPlayer.getZ();

        double length = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        if (length > 0) {
            deltaX = (deltaX / length) * 5.0;
            deltaZ = (deltaZ / length) * 5.0;
        }

        double targetX = entity.getX() + deltaX;
        double targetZ = entity.getZ() + deltaZ;

        entity.getNavigation().moveTo(targetX, entity.getY(), targetZ, 1.9);
    }

    @Override
    public void stop() {
        entity.getNavigation().stop();
        this.targetPlayer = null;
    }
}
