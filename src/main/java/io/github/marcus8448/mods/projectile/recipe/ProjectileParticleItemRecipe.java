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

package io.github.marcus8448.mods.projectile.recipe;

import io.github.marcus8448.mods.projectile.ProjectIle;
import io.github.marcus8448.mods.projectile.client.render.particle.IParticleItem;
import io.github.marcus8448.mods.projectile.item.ProjectIleItems;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class ProjectileParticleItemRecipe extends SpecialRecipe {

    private static final Ingredient PARTICLE_ITEM = Ingredient.fromItems(ProjectIleItems.PARTICLE_ITEM);

    public ProjectileParticleItemRecipe(ResourceLocation idIn) {
        super(idIn);
    }

    @Override
    public boolean matches(CraftingInventory inv, World worldIn) {
        boolean flag = false;
        boolean flag1 = false;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            if (!inv.getStackInSlot(i).isEmpty()) {
                if (PARTICLE_ITEM.test(inv.getStackInSlot(i))) {
                    if (flag) {
                        ProjectIle.LOGGER.info("2pi");
                        return false;
                    } else {
                        flag = true;
                    }
                } else {
                    if (flag1) {
                        ProjectIle.LOGGER.info("2ri");
                        return false;
                    } else {
                        flag1 = true;
                    }
                }
            }
        }
        ProjectIle.LOGGER.info("V: {}", flag && flag1);
        return flag && flag1;
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        int loc = -1;
        int locI = -1;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            if (!inv.getStackInSlot(i).isEmpty()) {
                if (PARTICLE_ITEM.test(inv.getStackInSlot(i))) {
                    loc = i;
                } else {
                    locI = i;
                }
            }
        }

        ItemStack particleItem = inv.getStackInSlot(loc).copy();
        ItemStack itemToApply = inv.getStackInSlot(locI);

        ResourceLocation id;
        String params;

        if (itemToApply.getItem() instanceof IParticleItem) {
            id = ((IParticleItem) itemToApply.getItem()).getParticle().getType().getRegistryName();
            params = ((IParticleItem) itemToApply.getItem()).getParticle().getParameters();
        } else {
            id = new ResourceLocation("item");
            params = new ItemParticleData(ParticleTypes.ITEM, itemToApply).getParameters();
        }

        ProjectIle.LOGGER.info(id + "_____" + params);

        particleItem.getOrCreateTag().putString("particle", String.valueOf(id));
        particleItem.getOrCreateTag().putString("params", params);

        return particleItem;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height > 1;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return ProjectileRecipeSerializers.PARTICLE_ITEM_RECIPE_SERIALIZER;
    }

}
