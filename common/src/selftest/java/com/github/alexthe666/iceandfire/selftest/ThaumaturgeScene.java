package com.github.alexthe666.iceandfire.selftest;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * SCENE_ONLY=thaumaturge tools/scenetest.sh -Pwith_thaumaturge (NeoForge): the tooltips of Ice and Fire items with their
 * Thaumaturge aspects (the scene script turns on Thaumaturge's "aspects on tooltips without Shift"), after discovering
 * every aspect, so Draco and Fulgur are drawn and not masked. Checks the client's aspect index (sent by the server)
 * and the translations of the two aspects.
 */
final class ThaumaturgeScene {
    /** drops and world blocks with aspects of their own, and blocks crafted from scales (aspects from the recipe) */
    private static final List<String> ITEMS = List.of("dragonscales_red", "ice_dragon_heart", "dragon_skull_fire",
        "dragonscale_electric", "crackled_grass", "dragonsteel_lightning_ingot", "gorgon_head", "troll_tusk", "sea_serpent_fang");

    private ThaumaturgeScene() {
    }

    /** Adds the steps from tick {@code t}; returns the tick after the last one */
    static int steps(int t) {
        DevScene.at(t, mc -> DevScene.server(mc, p -> {
            var source = p.createCommandSourceStack().withPermission(4).withSuppressedOutput();
            p.getServer().getCommands().performPrefixedCommand(source, "tt aspects " + p.getGameProfile().getName() + " discover all");
        }));
        DevScene.at(t += 20, mc -> {
            mc.options.hideGui = false;
            mc.options.guiScale().set(2);
            mc.resizeDisplay();
            mc.setScreen(new Tooltips());
        });
        DevScene.at(t += 20, mc -> {
            Set<String> scales = aspects(stack("dragonscales_red"));
            DevScene.check("client aspect index gives red dragon scales Draco and Ignis", scales.contains("iceandfire:draco") && scales.contains("thaumaturge:ignis"), scales);
            Set<String> ingot = aspects(stack("dragonsteel_lightning_ingot"));
            DevScene.check("client aspect index gives lightning dragonsteel Fulgur", ingot.contains("iceandfire:fulgur"), ingot);
            Set<String> armor = aspects(stack("armor_electric_chestplate"));
            DevScene.check("armor crafted from electric scales gets Draco and Fulgur from its recipe", armor.contains("iceandfire:draco") && armor.contains("iceandfire:fulgur"), armor);
            String lang = mc.options.languageCode;
            DevScene.check("Draco and Fulgur are translated", I18n.exists("aspect.iceandfire.draco.desc") && I18n.exists("aspect.iceandfire.fulgur.desc"), lang);
            DevScene.shot(mc, "thaumaturge_" + lang);
        });
        DevScene.at(t += 10, mc -> mc.setScreen(null));
        return t + 5;
    }

    private static ItemStack stack(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(IceAndFire.MODID, id)));
    }

    /** Thaumaturge's AspectIndexAccess.of(stack).aspects() by reflection (no compile dependency on it) */
    @SuppressWarnings("unchecked")
    private static Set<String> aspects(ItemStack stack) {
        Set<String> ids = new TreeSet<>();
        try {
            Object list = Class.forName("com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess").getMethod("of", ItemStack.class).invoke(null, stack);
            for (Holder<?> holder : (Set<Holder<?>>) list.getClass().getMethod("aspects").invoke(list)) {
                holder.unwrapKey().ifPresent(key -> ids.add(key.location().toString()));
            }
        } catch (ReflectiveOperationException e) {
            ids.add("error: " + e);
        }
        return ids;
    }

    /** Nine item tooltips in three rows, and the two aspects' names and descriptions from the language file */
    private static final class Tooltips extends Screen {
        Tooltips() {
            super(Component.literal("Thaumaturge"));
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.render(graphics, mouseX, mouseY, partialTick);
            for (int i = 0; i < 2; i++) {
                String key = "aspect.iceandfire." + (i == 0 ? "draco" : "fulgur");
                graphics.drawString(font, Component.translatable(key).append(": ").append(Component.translatable(key + ".desc"))
                    .append(" (").append(Component.translatable(key + ".help")).append(")"), 8, 6 + i * 11, 0xFFFFFF);
            }
            int columns = 3;
            int cell = width / columns;
            for (int i = 0; i < ITEMS.size(); i++) {
                // a tooltip is drawn 12 px right of and above the given point
                graphics.renderTooltip(font, stack(ITEMS.get(i)), (i % columns) * cell - 8, 44 + (i / columns) * 66);
            }
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }
}
