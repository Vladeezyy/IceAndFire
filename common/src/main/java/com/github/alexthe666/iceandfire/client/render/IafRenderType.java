package com.github.alexthe666.iceandfire.client.render;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.client.render.tile.RenderDreadPortal;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

/**
 * The mod's render types on 26.x pipelines (1.21.1 built them from render state shards): the ghost's blending, the
 * stone crack overlay and the Dreadlands portal shader; the rest are vanilla types with the same shader and states.
 */
public final class IafRenderType {

    private static final Identifier STONE_TEXTURE = Identifier.parse("textures/block/stone.png");

    /** The vanilla entity pipeline (cutout, no cull, per-face lighting) with the uniforms of vanilla's entity snippet */
    private static RenderPipeline.Builder entity(String location) {
        return RenderPipeline.builder()
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withUniform("Fog", UniformType.UNIFORM_BUFFER)
            .withUniform("Lighting", UniformType.UNIFORM_BUFFER)
            .withLocation(id(location))
            .withVertexShader("core/entity")
            .withFragmentShader("core/entity")
            .withSampler("Sampler0")
            .withSampler("Sampler1")
            .withSampler("Sampler2")
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withShaderDefine("PER_FACE_LIGHTING")
            .withVertexFormat(DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS)
            .withCull(false);
    }

    /** The ghost: premultiplied blending (ONE, ONE_MINUS_SRC_ALPHA) as the original's ghost transparency */
    private static final RenderPipeline GHOST_PIPELINE = entity("pipeline/ghost")
        .withColorTargetState(new ColorTargetState(new BlendFunction(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA)))
        .withDepthStencilState(DepthStencilState.DEFAULT)
        .build();
    /** The crack overlay of a stone statue: translucent, only where the statue already is (equal depth) */
    private static final RenderPipeline STONE_CRACK_PIPELINE = entity("pipeline/stone_crack")
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withDepthStencilState(new DepthStencilState(CompareOp.EQUAL, false))
        .build();
    /** The Dreadlands portal: assets/iceandfire/shaders/core/rendertype_dread_portal.* (5 layers as the original) */
    private static final RenderPipeline DREAD_PORTAL_PIPELINE = RenderPipeline.builder()
        .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
        .withUniform("Projection", UniformType.UNIFORM_BUFFER)
        .withUniform("Globals", UniformType.UNIFORM_BUFFER)
        .withLocation(id("pipeline/dread_portal"))
        .withVertexShader(id("core/rendertype_dread_portal"))
        .withFragmentShader(id("core/rendertype_dread_portal"))
        .withShaderDefine("PORTAL_LAYERS", 5)
        .withSampler("Sampler0")
        .withSampler("Sampler1")
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
        .withDepthStencilState(DepthStencilState.DEFAULT)
        .build();

    private static final RenderType DREADLANDS_PORTAL = RenderType.create("dreadlands_portal", RenderSetup.builder(DREAD_PORTAL_PIPELINE)
        .withTexture("Sampler0", RenderDreadPortal.DREAD_PORTAL_BACKGROUND)
        .withTexture("Sampler1", RenderDreadPortal.DREAD_PORTAL)
        .createRenderSetup());

    private static final Function<Identifier, RenderType> GHOST = Util.memoize(texture -> RenderType.create("ghost_iaf",
        RenderSetup.builder(GHOST_PIPELINE).withTexture("Sampler0", texture).useLightmap().useOverlay().sortOnUpload().createRenderSetup()));
    private static final Function<Identifier, RenderType> STONE_CRACK = Util.memoize(texture -> RenderType.create("stone_entity_type_crack",
        RenderSetup.builder(STONE_CRACK_PIPELINE).withTexture("Sampler0", texture).useLightmap().useOverlay().sortOnUpload().createRenderSetup()));

    private IafRenderType() {
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(IceAndFire.MODID, path);
    }

    /** Pipelines the loader registers (NeoForge: RegisterRenderPipelinesEvent; Fabric precompiles them on use) */
    public static RenderPipeline[] pipelines() {
        return new RenderPipeline[]{GHOST_PIPELINE, STONE_CRACK_PIPELINE, DREAD_PORTAL_PIPELINE};
    }

    public static RenderType getGhost(Identifier locationIn) {
        return GHOST.apply(locationIn);
    }

    /** The ghost by day: vanilla's translucent entity (the original: cutout no-cull shader, translucent, no cull) */
    public static RenderType getGhostDaytime(Identifier locationIn) {
        return RenderTypes.entityTranslucent(locationIn);
    }

    public static RenderType getDreadlandsPortal() {
        return DREADLANDS_PORTAL;
    }

    /** A mob turned to stone: the stone texture with the cutout entity shader, culled (the original's state) */
    public static RenderType getStoneMobRenderType(float x, float y) {
        return RenderTypes.entityCutoutCull(STONE_TEXTURE);
    }

    /** Ice of a frozen mob: the translucent beacon beam shader (as the original) */
    public static RenderType getIce(Identifier locationIn) {
        return RenderTypes.beaconBeam(locationIn, true);
    }

    public static RenderType getStoneCrackRenderType(Identifier crackTex) {
        return STONE_CRACK.apply(crackTex);
    }
}
