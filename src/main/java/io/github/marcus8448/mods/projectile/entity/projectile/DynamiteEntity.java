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

package io.github.marcus8448.mods.projectile.entity.projectile;

import io.github.marcus8448.mods.projectile.ProjectIle;
import io.github.marcus8448.mods.projectile.item.ProjectIleItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class DynamiteEntity extends ProjectileItemEntity {
    public DynamiteEntity(EntityType<? extends DynamiteEntity> p_i50159_1_, World p_i50159_2_) {
        super(p_i50159_1_, p_i50159_2_);
    }

    public DynamiteEntity(World world, LivingEntity ent) {
        super(ProjectIle.DYNAMITE_ENTITY_TYPE, ent, world);
    }

    public DynamiteEntity(World world, double x, double y, double z) {
        super(ProjectIle.DYNAMITE_ENTITY_TYPE, x, y, z, world);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ProjectIleItems.DYNAMITE);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        this.world.createExplosion(this, null, this.getPositionVec().x, this.getPositionVec().y, this.getPositionVec().z, 4.0F, false, Explosion.Mode.DESTROY);

        if (!this.world.isRemote) {
            this.world.setEntityState(this, (byte)3);
            this.remove();
        }
    }

    @Override
    protected Item func_213885_i() {
        return ProjectIleItems.DYNAMITE;
    }
}
