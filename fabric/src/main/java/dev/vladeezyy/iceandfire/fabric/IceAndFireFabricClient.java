package dev.vladeezyy.iceandfire.fabric;

import com.github.alexthe666.iceandfire.CommonProxy;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.client.IafClientSetup;
import com.github.alexthe666.iceandfire.client.IafItemRenderers;
import com.github.alexthe666.iceandfire.client.gui.IafGuiRegistry;
import com.github.alexthe666.iceandfire.config.ConfigHolder;
import com.github.alexthe666.iceandfire.item.IafArmorTextureItem;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.vladeezyy.iceandfire.bus.ClientEventIndex;
import dev.vladeezyy.iceandfire.bus.IafBus;
import dev.vladeezyy.iceandfire.bus.RenderLevelStageEvent;
import dev.vladeezyy.iceandfire.platform.ClientPlatform;
import dev.vladeezyy.iceandfire.platform.PacketContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/** The client side of Ice and Fire on Fabric */
public final class IceAndFireFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricConfig.load(ConfigHolder.CLIENT_SPEC, "client");
        CommonProxy.onConfigLoaded(true);
        FabricPlatform.clientSender = ClientPlayNetworking::send;
        registerClientPackets();
        ClientEventIndex.register();

        IafBus.post(new dev.vladeezyy.iceandfire.bus.FMLClientSetupEvent());
        IceAndFire.PROXY.clientInit();
        IafGuiRegistry.register(new IafGuiRegistry.ScreenSink() {
            @Override
            public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, MenuScreens.ScreenConstructor<M, U> constructor) {
                MenuScreens.register(type, constructor);
            }
        });
        CoreShaderRegistrationCallback.EVENT.register(context ->
            context.register(IafClientSetup.DREAD_PORTAL_SHADER, DefaultVertexFormat.POSITION_COLOR, IafClientSetup::setRendertypeDreadPortalShader));
        registerItemRenderers();
        // the pathfinding debug lines (NeoForge: RenderLevelStageEvent after the cutout blocks)
        WorldRenderEvents.BEFORE_ENTITIES.register(context -> {
            if (context.matrixStack() != null) {
                IafBus.post(new RenderLevelStageEvent(RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS, context.matrixStack(), context.tickCounter()));
            }
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerClientPackets() {
        for (var entry : FabricNetwork.CLIENT_HANDLERS.entrySet()) {
            BiConsumer<CustomPacketPayload, PacketContext> handler = (BiConsumer<CustomPacketPayload, PacketContext>) entry.getValue();
            ClientPlayNetworking.registerGlobalReceiver((CustomPacketPayload.Type) entry.getKey(), (payload, context) -> handler.accept((CustomPacketPayload) payload, new PacketContext() {
                @Override
                public Player player() {
                    return context.player();
                }

                @Override
                public void enqueueWork(Runnable work) {
                    context.client().execute(work);
                }

                @Override
                public PacketFlow flow() {
                    return PacketFlow.CLIENTBOUND;
                }
            }));
        }
    }

    /** Item renderers (builtin/entity models) and armor models and textures, from IafItemRenderers as on NeoForge */
    private static void registerItemRenderers() {
        for (var entry : IafItemRegistry.ITEMS.getEntries()) {
            Item item = entry.get();
            Supplier<BlockEntityWithoutLevelRenderer> renderer = IafItemRenderers.itemRenderer(item);
            if (renderer != null) {
                BuiltinItemRendererRegistry.INSTANCE.register(item, (stack, mode, poseStack, buffers, light, overlay) -> renderer.get().renderByItem(stack, mode, poseStack, buffers, light, overlay));
            } else if (item instanceof ArmorItem && (IafItemRenderers.hasArmorModel(item) || item instanceof IafArmorTextureItem)) {
                ArmorRenderer.register((poseStack, buffers, stack, entity, slot, light, contextModel) -> {
                    Model model = ClientPlatform.INSTANCE.armorModel(entity, stack, slot, contextModel);
                    if (!(model instanceof HumanoidModel<?> humanoid)) {
                        return;
                    }
                    @SuppressWarnings({"unchecked", "rawtypes"})
                    HumanoidModel<LivingEntity> target = (HumanoidModel) humanoid;
                    if (target != contextModel) {
                        contextModel.copyPropertiesTo(target);
                    }
                    target.setAllVisible(false);
                    switch (slot) {
                        case HEAD -> {
                            target.head.visible = true;
                            target.hat.visible = true;
                        }
                        case CHEST -> {
                            target.body.visible = true;
                            target.rightArm.visible = true;
                            target.leftArm.visible = true;
                        }
                        case LEGS -> {
                            target.body.visible = true;
                            target.rightLeg.visible = true;
                            target.leftLeg.visible = true;
                        }
                        case FEET -> {
                            target.rightLeg.visible = true;
                            target.leftLeg.visible = true;
                        }
                        default -> {
                        }
                    }
                    var layer = ((ArmorItem) stack.getItem()).getMaterial().value().layers().getFirst();
                    ArmorRenderer.renderPart(poseStack, buffers, light, stack, target, ClientPlatform.INSTANCE.armorTexture(entity, stack, layer, slot == EquipmentSlot.LEGS, slot));
                }, item);
            }
        }
    }
}
