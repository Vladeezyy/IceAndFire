package com.github.alexthe666.iceandfire.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ParticleBlood extends SingleQuadParticle {
    private static final Identifier BLOOD = Identifier.parse("iceandfire:textures/particles/blood.png");

    public ParticleBlood(ClientLevel world, double x, double y, double z) {
        super(world, x, y, z, 0, Math.random() * (double) 0.2F + 0.1, 0, IafParticleSprites.get(BLOOD));
        this.setPos(x, y, z);
        this.yd += 0.01D;
    }

    @Override
    public void extract(QuadParticleRenderState iafRenderState, Camera renderInfo, float partialTicks) {
        quadSize = 0.125F * (this.lifetime - (this.age));
        quadSize = quadSize * 0.09F;
        xd *= 0.75D;
        yd *= 0.75D;
        zd *= 0.75D;
        if (age > this.getLifetime()) {
            this.remove();
        }
        super.extract(iafRenderState, renderInfo, partialTicks);
    }


    @Override
    protected int getLightColor(float partialTick) {
        return 240;
    }

    public int getFXLayer() {
        return 3;
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

}
