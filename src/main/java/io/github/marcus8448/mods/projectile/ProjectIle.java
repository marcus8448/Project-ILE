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

package io.github.marcus8448.mods.projectile;

import io.github.marcus8448.mods.projectile.client.render.entity.WolfRendererBall;
import io.github.marcus8448.mods.projectile.entity.ai.goal.FollowBallGoal;
import io.github.marcus8448.mods.projectile.entity.projectile.BouncyBallEntity;
import io.github.marcus8448.mods.projectile.entity.projectile.BouncyDynamiteEntity;
import io.github.marcus8448.mods.projectile.entity.projectile.DyedSnowballEntity;
import io.github.marcus8448.mods.projectile.entity.projectile.DynamiteEntity;
import io.github.marcus8448.mods.projectile.item.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.item.*;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

@Mod("project-ile")
public class ProjectIle {
    public static final Logger LOGGER = LogManager.getLogger("Project-Ile");

    public static final EntityType<DyedSnowballEntity> DYED_SNOWBALL_ENTITY_TYPE = EntityType.Builder.<DyedSnowballEntity>create(DyedSnowballEntity::new, EntityClassification.MISC).disableSummoning().size(0.25F, 0.25F).build("project-ile:dyed_snowball");
    public static final EntityType<DynamiteEntity> DYNAMITE_ENTITY_TYPE = EntityType.Builder.<DynamiteEntity>create(DynamiteEntity::new, EntityClassification.MISC).disableSummoning().size(0.25F, 0.25F).build("project-ile:dynamite");
    public static final EntityType<BouncyDynamiteEntity> BOUNCY_DYNAMITE_ENTITY_TYPE = EntityType.Builder.<BouncyDynamiteEntity>create(BouncyDynamiteEntity::new, EntityClassification.MISC).disableSummoning().size(0.25F, 0.25F).build("project-ile:bouncy_dynamite");
    public static final EntityType<BouncyBallEntity> BOUNCY_BALL_ENTITY_TYPE = EntityType.Builder.<BouncyBallEntity>create(BouncyBallEntity::new, EntityClassification.MISC).disableSummoning().size(0.25F, 0.25F).build("project-ile:bouncy_ball");

    private static ItemGroup PROJECTILE_GROUP;

    public ProjectIle() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);


        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup));

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {

    }

    @OnlyIn(Dist.CLIENT)
    private void clientSetup(final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(DyedSnowballEntity.class, manager -> new SpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(DynamiteEntity.class, manager -> new SpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(BouncyDynamiteEntity.class, manager -> new SpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(BouncyBallEntity.class, manager -> new SpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));

        //RenderingRegistry.registerEntityRenderingHandler(WolfEntity.class, WolfRendererBall::new); //TODO - Server -> client desync
        LOGGER.info("Registered Entity Renderers");
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {

    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void entityJoinWorld(final EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof WolfEntity) {
            LOGGER.info("Added wolf chase ball goal");
            ((WolfEntity) event.getEntity()).goalSelector.addGoal(1, new FollowBallGoal(((WolfEntity) event.getEntity())));
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void registerBlocks(final RegistryEvent.Register<Block> event) {
            PROJECTILE_GROUP = new ItemGroup("projectiles") {
                byte i = 0;
                ItemStack stack = null;
                @Override
                @OnlyIn(Dist.CLIENT)
                public ItemStack createIcon() {
                    if (i++ >= 40 || stack == null) {
                        i = 0;
                        Random random;

                        if (Minecraft.getInstance().world != null) {
                            random = Minecraft.getInstance().world.rand;
                        } else {
                            random = new Random();
                        }
                        int index = random.nextInt(ProjectIleItems.class.getFields().length);
                        try {
                            if (ProjectIleItems.class.getFields()[index].get(null) != null) {
                                stack = new ItemStack((IItemProvider) ProjectIleItems.class.getFields()[index].get(null));
                            } else {
                                stack = null;
                                return new ItemStack(Items.SNOWBALL);
                            }
                        } catch (IllegalAccessException e) {
                            stack = null;
                            return new ItemStack(Items.SNOWBALL);
                        }
                    }
                    return stack;
                }

                @Override
                public ItemStack getIcon() {
                    return createIcon();
                }
            };
        }

        @SubscribeEvent
        public static void registerEntityTypes(final RegistryEvent.Register<EntityType<?>> event) {
            event.getRegistry().register(DYED_SNOWBALL_ENTITY_TYPE.setRegistryName(new ResourceLocation("project-ile", "dyed_snowball")));
            event.getRegistry().register(DYNAMITE_ENTITY_TYPE.setRegistryName(new ResourceLocation("project-ile", "dynamite")));
            event.getRegistry().register(BOUNCY_DYNAMITE_ENTITY_TYPE.setRegistryName(new ResourceLocation("project-ile", "bouncy_dynamite")));
            event.getRegistry().register(BOUNCY_BALL_ENTITY_TYPE.setRegistryName(new ResourceLocation("project-ile", "bouncy_ball")));
        }

        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event) {
            DyedSnowballItem white_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.WHITE, new ResourceLocation("project-ile", "white_snowball"));
            DyedSnowballItem orange_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.ORANGE, new ResourceLocation("project-ile", "orange_snowball"));
            DyedSnowballItem magenta_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.MAGENTA, new ResourceLocation("project-ile", "magenta_snowball"));
            DyedSnowballItem light_blue_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.LIGHT_BLUE, new ResourceLocation("project-ile", "light_blue_snowball"));
            DyedSnowballItem yellow_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.YELLOW, new ResourceLocation("project-ile", "yellow_snowball"));
            DyedSnowballItem lime_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.LIME, new ResourceLocation("project-ile", "lime_snowball"));
            DyedSnowballItem pink_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.PINK, new ResourceLocation("project-ile", "pink_snowball"));
            DyedSnowballItem gray_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.GRAY, new ResourceLocation("project-ile", "gray_snowball"));
            DyedSnowballItem light_gray_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.LIGHT_GRAY, new ResourceLocation("project-ile", "light_gray_snowball"));
            DyedSnowballItem cyan_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.CYAN, new ResourceLocation("project-ile", "cyan_snowball"));
            DyedSnowballItem purple_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.PURPLE, new ResourceLocation("project-ile", "purple_snowball"));
            DyedSnowballItem blue_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.BLUE, new ResourceLocation("project-ile", "blue_snowball"));
            DyedSnowballItem brown_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.BROWN, new ResourceLocation("project-ile", "brown_snowball"));
            DyedSnowballItem green_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.GREEN, new ResourceLocation("project-ile", "green_snowball"));
            DyedSnowballItem red_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.RED, new ResourceLocation("project-ile", "red_snowball"));
            DyedSnowballItem black_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.BLACK, new ResourceLocation("project-ile", "black_snowball"));

            DynamiteItem dynamite = new DynamiteItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), new ResourceLocation("project-ile", "dynamite"));
            BouncyDynamiteItem bouncy_dynamite = new BouncyDynamiteItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), new ResourceLocation("project-ile", "bouncy_dynamite"));

            BouncyBallItem white_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.WHITE, new ResourceLocation("project-ile", "white_bouncy_ball"));
            BouncyBallItem orange_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.ORANGE, new ResourceLocation("project-ile", "orange_bouncy_ball"));
            BouncyBallItem magenta_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.MAGENTA, new ResourceLocation("project-ile", "magenta_bouncy_ball"));
            BouncyBallItem light_blue_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.LIGHT_BLUE, new ResourceLocation("project-ile", "light_blue_bouncy_ball"));
            BouncyBallItem yellow_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.YELLOW, new ResourceLocation("project-ile", "yellow_bouncy_ball"));
            BouncyBallItem lime_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.LIME, new ResourceLocation("project-ile", "lime_bouncy_ball"));
            BouncyBallItem pink_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.PINK, new ResourceLocation("project-ile", "pink_bouncy_ball"));
            BouncyBallItem gray_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.GRAY, new ResourceLocation("project-ile", "gray_bouncy_ball"));
            BouncyBallItem light_gray_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.LIGHT_GRAY, new ResourceLocation("project-ile", "light_gray_bouncy_ball"));
            BouncyBallItem cyan_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.CYAN, new ResourceLocation("project-ile", "cyan_bouncy_ball"));
            BouncyBallItem purple_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.PURPLE, new ResourceLocation("project-ile", "purple_bouncy_ball"));
            BouncyBallItem blue_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.BLUE, new ResourceLocation("project-ile", "blue_bouncy_ball"));
            BouncyBallItem brown_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.BROWN, new ResourceLocation("project-ile", "brown_bouncy_ball"));
            BouncyBallItem green_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.GREEN, new ResourceLocation("project-ile", "green_bouncy_ball"));
            BouncyBallItem red_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.RED, new ResourceLocation("project-ile", "red_bouncy_ball"));
            BouncyBallItem black_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECTILE_GROUP), DyeColor.BLACK, new ResourceLocation("project-ile", "black_bouncy_ball"));

            event.getRegistry().registerAll(white_snowball, orange_snowball, magenta_snowball, light_blue_snowball, yellow_snowball, lime_snowball, pink_snowball, gray_snowball, light_gray_snowball, cyan_snowball, purple_snowball, blue_snowball, brown_snowball, green_snowball, red_snowball, black_snowball,
                    dynamite, bouncy_dynamite,
                    white_bouncy_ball, orange_bouncy_ball, magenta_bouncy_ball, light_blue_bouncy_ball, yellow_bouncy_ball, lime_bouncy_ball, pink_bouncy_ball, gray_bouncy_ball, light_gray_bouncy_ball, cyan_bouncy_ball, purple_bouncy_ball, blue_bouncy_ball, brown_bouncy_ball, green_bouncy_ball, red_bouncy_ball, black_bouncy_ball
            );
        }
    }
}
