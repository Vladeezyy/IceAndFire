package com.github.alexthe666.iceandfire.event;

import dev.vladeezyy.iceandfire.bus.EntityTickEvent;
import dev.vladeezyy.iceandfire.bus.EventBusSubscriber;
import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.client.ClientProxy;
import com.github.alexthe666.iceandfire.client.IafKeybindRegistry;
import com.github.alexthe666.iceandfire.client.gui.IceAndFireMainMenu;
import com.github.alexthe666.iceandfire.client.particle.CockatriceBeamRender;
import com.github.alexthe666.iceandfire.client.render.entity.RenderChain;
import com.github.alexthe666.iceandfire.client.render.tile.RenderFrozenState;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.props.EntityDataProvider;
import com.github.alexthe666.iceandfire.entity.util.ICustomMoveController;
import com.github.alexthe666.iceandfire.enums.EnumParticles;
import com.github.alexthe666.iceandfire.message.MessageDragonControl;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import dev.vladeezyy.iceandfire.bus.Dist;
import dev.vladeezyy.iceandfire.bus.OnlyIn;
import dev.vladeezyy.iceandfire.bus.RenderLevelStageEvent;
import dev.vladeezyy.iceandfire.bus.RenderLivingEvent;
import dev.vladeezyy.iceandfire.bus.ScreenEvent;
import dev.vladeezyy.iceandfire.bus.ViewportEvent;
import dev.vladeezyy.iceandfire.bus.EntityMountEvent;
import dev.vladeezyy.iceandfire.bus.LivingEvent;
import dev.vladeezyy.iceandfire.bus.EventPriority;
import dev.vladeezyy.iceandfire.bus.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class ClientEvents {

    /** assets/iceandfire/post_effect/siren.json (26.x post effect format; 26.x color_convolve has a fixed saturation) */
    private static final Identifier SIREN_SHADER = Identifier.parse("iceandfire:siren");

    private final Random rand = new Random();

    private static boolean shouldCancelRender(LivingEntity living) {
        if (living.getVehicle() != null && living.getVehicle() instanceof EntityDragonBase) {
            return ClientProxy.currentDragonRiders.contains(living.getUUID()) || living == Minecraft.getInstance().player && Minecraft.getInstance().options.getCameraType().isFirstPerson();
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void renderWorldLastEvent(@NotNull final RenderLevelStageEvent event)
    {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS) {
            // 26.x: vanilla's debug gizmos (shown with the next frame)
            try (net.minecraft.gizmos.Gizmos.TemporaryCollection ignored = net.minecraft.client.Minecraft.getInstance().levelRenderer.collectPerFrameGizmos()) {
                com.github.alexthe666.iceandfire.client.render.pathfinding.PathfindingDebugRenderer.render();
            }
        }
    }

    @SubscribeEvent
    public void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        Player player = Minecraft.getInstance().player;
        if (player.getVehicle() != null) {
            if (player.getVehicle() instanceof EntityDragonBase) {
                int currentView = IceAndFire.PROXY.getDragon3rdPersonView();
                float scale = ((EntityDragonBase) player.getVehicle()).getRenderSize() / 3;
                if (Minecraft.getInstance().options.getCameraType() == CameraType.THIRD_PERSON_BACK ||
                        Minecraft.getInstance().options.getCameraType() == CameraType.THIRD_PERSON_FRONT) {
                    if (currentView == 1) {
                        event.getCamera().move(-event.getCamera().getMaxZoom(scale * 1.2F), 0F, 0);
                    } else if (currentView == 2) {
                        event.getCamera().move(-event.getCamera().getMaxZoom(scale * 3F), 0F, 0);
                    } else if (currentView == 3) {
                        event.getCamera().move(-event.getCamera().getMaxZoom(scale * 5F), 0F, 0);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (living instanceof ICustomMoveController) {
            Entity entity = living;
            ICustomMoveController moveController = ((Entity & ICustomMoveController) living);
            if (entity.getVehicle() != null && entity.getVehicle() == mc.player) {
                byte previousState = moveController.getControlState();
                moveController.dismount(mc.options.keyShift.isDown());
                byte controlState = moveController.getControlState();
                if (controlState != previousState) {
                    IceAndFire.sendMSGToServer(new MessageDragonControl(entity.getId(), controlState, entity.getX(), entity.getY(), entity.getZ()));
                }
            }
        }
        if (living instanceof Player player) {
            if (player.level().isClientSide()) {

                if (player.getVehicle() instanceof ICustomMoveController) {
                    Entity entity = player.getVehicle();
                    ICustomMoveController moveController = ((Entity & ICustomMoveController) player.getVehicle());
                    byte previousState = moveController.getControlState();
                    moveController.up(mc.options.keyJump.isDown());
                    moveController.down(IafKeybindRegistry.dragon_down.isDown());
                    moveController.attack(IafKeybindRegistry.dragon_strike.isDown());
                    moveController.dismount(mc.options.keyShift.isDown());
                    moveController.strike(IafKeybindRegistry.dragon_fireAttack.isDown());
                    byte controlState = moveController.getControlState();
                    if (controlState != previousState) {
                        IceAndFire.sendMSGToServer(new MessageDragonControl(entity.getId(), controlState, entity.getX(), entity.getY(), entity.getZ()));
                    }
                }
            }
            if (player.level().isClientSide() && IafKeybindRegistry.dragon_change_view.isDown()) {
                int currentView = IceAndFire.PROXY.getDragon3rdPersonView();
                if (currentView + 1 > 3) {
                    currentView = 0;
                } else {
                    currentView++;
                }
                IceAndFire.PROXY.setDragon3rdPersonView(currentView);
            }

            if (player.level().isClientSide()) {
                GameRenderer renderer = Minecraft.getInstance().gameRenderer;

                EntityDataProvider.getExisting(player).ifPresent(data -> {
                    if (IafConfig.sirenShader && data.sirenData.charmedBy == null && renderer.currentPostEffect() != null) {
                        if (SIREN_SHADER.equals(renderer.currentPostEffect()))
                            renderer.clearPostEffect();
                    }

                    if (data.sirenData.charmedBy == null) {
                        return;
                    }

                    if (IafConfig.sirenShader && !data.sirenData.isCharmed && SIREN_SHADER.equals(renderer.currentPostEffect())) {
                        renderer.clearPostEffect();
                    }

                if (data.sirenData.isCharmed) {
                    if (player.level().isClientSide() && rand.nextInt(40) == 0) {
                        IceAndFire.PROXY.spawnParticle(EnumParticles.Siren_Appearance, player.getX(), player.getY(), player.getZ(), data.sirenData.charmedBy.getHairColor(), 0, 0);
                    }

                        if (IafConfig.sirenShader && renderer.currentPostEffect() == null) {
                            renderer.setPostEffect(SIREN_SHADER);
                        }

                    }
                });
            }
        }
    }

    @SubscribeEvent
    public void onPreRenderLiving(RenderLivingEvent.Pre event) {
        if (shouldCancelRender(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onPostRenderLiving(RenderLivingEvent.Post event) {

        LivingEntity entity = event.getEntity();

        EntityDataProvider.getExisting(entity).ifPresent(data -> {
            for (LivingEntity target : data.miscData.getTargetedByScepter()) {
                CockatriceBeamRender.render(entity, target, event.getPoseStack(), event.getMultiBufferSource(), event.getPartialTick());
            }

            if (data.frozenData.isFrozen) {
                RenderFrozenState.render(event.getEntity(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), data.frozenData.frozenTicks);
            }

            RenderChain.render(entity, event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), data.chainData.getChainedTo());
        });
    }

    @SubscribeEvent
    public void onGuiOpened(ScreenEvent.Opening event) {
        if (IafConfig.customMainMenu && event.getScreen() instanceof TitleScreen && !(event.getScreen() instanceof IceAndFireMainMenu)) {
            event.setNewScreen(new IceAndFireMainMenu());
        }
    }

    // TODO: add this to client side config
    public final boolean AUTO_ADAPT_3RD_PERSON = true;

    @SubscribeEvent
    public void onEntityMount(EntityMountEvent event) {
        if (event.getEntityBeingMounted() instanceof EntityDragonBase dragon && event.getLevel().isClientSide() && event.getEntityMounting() == Minecraft.getInstance().player) {
            if (dragon.isTame() && dragon.isOwnedBy(Minecraft.getInstance().player)) {
                if (AUTO_ADAPT_3RD_PERSON) {
                    // Auto adjust 3rd person camera's according to dragon's size
                    IceAndFire.PROXY.setDragon3rdPersonView(2);
                }
                if (IafConfig.dragonAuto3rdPerson) {
                    if (event.isDismounting()) {
                        Minecraft.getInstance().options.setCameraType(CameraType.values()[IceAndFire.PROXY.getPreviousViewType()]);
                    } else {
                        IceAndFire.PROXY.setPreviousViewType(Minecraft.getInstance().options.getCameraType().ordinal());
                        Minecraft.getInstance().options.setCameraType(CameraType.values()[1]);
                    }
                }
            }
        }
    }
}