package com.github.alexthe666.iceandfire.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;

/**
 * The mod's particle textures (textures/particles/*.png) as sprites of the particle atlas
 * (assets/minecraft/atlases/particles.json): 26.x particles draw one quad from an atlas sprite, 1.21.1's bound the
 * texture and drew the quad themselves.
 */
public final class IafParticleSprites {
    private IafParticleSprites() {}

    /** The sprite of {@code iceandfire:textures/particles/<name>.png} */
    public static TextureAtlasSprite get(Identifier texture) {
        String path = texture.getPath();
        if (path.startsWith("textures/")) {
            path = path.substring("textures/".length());
        }
        if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - ".png".length());
        }
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.PARTICLES).getSprite(Identifier.fromNamespaceAndPath(texture.getNamespace(), path));
    }
}
