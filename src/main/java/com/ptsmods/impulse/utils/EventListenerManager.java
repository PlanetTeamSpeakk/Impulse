package com.ptsmods.impulse.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ptsmods.impulse.Main;
import com.ptsmods.impulse.miscellaneous.SubscribeEvent;

import net.dv8tion.jda.core.events.Event;

public class EventListenerManager {

	private static Map<Class, List<Method>> listeners = new HashMap();

	public static void registerListenersFromClass(Class clazz) {
		for (Method method : Main.getMethods(clazz))
			if (method.isAnnotationPresent(SubscribeEvent.class) && method.getParameterCount() == 1 && Main.isSuperClass(method.getParameterTypes()[0], Event.class) && Modifier.isStatic(method.getModifiers()))
				listeners.put(method.getParameterTypes()[0], Main.add(listeners.getOrDefault(method.getParameterTypes()[0], new ArrayList()), method));
	}

	public static <E extends Event> void postEvent(E event) {
		if (Main.done())
			for (Method method : listeners.getOrDefault(event.getClass(), new ArrayList<>()))
				try {
					method.setAccessible(true);
					method.invoke(null, event);
				} catch (InvocationTargetException e) {
					e.getCause().printStackTrace();
				} catch (IllegalAccessException | IllegalArgumentException e) {
					e.printStackTrace();
				}
	}

}
