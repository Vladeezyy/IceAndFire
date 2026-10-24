package dev.vladeezyy.iceandfire.bus;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Documentation only (the client classes are listed in the client event index) */
@Retention(RetentionPolicy.SOURCE)
public @interface OnlyIn {
	Dist value();
}
