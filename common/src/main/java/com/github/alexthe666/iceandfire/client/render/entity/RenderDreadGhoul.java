package com.github.alexthe666.iceandfire.client.render.entity;

import com.github.alexthe666.iceandfire.client.model.ModelDreadGhoul;
import com.github.alexthe666.iceandfire.client.render.entity.layer.LayerGenericGlowing;
import com.github.alexthe666.iceandfire.entity.EntityDreadGhoul;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class RenderDreadGhoul extends MobRenderer<EntityDreadGhoul, ModelDreadGhoul> {

    public static final Identifier TEXTURE_EYES = Identifier.parse("iceandfire:textures/models/dread/dread_ghoul_eyes.png");

    public static final Identifier TEXTURE_0 = Identifier.parse("iceandfire:textures/models/dread/dread_ghoul_closed_1.png");
    public static final Identifier TEXTURE_1 = Identifier.parse("iceandfire:textures/models/dread/dread_ghoul_closed_2.png");
    public static final Identifier TEXTURE_2 = Identifier.parse("iceandfire:textures/models/dread/dread_ghoul_closed_3.png");
    public static final Identifier TEXTURE_0_MID = Identifier.parse("iceandfire:textures/models/dread/dread_ghoul_mid_1.png");
    public static final Identifier TEXTURE_1_MID = Identifier.parse("iceandfire:textures/models/dread/dread_ghoul_mid_2.png");
    public static final Identifier TEXTURE_2_MID = Identifier.parse("iceandfire:textures/models/dread/dread_ghoul_mid_3.png");
    public static final Identifier TEXTURE_0_OPEN = Identifier.parse("iceandfire:textures/models/dread/dread_ghoul_open_1.png");
    public static final Identifier TEXTURE_1_OPEN = Identifier.parse("iceandfire:textures/models/dread/dread_ghoul_open_2.png");
    public static final Identifier TEXTURE_2_OPEN = Identifier.parse("iceandfire:textures/models/dread/dread_ghoul_open_3.png");

    public RenderDreadGhoul(EntityRendererProvider.Context context) {
        super(context, new ModelDreadGhoul(0.0F), 0.5F);
        this.addLayer(new LayerGenericGlowing<>(this, TEXTURE_EYES));
    }


    @Override
    protected void scale(EntityDreadGhoul entity, PoseStack matrixStackIn, float partialTickTime) {
        float scale = (entity.getSize() < 0.01F ? 1F : entity.getSize()) * VanillaScale.undo(entity);
        matrixStackIn.scale(scale, scale, scale);
    }

    @Override
    public @NotNull Identifier getTextureLocation(EntityDreadGhoul ghoul) {
        if (ghoul.getScreamStage() == 2) {
            switch (ghoul.getVariant()) {
                case 1:
                    return TEXTURE_1_OPEN;
                case 2:
                    return TEXTURE_2_OPEN;
                default:
                    return TEXTURE_0_OPEN;
            }
        } else if (ghoul.getScreamStage() == 1) {
            switch (ghoul.getVariant()) {
                case 1:
                    return TEXTURE_1_MID;
                case 2:
                    return TEXTURE_2_MID;
                default:
                    return TEXTURE_0_MID;
            }
        } else {
            switch (ghoul.getVariant()) {
                case 1:
                    return TEXTURE_1;
                case 2:
                    return TEXTURE_2;
                default:
                    return TEXTURE_0;
            }
        }

    }

}
