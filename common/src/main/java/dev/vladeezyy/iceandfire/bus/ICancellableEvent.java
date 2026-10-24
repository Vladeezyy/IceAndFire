package dev.vladeezyy.iceandfire.bus;

public interface ICancellableEvent {
	default void setCanceled(boolean canceled) {
		((Event) this).cancel(canceled);
	}

	default boolean isCanceled() {
		return ((Event) this).canceled();
	}
}
