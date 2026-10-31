package dev.vladeezyy.iceandfire.neoforge;

import dev.vladeezyy.iceandfire.platform.ClientPlatform;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.neoforge.client.ClientHooks;

/** The client hooks of NeoForge the original render code called (26.x: armor goes through the item client extensions) */
public final class NeoForgeClientPlatform implements ClientPlatform {
    @Override
    public void renderMainMenu(Screen screen, GuiGraphicsExtractor graphics, Font font, int width, int height, int alpha) {
        if (screen instanceof TitleScreen title) {
            ClientHooks.renderMainMenu(title, graphics, font, width, height, alpha);
        }
    }

    @Override
    public void registerKeyMapping(net.minecraft.client.KeyMapping mapping) {
        // as the original: straight into the options, which exist when the mod is constructed
        var options = net.minecraft.client.Minecraft.getInstance().options;
        options.keyMappings = org.apache.commons.lang3.ArrayUtils.add(options.keyMappings, mapping);
    }
}
