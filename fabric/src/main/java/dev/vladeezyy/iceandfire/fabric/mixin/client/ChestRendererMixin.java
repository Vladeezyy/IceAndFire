package dev.vladeezyy.iceandfire.fabric.mixin.client;

import com.github.alexthe666.iceandfire.client.render.tile.RenderGhostChest;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityGhostChest;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** NeoForge's ChestRenderer#getMaterial: the ghost chest has its own textures */
@Mixin(ChestRenderer.class)
public abstract class ChestRendererMixin {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Sheets;chooseMaterial(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/level/block/state/properties/ChestType;Z)Lnet/minecraft/client/resources/model/Material;"))
    private Material iceandfire$material(BlockEntity blockEntity, ChestType type, boolean christmas) {
        if ((Object) this instanceof RenderGhostChest ghost && blockEntity instanceof TileEntityGhostChest chest) {
            return ghost.getMaterial(chest, type);
        }
        return Sheets.chooseMaterial(blockEntity, type, christmas);
    }
}
