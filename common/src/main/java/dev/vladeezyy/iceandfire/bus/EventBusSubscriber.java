package dev.vladeezyy.iceandfire.bus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks a class with {@link SubscribeEvent} methods; {@code value = Dist.CLIENT} puts them in the client index */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface EventBusSubscriber {
	Dist[] value() default {};

	String modid() default "";

	Bus bus() default Bus.GAME;

	enum Bus {
		GAME, MOD
	}
}
