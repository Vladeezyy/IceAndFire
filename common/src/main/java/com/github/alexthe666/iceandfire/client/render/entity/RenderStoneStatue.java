package com.github.alexthe666.iceandfire.client.render.entity;

import dev.vladeezyy.rampart.client.model.basic.BasicEntityModel;
import com.github.alexthe666.iceandfire.entity.util.Values;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.minecraft.util.ARGB;
import dev.vladeezyy.rampart.client.model.AdvancedEntityModel;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.client.model.ICustomStatueModel;
import com.github.alexthe666.iceandfire.client.model.ModelHydraBody;
import com.github.alexthe666.iceandfire.client.model.ModelStonePlayer;
import com.github.alexthe666.iceandfire.client.render.IafRenderType;
import com.github.alexthe666.iceandfire.client.render.entity.layer.LayerHydraHead;
import com.github.alexthe666.iceandfire.entity.EntityHydra;
import com.github.alexthe666.iceandfire.entity.EntityStoneStatue;
import com.github.alexthe666.iceandfire.entity.EntityTroll;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.pig.PigModel;
import net.minecraft.client.model.geom.ModelLayers;
import com.github.alexthe666.iceandfire.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class RenderStoneStatue extends IafEntityRenderer<EntityStoneStatue> {

    protected static final Identifier[] DESTROY_STAGES = new Identifier[]{Identifier.parse("textures/block/destroy_stage_0.png"), Identifier.parse("textures/block/destroy_stage_1.png"), Identifier.parse("textures/block/destroy_stage_2.png"), Identifier.parse("textures/block/destroy_stage_3.png"), Identifier.parse("textures/block/destroy_stage_4.png"), Identifier.parse("textures/block/destroy_stage_5.png"), Identifier.parse("textures/block/destroy_stage_6.png"), Identifier.parse("textures/block/destroy_stage_7.png"), Identifier.parse("textures/block/destroy_stage_8.png"), Identifier.parse("textures/block/destroy_stage_9.png")};
    private final Map<String, Model> modelMap = new HashMap();
    private final Map<String, EntityRenderer> rendererMap = new HashMap();
    private final Map<String, Entity> hollowEntityMap = new HashMap();
    /** Creatures whose render state cannot be made for a statue (the client scene checks that none of ours is here) */
    public static final java.util.Set<String> UNPOSED = new java.util.HashSet<>();
    private PigModel pigModel;
    private final EntityRendererProvider.Context context;

    public RenderStoneStatue(EntityRendererProvider.Context context) {
        super(context);
        this.context = context;
    }

    public @NotNull Identifier getTextureLocation(@NotNull EntityStoneStatue entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    protected void preRenderCallback(EntityStoneStatue entity, PoseStack matrixStackIn, float partialTickTime) {
        float scale = entity.getIafScale() < 0.01F ? 1F : entity.getIafScale();
        matrixStackIn.scale(scale, scale, scale);
    }

    @Override
    protected void renderLegacy(EntityStoneStatue entityIn, float entityYaw, float partialTicks, @NotNull PoseStack matrixStackIn, @NotNull MultiBufferSource bufferIn, int packedLightIn) {
        String key = entityIn.getTrappedEntityTypeString();
        Model model = modelMap.get(key);
        // the renderer the model came from (its render state poses the model), null for the fallback models
        EntityRenderer renderer = rendererMap.get(key);

        // Get the correct model
        if (model == null) {
            EntityRenderer found = Minecraft.getInstance().getEntityRenderDispatcher().renderers.get(entityIn.getTrappedEntityType());

            if (found instanceof RenderLayerParent) {
                model = ((RenderLayerParent<?, ?>) found).getModel();
                renderer = found;
                rendererMap.put(key, found);
            } else if (entityIn.getTrappedEntityType() == EntityTypes.PLAYER) {
                model = new ModelStonePlayer(context.bakeLayer(ModelLayers.PLAYER));
            } else {
                // baked once: 1.20.1 baked a pig model every frame for every statue
                if (pigModel == null) {
                    pigModel = new PigModel(context.bakeLayer(ModelLayers.PIG));
                }
                model = pigModel;
            }
            modelMap.put(key, model);
        }
        if (model == null)
            return;

        Entity fakeEntity = this.hollowEntityMap.get(key);
        if (fakeEntity == null) {
            fakeEntity = entityIn.getTrappedEntityType().create(Minecraft.getInstance().level, EntitySpawnReason.LOAD);
            if (fakeEntity != null) {
                // never added to the level: 26.2 throws on the id of such an entity, which the render state reads
                fakeEntity.setId(-1 - this.hollowEntityMap.size());
                try {
                    Values.load(fakeEntity, entityIn.getTrappedTag());
                } catch (Exception e) {
                    IceAndFire.LOGGER.warn("Mob " + key + " could not build statue NBT");
                }
                this.hollowEntityMap.put(key, fakeEntity);
            }
        }
        RenderType tex = IafRenderType.getStoneMobRenderType(200, 200);
        if (fakeEntity instanceof EntityTroll) {
            tex = RenderTypes.entityCutoutCull(((EntityTroll) fakeEntity).getTrollType().TEXTURE_STONE);
        }

        VertexConsumer ivertexbuilder = bufferIn.getBuffer(tex);


        matrixStackIn.pushPose();
        float yaw = entityIn.yRotO + (entityIn.getYRot() - entityIn.yRotO) * partialTicks;
        boolean shouldSit = entityIn.isPassenger() && (entityIn.getVehicle() != null && dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.shouldRiderSit(entityIn.getVehicle()));
        if (model instanceof BasicEntityModel basic) {
            basic.young = entityIn.isBaby();
            basic.riding = shouldSit;
            basic.attackTime = entityIn.getAttackAnim(partialTicks);
        }
        if (model instanceof AdvancedEntityModel) {
            ((AdvancedEntityModel) model).resetToDefaultPose();
        } else if (fakeEntity != null && renderer != null && !UNPOSED.contains(key)) {
            // 1.21.1: setupAnim(fakeEntity, 0, 0, -0.1, 0, 0); vanilla models pose from a render state on 26.x
            try {
                EntityRenderState state = renderer.createRenderState(fakeEntity, 0.0F);
                state.ageInTicks = -0.1F;
                if (state instanceof LivingEntityRenderState living) {
                    living.walkAnimationPos = 0.0F;
                    living.walkAnimationSpeed = 0.0F;
                    living.yRot = 0.0F;
                    living.xRot = 0.0F;
                }
                model.setupAnim(state);
            } catch (RuntimeException e) {
                // a creature (of another mod) whose renderer needs more than a copy outside the level: its rest pose
                UNPOSED.add(key);
                IceAndFire.LOGGER.warn("Statue of {} drawn in the rest pose: {}", key, e.toString());
            }
        }
        preRenderCallback(entityIn, matrixStackIn, partialTicks);
        matrixStackIn.translate(0, 1.5F, 0);
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(180.0F));
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(yaw));
        if (model instanceof ICustomStatueModel && fakeEntity != null) {
            ((ICustomStatueModel) model).renderStatue(matrixStackIn, ivertexbuilder, packedLightIn, fakeEntity);
            if (model instanceof ModelHydraBody && fakeEntity instanceof EntityHydra) {
                LayerHydraHead.renderHydraHeads((ModelHydraBody) model, true, matrixStackIn, bufferIn, packedLightIn, (EntityHydra) fakeEntity, 0, 0, partialTicks, 0, 0, 0);
            }
        } else {
            com.github.alexthe666.iceandfire.client.render.IafModels.render(model, matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, -1);
        }

        matrixStackIn.popPose();

        if (entityIn.getCrackAmount() >= 1) {
            int i = Mth.clamp(entityIn.getCrackAmount() - 1, 0, DESTROY_STAGES.length - 1);
            RenderType crackTex = IafRenderType.getStoneCrackRenderType(DESTROY_STAGES[i]);
            VertexConsumer ivertexbuilder2 = bufferIn.getBuffer(crackTex);
            matrixStackIn.pushPose();
            matrixStackIn.pushPose();
            preRenderCallback(entityIn, matrixStackIn, partialTicks);
            matrixStackIn.translate(0, 1.5F, 0);
            matrixStackIn.mulPose(Axis.XP.rotationDegrees(180.0F));
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(yaw));
            if (model instanceof ICustomStatueModel) {
                ((ICustomStatueModel) model).renderStatue(matrixStackIn, ivertexbuilder2, packedLightIn, fakeEntity);
            } else {
                com.github.alexthe666.iceandfire.client.render.IafModels.render(model, matrixStackIn, ivertexbuilder2, packedLightIn, OverlayTexture.NO_OVERLAY, -1);
            }
            matrixStackIn.popPose();
            matrixStackIn.popPose();
        }
        //super.render(entityIn, entityYaw, ARGB.colorFromFloat(packedLightIn, partialTicks, matrixStackIn, bufferIn));
    }
}