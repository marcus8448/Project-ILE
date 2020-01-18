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
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class ProjectileRecipeSerializers {
    public static final SpecialRecipeSerializer<ProjectileParticleItemRecipe> PARTICLE_ITEM_RECIPE_SERIALIZER = new SpecialRecipeSerializer<>(ProjectileParticleItemRecipe::new);

    static {
        ForgeRegistries.RECIPE_SERIALIZERS.register(PARTICLE_ITEM_RECIPE_SERIALIZER.setRegistryName(new ResourceLocation(ProjectIle.MODID, "particle_item_recipe")));
    }

    public static void init() {}
}
