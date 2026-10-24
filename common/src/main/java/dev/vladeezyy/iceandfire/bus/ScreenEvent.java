package dev.vladeezyy.iceandfire.bus;

import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;

public abstract class ScreenEvent extends Event {
    @Nullable
    private final Screen screen;

    protected ScreenEvent(@Nullable Screen screen) {
        this.screen = screen;
    }

    @Nullable
    public Screen getScreen() {
        return this.screen;
    }

    /** A screen is about to open; listeners may open another one instead; canceling it keeps the current screen */
    public static class Opening extends ScreenEvent implements ICancellableEvent {
        @Nullable
        private final Screen currentScreen;
        @Nullable
        private Screen newScreen;

        public Opening(@Nullable Screen currentScreen, @Nullable Screen screen) {
            super(screen);
            this.currentScreen = currentScreen;
            this.newScreen = screen;
        }

        @Nullable
        public Screen getCurrentScreen() {
            return this.currentScreen;
        }

        @Nullable
        public Screen getNewScreen() {
            return this.newScreen;
        }

        public void setNewScreen(@Nullable Screen newScreen) {
            this.newScreen = newScreen;
        }
    }
}
