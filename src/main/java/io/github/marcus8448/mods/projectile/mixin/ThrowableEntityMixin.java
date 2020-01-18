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

package io.github.marcus8448.mods.projectile.mixin;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.marcus8448.mods.projectile.ProjectIle;
import io.github.marcus8448.mods.projectile.item.ParticleItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ThrowableEntity.class)
public abstract class ThrowableEntityMixin implements IEntityAdditionalSpawnData {
    @Shadow @Nullable public abstract LivingEntity getThrower();

    private IParticleData particle = null;

    @Inject(at = @At("RETURN"), method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/world/World;)V")
    private void initProjectIle(EntityType<? extends ThrowableEntity> type, LivingEntity livingEntityIn, World worldIn, CallbackInfo ci) {
        if (this.getThrower() instanceof PlayerEntity) {
            for (int i = 0; i < ((PlayerEntity) this.getThrower()).inventory.getSizeInventory(); i++) {
                if (((PlayerEntity) this.getThrower()).inventory.getStackInSlot(i).getItem() instanceof ParticleItem) {
                    if (((PlayerEntity) this.getThrower()).inventory.getStackInSlot(i).getOrCreateTag().getBoolean("active")) {
                        ItemStack stack = ((PlayerEntity) this.getThrower()).inventory.getStackInSlot(i).copy();
                        stack.damageItem(1, this.getThrower(), livingEntity -> {});
                        ((PlayerEntity) this.getThrower()).inventory.setInventorySlotContents(i, stack);
                        ResourceLocation id = new ResourceLocation(((PlayerEntity) this.getThrower()).inventory.getStackInSlot(i).getOrCreateTag().getString("particle"));
                        String params = ((PlayerEntity) this.getThrower()).inventory.getStackInSlot(i).getOrCreateTag().getString("particle");
                        @SuppressWarnings("rawtypes") ParticleType t = ForgeRegistries.PARTICLE_TYPES.getValue(id);
                        if (t != null) {
                            try {
                                //noinspection unchecked
                                particle = t.getDeserializer().deserialize(t, new StringReader(params));
                            } catch (CommandSyntaxException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    }
                }
            }
        }
    }


    @Inject(at = @At("RETURN"), method = "tick")
    private void addCosmeticParticles(CallbackInfo ci) {
        if (particle != null) {
            ((ThrowableEntity) (Object) this).world.addParticle(particle, true, ((ThrowableEntity) (Object) this).getPositionVec().x, ((ThrowableEntity) (Object) this).getPositionVec().y, ((ThrowableEntity) (Object) this).getPositionVec().z, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        if (particle != null && particle.getType().getRegistryName() != null) {
            buffer.writeResourceLocation(particle.getType().getRegistryName());
        } else {
            buffer.writeResourceLocation(new ResourceLocation(ProjectIle.MODID, "null"));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        ResourceLocation id = additionalData.readResourceLocation();
        if (!id.getPath().equals("null") || !id.getNamespace().equals(ProjectIle.MODID)) {
            ParticleType type = ForgeRegistries.PARTICLE_TYPES.getValue(id);
            if (type != null) {
                this.particle = type.getDeserializer().read(type, additionalData);
            } else {
                ProjectIle.LOGGER.error("An invalid particle type: {} was sent by the server!", id);
            }
        }
    }

    @SuppressWarnings("unused")
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket((Entity) (Object)this);
    }

}
