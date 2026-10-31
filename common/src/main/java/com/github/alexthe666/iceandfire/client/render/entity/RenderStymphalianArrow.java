package com.github.alexthe666.iceandfire.client.render.entity;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class RenderStymphalianArrow extends ArrowRenderer {
    private static final Identifier TEXTURE = Identifier.parse("iceandfire:textures/models/misc/stymphalian_arrow.png");

    public RenderStymphalianArrow(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull Entity entity) {
        return TEXTURE;
    }
}