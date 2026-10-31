package com.github.alexthe666.iceandfire.item;

import dev.vladeezyy.rampart.server.item.CustomToolMaterial;

/** 1.21.1's {@code TieredItem.getTier()}: tools are plain items with a tool material since 1.21.5 */
public interface IafTieredItem {
    CustomToolMaterial getTier();
}
