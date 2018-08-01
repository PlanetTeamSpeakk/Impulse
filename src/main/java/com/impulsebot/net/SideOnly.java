package com.impulsebot.net;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target({TYPE, FIELD, METHOD, PACKAGE})
public @interface SideOnly {

	public Side value();

	public static enum Side {
		CLIENT,
		SERVER;
	}

}
