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
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;

public class BouncyBallEntity extends ProjectileItemEntity {

    int lifeTime = 0;

    public static ArrayList<Integer> bouncyBalls = new ArrayList<>();

    private static final IDataSerializer<DyeColor> DYE_SERIALIZER = new IDataSerializer<DyeColor>() {
        public void write(PacketBuffer buf, DyeColor value) {
            buf.writeEnumValue(value);
        }

        public DyeColor read(PacketBuffer buf) {
            return buf.readEnumValue(DyeColor.class);
        }

        public DataParameter<DyeColor> createKey(int id) {
            return new DataParameter<>(id, this);
        }

        public DyeColor copyValue(DyeColor value) {
            return value;
        }
    };


    public static final DataParameter<DyeColor> DYE_COLOR = EntityDataManager.createKey(BouncyBallEntity.class, DYE_SERIALIZER);

    static {
        DataSerializers.registerSerializer(DYE_SERIALIZER);
    }

    public BouncyBallEntity(EntityType<? extends BouncyBallEntity> p_i50159_1_, World p_i50159_2_) {
        super(p_i50159_1_, p_i50159_2_);
        bouncyBalls.add(this.getEntityId());
    }

    public BouncyBallEntity(World world, LivingEntity livingEntity, DyeColor color) {
        super(ProjectIle.BOUNCY_BALL_ENTITY_TYPE, livingEntity, world);
        this.dataManager.set(DYE_COLOR, color);
        bouncyBalls.add(this.getEntityId());
    }

    public BouncyBallEntity(World world, double x, double y, double z, DyeColor color) {
        super(ProjectIle.BOUNCY_BALL_ENTITY_TYPE, x, y, z, world);
        this.dataManager.set(DYE_COLOR, color);
        bouncyBalls.add(this.getEntityId());
    }

    @Override
    protected Item getDefaultItem() {
        return getItem().getItem();
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(ProjectIle.MODID, this.dataManager.get(DYE_COLOR).getTranslationKey() + "_bouncy_ball")));
    }

    @Override
    public void remove() {
        super.remove();
        //noinspection SuspiciousMethodCalls
        bouncyBalls.remove((Object)this.getEntityId());
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (lifeTime >= 6000) {
            if (!this.world.isRemote) {
                ItemEntity itemEntity = new ItemEntity(this.world, this.getPositionVec().x, this.getPositionVec().y, this.getPositionVec().z, new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(ProjectIle.MODID, this.dataManager.get(DYE_COLOR).getTranslationKey() + "_bouncy_ball"))));
                this.world.addEntity(itemEntity);
                this.remove();
            }
            return;
        }

        for(int i = 0; i < 8; ++i) {
            this.world.addParticle(new ItemParticleData(ParticleTypes.ITEM, new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(ProjectIle.MODID, this.dataManager.get(DYE_COLOR).getTranslationKey() + "_bouncy_ball")))), this.getPositionVec().x, this.getPositionVec().y, this.getPositionVec().z, 0.0D, 0.0D, 0.0D);
        }

        if (result.getType() == RayTraceResult.Type.BLOCK) {
            if (!world.getBlockState(((BlockRayTraceResult) result).getPos()).allowsMovement(world, ((BlockRayTraceResult) result).getPos(), PathType.LAND)) {
                bounce(((BlockRayTraceResult) result));
            }
        }
    }

    @Override
    public ActionResultType applyPlayerInteraction(PlayerEntity player, Vec3d vec, Hand hand) {
        ItemEntity itemEntity = new ItemEntity(this.world, this.getPositionVec().x, this.getPositionVec().y, this.getPositionVec().z, new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(ProjectIle.MODID, this.dataManager.get(DYE_COLOR).getTranslationKey() + "_bouncy_ball"))));
        itemEntity.setOwnerId(player.getUniqueID());
        this.world.addEntity(itemEntity);
        this.remove();
        return ActionResultType.SUCCESS;
    }

    @Override
    public void tick() {
        super.tick();
        lifeTime++;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.getDataManager().register(DYE_COLOR, DyeColor.WHITE);
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putInt("LifeTime", lifeTime);
        compound.putByte("Color", (byte) this.dataManager.get(DYE_COLOR).getId());
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        lifeTime = compound.getByte("LifeTime");
        if (compound.contains("Color")) {
            this.dataManager.set(DYE_COLOR, DyeColor.byId(compound.getByte("Color")));
        }
    }
    
    
    private void bounce(BlockRayTraceResult result) {
        if (result.getFace().getAxis() == Direction.Axis.Y) {
            this.setMotion(this.getMotion().x * 0.7F, -this.getMotion().y * 0.7F, this.getMotion().z * 0.7F);
        } else if (result.getFace().getAxis() == Direction.Axis.X) {
            this.setMotion(-this.getMotion().x * 0.7F, this.getMotion().y * 0.7F, this.getMotion().z * 0.7F);
        } else if (result.getFace().getAxis() == Direction.Axis.Z) {
            this.setMotion(this.getMotion().x * 0.7F, this.getMotion().y * 0.7F, -this.getMotion().z * 0.7F);
        }
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
