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
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;

public class DyedSnowballEntity extends ProjectileItemEntity {

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

    public static final DataParameter<DyeColor> DYE_COLOR = EntityDataManager.createKey(DyedSnowballEntity.class, DYE_SERIALIZER);

    static {
        DataSerializers.registerSerializer(DYE_SERIALIZER);
    }

    public DyedSnowballEntity(EntityType<? extends DyedSnowballEntity> type, World world) {
        super(type, world);
    }

    public DyedSnowballEntity(World world, LivingEntity entity, DyeColor color) {
        super(ProjectIle.DYED_SNOWBALL_ENTITY_TYPE, entity, world);
        this.dataManager.set(DYE_COLOR, color);
    }

    public DyedSnowballEntity(World world, double x, double y, double z, DyeColor color) {
        super(ProjectIle.DYED_SNOWBALL_ENTITY_TYPE, x, y, z, world);
        this.dataManager.set(DYE_COLOR, color);
    }

    @Override
    protected Item func_213885_i() {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation("project-ile", this.dataManager.get(DYE_COLOR).getTranslationKey() + "_snowball"));
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("project-ile", this.dataManager.get(DYE_COLOR).getTranslationKey() + "_snowball")));
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.getDataManager().register(DYE_COLOR, DyeColor.WHITE);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id == 3) {
            IParticleData iparticledata = new ItemParticleData(ParticleTypes.ITEM, new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("project-ile", this.dataManager.get(DYE_COLOR).getName() + "_snowball"))));

            for(int i = 0; i < 8; ++i) {
                this.world.addParticle(iparticledata, this.getPositionVec().x, this.getPositionVec().y, this.getPositionVec().z, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    protected void onImpact(RayTraceResult rayTraceResult) {
        if (rayTraceResult.getType() == RayTraceResult.Type.ENTITY) {
            Entity entity = ((EntityRayTraceResult) rayTraceResult).getEntity();
            entity.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), entity instanceof BlazeEntity ? 3F : 0F);

            if (entity instanceof LivingEntity) {
                for (EquipmentSlotType type : EquipmentSlotType.values()) {
                    if (type.getSlotType() == EquipmentSlotType.Group.ARMOR) {
                        if (!((LivingEntity) entity).getItemStackFromSlot(type).isEmpty() && ((LivingEntity) entity).getItemStackFromSlot(type).getItem() instanceof IDyeableArmorItem) {
                            entity.setItemStackToSlot(type, IDyeableArmorItem.func_219975_a(((LivingEntity) entity).getItemStackFromSlot(type), Collections.singletonList(DyeItem.getItem(this.dataManager.get(DYE_COLOR)))));
                        }
                    }
                }
            }

            if (entity instanceof SheepEntity) {
                ((SheepEntity) entity).setFleeceColor(this.dataManager.get(DYE_COLOR));
            }
        } else if(rayTraceResult.getType() == RayTraceResult.Type.BLOCK) {
            if (this.world.getBlockState(new BlockPos(rayTraceResult.getHitVec().x, rayTraceResult.getHitVec().y, rayTraceResult.getHitVec().z)).getBlock() == Blocks.WHITE_WOOL) {
                this.world.setBlockState(new BlockPos(rayTraceResult.getHitVec().x, rayTraceResult.getHitVec().y, rayTraceResult.getHitVec().z), ForgeRegistries.BLOCKS.getValue(new ResourceLocation(this.dataManager.get(DYE_COLOR).getName() + "_wool")).getDefaultState());
            }
        }

        if (!this.world.isRemote) {
            this.world.setEntityState(this, (byte) 3);
            this.remove();
        }

    }

    @Override
    public void writeAdditional(CompoundNBT tag) {
        super.writeAdditional(tag);
        tag.putByte("DyeColor", (byte) this.getDataManager().get(DYE_COLOR).getId());
    }

    @Override
    public void readAdditional(CompoundNBT tag) {
        super.readAdditional(tag);
        if (tag.contains("DyeColor")) {
            this.getDataManager().set(DYE_COLOR, DyeColor.byId(tag.getByte("DyeColor")));
        }
    }
}
