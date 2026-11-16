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

public class ParticleDreadPortal extends TextureSheetParticle {
    private static final ResourceLocation SNOWFLAKE = ResourceLocation.parse("iceandfire:textures/particles/snowflake_0.png");
    private static final ResourceLocation SNOWFLAKE_BIG = ResourceLocation.parse("iceandfire:textures/particles/snowflake_1.png");

    private final boolean big;

    public ParticleDreadPortal(ClientLevel world, double x, double y, double z, double motX, double motY, double motZ, float size) {
        super(world, x, y, z, motX, motY, motZ);
        this.setPos(x, y, z);
        big = random.nextBoolean();
        this.setSprite(IafParticleSprites.get(big ? SNOWFLAKE_BIG : SNOWFLAKE));
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        quadSize = 0.125F * (this.lifetime - (this.age));
        quadSize = quadSize * 0.09F;
        if (age > this.getLifetime()) {
            this.remove();
        }

        super.render(buffer, renderInfo, partialTicks);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }


    @Override
    public int getLightColor(float partialTick) {
        return 240;
    }

    public int getFXLayer() {
        return 3;
    }


}
