package dev.vladeezyy.iceandfire.neoforge;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.client.IafClientSetup;
import com.github.alexthe666.iceandfire.client.IafItemRenderers;
import com.github.alexthe666.iceandfire.client.gui.IafGuiRegistry;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.vladeezyy.iceandfire.bus.ClientEventIndex;
import dev.vladeezyy.iceandfire.bus.IafBus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Supplier;

/** The client side of Ice and Fire on NeoForge: client registrations and the client events posted on {@link IafBus} */
@Mod(value = IceAndFire.MODID, dist = Dist.CLIENT)
public final class IceAndFireNeoForgeClient {
    public IceAndFireNeoForgeClient(IEventBus modBus) {
        ClientEventIndex.register();
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            IafBus.post(new dev.vladeezyy.iceandfire.bus.FMLClientSetupEvent());
            IceAndFire.PROXY.clientInit();
        }));
        modBus.addListener(IceAndFireNeoForgeClient::registerClientExtensions);
        modBus.addListener((RegisterShadersEvent event) -> {
            try {
                event.registerShader(new ShaderInstance(event.getResourceProvider(), IafClientSetup.DREAD_PORTAL_SHADER, DefaultVertexFormat.POSITION_COLOR), IafClientSetup::setRendertypeDreadPortalShader);
            } catch (IOException e) {
                throw new RuntimeException("Could not load the dread portal shader", e);
            }
        });
        modBus.addListener((RegisterMenuScreensEvent event) -> IafGuiRegistry.register(new IafGuiRegistry.ScreenSink() {
            @Override
            public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, MenuScreens.ScreenConstructor<M, U> constructor) {
                event.register(type, constructor);
            }
        }));

        IEventBus bus = NeoForge.EVENT_BUS;
        bus.addListener((RenderLivingEvent.Pre<?, ?> event) -> {
            if (IafBus.post(pre(event))) {
                event.setCanceled(true);
            }
        });
        bus.addListener((RenderLivingEvent.Post<?, ?> event) -> IafBus.post(post(event)));
        bus.addListener((RenderPlayerEvent.Pre event) -> {
            if (IafBus.post(new dev.vladeezyy.iceandfire.bus.RenderPlayerEvent.Pre(event.getEntity(), event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight()))) {
                event.setCanceled(true);
            }
        });
        bus.addListener((ScreenEvent.Opening event) -> {
            var ours = new dev.vladeezyy.iceandfire.bus.ScreenEvent.Opening(event.getCurrentScreen(), event.getNewScreen());
            if (IafBus.post(ours)) {
                event.setCanceled(true);
            } else if (ours.getNewScreen() != event.getNewScreen()) {
                event.setNewScreen(ours.getNewScreen());
            }
        });
        bus.addListener((ViewportEvent.ComputeCameraAngles event) -> {
            var ours = new dev.vladeezyy.iceandfire.bus.ViewportEvent.ComputeCameraAngles(event.getCamera(), event.getPartialTick(), event.getYaw(), event.getPitch(), event.getRoll());
            IafBus.post(ours);
            event.setYaw(ours.getYaw());
            event.setPitch(ours.getPitch());
            event.setRoll(ours.getRoll());
        });
        bus.addListener((RenderLevelStageEvent event) -> {
            dev.vladeezyy.iceandfire.bus.RenderLevelStageEvent.Stage stage;
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS) {
                stage = dev.vladeezyy.iceandfire.bus.RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS;
            } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
                stage = dev.vladeezyy.iceandfire.bus.RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS;
            } else {
                return;
            }
            IafBus.post(new dev.vladeezyy.iceandfire.bus.RenderLevelStageEvent(stage, event.getPoseStack(), event.getPartialTick()));
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static dev.vladeezyy.iceandfire.bus.RenderLivingEvent.Pre<?, ?> pre(RenderLivingEvent.Pre<?, ?> event) {
        return new dev.vladeezyy.iceandfire.bus.RenderLivingEvent.Pre(event.getEntity(), event.getRenderer(), event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static dev.vladeezyy.iceandfire.bus.RenderLivingEvent.Post<?, ?> post(RenderLivingEvent.Post<?, ?> event) {
        return new dev.vladeezyy.iceandfire.bus.RenderLivingEvent.Post(event.getEntity(), event.getRenderer(), event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
    }

    /** Armor models and item renderers (IafItemRenderers), formerly the items' own client extensions */
    private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        for (var entry : IafItemRegistry.ITEMS.getEntries()) {
            Item item = entry.get();
            Supplier<BlockEntityWithoutLevelRenderer> renderer = IafItemRenderers.itemRenderer(item);
            if (renderer != null) {
                event.registerItem(new IClientItemExtensions() {
                    @Override
                    public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        return renderer.get();
                    }
                }, item);
            } else if (IafItemRenderers.hasArmorModel(item)) {
                event.registerItem(new IClientItemExtensions() {
                    @Override
                    public @NotNull HumanoidModel<?> getHumanoidArmorModel(@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull EquipmentSlot slot, @NotNull HumanoidModel<?> original) {
                        return IafItemRenderers.armorModel(stack.getItem(), slot, original);
                    }
                }, item);
            }
        }
    }
}
