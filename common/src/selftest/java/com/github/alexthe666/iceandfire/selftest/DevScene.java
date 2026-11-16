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
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
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
        // Particles in front of a black wall, drawn or not (CurseForge report 2026-11-16, GitHub #6: "all particles in the
        // game are not rendered"): the frame is compared with the same frame before the particles. Vanilla ones in the
        // middle, first alone, then next to the mod's at both sides (on 1.21.1 the mod's own were never drawn)
        // as early as the stage allows: on a busy machine the wall was not drawn yet ten ticks later
        at(61, mc -> server(mc, DevScene::particleWall));
        particleCheck(74, "vanilla particles drawn");
        at(91, mc -> grabPixels(mc, pixels -> DevScene.modParticleReference = pixels));
        for (int t = 92; t < 117; t++) {
            at(t, DevScene::spawnModParticles);
        }
        at(102, mc -> grabPixels(mc, pixels -> {
            int changed = changedPixels(DevScene.modParticleReference, pixels, false);
            check("mod particles drawn", changed >= 2000, changed + " pixels changed at the sides");
        }));
        particleCheck(97, "vanilla particles drawn next to the mod's");
        // the ghost's scare and the siren's charm drawn in front of the camera (models in the particle pass)
        at(104, mc -> server(mc, p -> place(p.level(), IafEntityRegistry.GHOST.get(), particleWallAt().offset(-5, 0, 2), 0)));
        at(112, mc -> {
            for (Entity e : mc.level.entitiesForRendering()) {
                if (e instanceof com.github.alexthe666.iceandfire.entity.EntityGhost) {
                    IceAndFire.PROXY.spawnParticle(com.github.alexthe666.iceandfire.enums.EnumParticles.Ghost_Appearance, mc.player.getX(), mc.player.getY(), mc.player.getZ(), e.getId(), 0, 0);
                }
            }
            IceAndFire.PROXY.spawnParticle(com.github.alexthe666.iceandfire.enums.EnumParticles.Siren_Appearance, mc.player.getX(), mc.player.getY(), mc.player.getZ(), 0, 0, 0);
        });
        at(116, mc -> shot(mc, "particles_appearances"));
        at(117, mc -> {
            mc.particleEngine.clearParticles();
            server(mc, p -> {
                particleWallRemove(p.level());
                p.level().getEntities(IafEntityRegistry.GHOST.get(), new net.minecraft.world.phys.AABB(particleWallAt()).inflate(10), e -> true).forEach(Entity::discard);
            });
        });
        if ("particles".equals(System.getenv("SCENE_ONLY"))) {
            at(119, mc -> {
                IceAndFire.LOGGER.info("[scene] DONE, failed checks: {}", failures);
                mc.stop();
            });
        }
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
        // Stone statues of every living creature (CurseForge report 2026-11-01: a statue crashed the game and the world
        // would not load again), then a gorgon turning live mobs to stone the way it does in its temple
        at(END - 380, mc -> {
            mc.options.hideGui = true;
            server(mc, DevScene::statues);
        });
        at(END - 340, mc -> checkStatues(mc, "statues of every creature", 50, 60));
        at(END - 336, mc -> check("statues posed", com.github.alexthe666.iceandfire.client.render.entity.RenderStoneStatue.UNPOSED.isEmpty(),
            com.github.alexthe666.iceandfire.client.render.entity.RenderStoneStatue.UNPOSED));
        at(END - 335, mc -> shot(mc, "statues"));
        at(END - 330, mc -> server(mc, DevScene::gorgonVictims));
        at(END - 10, mc -> checkStatues(mc, "statues made by the gorgon", 1, 7));
        at(END - 5, mc -> shot(mc, "statues_gorgon"));
        // Projectiles whose hits and ends run on the client too (CurseForge report 2026-11-08: a stymphalian feather
        // crashed the client, "ClientLevel cannot be cast to ServerLevel"; the hydra breath and the sea serpent bubbles
        // the same way on 1.21.11 and newer)
        at(END + 20, mc -> server(mc, DevScene::projectiles));
        at(END + 150, mc -> server(mc, DevScene::checkProjectiles));
        // A mob on a chain tied to a wall by a click with an empty hand (crash audit 2026-11-08): the click fires on the client
        // too, which made a knot of its own, without an entity id on 26.2 and newer, and crashed on it at the second hand
        at(END + 160, mc -> server(mc, DevScene::chainSetup));
        at(END + 185, DevScene::chainClick);
        at(END + 205, mc -> server(mc, DevScene::checkChain));
        // the same vanilla particles after everything else the scene showed
        at(END + 206, mc -> server(mc, DevScene::particleWall));
        particleCheck(END + 212, "vanilla particles drawn at the end");
        at(END + 229, mc -> {
            mc.particleEngine.clearParticles();
            server(mc, p -> particleWallRemove(p.level()));
        });
        at(END + 235, mc -> {
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
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
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

    private static int featherDropChance;

    /** Three blocks above the ground at this column. */
    private static BlockPos top(ServerLevel level, BlockPos pos) {
        return new BlockPos(pos.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()) + 3, pos.getZ());
    }

    /** Feathers that fall and go after 100 ticks, each dropping its item, and a hydra breath and sea serpent bubbles inside a pig. */
    private static void projectiles(ServerPlayer player) {
        ServerLevel level = level(player);
        featherDropChance = com.github.alexthe666.iceandfire.IafConfig.stymphalianBirdFeatherDropChance;
        com.github.alexthe666.iceandfire.IafConfig.stymphalianBirdFeatherDropChance = 1;
        BlockPos at = player.blockPosition();
        for (int i = 0; i < 4; i++) {
            // near the player, ticked by the server: each drops its item when it goes
            place(level, IafEntityRegistry.STYMPHALIAN_FEATHER.get(), top(level, at.offset(-6 + 4 * i, 0, -8)), 0).setDeltaMovement(0, -0.5, 0);
            // the client counts its own 100 ticks first and removes these itself, as with a lagging server or beyond the
            // server's simulation distance (the age is not synced)
            Entity late = place(level, IafEntityRegistry.STYMPHALIAN_FEATHER.get(), top(level, at.offset(-6 + 4 * i, 0, -4)), 0);
            late.setDeltaMovement(0, -0.5, 0);
            late.tickCount = -1000;
        }
        int x = -4;
        for (EntityType<?> type : List.<EntityType<?>>of(IafEntityRegistry.HYDRA_BREATH.get(), IafEntityRegistry.SEA_SERPENT_BUBBLES.get())) {
            BlockPos pos = top(level, at.offset(x, 0, -12)).below(3);
            x += 8;
            place(level, EntityType.PIG, pos, 0);
            // inside the pig: both sides find the hit, whatever motion the client was sent
            Entity shot = place(level, type, pos, 0);
            shot.setPos(pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5);
            shot.setDeltaMovement(0, -0.1, 0);
        }
    }

    private static void checkProjectiles(ServerPlayer player) {
        ServerLevel level = level(player);
        com.github.alexthe666.iceandfire.IafConfig.stymphalianBirdFeatherDropChance = featherDropChance;
        net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(player.blockPosition()).inflate(30);
        List<net.minecraft.world.entity.item.ItemEntity> items = level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, box,
                e -> e.getItem().is(IafItemRegistry.STYMPHALIAN_BIRD_FEATHER.get()));
        check("stymphalian feathers dropped on the server", items.size() >= 4, items.size());
        items.forEach(Entity::discard);
        level.getEntities(IafEntityRegistry.STYMPHALIAN_FEATHER.get(), new net.minecraft.world.phys.AABB(player.blockPosition()).inflate(100), e -> true).forEach(Entity::discard);
        level.getEntities(EntityType.PIG, box, e -> true).forEach(Entity::discard);
    }

    private static BlockPos chainWall;
    private static Entity chainedPig;

    /** A pig on a chain the player holds, a wall within reach and empty hands. */
    private static void chainSetup(ServerPlayer player) {
        ServerLevel level = level(player);
        BlockPos at = player.blockPosition();
        chainWall = at.offset(2, 0, 0);
        level.setBlock(chainWall, Blocks.COBBLESTONE_WALL.defaultBlockState(), 3);
        BlockPos pigAt = at.offset(-3, 0, 0);
        level.setBlock(pigAt.below(), Blocks.STONE.defaultBlockState(), 3);
        chainedPig = place(level, EntityType.PIG, pigAt, 0);
        // the first use of the pig's data, as with a player's first chain on a mob: the first tick of fresh data dropped
        // the chain (scene run 2026-11-08, all versions)
        com.github.alexthe666.iceandfire.entity.props.EntityDataProvider.getCapability(chainedPig).ifPresent(data -> data.chainData.attachChain(player));
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, ItemStack.EMPTY);
    }

    /** The click as Minecraft#startUseItem makes it: the main hand passes on a wall, then the off hand gets it. */
    private static void chainClick(Minecraft mc) {
        Entity pig = mc.level.getEntity(chainedPig.getId());
        boolean chained = pig != null && com.github.alexthe666.iceandfire.entity.props.EntityDataProvider.getCapability(pig)
                .map(data -> data.chainData.isChainedTo(mc.player)).orElse(false);
        check("chained pig synced to the client", chained, pig);
        net.minecraft.world.phys.BlockHitResult hit = new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(chainWall), net.minecraft.core.Direction.UP, chainWall, false);
        for (net.minecraft.world.InteractionHand hand : net.minecraft.world.InteractionHand.values()) {
            mc.gameMode.useItemOn(mc.player, hand, hit);
        }
    }

    private static void checkChain(ServerPlayer player) {
        ServerLevel level = level(player);
        List<Entity> chains = com.github.alexthe666.iceandfire.entity.props.EntityDataProvider.getCapability(chainedPig)
                .map(data -> data.chainData.getChainedTo()).orElse(List.of());
        check("chain tied to the wall on the server", chains.size() == 1 && chains.get(0) instanceof com.github.alexthe666.iceandfire.entity.EntityChainTie, chains);
        net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(chainWall).inflate(4);
        List<? extends Entity> knots = level.getEntities(IafEntityRegistry.CHAIN_TIE.get(), box, e -> true);
        check("one knot on the wall", knots.size() == 1, knots.size());
        knots.forEach(Entity::discard);
        chainedPig.discard();
    }

    private static int[] particleReference;
    private static int[] modParticleReference;

    private static BlockPos particleWallAt() {
        return stage.offset(0, 0, -40);
    }

    /** A black wall 13 x 9 at the far end of the stage, the camera 8 blocks in front of it. */
    private static void particleWall(ServerPlayer player) {
        ServerLevel level = player.level();
        for (int dx = -6; dx <= 6; dx++) {
            for (int dy = 0; dy < 9; dy++) {
                level.setBlock(particleWallAt().offset(dx, dy, 0), Blocks.COAL_BLOCK.defaultBlockState(), 2);
            }
        }
        hover(player);
        look(player, particleWallAt().offset(0, 4, 8), particleWallAt().offset(0, 4, 0));
    }

    private static void particleWallRemove(ServerLevel level) {
        for (int dx = -6; dx <= 6; dx++) {
            for (int dy = 0; dy < 9; dy++) {
                level.setBlock(particleWallAt().offset(dx, dy, 0), Blocks.AIR.defaultBlockState(), 2);
            }
        }
    }

    /** Reference frame at t0, vanilla particles for 15 ticks, then the check (16 ticks in all). */
    private static void particleCheck(int t0, String name) {
        at(t0, mc -> grabPixels(mc, pixels -> DevScene.particleReference = pixels));
        for (int t = t0 + 1; t < t0 + 16; t++) {
            at(t, DevScene::spawnParticles);
        }
        at(t0 + 16, mc -> {
            String count = mc.particleEngine.countParticles();
            int live = Integer.parseInt(count.substring(count.lastIndexOf(' ') + 1));
            grabPixels(mc, pixels -> {
                int changed = changedPixels(DevScene.particleReference, pixels, true);
                int wall = darkPixels(DevScene.particleReference);
                // only pixels dark in the reference count: without the wall in it (not drawn yet) there is nothing to compare
                // the wall fills most of the middle once the camera looks at it; part of it: still turning or not drawn yet
                if (wall < middlePixels(pixels) / 4) {
                    check(name + " (wall not drawn yet, see the later checks)", live >= 300, live + " live (" + count + "), wall " + wall + " pixels in the reference");
                } else {
                    check(name, live >= 300 && changed >= 300, live + " live (" + count + "), " + changed + " pixels changed in the middle, wall " + wall);
                }
            });
            shot(mc, "particles_" + t0);
        });
    }

    /** End rods and flames on a grid 2 blocks in front of the wall, straight into the engine (no particle option filter). */
    private static void spawnParticles(Minecraft mc) {
        BlockPos wall = particleWallAt();
        for (int i = -4; i <= 4; i++) {
            for (int j = 0; j < 5; j++) {
                double x = wall.getX() + 0.5 + i * 0.6;
                double y = wall.getY() + 2.5 + j * 0.6;
                double z = wall.getZ() + 2.5;
                mc.particleEngine.createParticle(j % 2 == 0 ? net.minecraft.core.particles.ParticleTypes.END_ROD : net.minecraft.core.particles.ParticleTypes.FLAME, x, y, z, 0, 0, 0);
            }
        }
    }

    /** One of each quad particle of the mod (not the appearances) at both sides of the wall, out of the middle of the frame. */
    private static void spawnModParticles(Minecraft mc) {
        BlockPos wall = particleWallAt();
        for (com.github.alexthe666.iceandfire.enums.EnumParticles type : com.github.alexthe666.iceandfire.enums.EnumParticles.values()) {
            if (type.name().endsWith("Appearance")) {
                continue;
            }
            for (int side = -1; side <= 1; side += 2) {
                IceAndFire.PROXY.spawnParticle(type, wall.getX() + 0.5 + side * 5.5, wall.getY() + 1 + type.ordinal() * 0.6, wall.getZ() + 1.5, 0, 0, 0, 1);
            }
        }
    }

    private static int particleWidth;

    /** The pixels of the next frame (ARGB), handed over when the GPU copy is read back. */
    private static void grabPixels(Minecraft mc, Consumer<int[]> sink) {
        Screenshot.takeScreenshot(mc.getMainRenderTarget(), image -> {
            try (image) {
                particleWidth = image.getWidth();
                sink.accept(image.getPixels());
            }
        });
    }

    /** Pixels of the middle third (across) or of the outer thirds, rows 15-85 %, dark in the reference frame (the wall) and at least 48 brighter in a channel now. */
    /** Pixels of the middle third, rows 15-85 % */
    private static int middlePixels(int[] frame) {
        int rows = frame.length / particleWidth;
        return (particleWidth * 2 / 3 - particleWidth / 3) * (rows * 85 / 100 - rows * 15 / 100);
    }

    /** Pixels of the middle third, rows 15-85 %, dark in the frame (the black wall) */
    private static int darkPixels(int[] frame) {
        if (frame == null) {
            return 0;
        }
        int dark = 0;
        int rows = frame.length / particleWidth;
        for (int i = 0; i < frame.length; i++) {
            int column = i % particleWidth;
            int row = i / particleWidth;
            if (column < particleWidth / 3 || column >= particleWidth * 2 / 3 || row < rows * 15 / 100 || row > rows * 85 / 100) {
                continue;
            }
            int a = frame[i];
            if ((a >> 16 & 255) < 48 && (a >> 8 & 255) < 48 && (a & 255) < 48) {
                dark++;
            }
        }
        return dark;
    }

    private static int changedPixels(int[] reference, int[] pixels, boolean middle) {
        if (reference == null || reference.length != pixels.length) {
            return -1;
        }
        int changed = 0;
        for (int i = 0; i < pixels.length; i++) {
            int column = i % particleWidth;
            int row = i / particleWidth;
            int rows = pixels.length / particleWidth;
            if ((column >= particleWidth / 3 && column < particleWidth * 2 / 3) != middle || row < rows * 15 / 100 || row > rows * 85 / 100) {
                continue;
            }
            int a = reference[i];
            int b = pixels[i];
            // only on the black wall: the landscape around it changes by itself (chunks, leaves, clouds)
            if ((a >> 16 & 255) >= 48 || (a >> 8 & 255) >= 48 || (a & 255) >= 48) {
                continue;
            }
            if (Math.abs((a >> 16 & 255) - (b >> 16 & 255)) >= 48 || Math.abs((a >> 8 & 255) - (b >> 8 & 255)) >= 48 || Math.abs((a & 255) - (b & 255)) >= 48) {
                changed++;
            }
        }
        return changed;
    }

    private static ServerLevel level(ServerPlayer player) {
        return player.level();
    }

    private static void closeUp(ServerPlayer player, EntityType<?> type) {
        ServerLevel level = player.level();
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
        setTime(level, BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath().equals("troll") ? 18000 : 6000);
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
                LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, false, Difficulty.NORMAL, true, new net.minecraft.world.level.gamerules.GameRules(WorldDataConfiguration.DEFAULT.enabledFeatures()), WorldDataConfiguration.DEFAULT);
                mc.createWorldOpenFlows().createFreshLevel(WORLD, settings, new WorldOptions(1234L, false, false), WorldPresets::createNormalWorldDimensions, new TitleScreen());
            }
            return;
        }
        if (mc.player == null || mc.getSingleplayerServer() == null) {
            return;
        }
        mc.options.pauseOnLostFocus = false;
        mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
        mc.getToastManager().clear();
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
        mc.getToastManager().clear();
        Screenshot.grab(mc.gameDirectory, "scene_" + name + ".png", mc.getMainRenderTarget(), 1, message -> IceAndFire.LOGGER.info("[scene] screenshot {}", name));
    }

    private static void selectTab(Minecraft mc, CreativeModeTab tab) {
        if (mc.screen instanceof CreativeModeInventoryScreen screen) {
            try {
                // Fabric puts the mods' tabs on further pages, and selectTab does not leave the current page: its
                // FabricCreativeModeInventoryScreen (on the screen through a mixin) switches the page and selects
                for (Class<?> api : screen.getClass().getInterfaces()) {
                    if (api.getName().equals("net.fabricmc.fabric.api.client.creativetab.v1.FabricCreativeModeInventoryScreen")) {
                        int page = (int) api.getMethod("getPage", CreativeModeTab.class).invoke(screen, tab);
                        api.getMethod("switchToPage", int.class).invoke(screen, page);
                        api.getMethod("setSelectedTab", CreativeModeTab.class).invoke(screen, tab);
                        return;
                    }
                }
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
        player.teleportTo(player.level(), eye.getX() + 0.5, eye.getY() - 1.62, eye.getZ() + 0.5, java.util.Set.of(), yaw, pitch, true);
    }

    private static void setup(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        player.setGameMode(GameType.CREATIVE);
        player.level().getGameRules().set(GameRules.SPAWN_MOBS, false, server);
        player.level().getGameRules().set(GameRules.ADVANCE_TIME, false, server);
        player.level().getGameRules().set(GameRules.ADVANCE_WEATHER, false, server);
        setTime(player.level(), 6000);
        player.level().getServer().getCommands().performPrefixedCommand(player.level().getServer().createCommandSourceStack().withSuppressedOutput(), "weather clear");
        // The other mods of the run (tools/scenetest.sh -Pwith_compat)
        List<String> mods = new ArrayList<>();
        mods.addAll(OTHER_MODS.get());
        IceAndFire.LOGGER.info("[scene] other mods: {}", mods);
    }

    /** Flat grass stage 90 x 90 at the spawn. */
    private static void buildStage(ServerPlayer player) {
        ServerLevel level = player.level();
        BlockPos spawn = level.getRespawnData().pos();
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
        Entity entity = type.create(level, EntitySpawnReason.COMMAND);
        entity.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0);
        if (entity instanceof Mob mob) {
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.COMMAND, null);
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
        ServerLevel level = player.level();
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
        ServerLevel level = player.level();
        int x = -36;
        int z = -6;
        int count = 0;
        List<String> skipped = new ArrayList<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (!id.getNamespace().equals(IceAndFire.MODID) || id.getPath().contains("dragon") || id.getPath().contains("multipart")) {
                continue;
            }
            Entity probe = type.create(level, EntitySpawnReason.COMMAND);
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

    private static BlockPos statueField() {
        return stage.offset(0, 0, -30);
    }

    /** A statue of every living entity type (the player's own too) in rows, the creatures themselves removed. */
    private static void statues(ServerPlayer player) {
        ServerLevel level = player.level();
        level.getEntities((Entity) null, new net.minecraft.world.phys.AABB(stage).inflate(64, 30, 64), e -> !(e instanceof ServerPlayer)).forEach(Entity::discard);
        setTime(level, 6000);
        List<net.minecraft.world.entity.LivingEntity> sources = new ArrayList<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (type == IafEntityRegistry.STONE_STATUE.get() || type == EntityType.ENDER_DRAGON) {
                continue;
            }
            if (type.create(level, EntitySpawnReason.COMMAND) instanceof net.minecraft.world.entity.LivingEntity living) {
                if (living instanceof Mob mob) {
                    mob.finalizeSpawn(level, level.getCurrentDifficultyAt(statueField()), EntitySpawnReason.COMMAND, null);
                }
                sources.add(living);
            }
        }
        sources.add(player);
        int x = -40;
        int z = 0;
        int count = 0;
        for (net.minecraft.world.entity.LivingEntity source : sources) {
            BlockPos pos = statueField().offset(x, 0, z);
            source.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 180, 0);
            com.github.alexthe666.iceandfire.entity.EntityStoneStatue statue = com.github.alexthe666.iceandfire.entity.EntityStoneStatue.buildStatueEntity(source);
            statue.absSnapTo(source.getX(), source.getY(), source.getZ(), 180, 0);
            statue.yBodyRot = 180;
            statue.yHeadRot = 180;
            if (level.addFreshEntity(statue)) {
                count++;
            }
            x += 4;
            if (x > 40) {
                x = -40;
                z -= 5;
            }
        }
        check("statues placed", count == sources.size(), count + " of " + sources.size());
        hover(player);
        look(player, statueField().offset(0, 14, 16), statueField().offset(0, 0, -12));
    }

    /** Flying, so that the camera stays where look() put it. */
    private static void hover(ServerPlayer player) {
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
    }

    private static void checkStatues(Minecraft mc, String name, int min, double radius) {
        int statues = 0;
        BlockPos center = statueField();
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof com.github.alexthe666.iceandfire.entity.EntityStoneStatue && Math.abs(e.getX() - center.getX()) <= radius && Math.abs(e.getZ() - center.getZ()) <= radius) {
                statues++;
            }
        }
        check(name + " on the client", statues >= min, statues);
    }

    /** A gorgon with its AI among a few mobs it turns to stone (blind mobs and the player in creative are not targets). */
    private static void gorgonVictims(ServerPlayer player) {
        ServerLevel level = player.level();
        level.getEntities((Entity) null, new net.minecraft.world.phys.AABB(stage).inflate(64, 30, 64), e -> !(e instanceof ServerPlayer)).forEach(Entity::discard);
        BlockPos center = statueField();
        // At night: the gorgon flees the sun and the undead burn
        setTime(level, 18000);
        // A glass pen keeps them in front of the camera (and in the checked square)
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                if (Math.abs(dx) == 7 || Math.abs(dz) == 7) {
                    level.setBlock(center.offset(dx, 0, dz), Blocks.GLASS.defaultBlockState(), 2);
                    level.setBlock(center.offset(dx, 1, dz), Blocks.GLASS.defaultBlockState(), 2);
                }
            }
        }
        Entity gorgon = place(level, IafEntityRegistry.GORGON.get(), center, 0);
        int i = 0;
        Entity first = null;
        for (EntityType<?> type : List.of(EntityType.ZOMBIE, EntityType.VILLAGER, EntityType.PIG, EntityType.SKELETON, EntityType.COW, EntityType.SHEEP)) {
            double angle = Math.PI * 2 * i++ / 6;
            Entity victim = place(level, type, center.offset((int) Math.round(Math.sin(angle) * 5), 0, (int) Math.round(Math.cos(angle) * 5)), 0);
            if (victim instanceof Mob mob) {
                mob.setNoAi(false);
            }
            first = first == null ? victim : first;
        }
        if (gorgon instanceof Mob mob) {
            mob.setNoAi(false);
            mob.setTarget((net.minecraft.world.entity.LivingEntity) first);
        }
        hover(player);
        look(player, center.offset(0, 8, 12), center);
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

    /** 26.x: the level time is a clock (1.21.1: setDayTime); the command sets it */
    private static void setTime(ServerLevel level, int time) {
        level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack().withSuppressedOutput(), "time set " + time);
    }
}
