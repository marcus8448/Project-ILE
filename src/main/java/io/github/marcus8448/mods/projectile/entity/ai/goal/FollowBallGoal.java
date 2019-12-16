/*
 *     Copyright (C) 2019 marcus8448
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.marcus8448.mods.projectile.entity.ai.goal;

import io.github.marcus8448.mods.projectile.entity.projectile.BouncyBallEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

public class FollowBallGoal extends Goal {

    public final TameableEntity tameable;
    private BouncyBallEntity target;
    private ItemStack ballItem = ItemStack.EMPTY;
    boolean flag = false;
    private Entity targetEntity;
    private Vec3d targetEntityPos;

    public FollowBallGoal(TameableEntity tameableEntity) {
        this.tameable = tameableEntity;
        //this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        tameableEntity.goalSelector.disableFlag(Flag.MOVE);
        tameableEntity.goalSelector.disableFlag(Flag.LOOK);
    }

    @Override
    public boolean shouldExecute() {
        if (tameable.isTamed()) {
            for (int i : BouncyBallEntity.bouncyBalls) {
                Entity e = tameable.getEntityWorld().getEntityByID(i);
                if (e instanceof BouncyBallEntity) {
                    return  ((e.getPositionVec().squareDistanceTo(tameable.getPositionVec()) < 30 * 30 * 30));
                }
            }
        }
        return false;
    }

    public ItemStack getStack() {
        return ballItem;
    }

    @Override
    public void startExecuting() {
        flag = false;
        this.tameable.getNavigator().clearPath();
        if (tameable.isTamed() && tameable.getOwner() != null && !tameable.isSitting()) {
            for (int i : BouncyBallEntity.bouncyBalls) {
                Entity e = tameable.getEntityWorld().getEntityByID(i);
                if (e instanceof BouncyBallEntity) {
                    if ((e.getPositionVec().squareDistanceTo(tameable.getPositionVec()) < 30 * 30 * 30)) {
                        target = (BouncyBallEntity) e;
                        return;
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (flag) {
            flag = false;
            resetTask();
            return false;
        }
        if (ballItem != ItemStack.EMPTY && tameable.isTamed() && !tameable.isSitting() && tameable.getOwner() != null) {
            return true;
        }

        if (tameable.isTamed() && tameable.getOwner() != null && !tameable.isSitting()) {
            if (target != null) {
                if (target.isAlive()) {
                    return (target.getPositionVec().squareDistanceTo(tameable.getPositionVec()) < 30 * 30 * 30);
                }
            }

        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (tameable.getOwner() != null && !tameable.isSitting()) {
            if (ballItem != ItemStack.EMPTY) {
                if (tameable.getOwner().getPositionVec().squareDistanceTo(tameable.getPositionVec()) < 2.25F) {
                    ItemEntity item = new ItemEntity(this.tameable.world, this.tameable.posX, this.tameable.posY, this.tameable.posZ, ballItem);
                    item.setOwnerId(tameable.getOwnerId());
                    this.tameable.world.addEntity(item);
                    this.ballItem = ItemStack.EMPTY;
                    flag = true;
                } else {
                    targetEntity = tameable.getOwner();
                }
            } else {
                if (target.getPositionVec().squareDistanceTo(tameable.getPositionVec()) < 1.15F) {
                    ballItem = target.getItem();
                    target.remove();
                    target = null;
                    if (tameable.getOwner() != null) {
                        targetEntity = tameable.getOwner();
                    }
                } else {
                    targetEntity = target;
                }
            }

        if (targetEntity != null) {
            if (targetEntityPos != targetEntity.getPositionVec()) {
                targetEntityPos = targetEntity.getPositionVec();
                this.tameable.getNavigator().tryMoveToXYZ(targetEntityPos.x, targetEntityPos.y, targetEntityPos.z, 1.2F);
            }
        }
        }
    }

    public void resetTask() {
        target = null;
        flag = false;
        this.tameable.getNavigator().clearPath();
        if (ballItem != ItemStack.EMPTY) {
            ItemEntity item = new ItemEntity(this.tameable.world, this.tameable.posX, this.tameable.posY, this.tameable.posZ, ballItem);
            this.tameable.world.addEntity(item);
        }
        tameable.goalSelector.enableFlag(Flag.MOVE);
        tameable.goalSelector.enableFlag(Flag.LOOK);

    }
}
