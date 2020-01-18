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

package io.github.marcus8448.mods.projectile.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ParticleItem extends Item {
    public ParticleItem(Properties properties, ResourceLocation id) {
        super(properties);
        this.setRegistryName(id);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn).copy();
        CompoundNBT compound = stack.getOrCreateTag();
        compound.putBoolean("enabled", !stack.getOrCreateTag().getBoolean("enabled"));

        stack.setTag(compound);

        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (stack.getOrCreateTag().getBoolean("enabled")) {
            tooltip.add(new TranslationTextComponent("tooltip.project-ile.particle.enabled").setStyle(new Style().setColor(TextFormatting.GREEN)));
        } else {
            tooltip.add(new TranslationTextComponent("tooltip.project-ile.particle.disabled").setStyle(new Style().setColor(TextFormatting.DARK_RED)));
        }
        tooltip.add(new TranslationTextComponent("tooltip.project-ile.particle.invert").setStyle(new Style().setColor(TextFormatting.GRAY)));
        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
