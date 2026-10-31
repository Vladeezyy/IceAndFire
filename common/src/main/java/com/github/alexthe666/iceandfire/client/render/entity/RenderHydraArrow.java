package com.github.alexthe666.iceandfire.client.render.entity;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class RenderHydraArrow extends ArrowRenderer {
    private static final Identifier TEXTURES = Identifier.parse("iceandfire:textures/models/misc/hydra_arrow.png");

    public RenderHydraArrow(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull Entity entity) {
        return TEXTURES;
    }

}