package com.github.alexthe666.iceandfire.client.gui;
import net.minecraft.client.renderer.RenderPipelines;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.inventory.ContainerHippogryph;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;

//TODO: We do the same thing here as we do for the other GUI entity screens, that's dumb
public class GuiHippogryph extends AbstractContainerScreen<ContainerHippogryph> {
    private static final Identifier TEXTURE = Identifier.parse("iceandfire:textures/gui/hippogryph.png");
    private float mousePosx;
    private float mousePosY;

    public GuiHippogryph(ContainerHippogryph dragonInv, Inventory playerInv, Component name) {
        super(dragonInv, playerInv, name);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor pGuiGraphics, int mouseX, int mouseY) {
        Entity entity = IceAndFire.PROXY.getReferencedMob();
        Font font = this.minecraft.font;
        if (entity instanceof EntityHippogryph) {
            EntityHippogryph hippo = (EntityHippogryph) entity;
            pGuiGraphics.text(font, hippo.getDisplayName().getString(), 8, 6, net.minecraft.util.ARGB.opaque(4210752), false);
        }
        pGuiGraphics.text(font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, net.minecraft.util.ARGB.opaque(4210752), false);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.mousePosx = mouseX;
        this.mousePosY = mouseY;
        super.extractRenderState(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.extractTooltip(pGuiGraphics, mouseX, mouseY);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(pGuiGraphics, mouseX, mouseY, partialTicks);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, (float) (0), (float) (0), this.imageWidth, this.imageHeight, 256, 256);
        Entity entity = IceAndFire.PROXY.getReferencedMob();
        if (entity instanceof EntityHippogryph) {
            EntityHippogryph hippo = (EntityHippogryph) entity;
            if (hippo.isChested()) {
                pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i + 79, j + 17, (float) (0), (float) (this.imageHeight), 5 * 18, 54, 256, 256);
            }
            GuiEntityRenderUtil.renderEntityFollowsMouse(pGuiGraphics,i + 51, j + 60, 17, i + 51 - this.mousePosx, j + 75 - 50 - this.mousePosY,
                hippo);
        }
    }

}