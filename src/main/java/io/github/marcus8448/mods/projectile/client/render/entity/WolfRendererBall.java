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

package io.github.marcus8448.mods.projectile.client.render.entity;

import io.github.marcus8448.mods.projectile.ProjectIle;
import io.github.marcus8448.mods.projectile.entity.ai.goal.FollowBallGoal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.item.ItemStack;

public class WolfRendererBall extends WolfRenderer {
    public WolfRendererBall(EntityRendererManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void doRender(WolfEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        ItemStack stack = ItemStack.EMPTY;
        ProjectIle.LOGGER.info(entity.goalSelector.goals.size());
        for (PrioritizedGoal goal : entity.goalSelector.goals) {
            if (goal.getGoal() instanceof FollowBallGoal) {
                stack = ((FollowBallGoal) goal.getGoal()).getStack(); //FIXME - Server -> Client sync
                ProjectIle.LOGGER.info(stack);
            }
        }

        if (stack != ItemStack.EMPTY) {
            ItemRenderer renderer = new ItemRenderer(renderManager, Minecraft.getInstance().getItemRenderer()) {
                @Override
                public boolean shouldBob() {
                    return false;
                }
            };
            renderer.doRender(new ItemEntity(entity.world, entity.posX, entity.posY + 1, entity.posZ, stack), entity.posX, entity.posY + 1, entity.posZ, entityYaw, partialTicks);
        }
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}
