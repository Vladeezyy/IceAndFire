package com.github.alexthe666.iceandfire.client.gui;
import net.minecraft.client.renderer.RenderPipelines;

import net.minecraft.client.gui.components.Renderable;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.client.ClientProxy;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.alexthe666.iceandfire.message.MessageGetMyrmexHive;
import com.github.alexthe666.iceandfire.world.gen.WorldGenMyrmexHive;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GuiMyrmexAddRoom extends Screen {
    private static final Identifier JUNGLE_TEXTURE = Identifier.parse("iceandfire:textures/gui/myrmex_staff_jungle.png");
    private static final Identifier DESERT_TEXTURE = Identifier.parse("iceandfire:textures/gui/myrmex_staff_desert.png");
    private final boolean jungle;
    private final BlockPos interactPos;
    private final Direction facing;

    public GuiMyrmexAddRoom(ItemStack staff, BlockPos interactPos, Direction facing) {
        super(Component.translatable("myrmex_add_room"));
        this.jungle = staff.getItem() == IafItemRegistry.MYRMEX_JUNGLE_STAFF.get();
        this.interactPos = interactPos;
        this.facing = facing;
        init();
    }

    public static void onGuiClosed() {
        IceAndFire.sendMSGToServer(new MessageGetMyrmexHive(ClientProxy.getReferedClientHive().toNBT()));
    }

    @Override
    protected void init() {
        super.init();
        this.renderables.clear();
        int i = (this.width - 248) / 2;
        int j = (this.height - 166) / 2;
        if (ClientProxy.getReferedClientHive() != null) {
            Player player = Minecraft.getInstance().player;
            this.addWidget(
                    Button.builder (
                            Component.translatable("myrmex.message.establishroom_food"), (p_214132_1_) -> {
                                ClientProxy.getReferedClientHive().addRoomWithMessage(player, interactPos, WorldGenMyrmexHive.RoomType.FOOD);
                                onGuiClosed();
                                Minecraft.getInstance().gui.setScreen(null);
                                })
                            .pos(i + 50, j + 35)
                            .size(150, 20)
                            .build());
            this.addWidget(
                    Button.builder(
                            Component.translatable("myrmex.message.establishroom_nursery"), (p_214132_1_) -> {
                                ClientProxy.getReferedClientHive().addRoomWithMessage(player, interactPos, WorldGenMyrmexHive.RoomType.NURSERY);
                                onGuiClosed();
                                Minecraft.getInstance().gui.setScreen(null);
                            })
                            .pos(i + 50, j + 60)
                            .size(150, 20)
                            .build());

            this.addWidget(
                    Button.builder(
                            Component.translatable("myrmex.message.establishroom_enterance_surface"), (p_214132_1_) -> {
                                ClientProxy.getReferedClientHive().addEnteranceWithMessage(player, false, interactPos, facing);
                                onGuiClosed();
                                Minecraft.getInstance().gui.setScreen(null);
                            })
                            .pos(i + 50, j + 85)
                            .size(150, 20)
                            .build());
            this.addWidget(
                    Button.builder(
                            Component.translatable("myrmex.message.establishroom_enterance_bottom"), (p_214132_1_) -> {
                                ClientProxy.getReferedClientHive().addEnteranceWithMessage(player, true, interactPos, facing);
                                onGuiClosed();
                                Minecraft.getInstance().gui.setScreen(null);
                            })
                            .pos(i + 50, j + 110)
                            .size(150, 20)
                            .build());
            this.addWidget(
                    Button.builder(Component.translatable("myrmex.message.establishroom_misc"), (p_214132_1_) -> {
                        ClientProxy.getReferedClientHive().addRoomWithMessage(player, interactPos, WorldGenMyrmexHive.RoomType.EMPTY);
                        onGuiClosed();
                        Minecraft.getInstance().gui.setScreen(null);
                    })
                            .pos(i + 50, j + 135)
                            .size(150, 20)
                            .build());
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor ms, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(ms, mouseX, mouseY, partialTicks);
        int i = (this.width - 248) / 2;
        int j = (this.height - 166) / 2;
        ms.blit(RenderPipelines.GUI_TEXTURED, jungle ? JUNGLE_TEXTURE : DESERT_TEXTURE, i, j, (float) (0), (float) (0), 248, 166, 256, 256);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ms, int mouseX, int mouseY, float partialTicks) {
        // 26.x: Screen draws the background (extractBackground) before this; a second call blurs twice and throws
        init();
        int i = (this.width - 248) / 2 + 10;
        int j = (this.height - 166) / 2 + 8;
        // Screen#render would draw the background a second time (and blur twice, which throws in 1.21)
        for (Renderable renderable : this.renderables) {
            renderable.extractRenderState(ms, mouseX, mouseY, partialTicks);
        }
        int color = this.jungle ? 0X35EA15 : 0XFFBF00;
        if (ClientProxy.getReferedClientHive() != null) {
            if (!ClientProxy.getReferedClientHive().colonyName.isEmpty()) {
                String title = I18n.get("myrmex.message.colony_named", ClientProxy.getReferedClientHive().colonyName);
                ms.text(this.minecraft.font, title, (int) (i + 40 - title.length() / 2), (int) (j - 3), net.minecraft.util.ARGB.opaque(color), false);
            } else {
                ms.text(this.minecraft.font, I18n.get("myrmex.message.colony"), (int) (i + 80), (int) (j - 3), net.minecraft.util.ARGB.opaque(color), false);
            }
            ms.text(this.minecraft.font, I18n.get("myrmex.message.create_new_room", interactPos.getX(), interactPos.getY(), interactPos.getZ()), (int) (i + 30), (int) (j + 6), net.minecraft.util.ARGB.opaque(color), false);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
