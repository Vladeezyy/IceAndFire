package com.github.alexthe666.iceandfire.client.render.entity;

import com.github.alexthe666.iceandfire.entity.EntityDreadHorse;
import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/** The dread knight's horse: vanilla's horse model with the dread texture and glowing eyes (26.x horse renderer) */
public class RenderDreadHorse extends AbstractHorseRenderer<EntityDreadHorse, EquineRenderState, AbstractEquineModel<EquineRenderState>> {
    public static final Identifier TEXTURE = Identifier.parse("iceandfire:textures/models/dread/dread_knight_horse.png");
    public static final Identifier TEXTURE_EYES = Identifier.parse("iceandfire:textures/models/dread/dread_knight_horse_eyes.png");
    private static final RenderType EYES = RenderTypes.eyes(TEXTURE_EYES);

    public RenderDreadHorse(EntityRendererProvider.Context context) {
        super(context, new HorseModel(context.bakeLayer(ModelLayers.HORSE)), new HorseModel(context.bakeLayer(ModelLayers.HORSE_BABY)));
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return EYES;
            }
        });
    }

    @Override
    public Identifier getTextureLocation(EquineRenderState state) {
        return TEXTURE;
    }

    @Override
    public EquineRenderState createRenderState() {
        return new EquineRenderState();
    }
}
