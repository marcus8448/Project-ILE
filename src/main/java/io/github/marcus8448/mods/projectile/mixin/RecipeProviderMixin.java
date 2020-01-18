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

import io.github.marcus8448.mods.projectile.ProjectIle;
import io.github.marcus8448.mods.projectile.recipe.ProjectileRecipeSerializers;
import net.minecraft.data.CustomRecipeBuilder;
import net.minecraft.data.IFinishedRecipe;
import net.minecraftforge.common.data.ForgeRecipeProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ForgeRecipeProvider.class)
public abstract class RecipeProviderMixin {

    @Inject(at = @At("RETURN"), method = "registerRecipes")
    private void regRecipes(Consumer<IFinishedRecipe> consumer, CallbackInfo ci) {
        CustomRecipeBuilder.func_218656_a(ProjectileRecipeSerializers.PARTICLE_ITEM_RECIPE_SERIALIZER).build(consumer, ProjectIle.MODID + ":particle_item_recipe");
        ProjectIle.LOGGER.info("Added custom recipes!");
    }
}
