package com.github.alexthe666.iceandfire.entity.util;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;

import java.util.function.Consumer;

/**
 * 1.21.1's {@code SimpleContainer.addListener}: 26.x containers have no listeners, this one calls back on every change
 * once {@link #listen} was called (as 1.21.1 added the listener after copying the old inventory in).
 */
public class ListenedContainer extends SimpleContainer {
    private final Consumer<Container> listener;
    private boolean listening;

    public ListenedContainer(int size, Consumer<Container> listener) {
        super(size);
        this.listener = listener;
    }

    public void listen() {
        this.listening = true;
    }

    public void stopListening() {
        this.listening = false;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.listening) {
            this.listener.accept(this);
        }
    }
}
