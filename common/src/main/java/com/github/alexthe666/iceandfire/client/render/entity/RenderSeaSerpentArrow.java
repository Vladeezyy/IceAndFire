package com.github.alexthe666.iceandfire.client.render.entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class RenderSeaSerpentArrow extends ArrowRenderer<AbstractArrow, ArrowRenderState> {
    private static final Identifier TEXTURE = Identifier.parse("iceandfire:textures/models/misc/sea_serpent_arrow.png");

    public RenderSeaSerpentArrow(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }

    @Override
    protected @NotNull Identifier getTextureLocation(@NotNull ArrowRenderState state) {
        return TEXTURE;
    }

}