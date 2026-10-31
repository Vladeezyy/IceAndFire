package com.github.alexthe666.iceandfire.compat.jei.icedragonforge;

import net.minecraft.client.renderer.RenderPipelines;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class IceDragonForgeDrawable implements IDrawable {
    private static final Identifier TEXTURE = Identifier.parse("iceandfire:textures/gui/dragonforge_ice.png");

    @Override
    public int getWidth() {
        return 176;
    }

    @Override
    public int getHeight() {
        return 120;
    }

    @Override
    public void draw(@NotNull GuiGraphicsExtractor ms, int xOffset, int yOffset) {
        this.drawTexturedModalRect(ms, xOffset, yOffset, 3, 4, 170, 79);
        int scaledProgress = (Minecraft.getInstance().player.tickCount % 100) * 128 / 100;
        this.drawTexturedModalRect(ms, xOffset + 9, yOffset + 19, 0, 166, scaledProgress, 38);
    }

    public void drawTexturedModalRect(GuiGraphicsExtractor ms, int x, int y, int textureX, int textureY, int width, int height) {
        ms.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, textureX, textureY, width, height, 256, 256);
    }
}