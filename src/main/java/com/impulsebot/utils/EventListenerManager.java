package com.impulsebot.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.impulsebot.Main;
import com.impulsebot.Main.LogType;
import com.impulsebot.miscellaneous.SubscribeEvent;

import net.dv8tion.jda.core.events.Event;

public class EventListenerManager {

	private static Map<Class, List<Method>> listeners = new HashMap();

	public static void registerListenersFromClass(Class clazz) {
		for (Method method : Main.getMethods(clazz))
			registerListener(method);
	}

	public static void registerListener(Method listener) {
		if (listener.isAnnotationPresent(SubscribeEvent.class) && listener.getParameterCount() == 1 && Main.isSuperClass(listener.getParameterTypes()[0], Event.class) && Modifier.isStatic(listener.getModifiers()))
			listeners.put(listener.getParameterTypes()[0], Main.add(listeners.getOrDefault(listener.getParameterTypes()[0], new ArrayList()), listener));
	}

	public static <E extends Event> void postEvent(E event) {
		if (Main.done() && listeners.containsKey(event.getClass()))
			for (Method method : listeners.getOrDefault(event.getClass(), new ArrayList<>())) {
				method.setAccessible(true);
				Main.runAsynchronously(null, method, event);
			}
	}

}
