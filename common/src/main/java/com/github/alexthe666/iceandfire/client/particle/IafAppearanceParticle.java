package com.github.alexthe666.iceandfire.client.particle;

import com.github.alexthe666.iceandfire.client.render.IafBufferSource;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;

/**
 * A mob model in front of the camera (the ghost's scare, the siren's charm), like vanilla's elder guardian.
 * 26.x draws particles by group: the loaders register {@link #GROUP} (NeoForge RegisterParticleGroupsEvent, Fabric a mixin).
 */
public abstract class IafAppearanceParticle extends Particle {
    public static final ParticleRenderType GROUP = new ParticleRenderType("iceandfire_appearances");

    protected IafAppearanceParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @Override
    public ParticleRenderType getGroup() {
        return GROUP;
    }

    /** Draws the model (camera space, 1.21.1's render) into the recorder */
    protected abstract void renderAppearance(IafBufferSource buffers, Camera camera, float partialTicks);

    public static class Group extends ParticleGroup<IafAppearanceParticle> {
        public Group(ParticleEngine engine) {
            super(engine);
        }

        @Override
        public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
            IafBufferSource buffers = new IafBufferSource();
            for (IafAppearanceParticle particle : this.particles) {
                particle.renderAppearance(buffers, camera, partialTickTime);
            }
            return (collector, cameraState) -> buffers.submit(collector);
        }
    }
}
