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

package io.github.marcus8448.mods.projectile;

import io.github.marcus8448.mods.projectile.entity.ai.goal.FollowBallGoal;
import io.github.marcus8448.mods.projectile.entity.projectile.BouncyBallEntity;
import io.github.marcus8448.mods.projectile.entity.projectile.BouncyDynamiteEntity;
import io.github.marcus8448.mods.projectile.entity.projectile.DyedSnowballEntity;
import io.github.marcus8448.mods.projectile.entity.projectile.DynamiteEntity;
import io.github.marcus8448.mods.projectile.item.*;
import io.github.marcus8448.mods.projectile.packet.ProjectIlePacketHandler;
import io.github.marcus8448.mods.projectile.recipe.ProjectileRecipeSerializers;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

@Mod(ProjectIle.MODID)
public class ProjectIle {

    public static final String MODID = "project-ile";

    public static final Logger LOGGER = LogManager.getLogger(ProjectIle.MODID);

    public static final EntityType<DyedSnowballEntity> DYED_SNOWBALL_ENTITY_TYPE = EntityType.Builder.<DyedSnowballEntity>create(DyedSnowballEntity::new, EntityClassification.MISC).disableSummoning().size(0.25F, 0.25F).build("project-ile:dyed_snowball");
    public static final EntityType<DynamiteEntity> DYNAMITE_ENTITY_TYPE = EntityType.Builder.<DynamiteEntity>create(DynamiteEntity::new, EntityClassification.MISC).disableSummoning().size(0.25F, 0.25F).build("project-ile:dynamite");
    public static final EntityType<BouncyDynamiteEntity> BOUNCY_DYNAMITE_ENTITY_TYPE = EntityType.Builder.<BouncyDynamiteEntity>create(BouncyDynamiteEntity::new, EntityClassification.MISC).disableSummoning().size(0.25F, 0.25F).build("project-ile:bouncy_dynamite");
    public static final EntityType<BouncyBallEntity> BOUNCY_BALL_ENTITY_TYPE = EntityType.Builder.<BouncyBallEntity>create(BouncyBallEntity::new, EntityClassification.MISC).disableSummoning().size(0.25F, 0.25F).build("project-ile:bouncy_ball");

    private static ItemGroup PROJECT_ILE_GROUP;

    public ProjectIle() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        });

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        ProjectIlePacketHandler.register();
    }

    @OnlyIn(Dist.CLIENT)
    private void clientSetup(final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(DYED_SNOWBALL_ENTITY_TYPE, manager -> new SpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(DYNAMITE_ENTITY_TYPE, manager -> new SpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(BOUNCY_DYNAMITE_ENTITY_TYPE, manager -> new SpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(BOUNCY_BALL_ENTITY_TYPE, manager -> new SpriteRenderer<>(manager, Minecraft.getInstance().getItemRenderer()));

        //RenderingRegistry.registerEntityRenderingHandler(WolfEntity.class, WolfRendererBall::new); //TODO - Server -> client desync
        LOGGER.info("Registered Entity Renderers");
    }
//
//    @SubscribeEvent
//    public void onServerStarting(FMLServerStartingEvent event) {
//
//    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void entityJoinWorld(final EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof WolfEntity) {
            ((WolfEntity) event.getEntity()).goalSelector.addGoal(1, new FollowBallGoal(((WolfEntity) event.getEntity())));
        }
    }

    @SubscribeEvent
    public void attachCapabilities(final AttachCapabilitiesEvent<Entity> event) {

    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void registerBlocks(final RegistryEvent.Register<Block> event) {
            PROJECT_ILE_GROUP = new ItemGroup("projectiles") {
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
            event.getRegistry().register(DYED_SNOWBALL_ENTITY_TYPE.setRegistryName(new ResourceLocation(ProjectIle.MODID, "dyed_snowball")));
            event.getRegistry().register(DYNAMITE_ENTITY_TYPE.setRegistryName(new ResourceLocation(ProjectIle.MODID, "dynamite")));
            event.getRegistry().register(BOUNCY_DYNAMITE_ENTITY_TYPE.setRegistryName(new ResourceLocation(ProjectIle.MODID, "bouncy_dynamite")));
            event.getRegistry().register(BOUNCY_BALL_ENTITY_TYPE.setRegistryName(new ResourceLocation(ProjectIle.MODID, "bouncy_ball")));
        }

        @SubscribeEvent
        public static void registerRecipeSerializers(final RegistryEvent.Register<IRecipeSerializer<?>> event) {
            ProjectileRecipeSerializers.init();
        }

        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event) {
            DyedSnowballItem white_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.WHITE, new ResourceLocation(ProjectIle.MODID, "white_snowball"));
            DyedSnowballItem orange_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.ORANGE, new ResourceLocation(ProjectIle.MODID, "orange_snowball"));
            DyedSnowballItem magenta_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.MAGENTA, new ResourceLocation(ProjectIle.MODID, "magenta_snowball"));
            DyedSnowballItem light_blue_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.LIGHT_BLUE, new ResourceLocation(ProjectIle.MODID, "light_blue_snowball"));
            DyedSnowballItem yellow_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.YELLOW, new ResourceLocation(ProjectIle.MODID, "yellow_snowball"));
            DyedSnowballItem lime_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.LIME, new ResourceLocation(ProjectIle.MODID, "lime_snowball"));
            DyedSnowballItem pink_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.PINK, new ResourceLocation(ProjectIle.MODID, "pink_snowball"));
            DyedSnowballItem gray_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.GRAY, new ResourceLocation(ProjectIle.MODID, "gray_snowball"));
            DyedSnowballItem light_gray_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.LIGHT_GRAY, new ResourceLocation(ProjectIle.MODID, "light_gray_snowball"));
            DyedSnowballItem cyan_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.CYAN, new ResourceLocation(ProjectIle.MODID, "cyan_snowball"));
            DyedSnowballItem purple_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.PURPLE, new ResourceLocation(ProjectIle.MODID, "purple_snowball"));
            DyedSnowballItem blue_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.BLUE, new ResourceLocation(ProjectIle.MODID, "blue_snowball"));
            DyedSnowballItem brown_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.BROWN, new ResourceLocation(ProjectIle.MODID, "brown_snowball"));
            DyedSnowballItem green_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.GREEN, new ResourceLocation(ProjectIle.MODID, "green_snowball"));
            DyedSnowballItem red_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.RED, new ResourceLocation(ProjectIle.MODID, "red_snowball"));
            DyedSnowballItem black_snowball = new DyedSnowballItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.BLACK, new ResourceLocation(ProjectIle.MODID, "black_snowball"));

            DynamiteItem dynamite = new DynamiteItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), new ResourceLocation(ProjectIle.MODID, "dynamite"));
            BouncyDynamiteItem bouncy_dynamite = new BouncyDynamiteItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), new ResourceLocation(ProjectIle.MODID, "bouncy_dynamite"));

            BouncyBallItem white_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.WHITE, new ResourceLocation(ProjectIle.MODID, "white_bouncy_ball"));
            BouncyBallItem orange_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.ORANGE, new ResourceLocation(ProjectIle.MODID, "orange_bouncy_ball"));
            BouncyBallItem magenta_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.MAGENTA, new ResourceLocation(ProjectIle.MODID, "magenta_bouncy_ball"));
            BouncyBallItem light_blue_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.LIGHT_BLUE, new ResourceLocation(ProjectIle.MODID, "light_blue_bouncy_ball"));
            BouncyBallItem yellow_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.YELLOW, new ResourceLocation(ProjectIle.MODID, "yellow_bouncy_ball"));
            BouncyBallItem lime_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.LIME, new ResourceLocation(ProjectIle.MODID, "lime_bouncy_ball"));
            BouncyBallItem pink_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.PINK, new ResourceLocation(ProjectIle.MODID, "pink_bouncy_ball"));
            BouncyBallItem gray_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.GRAY, new ResourceLocation(ProjectIle.MODID, "gray_bouncy_ball"));
            BouncyBallItem light_gray_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.LIGHT_GRAY, new ResourceLocation(ProjectIle.MODID, "light_gray_bouncy_ball"));
            BouncyBallItem cyan_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.CYAN, new ResourceLocation(ProjectIle.MODID, "cyan_bouncy_ball"));
            BouncyBallItem purple_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.PURPLE, new ResourceLocation(ProjectIle.MODID, "purple_bouncy_ball"));
            BouncyBallItem blue_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.BLUE, new ResourceLocation(ProjectIle.MODID, "blue_bouncy_ball"));
            BouncyBallItem brown_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.BROWN, new ResourceLocation(ProjectIle.MODID, "brown_bouncy_ball"));
            BouncyBallItem green_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.GREEN, new ResourceLocation(ProjectIle.MODID, "green_bouncy_ball"));
            BouncyBallItem red_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.RED, new ResourceLocation(ProjectIle.MODID, "red_bouncy_ball"));
            BouncyBallItem black_bouncy_ball = new BouncyBallItem(new Item.Properties().maxStackSize(16).group(PROJECT_ILE_GROUP), DyeColor.BLACK, new ResourceLocation(ProjectIle.MODID, "black_bouncy_ball"));

            ParticleItem particle_item = new ParticleItem(new Item.Properties().maxStackSize(1).maxDamage(2048).group(PROJECT_ILE_GROUP), new ResourceLocation(ProjectIle.MODID, "particle_item"));

            event.getRegistry().registerAll(white_snowball, orange_snowball, magenta_snowball, light_blue_snowball, yellow_snowball, lime_snowball, pink_snowball, gray_snowball, light_gray_snowball, cyan_snowball, purple_snowball, blue_snowball, brown_snowball, green_snowball, red_snowball, black_snowball,
                    dynamite, bouncy_dynamite,
                    white_bouncy_ball, orange_bouncy_ball, magenta_bouncy_ball, light_blue_bouncy_ball, yellow_bouncy_ball, lime_bouncy_ball, pink_bouncy_ball, gray_bouncy_ball, light_gray_bouncy_ball, cyan_bouncy_ball, purple_bouncy_ball, blue_bouncy_ball, brown_bouncy_ball, green_bouncy_ball, red_bouncy_ball, black_bouncy_ball,
                    particle_item
            );
            
        }
    }
}
