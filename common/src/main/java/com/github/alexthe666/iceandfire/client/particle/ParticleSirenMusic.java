package com.github.alexthe666.iceandfire.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ParticleSirenMusic extends TextureSheetParticle {
    private static final ResourceLocation SIREN_MUSIC = ResourceLocation.parse("iceandfire:textures/particles/siren_music.png");

    float noteParticleScale;
    float colorScale;

    public ParticleSirenMusic(ClientLevel world, double x, double y, double z, double motX, double motY, double motZ, float size) {
        super(world, x, y, z, motX, motY, motZ);
        this.setSprite(IafParticleSprites.get(SIREN_MUSIC));
        this.setPos(x, y, z);
        this.colorScale = (float) 1;
        this.rCol = Math.max(0.0F, Mth.sin((colorScale + 0.0F) * 6.2831855F) * 0.65F + 0.35F);
        this.gCol = Math.max(0.0F, Mth.sin((colorScale + 0.33333334F) * 6.2831855F) * 0.65F + 0.35F);
        this.bCol = Math.max(0.0F, Mth.sin((colorScale + 0.6666667F) * 6.2831855F) * 0.65F + 0.35F);
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        if (age > this.getLifetime()) {
            this.remove();
        }

        super.render(buffer, renderInfo, partialTicks);
    }

    @Override
    public void tick() {
        super.tick();
        colorScale += 0.015;
        if (colorScale > 25) {
            colorScale = 0;
        }
        this.rCol = Math.max(0.0F, Mth.sin((colorScale + 0.0F) * 6.2831855F) * 0.65F + 0.35F);
        this.gCol = Math.max(0.0F, Mth.sin((colorScale + 0.33333334F) * 6.2831855F) * 0.65F + 0.35F);
        this.bCol = Math.max(0.0F, Mth.sin((colorScale + 0.6666667F) * 6.2831855F) * 0.65F + 0.35F);

    }

    @Override
    public int getLightColor(float partialTick) {
        return super.getLightColor(partialTick);
    }

    public int getFXLayer() {
        return 3;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

}