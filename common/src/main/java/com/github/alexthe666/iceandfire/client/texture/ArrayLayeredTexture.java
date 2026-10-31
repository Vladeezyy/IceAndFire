package com.github.alexthe666.iceandfire.client.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * The first texture with the others alpha-blended over it (dragon skin + overlays, dragon armor pieces).
 * 26.x: a reloadable texture, loaded with TextureManager#registerAndLoad (register no longer loads).
 */
public class ArrayLayeredTexture extends ReloadableTexture {
    public final List<String> layeredTextureNames;

    public ArrayLayeredTexture(Identifier id, List<String> textureNames) {
        super(id);
        this.layeredTextureNames = textureNames;
    }

    @Override
    public @NotNull TextureContents loadContents(@NotNull ResourceManager manager) throws IOException {
        Iterator<String> iterator = this.layeredTextureNames.iterator();
        TextureContents base = TextureContents.load(manager, Identifier.parse(iterator.next()));
        NativeImage nativeimage = base.image();
        while (iterator.hasNext()) {
            String s1 = iterator.next();
            if (s1 != null) {
                try (TextureContents layer = TextureContents.load(manager, Identifier.parse(s1))) {
                    NativeImage nativeimage1 = layer.image();
                    for (int i = 0; i < Math.min(nativeimage1.getHeight(), nativeimage.getHeight()); i++) {
                        for (int j = 0; j < Math.min(nativeimage1.getWidth(), nativeimage.getWidth()); j++) {
                            nativeimage.setPixel(j, i, ARGB.alphaBlend(nativeimage.getPixel(j, i), nativeimage1.getPixel(j, i)));
                        }
                    }
                }
            }
        }
        return base;
    }
}
