/*
 * Project-ILE
 * Copyright (C) 2019 marcus8448
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.marcus8448.mods.projectile.entity.projectile;

import io.github.marcus8448.mods.projectile.ProjectIle;
import io.github.marcus8448.mods.projectile.item.ProjectIleItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class BouncyDynamiteEntity extends ProjectileItemEntity {

    byte bounces = 0;

    public BouncyDynamiteEntity(EntityType<? extends BouncyDynamiteEntity> p_i50159_1_, World p_i50159_2_) {
        super(p_i50159_1_, p_i50159_2_);
    }

    public BouncyDynamiteEntity(World world, LivingEntity livingEntity) {
        super(ProjectIle.BOUNCY_DYNAMITE_ENTITY_TYPE, livingEntity, world);
    }

    public BouncyDynamiteEntity(World world, double x, double y, double z) {
        super(ProjectIle.BOUNCY_DYNAMITE_ENTITY_TYPE, x, y, z, world);
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (bounces >= 5) {
            if (!this.world.isRemote) {
                this.world.setEntityState(this, (byte)3);
                this.world.createExplosion(this, null, this.getPositionVec().x, this.getPositionVec().y, this.getPositionVec().z, 4.0F, false, Explosion.Mode.DESTROY);
                this.remove();
            }
            return;
        }

        if (result.getType() == RayTraceResult.Type.BLOCK) {
            bounces++;
            bounce(((BlockRayTraceResult) result));
        }
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ProjectIleItems.BOUNCY_DYNAMITE);
    }

    @Override
    protected Item getDefaultItem() {
        return ProjectIleItems.BOUNCY_DYNAMITE;
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putByte("Bounces", bounces);
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        bounces = compound.getByte("Bounces");
    }
    
    
    private void bounce(BlockRayTraceResult result) {
        if (result.getFace().getAxis() == Direction.Axis.Y) {
            this.setMotion(this.getMotion().x * 0.8F, -this.getMotion().y * 0.8F, this.getMotion().z * 0.8F);
        } else if (result.getFace().getAxis() == Direction.Axis.X) {
            this.setMotion(-this.getMotion().x * 0.8F, this.getMotion().y * 0.8F, this.getMotion().z * 0.8F);
        } else if (result.getFace().getAxis() == Direction.Axis.Z) {
            this.setMotion(this.getMotion().x * 0.8F, this.getMotion().y * 0.8F, -this.getMotion().z * 0.8F);
        }
    }

}
