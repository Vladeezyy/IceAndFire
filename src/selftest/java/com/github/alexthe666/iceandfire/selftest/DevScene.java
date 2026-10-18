package com.github.alexthe666.iceandfire.selftest;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.alexthe666.iceandfire.item.IafTabRegistry;
import com.github.alexthe666.iceandfire.client.gui.bestiary.GuiBestiary;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Client scene, inert unless {@code iceandfire.scene} is set (gradle runScene, tools/scenetest.sh): creates the world
 * {@code iaf_scene}, shows the dragons and the other creatures, armor, the creative tabs and the bestiary, saves
 * run/screenshots/scene_*.png, logs "[scene] CHECK" lines and quits. Not part of the jar.
 */
@EventBusSubscriber(modid = IceAndFire.MODID, value = Dist.CLIENT)
public final class DevScene {
    private static final boolean ENABLED = Boolean.getBoolean("iceandfire.scene");
    private static final String WORLD = "iaf_scene";
    private static final List<Pair<Integer, Consumer<Minecraft>>> STEPS = new ArrayList<>();
    private static boolean opened;
    private static int tick;
    private static int failures;
    private static BlockPos stage;

    static {
        at(40, mc -> server(mc, DevScene::setup));
        at(45, mc -> mc.options.hideGui = true);
        at(60, mc -> server(mc, DevScene::buildStage));
        at(120, mc -> server(mc, DevScene::dragons));
        at(220, mc -> shot(mc, "dragons"));
        at(225, mc -> server(mc, p -> look(p, stage.offset(0, 2, 30), stage.offset(0, 0, 8))));
        at(260, mc -> shot(mc, "dragons_close"));
        at(270, mc -> server(mc, DevScene::creatures));
        at(370, mc -> shot(mc, "creatures"));
        at(375, mc -> server(mc, p -> look(p, stage.offset(-20, 5, -10), stage.offset(-20, 0, -30))));
        at(420, mc -> shot(mc, "creatures_left"));
        at(425, mc -> server(mc, p -> look(p, stage.offset(20, 5, -10), stage.offset(20, 0, -30))));
        at(470, mc -> shot(mc, "creatures_right"));
        // Armor in third person (issues #5112, #5205: armor models and textures)
        at(480, mc -> server(mc, DevScene::armor));
        at(485, mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
        at(530, mc -> shot(mc, "armor"));
        at(535, mc -> mc.options.setCameraType(CameraType.FIRST_PERSON));
        // Creative tabs
        at(540, mc -> {
            mc.options.hideGui = false;
            mc.setScreen(new CreativeModeInventoryScreen(mc.player, mc.player.connection.enabledFeatures(), true));
        });
        at(550, mc -> selectTab(mc, IafTabRegistry.TAB_ITEMS.get()));
        at(570, mc -> shot(mc, "tab_items"));
        at(575, mc -> selectTab(mc, IafTabRegistry.TAB_BLOCKS.get()));
        at(595, mc -> shot(mc, "tab_blocks"));
        at(600, mc -> mc.setScreen(null));
        // Bestiary with every page
        at(610, mc -> {
            ItemStack book = new ItemStack(IafItemRegistry.BESTIARY.get());
            com.github.alexthe666.iceandfire.enums.EnumBestiaryPages.addRandomPage(book);
            for (com.github.alexthe666.iceandfire.enums.EnumBestiaryPages page : com.github.alexthe666.iceandfire.enums.EnumBestiaryPages.values()) {
                com.github.alexthe666.iceandfire.enums.EnumBestiaryPages.addPage(page, book);
            }
            mc.setScreen(new GuiBestiary(book));
        });
        at(640, mc -> shot(mc, "bestiary"));
        at(650, mc -> mc.setScreen(null));
        at(660, DevScene::checkRendered);
        at(680, mc -> {
            IceAndFire.LOGGER.info("[scene] DONE, failed checks: {}", failures);
            mc.stop();
        });
    }

    private static void at(int t, Consumer<Minecraft> action) {
        STEPS.add(Pair.of(t, action));
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        if (!ENABLED) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (!opened && mc.screen instanceof net.minecraft.client.gui.screens.AccessibilityOnboardingScreen) {
            mc.options.onboardAccessibility = false;
            mc.options.save();
            mc.setScreen(new TitleScreen());
            return;
        }
        if (!opened && mc.screen instanceof TitleScreen) {
            opened = true;
            if (mc.getLevelSource().levelExists(WORLD)) {
                mc.createWorldOpenFlows().openWorld(WORLD, () -> mc.setScreen(new TitleScreen()));
            } else {
                LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, false, Difficulty.PEACEFUL, true, new GameRules(), WorldDataConfiguration.DEFAULT);
                mc.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(1234L, false, false), WorldPresets::createNormalWorldDimensions, new TitleScreen());
            }
            return;
        }
        if (mc.player == null || mc.getSingleplayerServer() == null) {
            return;
        }
        mc.options.pauseOnLostFocus = false;
        mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
        mc.getToasts().clear();
        if (mc.screen instanceof PauseScreen) {
            mc.setScreen(null);
        }
        tick++;
        for (Pair<Integer, Consumer<Minecraft>> step : STEPS) {
            if (step.getFirst() == tick) {
                IceAndFire.LOGGER.info("[scene] tick {}", tick);
                try {
                    step.getSecond().accept(mc);
                } catch (Throwable t) {
                    check("step " + tick, false, t);
                    IceAndFire.LOGGER.error("[scene] step failed", t);
                }
            }
        }
    }

    private static void server(Minecraft mc, Consumer<ServerPlayer> action) {
        MinecraftServer server = mc.getSingleplayerServer();
        server.execute(() -> {
            try {
                action.accept(server.getPlayerList().getPlayer(mc.player.getUUID()));
            } catch (Throwable t) {
                check("server step", false, t);
                IceAndFire.LOGGER.error("[scene] server step failed", t);
            }
        });
    }

    private static void check(String name, boolean ok, Object detail) {
        if (!ok) {
            failures++;
        }
        IceAndFire.LOGGER.info("[scene] CHECK {} {} ({})", ok ? "OK  " : "FAIL", name, detail);
    }

    private static void shot(Minecraft mc, String name) {
        mc.getToasts().clear();
        Screenshot.grab(mc.gameDirectory, "scene_" + name + ".png", mc.getMainRenderTarget(), message -> IceAndFire.LOGGER.info("[scene] screenshot {}", name));
    }

    private static void selectTab(Minecraft mc, CreativeModeTab tab) {
        if (mc.screen instanceof CreativeModeInventoryScreen screen) {
            try {
                var select = CreativeModeInventoryScreen.class.getDeclaredMethod("selectTab", CreativeModeTab.class);
                select.setAccessible(true);
                select.invoke(screen, tab);
            } catch (ReflectiveOperationException e) {
                check("select creative tab", false, e);
            }
        }
    }

    /** Puts the player at {@code eye} looking at {@code target}. */
    private static void look(ServerPlayer player, BlockPos eye, BlockPos target) {
        double dx = target.getX() - eye.getX();
        double dy = target.getY() - eye.getY();
        double dz = target.getZ() - eye.getZ();
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        player.teleportTo(player.serverLevel(), eye.getX() + 0.5, eye.getY() - 1.62, eye.getZ() + 0.5, yaw, pitch);
    }

    private static void setup(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        player.setGameMode(GameType.CREATIVE);
        server.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
        server.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
        server.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);
        player.serverLevel().setDayTime(6000);
        player.serverLevel().setWeatherParameters(6000, 0, false, false);
    }

    /** Flat grass stage 90 x 90 at the spawn. */
    private static void buildStage(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos spawn = level.getSharedSpawnPos();
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawn.getX(), spawn.getZ());
        stage = new BlockPos(spawn.getX(), y, spawn.getZ());
        for (int dx = -45; dx <= 45; dx++) {
            for (int dz = -45; dz <= 45; dz++) {
                level.setBlock(stage.offset(dx, -1, dz), Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                for (int dy = 0; dy < 25; dy++) {
                    level.setBlock(stage.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        look(player, stage.offset(0, 14, 50), stage.offset(0, 4, 0));
    }

    private static Entity place(ServerLevel level, EntityType<?> type, BlockPos pos, float yaw) {
        Entity entity = type.create(level);
        entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0);
        if (entity instanceof Mob mob) {
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.COMMAND, null);
            mob.setNoAi(true);
            mob.setPersistenceRequired();
            mob.setYHeadRot(yaw);
            mob.yBodyRot = yaw;
        }
        level.addFreshEntity(entity);
        return entity;
    }

    /** The three dragons, adult (100 days), side by side and facing the camera. */
    private static void dragons(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        int i = -1;
        int count = 0;
        for (EntityType<?> type : List.of(IafEntityRegistry.FIRE_DRAGON.get(), IafEntityRegistry.ICE_DRAGON.get(), IafEntityRegistry.LIGHTNING_DRAGON.get())) {
            Entity entity = place(level, type, stage.offset(i * 16, 0, 0), 180);
            if (entity instanceof EntityDragonBase dragon) {
                dragon.setAgeInDays(100);
                count++;
            }
            i++;
        }
        check("dragons placed", count == 3, count);
    }

    /** Every other living creature of the mod in rows behind the dragons. */
    private static void creatures(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        int x = -40;
        int z = -16;
        int count = 0;
        List<String> skipped = new ArrayList<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (!id.getNamespace().equals(IceAndFire.MODID) || id.getPath().contains("dragon") || id.getPath().contains("multipart")) {
                continue;
            }
            Entity probe = type.create(level);
            if (!(probe instanceof Mob)) {
                skipped.add(id.getPath());
                continue;
            }
            place(level, type, stage.offset(x, 0, z), 180);
            count++;
            x += 8;
            if (x > 40) {
                x = -40;
                z -= 12;
            }
        }
        check("creatures placed", count > 25, count + ", not mobs: " + skipped);
    }

    private static void armor(ServerPlayer player) {
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(IafItemRegistry.DRAGONSTEEL_FIRE_HELMET.get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(IafItemRegistry.DRAGONSTEEL_FIRE_CHESTPLATE.get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(IafItemRegistry.DRAGONSTEEL_FIRE_LEGGINGS.get()));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(IafItemRegistry.DRAGONSTEEL_FIRE_BOOTS.get()));
        look(player, stage.offset(0, 2, 40), stage.offset(0, 2, 30));
    }

    private static void checkRendered(Minecraft mc) {
        int entities = 0;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getNamespace().equals(IceAndFire.MODID)) {
                entities++;
            }
        }
        check("entities on the client", entities > 25, entities);
    }
}
