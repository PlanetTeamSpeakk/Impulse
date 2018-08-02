package com.impulsebot.utils.commands;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.common.collect.Lists;
import com.impulsebot.Main;
import com.impulsebot.Main.LogType;
import com.impulsebot.utils.ArrayMap;
import com.impulsebot.utils.EventListenerManager;

public class CommandManager {

	private static Map<String, String>					loadedExtensions	= new ArrayMap();
	private static List<String>							categories			= new ArrayList();
	private static List<CommandContainer>				commands			= new ArrayList<>();
	private static List<CommandContainer>				subcommands			= new ArrayList<>();
	private static Map<String, List<CommandContainer>>	linkedSubcommands	= new HashMap();
	private static List<ExtensionContainer>				extensions			= new ArrayList<>();

	private CommandManager() {
	}

	public static void loadCommand(Method method, ExtensionContainer extension) {
		if (method.isAnnotationPresent(Command.class)) {
			if (Lists.newArrayList(method.getParameterTypes()).equals(Lists.newArrayList(CommandEvent.class))) {
				commands.add(new CommandContainer(method, extension));
				if (!categories.contains(method.getAnnotation(Command.class).category())) categories.add(method.getAnnotation(Command.class).category());
				categories.sort(null);
			} else Main.print(LogType.DEBUG, "Found a command that requires more than just a CommandEvent. " + method);
		} else if (method.isAnnotationPresent(Subcommand.class)) if (Lists.newArrayList(method.getParameterTypes()).equals(Lists.newArrayList(CommandEvent.class)))
			try {
				Class parent = Class.forName(Main.joinCustomChar(".", Main.removeArg(method.getAnnotation(Subcommand.class).parent().split("\\."), method.getAnnotation(Subcommand.class).parent().split("\\.").length - 1)), false, ClassLoader.getSystemClassLoader());
				boolean found = false;
				for (Method method0 : Main.getMethods(parent))
					if (method0.getName().equals(method.getAnnotation(Subcommand.class).parent().split("\\.")[method.getAnnotation(Subcommand.class).parent().split("\\.").length - 1]) && (method0.isAnnotationPresent(Subcommand.class) || method0.isAnnotationPresent(Command.class))) {
						subcommands.add(new CommandContainer(method, extension));
						found = true;
					}
				if (!found) throw new Exception();
			} catch (Exception e) {
				Main.print(LogType.DEBUG, "Found a subcommand that has an invalid parent. " + method);
			}
		else Main.print(LogType.DEBUG, "Found a subcommand that requires more than just a CommandEvent. " + method);
	}

	public static void loadCommandsFromClass(Class clazz, ExtensionContainer extension) {
		Main.runAsynchronously(() -> {
			// initializing class asynchronously.
			try {
				Class.forName(clazz.getName());
			} catch (Throwable e) {
				Main.print(LogType.DEBUG, clazz);
				e.printStackTrace();
			}
		});
		EventListenerManager.registerListenersFromClass(clazz);
		for (Method method : Main.getMethods(clazz))
			loadCommand(method, extension);
	}

	public static void checkExtensions() throws IOException {
		File extDir = new File("extensions/");
		if (!extDir.exists() || !extDir.isDirectory()) extDir.mkdir();
		for (File file : extDir.listFiles())
			if (file.getName().endsWith(".jar") && (!loadedExtensions.containsKey(file.toString()) || !loadedExtensions.get(file.toString()).equals(Main.getMD5(file)))) {
				String mainClassName = "";
				ZipFile zipFile = new ZipFile(file);
				List<ZipEntry> entries = new ArrayList();
				Enumeration<? extends ZipEntry> entriesEnum = zipFile.entries();
				while (entriesEnum.hasMoreElements())
					entries.add(entriesEnum.nextElement());
				for (ZipEntry entry : entries)
					if (entry.getName().endsWith("MANIFEST.MF")) {
						for (String line : Main.convertStreamToString(zipFile.getInputStream(entry)).split(System.lineSeparator()))
							if (line.startsWith("Main-Class: ")) mainClassName = line.substring(12);
						break;
					}
				try {
					Main.addJarToClassPath(file);
				} catch (Exception e) {
					zipFile.close();
					throw new IOException(e);
				}
				loadedExtensions.put(file.toString(), Main.getMD5(file)); // on Linux files used by processes can be changed while on Windows they can't.
				if (!mainClassName.isEmpty()) {
					Class mainClass = null;
					try {
						mainClass = Class.forName(mainClassName);
					} catch (ClassNotFoundException e) {
						Main.print(LogType.INFO, "File", file.getName(), "was added to the classpath, but its main class set in its manifest could not be found and thus not checked if it's an extension.");
						zipFile.close();
						continue;
					}
					if (Extension.class.isAssignableFrom(mainClass)) {
						Extension instance = null;
						for (Constructor con : mainClass.getDeclaredConstructors())
							if (con.getParameterTypes().length == 0) {
								con.setAccessible(true);
								try {
									instance = (Extension) con.newInstance();
								} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
									Main.print(LogType.ERROR, "File", file.getName(), "is an extension, but its constructor which takes no parameters threw an error when executing.");
									e.printStackTrace();
									continue;
								}
							}
						if (instance == null) {
							Main.print(LogType.WARN, "File", file.getName(), "is an extension, but it has no constructor which takes no parameters and thus cannot be used.");
							continue;
						}
						ExtensionContainer container = new ExtensionContainer(instance, mainClass, file);
						for (Class clazz : Main.getClassesInPackage(instance.getCommandsPackage()))
							loadCommandsFromClass(clazz, container);
						extensions.add(container);
						loadedExtensions.put(file.toString(), Main.getMD5(file));
						Main.print(LogType.SUCCESSFUL, "Successfully loaded", file.getName(), "and found an Extension named", instance.getName(), "of version", instance.getVersion() + ".");
					} else Main.print(LogType.WARN, "File", file.getName(), "was added to the classpath, but it's not an extension since its main class,", mainClassName + ", doesn't implement Extension.");
				} else Main.print(LogType.INFO, "File", file.getName(), "was added to the classpath, but it can't be checked if it's an extension since its manifest doesn't contain a Main-Class variable.");
				zipFile.close();
			}
	}

	public static void initialize() throws IOException {
		for (Class clazz : Main.getClassesInPackage("com.impulsebot.commands"))
			loadCommandsFromClass(clazz, new ExtensionContainer(new Main(), Main.class, Main.getJarFile()));
		checkExtensions();
		for (CommandContainer subcommand : subcommands)
			if (!subcommand.isSubcommand()) Main.print(LogType.DEBUG, subcommand.toMethod(), "is not a subcommand while it is in the subcommands list.");
		for (CommandContainer subcommand : subcommands) {
			Method parentCommand = Main.getParentCommand(subcommand.toSubcommand());
			if (!linkedSubcommands.containsKey(parentCommand.toString()))
				linkedSubcommands.put(parentCommand.toString(), Lists.newArrayList(subcommand));
			else((List) linkedSubcommands.get(parentCommand.toString())).add(subcommand);
		}
		for (CommandContainer command : commands)
			if (!linkedSubcommands.containsKey(command.toMethod().toString())) linkedSubcommands.put(command.toMethod().toString(), new ArrayList());
	}

	public static List<CommandContainer> getCommands() {
		return Collections.unmodifiableList(commands);
	}

	public static List<CommandContainer> getSubcommands() {
		return Collections.unmodifiableList(subcommands);
	}

	public static List<ExtensionContainer> getExtensions() {
		return Collections.unmodifiableList(extensions);
	}

	public static List<CommandContainer> getSubcommands(CommandContainer parent) {
		return getSubcommands(parent.toMethod());
	}

	public static List<CommandContainer> getSubcommands(Method parent) {
		return Collections.unmodifiableList(linkedSubcommands.getOrDefault(parent.toString(), new ArrayList()));
	}

	public static List<String> getCategories() {
		return Collections.unmodifiableList(categories);
	}
}
