package com.github.alexthe666.iceandfire.client.render.tile;

import net.minecraft.util.ARGB;
import com.github.alexthe666.iceandfire.block.BlockPixieHouse;
import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityDreadPortal;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityGhostChest;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class IceAndFireTEISR implements com.github.alexthe666.iceandfire.client.render.IafLegacyItemRenderer {

    private final RenderPixieHouse PIXIE_HOUSE_RENDERER;
    private final RenderDreadPortal<TileEntityDreadPortal> portalRenderer = new RenderDreadPortal<>(null);
    private final EntityModelSet entityModelSet;
    private final TileEntityGhostChest chest = new TileEntityGhostChest(BlockPos.ZERO, IafBlockRegistry.GHOST_CHEST.get().defaultBlockState());
    private final TileEntityDreadPortal portal = new TileEntityDreadPortal(BlockPos.ZERO, IafBlockRegistry.DREAD_PORTAL.get().defaultBlockState());

    public IceAndFireTEISR() {
        EntityModelSet modelSet = net.minecraft.client.Minecraft.getInstance().getEntityModels();
        this.entityModelSet = modelSet;
        PIXIE_HOUSE_RENDERER = new RenderPixieHouse(null);

    }

    @Override
    public void renderByItem(ItemStack stack, @NotNull ItemDisplayContext type, @NotNull PoseStack stackIn, @NotNull MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        // the ghost chest item is vanilla's chest special model (assets/iceandfire/items/ghost_chest.json)
        if (stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() == IafBlockRegistry.DREAD_PORTAL.get()) {
            this.portalRenderer.renderLegacy(this.portal, 0, stackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        }
        if (stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof BlockPixieHouse) {
            PIXIE_HOUSE_RENDERER.metaOverride = (BlockItem) stack.getItem();
            PIXIE_HOUSE_RENDERER.renderLegacy(null, 0, stackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        }

    }
}
