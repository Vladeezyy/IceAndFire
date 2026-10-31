package com.github.alexthe666.iceandfire.client.render.entity;

import com.github.alexthe666.iceandfire.client.render.entity.layer.LayerGenericGlowing;
import com.github.alexthe666.iceandfire.entity.EntityDreadHorse;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class RenderDreadHorse extends MobRenderer<EntityDreadHorse, HorseModel<EntityDreadHorse>> {
    public static final Identifier TEXTURE = Identifier.parse("iceandfire:textures/models/dread/dread_knight_horse.png");
    public static final Identifier TEXTURE_EYES = Identifier.parse("iceandfire:textures/models/dread/dread_knight_horse_eyes.png");

    public RenderDreadHorse(EntityRendererProvider.Context context) {
        super(context, new HorseModel<>(context.bakeLayer(ModelLayers.HORSE)), 0.75F);
        this.addLayer(new LayerGenericGlowing(this, TEXTURE_EYES));
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull EntityDreadHorse entity) {
        return TEXTURE;
    }
}
