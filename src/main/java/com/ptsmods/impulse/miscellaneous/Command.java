package com.ptsmods.impulse.miscellaneous;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.dv8tion.jda.core.Permission;

@Retention(RUNTIME)
@Target(METHOD)
public @interface Command {

	public String name();
	public String help();
	public String category();
	public boolean guildOnly() default false;
	public boolean hidden() default false;
	public String[] aliases() default {};
	public String arguments() default "";
	public Permission[] botPermissions() default {};
	public Permission[] userPermissions() default {};
	public int cooldown() default 1;
	public boolean ownerCommand() default false;
	public String requiredRole() default "";

}