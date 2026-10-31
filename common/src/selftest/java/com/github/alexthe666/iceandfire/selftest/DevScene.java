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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Client scene, inert unless {@code iceandfire.scene} is set (gradle runScene, tools/scenetest.sh): creates the world
 * {@code iaf_scene}, shows the dragons and the other creatures, armor, the creative tabs and the bestiary, saves
 * run/screenshots/scene_*.png, logs "[scene] CHECK" lines and quits. Not part of the jar.
 */
public final class DevScene {
    /** The other mods of the run ("id version"), set by the loader's test wiring */
    public static java.util.function.Supplier<List<String>> OTHER_MODS = List::of;

    private static final boolean ENABLED = Boolean.getBoolean("iceandfire.scene");
    private static final String WORLD = "iaf_scene";
    private static final List<Pair<Integer, Consumer<Minecraft>>> STEPS = new ArrayList<>();
    /** The close-ups start at tick 800, one creature every 30 ticks; END leaves room for 40 of them. */
    private static final int END = 800 + 40 * 30;
    private static boolean opened;
    private static int tick;
    private static int failures;
    private static BlockPos stage;

    private static final List<java.util.function.Supplier<ItemStack>> HELMETS = List.of(
        () -> ItemStack.EMPTY,
        () -> new ItemStack(IafItemRegistry.DRAGONSTEEL_FIRE_HELMET.get()),
        () -> new ItemStack(com.github.alexthe666.iceandfire.enums.EnumDragonArmor.values()[0].helmet.get()),
        () -> new ItemStack(IafItemRegistry.SILVER_HELMET.get()),
        () -> new ItemStack(IafItemRegistry.COPPER_HELMET.get()),
        () -> new ItemStack(IafItemRegistry.DEATHWORM_YELLOW_HELMET.get()));

    static {
        at(40, mc -> server(mc, DevScene::setup));
        at(45, mc -> mc.options.hideGui = true);
        at(60, mc -> server(mc, DevScene::buildStage));
        at(120, mc -> server(mc, DevScene::dragons));
        at(130, mc -> server(mc, p -> look(p, stage.offset(0, 8, 38), stage.offset(0, 3, 20))));
        at(220, mc -> shot(mc, "dragons"));
        at(230, mc -> server(mc, p -> level(p).getEntitiesOfClass(EntityDragonBase.class, p.getBoundingBox().inflate(80)).forEach(Entity::discard)));
        // Helmets close up, front (#5112: the hat layer of the skin through the helmet); first without a helmet
        at(236, mc -> {
            mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            mc.options.fov().set(30);
        });
        for (int k = 0; k < HELMETS.size(); k++) {
            final int n = k;
            at(240 + k * 40, mc -> server(mc, p -> {
                p.setItemSlot(EquipmentSlot.HEAD, HELMETS.get(n).get());
                look(p, stage.offset(0, 2, 40), stage.offset(0, 2, 30));
            }));
            at(270 + k * 40, mc -> shot(mc, "helmet_" + n));
        }
        at(240 + HELMETS.size() * 40, mc -> {
            mc.options.fov().set(70);
            mc.options.setCameraType(CameraType.FIRST_PERSON);
        });
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
        // Pages with text (the translations must fit the page): SCENE_LANG=ru_ru tools/scenetest.sh
        at(645, mc -> bestiaryPage(mc, "FIREDRAGON", 0));
        at(655, mc -> shot(mc, "bestiary_firedragon_0"));
        at(660, mc -> bestiaryPage(mc, "TAMEDDRAGONS", 1));
        at(670, mc -> shot(mc, "bestiary_tameddragons_1"));
        at(675, mc -> bestiaryPage(mc, "MYRMEX", 3));
        at(685, mc -> shot(mc, "bestiary_myrmex_3"));
        at(690, mc -> mc.setScreen(null));
        // Screens with labels: the dragon (name, health, stage, gender, hunger), the lectern, the dragon forge
        at(700, mc -> server(mc, DevScene::dragonScreen));
        at(725, mc -> shot(mc, "gui_dragon"));
        at(730, mc -> {
            mc.setScreen(null);
            server(mc, p -> level(p).getEntitiesOfClass(EntityDragonBase.class, p.getBoundingBox().inflate(40)).forEach(Entity::discard));
        });
        at(740, mc -> mc.setScreen(new com.github.alexthe666.iceandfire.client.gui.GuiLectern(
            new com.github.alexthe666.iceandfire.inventory.ContainerLectern(0, mc.player.getInventory()), mc.player.getInventory(),
            net.minecraft.network.chat.Component.translatable("block.iceandfire.lectern"))));
        at(750, mc -> shot(mc, "gui_lectern"));
        at(755, mc -> mc.setScreen(new com.github.alexthe666.iceandfire.client.gui.GuiDragonForge(
            new com.github.alexthe666.iceandfire.inventory.ContainerDragonForge(0, mc.player.getInventory()), mc.player.getInventory(),
            net.minecraft.network.chat.Component.translatable("iceandfire.fire_dragon_forge"))));
        at(765, mc -> shot(mc, "gui_dragonforge"));
        at(770, mc -> mc.setScreen(null));
        at(END + 20, mc -> {
            IceAndFire.LOGGER.info("[scene] DONE, failed checks: {}", failures);
            mc.stop();
        });
    }

    /** The living creatures of the mod except dragons (by registry order). */
    private static final List<EntityType<?>> CREATURES = new ArrayList<>();
    private static final List<String> MOBS = List.of("hippogryph", "gorgon", "pixie", "cyclops", "siren", "hippocampus", "deathworm", "cockatrice",
            "stymphalian_bird", "troll", "myrmex_worker", "myrmex_soldier", "myrmex_sentinel", "myrmex_royal", "myrmex_queen", "amphithere", "sea_serpent",
            "myrmex_swarmer", "dread_thrall", "dread_ghoul", "dread_beast", "dread_scuttler", "dread_lich", "dread_knight", "dread_horse", "hydra", "ghost");
    private static Entity lastCloseUp;

    static {
    }

    private static void addCloseUps() {
        int t0 = 800;
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (id.getNamespace().equals(IceAndFire.MODID) && MOBS.contains(id.getPath())) {
                CREATURES.add(type);
            }
        }
        for (int i = 0; i < CREATURES.size(); i++) {
            final int n = i;
            at(t0 + i * 30, mc -> server(mc, p -> closeUp(p, CREATURES.get(n))));
            at(t0 + i * 30 + 25, mc -> {
                EntityType<?> type = CREATURES.get(n);
                boolean present = false;
                for (Entity e : mc.level.entitiesForRendering()) {
                    present |= e.getType() == type;
                }
                String name = BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath();
                List<String> near = new ArrayList<>();
                for (Entity e : mc.level.entitiesForRendering()) {
                    if (e.distanceTo(mc.player) < 30 && !(e instanceof net.minecraft.world.entity.item.ItemEntity)) {
                        near.add(e.getClass().getSimpleName() + "@" + e.blockPosition().subtract(stage).toShortString() + (e == mc.getCameraEntity() ? "(camera)" : ""));
                    }
                }
                IceAndFire.LOGGER.info("[scene] client entities near {}: {} player at {}", name, near, mc.player.blockPosition().subtract(stage).toShortString());
                check("client has " + name, present, "");
                shot(mc, "mob_" + name);
            });
        }
    }

    private static void bestiaryPage(Minecraft mc, String page, int number) {
        if (mc.screen instanceof GuiBestiary gui) {
            gui.pageType = com.github.alexthe666.iceandfire.enums.EnumBestiaryPages.valueOf(page);
            gui.bookPages = number;
            try {
                java.lang.reflect.Field index = GuiBestiary.class.getDeclaredField("index");
                index.setAccessible(true);
                index.setBoolean(gui, false);
            } catch (ReflectiveOperationException e) {
                check("bestiary page " + page, false, e);
            }
        } else {
            check("bestiary open for " + page, false, mc.screen);
        }
    }

    /** An adult tamed fire dragon of the player, its inventory screen open. */
    private static void dragonScreen(ServerPlayer player) {
        // Next to the player: ContainerDragon closes beyond 8 blocks
        BlockPos near = player.blockPosition().offset(3, 0, 0);
        near = near.atY(level(player).getHeight(Heightmap.Types.MOTION_BLOCKING, near.getX(), near.getZ()));
        Entity entity = place(level(player), IafEntityRegistry.FIRE_DRAGON.get(), near, 180);
        if (entity instanceof EntityDragonBase dragon) {
            dragon.setAgeInDays(60);
            dragon.tame(player);
            dragon.openInventory(player);
        }
    }

    private static ServerLevel level(ServerPlayer player) {
        return player.serverLevel();
    }

    private static void closeUp(ServerPlayer player, EntityType<?> type) {
        ServerLevel level = player.serverLevel();
        if (lastCloseUp != null) {
            lastCloseUp.discard();
        }
        // Trolls turn into stone statues in daylight (a feature of the mod): clear whatever the last creature left
        level.getEntities((Entity) null, new net.minecraft.world.phys.AABB(stage).inflate(6), e -> !(e instanceof ServerPlayer)).forEach(Entity::discard);
        List<String> others = new ArrayList<>();
        for (Entity e : level.getEntities((Entity) null, new net.minecraft.world.phys.AABB(stage).inflate(64), e -> !(e instanceof ServerPlayer))) {
            others.add(BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()) + "@" + e.blockPosition().subtract(stage).toShortString());
        }
        IceAndFire.LOGGER.info("[scene] around the stage before {}: {}", BuiltInRegistries.ENTITY_TYPE.getKey(type), others);
        // Night for the troll (stone in daylight), day for the rest
        level.setDayTime(BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath().equals("troll") ? 18000 : 6000);
        Entity entity = place(level, type, stage.offset(0, 0, 0), 200);
        lastCloseUp = entity;
        float size = Math.max(entity.getBbWidth(), entity.getBbHeight());
        if (entity instanceof com.github.alexthe666.iceandfire.entity.EntitySeaSerpent serpent) {
            // The body (its parts) is about 4.5 times its scale long, the hitbox is the head only: the camera was inside
            size = Math.max(size, serpent.getSeaSerpentScale() * 4.5F);
        }
        int distance = Math.max(4, (int) Math.ceil(size * 2.2F));
        look(player, stage.offset(distance / 3, Math.max(2, (int) (entity.getBbHeight() * 0.8F) + 1), distance), stage.offset(0, (int) (entity.getBbHeight() / 2), 0));
    }

    private static void at(int t, Consumer<Minecraft> action) {
        STEPS.add(Pair.of(t, action));
    }

    public static void onTick() {
        if (!ENABLED) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        // NeoForge lists the loading warnings of other mods (deprecated fields) on a screen that waits for a click
        if (!opened && mc.screen != null && mc.screen.getClass().getSimpleName().equals("LoadingErrorScreen")) {
            mc.setScreen(new TitleScreen());
            return;
        }
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
                LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, false, Difficulty.NORMAL, true, new GameRules(), WorldDataConfiguration.DEFAULT);
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
        if (tick == 1) {
            addCloseUps();
        }
        for (Pair<Integer, Consumer<Minecraft>> step : new ArrayList<>(STEPS)) {
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
        // The other mods of the run (tools/scenetest.sh -Pwith_compat)
        List<String> mods = new ArrayList<>();
        mods.addAll(OTHER_MODS.get());
        IceAndFire.LOGGER.info("[scene] other mods: {}", mods);
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
            Entity entity = place(level, type, stage.offset(i * 12, 0, 20), 180);
            if (entity instanceof EntityDragonBase dragon) {
                dragon.setAgeInDays(40);
                count++;
            }
            i++;
        }
        check("dragons placed", count == 3, count);
    }

    /** Every other living creature of the mod in rows behind the dragons. */
    private static void creatures(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        int x = -36;
        int z = -6;
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
            x += 7;
            if (x > 36) {
                x = -36;
                z -= 8;
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
