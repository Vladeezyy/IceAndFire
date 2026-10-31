package com.github.alexthe666.iceandfire.client.render.entity;

import com.github.alexthe666.iceandfire.client.model.ModelHydraBody;
import com.github.alexthe666.iceandfire.client.render.entity.layer.LayerGenericGlowing;
import com.github.alexthe666.iceandfire.client.render.entity.layer.LayerHydraHead;
import com.github.alexthe666.iceandfire.entity.EntityHydra;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class RenderHydra extends MobRenderer<EntityHydra, ModelHydraBody> {

    public static final Identifier TEXUTURE_0 = Identifier.parse("iceandfire:textures/models/hydra/hydra_0.png");
    public static final Identifier TEXUTURE_1 = Identifier.parse("iceandfire:textures/models/hydra/hydra_1.png");
    public static final Identifier TEXUTURE_2 = Identifier.parse("iceandfire:textures/models/hydra/hydra_2.png");
    public static final Identifier TEXUTURE_EYES = Identifier.parse("iceandfire:textures/models/hydra/hydra_eyes.png");

    public RenderHydra(EntityRendererProvider.Context context) {
        super(context, new ModelHydraBody(), 1.2F);
        this.addLayer(new LayerHydraHead(this));
        this.addLayer(new LayerGenericGlowing(this, TEXUTURE_EYES));
    }

    @Override
    public void scale(@NotNull EntityHydra LivingEntityIn, PoseStack stack, float partialTickTime) {
        stack.scale(1.75F, 1.75F, 1.75F);
    }

    @Override
    public @NotNull Identifier getTextureLocation(EntityHydra gorgon) {
        switch (gorgon.getVariant()) {
            default:
                return TEXUTURE_0;
            case 1:
                return TEXUTURE_1;
            case 2:
                return TEXUTURE_2;
        }
    }

}
