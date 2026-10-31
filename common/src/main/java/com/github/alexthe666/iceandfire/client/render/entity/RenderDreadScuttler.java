package com.github.alexthe666.iceandfire.client.render.entity;

import com.github.alexthe666.iceandfire.client.model.ModelDreadScuttler;
import com.github.alexthe666.iceandfire.client.render.entity.layer.LayerGenericGlowing;
import com.github.alexthe666.iceandfire.entity.EntityDreadScuttler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class RenderDreadScuttler extends IafMobRenderer<EntityDreadScuttler, ModelDreadScuttler> {

    public static final Identifier TEXTURE_EYES = Identifier.parse("iceandfire:textures/models/dread/dread_scuttler_eyes.png");
    public static final Identifier TEXTURE = Identifier.parse("iceandfire:textures/models/dread/dread_scuttler.png");

    public RenderDreadScuttler(EntityRendererProvider.Context context) {
        super(context, new ModelDreadScuttler(), 0.75F);
        this.addLayer(new LayerGenericGlowing(this, TEXTURE_EYES));
    }

    @Override
    protected void scale(IafMobRenderState iafState, PoseStack stack) {
        EntityDreadScuttler LivingEntityIn = (EntityDreadScuttler) iafState.pose.entity;
        float partialTickTime = iafState.partialTick;
        float s = LivingEntityIn.getSize() * VanillaScale.undo(LivingEntityIn);
        stack.scale(s, s, s);
    }

    @Override
    protected Identifier getTexture(EntityDreadScuttler beast) {
        return TEXTURE;

    }

}
