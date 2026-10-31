package dev.vladeezyy.iceandfire.fabric;

import java.util.List;
import java.util.ArrayList;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import dev.vladeezyy.iceandfire.platform.ClientRecipes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.github.alexthe666.iceandfire.recipe.IafRecipeRegistry;
import com.github.alexthe666.iceandfire.client.render.IafItemSpecialRenderer;
import com.github.alexthe666.iceandfire.client.render.IafItemModels;
import com.github.alexthe666.iceandfire.CommonProxy;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.client.IafClientSetup;
import com.github.alexthe666.iceandfire.client.IafItemRenderers;
import com.github.alexthe666.iceandfire.client.gui.IafGuiRegistry;
import com.github.alexthe666.iceandfire.config.ConfigHolder;
import com.github.alexthe666.iceandfire.item.IafArmorTextureItem;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import dev.vladeezyy.iceandfire.bus.ClientEventIndex;
import dev.vladeezyy.iceandfire.bus.IafBus;
import dev.vladeezyy.iceandfire.bus.RenderLevelStageEvent;
import dev.vladeezyy.iceandfire.platform.PacketContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.model.Model;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;

import java.util.function.BiConsumer;

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
        // item model types of the mod's item definitions (NeoForge: the Register*ItemModel*Event)
        SpecialModelRenderers.ID_MAPPER.put(IafItemSpecialRenderer.ID, IafItemSpecialRenderer.Unbaked.MAP_CODEC);
        RangeSelectItemModelProperties.ID_MAPPER.put(IafItemModels.ICE_OR_FIRE, IafItemModels.IceOrFire.MAP_CODEC);
        ConditionalItemModelProperties.ID_MAPPER.put(IafItemModels.HAS_DRAGON, IafItemModels.HasDragon.MAP_CODEC);
        registerArmorRenderers();
        // the pathfinding debug lines (NeoForge: RenderLevelStageEvent after the opaque blocks)
        LevelRenderEvents.AFTER_OPAQUE_TERRAIN.register(context -> IafBus.post(new RenderLevelStageEvent(
            RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS, new PoseStack(), Minecraft.getInstance().getDeltaTracker())));
        // the dragon forge recipes for JEI (26.x clients only get the recipe serializers the server syncs)
        ClientRecipeSynchronizedEvent.EVENT.register((client, recipes) -> ClientRecipes.setDragonForge(
            recipes.getAllOfType(IafRecipeRegistry.DRAGON_FORGE_TYPE.get()).stream().map(RecipeHolder::value).toList()));
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

    /** The mod's armor: its own models and textures (IafItemRenderers, as NeoForge's item client extensions), posed from the wearer */
    private static void registerArmorRenderers() {
        List<Item> armor = new ArrayList<>();
        for (var entry : IafItemRegistry.ITEMS.getEntries()) {
            if (entry.get() instanceof IafArmorTextureItem) {
                armor.add(entry.get());
            }
        }
        ArmorRenderer.register((poseStack, collector, stack, state, slot, light, contextModel) -> {
            @SuppressWarnings("unchecked")
            Model<HumanoidRenderState> model = (Model<HumanoidRenderState>) IafItemRenderers.armorModel(stack, contextModel);
            Identifier texture = IafItemRenderers.armorTexture(stack, MissingTextureAtlasSprite.getLocation());
            collector.submitModel(model, state, poseStack, RenderTypes.armorCutoutNoCull(texture), light, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
            if (stack.hasFoil()) {
                collector.order(1).submitModel(model, state, poseStack, RenderTypes.armorEntityGlint(), light, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
            }
        }, armor.toArray(Item[]::new));
    }
}
