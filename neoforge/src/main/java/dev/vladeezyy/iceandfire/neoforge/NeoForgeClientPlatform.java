package dev.vladeezyy.iceandfire.neoforge;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.vladeezyy.iceandfire.platform.ClientPlatform;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.NeoForge;

/** The client hooks of NeoForge the original render code called */
public final class NeoForgeClientPlatform implements ClientPlatform {
    @Override
    public Model armorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
        return ClientHooks.getArmorModel(entity, stack, slot, original);
    }

    @Override
    public ResourceLocation armorTexture(Entity entity, ItemStack stack, ArmorMaterial.Layer layer, boolean innerModel, EquipmentSlot slot) {
        return ClientHooks.getArmorTexture(entity, stack, layer, innerModel, slot);
    }

    @Override
    public int armorDefaultDyeColor(ItemStack stack) {
        return IClientItemExtensions.of(stack).getDefaultDyeColor(stack);
    }

    @Override
    public int armorLayerTint(ItemStack stack, LivingEntity entity, ArmorMaterial.Layer layer, int layerIndex, int fallbackColor) {
        return IClientItemExtensions.of(stack).getArmorLayerTintColor(stack, entity, layer, layerIndex, fallbackColor);
    }

    @Override
    public ResourceLocation fluidStillTexture(Fluid fluid) {
        return IClientFluidTypeExtensions.of(fluid).getStillTexture();
    }

    @Override
    public boolean isNameplateInRenderDistance(Entity entity, double distanceSq) {
        return ClientHooks.isNameplateInRenderDistance(entity, distanceSq);
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> boolean renderLivingPre(T entity, LivingEntityRenderer<T, M> renderer, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        return NeoForge.EVENT_BUS.post(new RenderLivingEvent.Pre<>(entity, renderer, partialTick, poseStack, buffers, light)).isCanceled();
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void renderLivingPost(T entity, LivingEntityRenderer<T, M> renderer, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        NeoForge.EVENT_BUS.post(new RenderLivingEvent.Post<>(entity, renderer, partialTick, poseStack, buffers, light));
    }

    @Override
    public void renderMainMenu(Screen screen, GuiGraphics graphics, Font font, int width, int height, int alpha) {
        if (screen instanceof TitleScreen title) {
            ClientHooks.renderMainMenu(title, graphics, font, width, height, alpha);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setRenderLayer(Block block, RenderType type) {
        ItemBlockRenderTypes.setRenderLayer(block, type);
    }
}
