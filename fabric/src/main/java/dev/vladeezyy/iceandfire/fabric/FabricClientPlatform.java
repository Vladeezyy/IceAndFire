package dev.vladeezyy.iceandfire.fabric;

import com.github.alexthe666.iceandfire.client.IafItemRenderers;
import com.github.alexthe666.iceandfire.item.IafArmorTextureItem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vladeezyy.iceandfire.bus.IafBus;
import dev.vladeezyy.iceandfire.bus.RenderLivingEvent;
import dev.vladeezyy.iceandfire.platform.ClientPlatform;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

/** The client side on Fabric: the mod's armor models and textures, the render events on the mod's bus */
public final class FabricClientPlatform implements ClientPlatform {
    @Override
    public Model armorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
        return IafItemRenderers.hasArmorModel(stack.getItem()) ? IafItemRenderers.armorModel(stack.getItem(), slot, original) : original;
    }

    @Override
    public Identifier armorTexture(Entity entity, ItemStack stack, ArmorMaterial.Layer layer, boolean innerModel, EquipmentSlot slot) {
        if (stack.getItem() instanceof IafArmorTextureItem armor) {
            Identifier texture = armor.getArmorTexture(stack, entity, slot, layer, innerModel);
            if (texture != null) {
                return texture;
            }
        }
        return layer.texture(innerModel);
    }

    @Override
    public Identifier fluidStillTexture(Fluid fluid) {
        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(FluidVariant.of(fluid));
        return sprite != null ? sprite.contents().name() : MissingTextureAtlasSprite.getLocation();
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> boolean renderLivingPre(T entity, LivingEntityRenderer<T, M> renderer, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        return IafBus.post(new RenderLivingEvent.Pre<>(entity, renderer, partialTick, poseStack, buffers, light));
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void renderLivingPost(T entity, LivingEntityRenderer<T, M> renderer, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        IafBus.post(new RenderLivingEvent.Post<>(entity, renderer, partialTick, poseStack, buffers, light));
    }

    @Override
    public void setRenderLayer(Block block, RenderType type) {
        BlockRenderLayerMap.INSTANCE.putBlock(block, type);
    }

    @Override
    public void registerKeyMapping(KeyMapping mapping) {
        KeyBindingHelper.registerKeyBinding(mapping);
    }
}
