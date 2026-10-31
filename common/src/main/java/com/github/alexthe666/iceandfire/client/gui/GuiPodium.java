package com.github.alexthe666.iceandfire.client.gui;
import net.minecraft.client.renderer.RenderPipelines;

import com.github.alexthe666.iceandfire.inventory.ContainerPodium;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class GuiPodium extends AbstractContainerScreen<ContainerPodium> {

    public static final Identifier PODUIM_TEXTURE = Identifier.parse("iceandfire:textures/gui/podium.png");

    public GuiPodium(ContainerPodium container, Inventory inv, Component name) {
        super(container, inv, name);
        this.imageHeight = 133;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor pGuiGraphics, int x, int y) {
        if (menu != null) {
            String s = I18n.get("block.iceandfire.podium");
            pGuiGraphics.text(this.font, s, this.imageWidth / 2 - this.minecraft.font.width(s) / 2, 6, net.minecraft.util.ARGB.opaque(4210752), false);
        }
        pGuiGraphics.text(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, net.minecraft.util.ARGB.opaque(4210752), false);
    }


    @Override
    public void extractRenderState(GuiGraphicsExtractor pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.extractTooltip(pGuiGraphics, mouseX, mouseY);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor pGuiGraphics, int x, int y, float partialTicks) {
        super.extractBackground(pGuiGraphics, x, y, partialTicks);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, PODUIM_TEXTURE, i, j, (float) (0), (float) (0), this.imageWidth, this.imageHeight, 256, 256);
    }

}