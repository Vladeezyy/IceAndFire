package com.github.alexthe666.iceandfire.client.render.tile;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityGhostChest;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * The ghost chest: vanilla's chest renderer (26.x) with the ghost textures of the chest atlas
 * (textures/entity/chest/ghost_chest*.png; 1.21.1 overrode the chest material). The item is vanilla's chest special model.
 */
public class RenderGhostChest implements BlockEntityRenderer<TileEntityGhostChest, ChestRenderState> {
    private static final SpriteId SINGLE = sprite("ghost_chest");
    private static final SpriteId LEFT = sprite("ghost_chest_left");
    private static final SpriteId RIGHT = sprite("ghost_chest_right");
    private final SpriteGetter sprites;
    private final ChestModel single;
    private final ChestModel left;
    private final ChestModel right;

    public RenderGhostChest(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
        this.single = new ChestModel(context.bakeLayer(net.minecraft.client.model.geom.ModelLayers.CHEST));
        this.left = new ChestModel(context.bakeLayer(net.minecraft.client.model.geom.ModelLayers.DOUBLE_CHEST_LEFT));
        this.right = new ChestModel(context.bakeLayer(net.minecraft.client.model.geom.ModelLayers.DOUBLE_CHEST_RIGHT));
    }

    private static SpriteId sprite(String name) {
        return new SpriteId(Sheets.CHEST_SHEET, Identifier.fromNamespaceAndPath(IceAndFire.MODID, "entity/chest/" + name));
    }

    @Override
    public ChestRenderState createRenderState() {
        return new ChestRenderState();
    }

    @Override
    public void extractRenderState(TileEntityGhostChest blockEntity, ChestRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        boolean hasLevel = blockEntity.getLevel() != null;
        BlockState blockState = blockEntity.getBlockState();
        state.type = blockState.hasProperty(ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        state.facing = blockState.getValue(ChestBlock.FACING);
        DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combineResult;
        if (hasLevel && blockState.getBlock() instanceof ChestBlock chestBlock) {
            combineResult = chestBlock.combine(blockState, blockEntity.getLevel(), blockEntity.getBlockPos(), true);
        } else {
            combineResult = DoubleBlockCombiner.Combiner::acceptNone;
        }
        state.open = combineResult.apply(ChestBlock.opennessCombiner(blockEntity)).get(partialTicks);
        if (state.type != ChestType.SINGLE) {
            state.lightCoords = combineResult.apply(new BrightnessCombiner<>()).applyAsInt(state.lightCoords);
        }
    }

    @Override
    public void submit(ChestRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(ChestRenderer.modelTransformation(state.facing));
        float open = 1.0F - state.open;
        open = 1.0F - open * open * open;
        SpriteId sprite = state.type == ChestType.LEFT ? LEFT : state.type == ChestType.RIGHT ? RIGHT : SINGLE;
        ChestModel model = state.type == ChestType.LEFT ? this.left : state.type == ChestType.RIGHT ? this.right : this.single;
        collector.submitModel(model, open, poseStack, state.lightCoords, OverlayTexture.NO_OVERLAY, -1, sprite, this.sprites, 0, state.breakProgress);
        poseStack.popPose();
    }
}
