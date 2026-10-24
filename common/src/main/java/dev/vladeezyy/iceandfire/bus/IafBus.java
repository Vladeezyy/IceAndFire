package dev.vladeezyy.iceandfire.bus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The mod's own event bus: listeners per event class (exact class, no hierarchy), in priority order. Posting an event
 * nobody listens to costs one map lookup.
 */
public final class IafBus {
	private static final Map<Class<?>, Listener<?>[]> LISTENERS = new IdentityHashMap<>();
	private static final Listener<?>[] NONE = new Listener<?>[0];

	private record Listener<E>(EventPriority priority, Consumer<E> handler) {
	}

	private IafBus() {
	}

	public static synchronized <E extends Event> void add(Class<E> type, EventPriority priority, Consumer<E> handler) {
		List<Listener<?>> list = new ArrayList<>(List.of(LISTENERS.getOrDefault(type, NONE)));
		list.add(new Listener<>(priority, handler));
		list.sort(Comparator.comparing(Listener::priority));
		LISTENERS.put(type, list.toArray(NONE));
	}

	public static boolean hasListeners(Class<? extends Event> type) {
		return LISTENERS.containsKey(type);
	}

	/** Runs the listeners; returns true if the event was canceled */
	@SuppressWarnings("unchecked")
	public static <E extends Event> boolean post(E event) {
		for (Listener<?> listener : LISTENERS.getOrDefault(event.getClass(), NONE)) {
			((Consumer<E>) listener.handler()).accept(event);
		}
		return event.canceled();
	}
}
