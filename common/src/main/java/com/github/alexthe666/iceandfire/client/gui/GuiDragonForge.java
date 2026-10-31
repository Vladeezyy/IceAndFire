package com.github.alexthe666.iceandfire.client.gui;
import net.minecraft.client.renderer.RenderPipelines;

import net.minecraft.world.item.crafting.RecipeHolder;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.DragonType;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityDragonforge;
import com.github.alexthe666.iceandfire.inventory.ContainerDragonForge;
import com.github.alexthe666.iceandfire.recipe.DragonForgeRecipe;
import com.github.alexthe666.iceandfire.recipe.IafRecipeRegistry;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.stream.Collectors;

public class GuiDragonForge extends AbstractContainerScreen<ContainerDragonForge> {
    private static final Identifier TEXTURE_FIRE = Identifier.parse("iceandfire:textures/gui/dragonforge_fire.png");
    private static final Identifier TEXTURE_ICE = Identifier.parse("iceandfire:textures/gui/dragonforge_ice.png");
    private static final Identifier TEXTURE_LIGHTNING = Identifier.parse("iceandfire:textures/gui/dragonforge_lightning.png");
    private final ContainerDragonForge tileFurnace;
    private final int dragonType;

    public GuiDragonForge(ContainerDragonForge container, Inventory inv, Component name) {
        super(container, inv, name);
        this.tileFurnace = container;
        this.dragonType = tileFurnace.fireType;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor pGuiGraphics, int mouseX, int mouseY) {
        Font font = this.minecraft.font;
        if (tileFurnace != null) {
            String s = I18n.get("block.iceandfire.dragonforge_" + DragonType.getNameFromInt(dragonType) + "_core");
            // A long name (ru: "Сердце огненной драконьей кузни") is squeezed into the frame
            int room = this.imageWidth - 12;
            int width = font.width(s);
            pGuiGraphics.pose().pushMatrix();
            pGuiGraphics.pose().translate((float) (this.imageWidth / 2F), (float) (6));
            if (width > room) {
                pGuiGraphics.pose().scale((float) ((float) room / width), (float) (1F));
            }
            pGuiGraphics.text(this.font, s, -width / 2, 0, net.minecraft.util.ARGB.opaque(4210752), false);
            pGuiGraphics.pose().popMatrix();
        }
        pGuiGraphics.text(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, net.minecraft.util.ARGB.opaque(4210752), false);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.extractBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        Identifier texture = TEXTURE_FIRE;
        if (dragonType == 0) {
            texture = TEXTURE_FIRE;
        } else if (dragonType == 1) {
            texture = TEXTURE_ICE;
        } else {
            texture = TEXTURE_LIGHTNING;
        }

        int k = (this.width - this.imageWidth) / 2;
        int l = (this.height - this.imageHeight) / 2;
        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, k, l, (float) (0), (float) (0), this.imageWidth, this.imageHeight, 256, 256);
        int i1 = this.getCookTime(126);
        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, k + 12, l + 23, (float) (0), (float) (166), i1, 38, 256, 256);
    }

    private int getCookTime(int p_175381_1_) {
        BlockEntity te = IceAndFire.PROXY.getRefrencedTE();
        int j = 0;

        // 26.x: the client has the recipes the server synced (ClientRecipes)
        List<DragonForgeRecipe> recipes = dev.vladeezyy.iceandfire.platform.ClientRecipes.dragonForge()
                .stream().filter(item ->
                        item.isValidInput(tileFurnace.getSlot(0).getItem()) && item.isValidBlood(tileFurnace.getSlot(1).getItem())).collect(Collectors.toList());
        int maxCookTime = recipes.isEmpty() ? 100 : recipes.get(0).getCookTime();
        if (te instanceof TileEntityDragonforge) {
            j = Math.min(((TileEntityDragonforge) te).cookTime, maxCookTime);
        }
        return j != 0 ? j * p_175381_1_ / maxCookTime : 0;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor pGuiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.extractTooltip(pGuiGraphics, mouseX, mouseY);
    }

}