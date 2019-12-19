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

package io.github.marcus8448.mods.projectile.item;

import io.github.marcus8448.mods.projectile.entity.projectile.BouncyBallEntity;
import io.github.marcus8448.mods.projectile.entity.projectile.DyedSnowballEntity;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.ProjectileDispenseBehavior;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SnowballItem;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class BouncyBallItem extends Item {
    private final DyeColor color;

    public BouncyBallItem(Properties properties, DyeColor color, ResourceLocation id) {
        super(properties);
        this.color = color;
        this.setRegistryName(id);

        DispenserBlock.registerDispenseBehavior(this, new ProjectileDispenseBehavior() {
            protected IProjectile getProjectileEntity(World worldIn, IPosition position, ItemStack stackIn) {
                return Util.make(new BouncyBallEntity(worldIn, position.getX(), position.getY(), position.getZ(), color), (item) -> item.func_213884_b(stackIn));
            }
        });
    }

    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.abilities.isCreativeMode) {
            stack.shrink(1);
        }

        world.playSound(null, player.getPositionVec().x, player.getPositionVec().y, player.getPositionVec().z, SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
        if (!world.isRemote) {
            BouncyBallEntity bouncyBallEntity = new BouncyBallEntity(world, player, color);
            bouncyBallEntity.func_213884_b(stack);
            bouncyBallEntity.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.0F, 0.7F);
            world.addEntity(bouncyBallEntity);
        }

        player.addStat(Stats.ITEM_USED.get(this));
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }
}
