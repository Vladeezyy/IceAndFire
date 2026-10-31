package com.github.alexthe666.iceandfire.client.gui;
import net.minecraft.client.renderer.RenderPipelines;

import net.minecraft.client.Minecraft;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityLectern;
import com.github.alexthe666.iceandfire.enums.EnumBestiaryPages;
import com.github.alexthe666.iceandfire.inventory.ContainerLectern;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.GameRenderer;
import com.github.alexthe666.iceandfire.client.render.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Random;

public class GuiLectern extends AbstractContainerScreen<ContainerLectern> {
    private static final Identifier ENCHANTMENT_TABLE_GUI_TEXTURE = Identifier.parse("iceandfire:textures/gui/lectern.png");
    private static final Identifier ENCHANTMENT_TABLE_BOOK_TEXTURE = Identifier.parse("iceandfire:textures/models/lectern_book.png");
    private static BookModel bookModel;
    private final Random random = new Random();
    private final Component nameable;
    public int ticks;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    private ItemStack last = ItemStack.EMPTY;
    private int flapTimer = 0;

    public GuiLectern(ContainerLectern container, Inventory inv, Component name) {
        super(container, inv, name);
        this.nameable = name;
    }

    @Override
    protected void init() {
        super.init();
        bookModel = new BookModel(this.minecraft.getEntityModels().bakeLayer(ModelLayers.BOOK));
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor ms, int mouseX, int mouseY) {
        Font font = this.minecraft.font;
        ms.text(font, this.nameable.getString(), (int) (12), (int) (4), net.minecraft.util.ARGB.opaque(4210752), false);
        ms.text(font, this.playerInventoryTitle, (int) (8), (int) (this.imageHeight - 96 + 2), net.minecraft.util.ARGB.opaque(4210752), false);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.menu.onUpdate();
        this.tickBook();
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent iafEvent, boolean iafDoubleClick) {
        double mouseX = iafEvent.x();
        double mouseY = iafEvent.y();
        int mouseButton = iafEvent.button();
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        for (int k = 0; k < 3; ++k) {
            double l = mouseX - (i + 60);
            double i1 = mouseY - (j + 14 + 19 * k);

            if (l >= 0 && i1 >= 0 && l < 108 && i1 < 19 && this.menu.clickMenuButton(this.minecraft.player, k)) {
                flapTimer = 5;
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, k);
                return true;
            }
        }
        return super.mouseClicked(iafEvent, iafDoubleClick);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor ms, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(ms, mouseX, mouseY, partialTicks);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        ms.blit(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_TABLE_GUI_TEXTURE, i, j, (float) (0), (float) (0), this.imageWidth, this.imageHeight, 256, 256);
        // 26.x: the book as the enchanting table draws it (1.21.1 set up its own projection and drew the model)
        float open = Mth.lerp(partialTicks, this.oOpen, this.open);
        float flip = Mth.lerp(partialTicks, this.oFlip, this.flip);
        ms.book(this.bookModel, ENCHANTMENT_TABLE_BOOK_TEXTURE, 40.0F, open, flip, i + 14, j + 14, i + 14 + 38, j + 14 + 31);
        this.menu.getManuscriptAmount();

        for (int i1 = 0; i1 < 3; ++i1) {
            int j1 = i + 60;
            int k1 = j1 + 20;
            int l1 = this.menu.getPossiblePages()[i1] == null ? -1 : this.menu.getPossiblePages()[i1].ordinal();//enchantment level
            if (l1 == -1) {
                ms.blit(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_TABLE_GUI_TEXTURE, j1, j + 14 + 19 * i1, (float) (0), (float) (185), 108, 19, 256, 256);
            } else {
                String s = "" + 3;
                Font fontrenderer = this.minecraft.font;
                String s1 = "";
                float textScale = 1.0F;
                EnumBestiaryPages enchantment = this.menu.getPossiblePages()[i1];
                if (enchantment != null) {
                    s1 = I18n.get("bestiary." + enchantment.toString().toLowerCase());//EnchantmentNameParts.getInstance().generateNewRandomName(this.fontRenderer, l1);
                    if (fontrenderer.width(s1) > 80) {
                        textScale = 1.0F - (fontrenderer.width(s1) - 80) * 0.01F;
                    }
                }
                int j2 = 6839882;
                if (IceAndFire.PROXY.getRefrencedTE() instanceof TileEntityLectern) {
                    IceAndFire.PROXY.getRefrencedTE();
                    if (menu.getSlot(0).getItem().getItem() == IafItemRegistry.BESTIARY.get()) { // Forge: render buttons as disabled when enchantable but enchantability not met on lower levels
                        int k2 = mouseX - (i + 60);
                        int l2 = mouseY - (j + 14 + 19 * i1);
                        int j3 = 0X9F988C;
                        if (k2 >= 0 && l2 >= 0 && k2 < 108 && l2 < 19) {
                            ms.blit(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_TABLE_GUI_TEXTURE, j1, j + 14 + 19 * i1, (float) (0), (float) (204), 108, 19, 256, 256);
                            j2 = 16777088;
                            j3 = 16777088;
                        } else {
                            ms.blit(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_TABLE_GUI_TEXTURE, j1, j + 14 + 19 * i1, (float) (0), (float) (166), 108, 19, 256, 256);
                        }

                        ms.blit(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_TABLE_GUI_TEXTURE, j1 + 1, j + 15 + 19 * i1, (float) (16 * i1), (float) (223), 16, 16, 256, 256);
                        ms.pose().pushMatrix();
                        ms.pose().translate((float) (width / 2F - 10), (float) (height / 2F - 83 + (1.0F - textScale) * 55));
                        ms.pose().scale((float) (textScale), (float) (textScale));
                        ms.text(fontrenderer, s1, (int) (0), (int) (20 + 19 * i1), net.minecraft.util.ARGB.opaque(j2), false);
                        ms.pose().popMatrix();
                        fontrenderer = this.minecraft.font;
                        ms.text(fontrenderer, s, (int) (k1 + 84 - fontrenderer.width(s)), (int) (j + 13 + 19 * i1 + 7), net.minecraft.util.ARGB.opaque(j3), true);
                    } else {
                        ms.blit(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_TABLE_GUI_TEXTURE, j1, j + 14 + 19 * i1, (float) (0), (float) (185), 108, 19, 256, 256);
                        ms.blit(RenderPipelines.GUI_TEXTURED, ENCHANTMENT_TABLE_GUI_TEXTURE, j1 + 1, j + 15 + 19 * i1, (float) (16 * i1), (float) (239), 16, 16, 256, 256);
                    }
                }
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ms, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(ms, mouseX, mouseY, partialTicks);
        this.extractTooltip(ms, mouseX, mouseY);
        boolean flag = this.minecraft.player.isCreative();
        int i = this.menu.getManuscriptAmount();

        for (int j = 0; j < 3; ++j) {
            int k = 1;
            EnumBestiaryPages enchantment = this.menu.getPossiblePages()[j];
            int i1 = 3;

            if (this.isHovering(60, 14 + 19 * j, 108, 17, mouseX, mouseY) && k > 0) {
                List<FormattedCharSequence> list = Lists.newArrayList();

                if (enchantment == null) {
                    list.add(Component.literal(ChatFormatting.RED + I18n.get("container.lectern.no_bestiary")).getVisualOrderText());
                } else if (!flag) {
                    list.add(Component.literal("" + ChatFormatting.WHITE + ChatFormatting.ITALIC + I18n.get(enchantment == null ? "" : "bestiary." + enchantment.name().toLowerCase())).getVisualOrderText());
                    ChatFormatting textformatting = i >= i1 ? ChatFormatting.GRAY : ChatFormatting.RED;
                    list.add(Component.literal(textformatting + "" + I18n.get("container.lectern.costs")).getVisualOrderText());
                    String s = I18n.get("container.lectern.manuscript.many", i1);
                    list.add(Component.literal(textformatting + "" + s).getVisualOrderText());
                }

                this.extractTooltip(ms, mouseX, mouseY);
                break;
            }
        }
    }

    public void tickBook() {
        ItemStack itemstack = this.menu.getSlot(0).getItem();

        if (!ItemStack.matches(itemstack, this.last)) {
            this.last = itemstack;

            while (true) {
                this.flipT += this.random.nextInt(4) - this.random.nextInt(4);

                if (this.flip > this.flipT + 1.0F || this.flip < this.flipT - 1.0F) {
                    break;
                }
            }
        }

        ++this.ticks;
        this.oFlip = this.flip;
        this.oOpen = this.open;
        boolean flag = false;

        for (int i = 0; i < 3; ++i) {
            if (this.menu.getPossiblePages()[i] != null) {
                flag = true;
            }
        }

        if (flag) {
            this.open += 0.2F;
        } else {
            this.open -= 0.2F;
        }

        this.open = Mth.clamp(this.open, 0.0F, 1.0F);
        float f1 = (this.flipT - this.flip) * 0.4F;
        if (flapTimer > 0) {
            f1 = (ticks + this.minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false)) * 0.5F;
            flapTimer--;
        }
        f1 = Mth.clamp(f1, -0.2F, 0.2F);
        this.flipA += (f1 - this.flipA) * 0.9F;
        this.flip += this.flipA;
    }

}