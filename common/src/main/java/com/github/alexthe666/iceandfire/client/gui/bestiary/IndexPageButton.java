package com.github.alexthe666.iceandfire.client.gui.bestiary;
import net.minecraft.client.renderer.RenderPipelines;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class IndexPageButton extends Button {

    public IndexPageButton(int x, int y, Component buttonText,
                           net.minecraft.client.gui.components.Button.OnPress butn) {
        super(x, y, 160, 32, buttonText, butn, DEFAULT_NARRATION);
        this.width = 160;
        this.height = 32;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor pGuiGraphics, int mouseX, int mouseY, float partial) {
        if (this.active) {
            pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
                                    Font font = IafConfig.useVanillaFont ? Minecraft.getInstance().font : (Font) IceAndFire.PROXY.getFontRenderer();
            boolean flag = isHoveredOrFocused();
            pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse("iceandfire:textures/gui/bestiary/widgets.png"), this.getX(), this.getY(), (float) (0), (float) (flag ? 32 : 0), this.width, this.height, 256, 256);
            pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            // Dark ink without a shadow, like the text of the pages: the white button text of the original could not be
            // read on the light parchment; the hovered entry turns the red-brown of the book's spine
            int color = (flag ? 0x7A2410 : 0x303030) | Mth.ceil(this.alpha * 255.0F) << 24;
            Component text = this.getMessage();
            int textWidth = font.width(text);
            pGuiGraphics.text(font, text, this.getX() + (this.width - textWidth) / 2, this.getY() + (this.height - 8) / 2, net.minecraft.util.ARGB.opaque(color), false);
        }
    }
}
