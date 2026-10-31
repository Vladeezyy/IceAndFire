package dev.vladeezyy.iceandfire.neoforge;

import com.github.alexthe666.iceandfire.client.particle.IafAppearanceParticle;
import com.github.alexthe666.iceandfire.client.render.IafItemModels;
import com.github.alexthe666.iceandfire.client.render.IafItemSpecialRenderer;
import com.github.alexthe666.iceandfire.client.render.IafRenderType;
import com.github.alexthe666.iceandfire.item.IafArmorTextureItem;
import com.github.alexthe666.iceandfire.recipe.IafRecipeRegistry;
import dev.vladeezyy.iceandfire.platform.ClientRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterParticleGroupsEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.client.IafClientSetup;
import com.github.alexthe666.iceandfire.client.IafItemRenderers;
import com.github.alexthe666.iceandfire.client.gui.IafGuiRegistry;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import dev.vladeezyy.iceandfire.bus.ClientEventIndex;
import dev.vladeezyy.iceandfire.bus.IafBus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;


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
        modBus.addListener((RegisterRenderPipelinesEvent event) -> {
            for (var pipeline : IafRenderType.pipelines()) {
                event.registerPipeline(pipeline);
            }
        });
        modBus.addListener((RegisterSpecialModelRendererEvent event) -> event.register(IafItemSpecialRenderer.ID, IafItemSpecialRenderer.Unbaked.MAP_CODEC));
        modBus.addListener((RegisterRangeSelectItemModelPropertyEvent event) -> event.register(IafItemModels.ICE_OR_FIRE, IafItemModels.IceOrFire.MAP_CODEC));
        modBus.addListener((RegisterConditionalItemModelPropertyEvent event) -> event.register(IafItemModels.HAS_DRAGON, IafItemModels.HasDragon.MAP_CODEC));
        modBus.addListener((net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event) -> NeoForgeClientPlatform.KEYS.forEach(event::register));
        modBus.addListener((RegisterParticleGroupsEvent event) -> event.register(IafAppearanceParticle.GROUP, IafAppearanceParticle.Group::new));
        modBus.addListener((RegisterMenuScreensEvent event) -> IafGuiRegistry.register(new IafGuiRegistry.ScreenSink() {
            @Override
            public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, MenuScreens.ScreenConstructor<M, U> constructor) {
                event.register(type, constructor);
            }
        }));

        IEventBus bus = NeoForge.EVENT_BUS;
        // RenderLivingEvent and RenderPlayerEvent: posted by the common LivingEntityRendererMixin on 26.x
        // the dragon forge recipes for JEI (26.x clients only get the recipe types the server sends)
        bus.addListener((RecipesReceivedEvent event) -> {
            if (event.getRecipeTypes().contains(IafRecipeRegistry.DRAGON_FORGE_TYPE.get())) {
                ClientRecipes.setDragonForge(event.getRecipeMap().byType(IafRecipeRegistry.DRAGON_FORGE_TYPE.get()).stream().map(RecipeHolder::value).toList());
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
        bus.addListener((RenderLevelStageEvent.AfterOpaqueBlocks event) -> IafBus.post(new dev.vladeezyy.iceandfire.bus.RenderLevelStageEvent(
            dev.vladeezyy.iceandfire.bus.RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS, event.getPoseStack(), Minecraft.getInstance().getDeltaTracker())));
    }

    /** Armor models and textures (IafItemRenderers), formerly the items' own client extensions; item renderers are item model definitions on 26.x */
    private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        IClientItemExtensions armor = new IClientItemExtensions() {
            @Override
            public @NotNull Model getHumanoidArmorModel(@NotNull ItemStack stack, EquipmentClientInfo.@NotNull LayerType layerType, @NotNull Model original) {
                return IafItemRenderers.armorModel(stack, original);
            }

            @Override
            public @NotNull Identifier getArmorTexture(@NotNull ItemStack stack, EquipmentClientInfo.@NotNull LayerType type, EquipmentClientInfo.@NotNull Layer layer, @NotNull Identifier _default) {
                return IafItemRenderers.armorTexture(stack, _default);
            }
        };
        for (var entry : IafItemRegistry.ITEMS.getEntries()) {
            Item item = entry.get();
            if (item instanceof IafArmorTextureItem) {
                event.registerItem(armor, item);
            }
        }
    }
}
