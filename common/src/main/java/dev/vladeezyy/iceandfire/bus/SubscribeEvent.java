package dev.vladeezyy.iceandfire.bus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** A listener of {@link IafBus}; {@code tools/port_events.py} lists them in the event index */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface SubscribeEvent {
	EventPriority priority() default EventPriority.NORMAL;
}
