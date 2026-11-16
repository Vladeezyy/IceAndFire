package com.github.alexthe666.iceandfire.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

/**
 * The mod's particle textures (textures/particles/*.png) as sprites of the particle atlas
 * (assets/minecraft/atlases/particles.json): the quads then go into the particle engine's one batch; they bound their
 * own texture and drew each quad alone (one draw call per particle, hundreds in a dragon's breath).
 */
public final class IafParticleSprites {
    private IafParticleSprites() {}

    /** The sprite of {@code iceandfire:textures/particles/<name>.png} */
    public static TextureAtlasSprite get(ResourceLocation texture) {
        String path = texture.getPath();
        if (path.startsWith("textures/")) {
            path = path.substring("textures/".length());
        }
        if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - ".png".length());
        }
        // the particle atlas belongs to the particle engine, which registers it with the texture manager (not in ModelManager's atlases on 1.21.1)
        TextureAtlas atlas = (TextureAtlas) Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_PARTICLES);
        return atlas.getSprite(ResourceLocation.fromNamespaceAndPath(texture.getNamespace(), path));
    }
}
