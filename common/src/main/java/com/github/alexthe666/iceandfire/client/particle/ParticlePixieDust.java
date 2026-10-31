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

public class ParticlePixieDust extends SingleQuadParticle {
    private static final Identifier PIXIE_DUST = Identifier.parse("iceandfire:textures/particles/pixie_dust.png");
    float reddustParticleScale;

    public ParticlePixieDust(ClientLevel worldIn, double xCoordIn, double yCoordIn, double zCoordIn, float p_i46349_8_, float p_i46349_9_, float p_i46349_10_) {
        this(worldIn, xCoordIn, yCoordIn, zCoordIn, 1F, p_i46349_8_, p_i46349_9_, p_i46349_10_);
    }

    protected ParticlePixieDust(ClientLevel worldIn, double xCoordIn, double yCoordIn, double zCoordIn, float scale, float red, float green, float blue) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D, IafParticleSprites.get(PIXIE_DUST));
        this.xd *= 0.10000000149011612D;
        this.yd *= 0.10000000149011612D;
        this.zd *= 0.10000000149011612D;
        float f = (float) Math.random() * 0.4F + 0.6F;
        this.rCol = ((float) (Math.random() * 0.20000000298023224D) + 0.8F) * red * f;
        this.gCol = ((float) (Math.random() * 0.20000000298023224D) + 0.8F) * green * f;
        this.bCol = ((float) (Math.random() * 0.20000000298023224D) + 0.8F) * blue * f;
        this.quadSize *= scale;
        this.reddustParticleScale = this.quadSize;
        this.lifetime = (int) (8.0D / (Math.random() * 0.8D + 0.2D));
        this.lifetime = (int) ((float) this.lifetime * scale);
    }

    @Override
    public void extract(QuadParticleRenderState iafRenderState, Camera renderInfo, float partialTicks) {
        float scaley = ((float) this.age + partialTicks) / (float) this.lifetime * 32.0F;
        scaley = Mth.clamp(scaley, 0.0F, 1.0F);
        this.quadSize = this.reddustParticleScale * scaley;

        float width = quadSize * 0.09F;
        if (age > this.getLifetime()) {
            this.remove();
        }
        super.extract(iafRenderState, renderInfo, partialTicks);
    }


    @Override
    protected int getLightColor(float partialTick) {
        return 240;
    }

    public void onUpdate() {
        this.xo = x;
        this.yo = y;
        this.zo = z;

        if (this.age++ >= this.lifetime) {
            this.remove();
        }

        this.move(this.xd, this.yd, this.zd);

        if (this.y == this.yo) {
            this.xd *= 1.1D;
            this.zd *= 1.1D;
        }

        this.xd *= 0.9599999785423279D;
        this.yd *= 0.9599999785423279D;
        this.zd *= 0.9599999785423279D;

        if (this.onGround) {
            this.xd *= 0.699999988079071D;
            this.zd *= 0.699999988079071D;
        }
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }
}
