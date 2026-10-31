package com.github.alexthe666.iceandfire.client.gui;
import net.minecraft.client.renderer.RenderPipelines;

import org.joml.Vector3f;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.client.StatCollector;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.inventory.ContainerDragon;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

public class GuiDragon extends AbstractContainerScreen<ContainerDragon> {
    private static final Identifier texture = Identifier.parse("iceandfire:textures/gui/dragon.png");

    public GuiDragon(ContainerDragon dragonInv, Inventory playerInv, Component name) {
        super(dragonInv, playerInv, name);
        this.imageWidth = 176;
        this.imageHeight = 214;
    }

    @Override
    protected void renderLabels(GuiGraphics matrixStack, int mouseX, int mouseY) {

    }

    @Override
    public void render(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics matrixStack, float partialTicks, int mouseX, int mouseY) {
        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        matrixStack.blit(RenderPipelines.GUI_TEXTURED, texture, k, l, (float) (0), (float) (0), this.imageWidth, this.imageHeight, 256, 256);
        Entity entity = IceAndFire.PROXY.getReferencedMob();
        if (entity instanceof EntityDragonBase) {
            EntityDragonBase dragon = (EntityDragonBase) entity;
            float dragonScale = 1F / Math.max(0.0001F, dragon.getIafScale());
            Quaternionf quaternionf = (new Quaternionf()).rotateY((float) Mth.lerp((float) mouseX / this.width, 0, Math.PI)).rotateZ((float) Mth.lerp((float) mouseY / this.width, Math.PI, Math.PI + 0.2));
            GuiEntityRenderUtil.renderEntityInInventory(matrixStack, k + 88, l + (int) (0.5F * (dragon.flyProgress)) + 55, (int) (dragonScale * 23F), new Vector3f(), quaternionf, null, dragon);
        }
        if (entity instanceof EntityDragonBase) {
            EntityDragonBase dragon = (EntityDragonBase) entity;

            Font font = this.minecraft.font;
            String s3 = dragon.getCustomName() == null ? StatCollector.translateToLocal("dragon.unnamed") : StatCollector.translateToLocal("dragon.name") + " " + dragon.getCustomName().getString();
            matrixStack.drawString(font, s3, (int) (k + this.imageWidth / 2 - font.width(s3) / 2), (int) (l + 75), net.minecraft.util.ARGB.opaque(0XFFFFFF), false);
            String s2 = StatCollector.translateToLocal("dragon.health") + " " + Math.floor(Math.min(dragon.getHealth(), dragon.getMaxHealth())) + " / " + dragon.getMaxHealth();
            matrixStack.drawString(font, s2, (int) (k + this.imageWidth / 2 - font.width(s2) / 2), (int) (l + 84), net.minecraft.util.ARGB.opaque(0XFFFFFF), false);
            String s5 = StatCollector.translateToLocal("dragon.gender") + " " + StatCollector.translateToLocal((dragon.isMale() ? "dragon.gender.male" : "dragon.gender.female"));
            matrixStack.drawString(font, s5, (int) (k + this.imageWidth / 2 - font.width(s5) / 2), (int) (l + 93), net.minecraft.util.ARGB.opaque(0XFFFFFF), false);
            String s6 = StatCollector.translateToLocal("dragon.hunger") + " " + dragon.getHunger() + "/100";
            matrixStack.drawString(font, s6, (int) (k + this.imageWidth / 2 - font.width(s6) / 2), (int) (l + 102), net.minecraft.util.ARGB.opaque(0XFFFFFF), false);
            String s4 = StatCollector.translateToLocal("dragon.stage") + " " + dragon.getDragonStage() + " " + StatCollector.translateToLocal("dragon.days.front") + dragon.getAgeInDays() + " " + StatCollector.translateToLocal("dragon.days.back");
            matrixStack.drawString(font, s4, (int) (k + this.imageWidth / 2 - font.width(s4) / 2), (int) (l + 111), net.minecraft.util.ARGB.opaque(0XFFFFFF), false);
            String s7 = dragon.getOwner() != null ? StatCollector.translateToLocal("dragon.owner") + " " + dragon.getOwner().getName().getString() : StatCollector.translateToLocal("dragon.untamed");
            matrixStack.drawString(font, s7, (int) (k + this.imageWidth / 2 - font.width(s7) / 2), (int) (l + 120), net.minecraft.util.ARGB.opaque(0XFFFFFF), false);
        }
    }


}