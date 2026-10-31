package com.github.alexthe666.iceandfire.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ParticleDreadTorch extends SingleQuadParticle {
    private static final Identifier SNOWFLAKE = Identifier.parse("iceandfire:textures/particles/snowflake_0.png");
    private static final Identifier SNOWFLAKE_BIG = Identifier.parse("iceandfire:textures/particles/snowflake_1.png");

    private final boolean big;

    public ParticleDreadTorch(ClientLevel world, double x, double y, double z, double motX, double motY, double motZ, float size) {
        super(world, x, y, z, motX, motY, motZ, IafParticleSprites.get(SNOWFLAKE));
        this.setPos(x, y, z);
        this.yd += 0.01D;
        big = random.nextBoolean();
    }

    @Override
    public void extract(QuadParticleRenderState iafRenderState, Camera renderInfo, float partialTicks) {
        this.setSprite(IafParticleSprites.get(big ? SNOWFLAKE_BIG : SNOWFLAKE));
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
    protected int getLightCoords(float partialTick) {
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
