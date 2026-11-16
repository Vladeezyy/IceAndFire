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

public class ParticleSerpentBubble extends TextureSheetParticle {
    private static final ResourceLocation BUBBLE_TEXTURE = ResourceLocation.parse("iceandfire:textures/particles/sea_serpent_bubble.png");

    public ParticleSerpentBubble(ClientLevel world, double x, double y, double z, double motX, double motY, double motZ, float size) {
        super(world, x, y, z, motX, motY, motZ);
        this.setSprite(IafParticleSprites.get(BUBBLE_TEXTURE));
        this.setPos(x, y, z);
        this.quadSize = 0.3F;
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        if (age > this.getLifetime()) {
            this.remove();
        }

        super.render(buffer, renderInfo, partialTicks);
    }

    @Override
    public int getLightColor(float partialTick) {
        //If uncomment : BlockPos needs integers
//        BlockPos blockpos = new BlockPos(this.x, this.y, this.z);
        return 240;
    }

    public int getFXLayer() {
        return 3;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

}
