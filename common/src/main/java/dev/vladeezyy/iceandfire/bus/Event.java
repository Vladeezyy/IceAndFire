package dev.vladeezyy.iceandfire.bus;

public abstract class Event {
	private boolean canceled;

	boolean canceled() {
		return this.canceled;
	}

	void cancel(boolean canceled) {
		this.canceled = canceled;
	}
}
