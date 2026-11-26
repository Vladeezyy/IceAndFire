package com.github.alexthe666.iceandfire.selftest;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * SCENE_ONLY=thaumaturge tools/scenetest.sh -Pwith_thaumaturge (NeoForge): the tooltips of Ice and Fire items with their
 * Thaumaturge aspects (the scene script turns on Thaumaturge's "aspects on tooltips without Shift"), after discovering
 * every aspect, so Draco and Fulgur are drawn and not masked. Checks the client's aspect index (sent by the server)
 * and the translations of the two aspects. Since 1.21.6 a screen draws one tooltip a frame (setTooltipForNextFrame),
 * so one screenshot per item.
 */
final class ThaumaturgeScene {
    /** drops and world blocks with aspects of their own, and blocks crafted from scales (aspects from the recipe) */
    private static final List<String> ITEMS = List.of("dragonscales_red", "ice_dragon_heart", "dragon_skull_fire",
        "dragonscale_electric", "crackled_grass", "dragonsteel_lightning_ingot", "gorgon_head", "troll_tusk", "sea_serpent_fang");
    private static int shown;

    private ThaumaturgeScene() {
    }

    /** Adds the steps from tick {@code t}; returns the tick after the last one */
    static int steps(int t) {
        DevScene.at(t, mc -> DevScene.server(mc, p -> {
            var server = p.level().getServer();
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), "tt aspects " + p.getScoreboardName() + " discover all");
        }));
        DevScene.at(t += 20, mc -> {
            mc.options.hideGui = false;
            mc.options.guiScale().set(2);
            mc.resizeGui();
            mc.setScreen(new Tooltips());
        });
        DevScene.at(t += 20, mc -> {
            Set<String> scales = aspects(stack("dragonscales_red"));
            DevScene.check("client aspect index gives red dragon scales Draco and Ignis", scales.contains("iceandfire:draco") && scales.contains("thaumaturge:ignis"), scales);
            Set<String> ingot = aspects(stack("dragonsteel_lightning_ingot"));
            DevScene.check("client aspect index gives lightning dragonsteel Fulgur", ingot.contains("iceandfire:fulgur"), ingot);
            Set<String> armor = aspects(stack("armor_electric_chestplate"));
            DevScene.check("armor crafted from electric scales gets Draco and Fulgur from its recipe", armor.contains("iceandfire:draco") && armor.contains("iceandfire:fulgur"), armor);
            DevScene.check("Draco and Fulgur are translated", I18n.exists("aspect.iceandfire.draco.desc") && I18n.exists("aspect.iceandfire.fulgur.desc"), mc.options.languageCode);
        });
        for (int i = 0; i < ITEMS.size(); i++) {
            int item = i;
            DevScene.at(t += 5, mc -> shown = item);
            DevScene.at(t += 5, mc -> DevScene.shot(mc, "thaumaturge_" + mc.options.languageCode + "_" + item + "_" + ITEMS.get(item)));
        }
        DevScene.at(t += 10, mc -> mc.setScreen(null));
        return t + 5;
    }

    private static ItemStack stack(String id) {
        return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(IceAndFire.MODID, id)));
    }

    /** Thaumaturge's AspectIndexAccess.of(stack).aspects() by reflection (no compile dependency on it) */
    @SuppressWarnings("unchecked")
    private static Set<String> aspects(ItemStack stack) {
        Set<String> ids = new TreeSet<>();
        try {
            Object list = Class.forName("com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess").getMethod("of", ItemStack.class).invoke(null, stack);
            for (Holder<?> holder : (Set<Holder<?>>) list.getClass().getMethod("aspects").invoke(list)) {
                holder.unwrapKey().ifPresent(key -> ids.add(key.identifier().toString()));
            }
        } catch (ReflectiveOperationException e) {
            ids.add("error: " + e);
        }
        return ids;
    }

    /** One item tooltip, and the two aspects' names and descriptions from the language file */
    private static final class Tooltips extends Screen {
        Tooltips() {
            super(Component.literal("Thaumaturge"));
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            super.extractRenderState(graphics, mouseX, mouseY, partialTick);
            for (int i = 0; i < 2; i++) {
                String key = "aspect.iceandfire." + (i == 0 ? "draco" : "fulgur");
                graphics.text(font, Component.translatable(key).append(": ").append(Component.translatable(key + ".desc"))
                    .append(" (").append(Component.translatable(key + ".help")).append(")"), 8, 6 + i * 11, 0xFFFFFFFF, true);
            }
            graphics.setTooltipForNextFrame(font, stack(ITEMS.get(shown)), 8, 44);
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }
}
