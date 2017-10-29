package com.ptsmods.impulse.miscellaneous;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ptsmods.impulse.Main;
import com.ptsmods.impulse.Main.LogType;
import com.ptsmods.impulse.utils.Config;
import com.ptsmods.impulse.utils.DataIO;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.impl.MessageImpl;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class EventHandler extends ListenerAdapter {

	private static List<String> prefixedMessages = new ArrayList();

	@Override
	public void onGenericEvent(Event event) {
		for (Class clazz : Main.getAllEvents())
			if (event.getClass().getName().equals(clazz.getName()))
				for (Method cmd : Main.getCommands()) {
					Method method = Main.getMethod(cmd.getDeclaringClass(), "on" + clazz.getSimpleName().replaceAll("Event", ""), clazz);
					if (method != null && method.isAnnotationPresent(SubscribeEvent.class)) {
						method.setAccessible(true);
						Main.runAsynchronously(() -> {
							try {
								method.invoke(cmd.getDeclaringClass().newInstance(), event);
							} catch (InvocationTargetException e) {
								e.getCause().printStackTrace();
							} catch (IllegalAccessException | IllegalArgumentException | InstantiationException e) {
								e.printStackTrace();
							}
						});
					}
				}
	}

	@Override
	public void onReady(ReadyEvent event) {
		Main.print(LogType.INFO, "Shard " + event.getJDA().getShardInfo().getShardId() + " ready!");
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		Main.addReceivedMessage(event.getMessage());
		Main.runAsynchronously(new Runnable() {@Override public void run() {System.gc();}});
		String prefix = Config.getValue("prefix");
		if (Main.devMode() && !event.getAuthor().getId().equals(Config.getValue("ownerId")) && event.getMessage().getContent().startsWith(prefix)) {
			event.getChannel().sendMessage("Developer mode is turned on, so only my owner can use commands.").complete();
			return;
		}
		try {
			Map prefixes = DataIO.loadJson("data/mod/serverprefixes.json", Map.class);
			prefixes = prefixes == null ? new HashMap<>() : prefixes;
			String serverPrefix = Config.getValue("prefix");
			try {
				if (prefixes.containsKey(event.getGuild().getId())) serverPrefix = (String) ((Map) prefixes.get(event.getGuild().getId())).get("serverPrefix");
			} catch (NullPointerException e) { }
			if (event.getMessage().getContent().startsWith(Config.getValue("prefix")) && !serverPrefix.equals(Config.getValue("prefix")) && !prefixedMessages.contains(event.getMessage().getId())) return;
			if (event.getMessage().getContent().startsWith(serverPrefix) && !serverPrefix.equals(Config.getValue("prefix"))) {
				prefixedMessages.add(event.getMessageId());
				onMessageReceived(
						new MessageReceivedEvent(
								event.getJDA(),
								event.getResponseNumber(),
								((MessageImpl) event.getMessage()).setContent(Config.getValue("prefix") + event.getMessage().getRawContent().substring(serverPrefix.length())))
						);
				return;
			}
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		if (event.getMessage().getContent().startsWith(prefix) && Main.getCommandNames().contains(event.getMessage().getContent().split(" ")[0].substring(prefix.length())))
			Main.print(LogType.INFO, String.format("%s#%s used the '%s' command in %s.",
					event.getAuthor().getName(),
					event.getAuthor().getDiscriminator(),
					event.getMessage().getContent().substring(prefix.length()).replaceAll("\n", " "),
					event.getGuild() == null ? "private messages" : event.getGuild().getName()));
		Main.executeCommand(() -> {
			try {
				if(event.getAuthor().isBot())
					return;
				String[] parts = null;
				String rawContent = event.getMessage().getRawContent();
				if (rawContent.toLowerCase().startsWith(prefix.toLowerCase()))
					parts = Arrays.copyOf(rawContent.substring(prefix.length()).trim().split("\\s+",2), 2);
				if (parts != null)
					if (event.isFromType(ChannelType.PRIVATE) || event.getTextChannel().canTalk()) {
						String name = parts[0];
						String args = parts[1] == null ? "" : parts[1];
						int i = Main.getCommandIndex().getOrDefault(name.toLowerCase(), -1);
						if (i != -1) {
							Method command = Main.getCommands().get(i);
							while (args.split(" ").length != 0 && !Main.getSubcommands(command).isEmpty()) {
								boolean found = false;
								for (Method subcommand : Main.getSubcommands(command))
									if (subcommand.getAnnotation(Subcommand.class).name().equals(args.split(" ")[0])) {
										command = subcommand;
										args = Main.join(Main.removeArg(args.split(" "), 0));
										found = true;
										break;
									}
								if (!found) break;
							}
							Permission[] permissions = {};
							Permission[] botPermissions = {};
							boolean guildOnly = false;
							boolean ownerCommand = false;
							if (command.isAnnotationPresent(Command.class)) {
								Command annotation = command.getAnnotation(Command.class);
								permissions = annotation.userPermissions();
								botPermissions = annotation.botPermissions();
								guildOnly = annotation.guildOnly();
								ownerCommand = annotation.ownerCommand();
							} else if (command.isAnnotationPresent(Subcommand.class)) {
								Subcommand annotation = command.getAnnotation(Subcommand.class);
								permissions = annotation.userPermissions();
								botPermissions = annotation.botPermissions();
								guildOnly = annotation.guildOnly();
								ownerCommand = annotation.ownerCommand();
							}
							if (event.getGuild() == null && guildOnly) {
								event.getChannel().sendMessage("That command cannot be used in direct messages.").queue();;
							} else if (ownerCommand && !event.getAuthor().getId().equals(Main.getOwner().getId()))
								event.getChannel().sendMessage("That command can only be used by my owner.").queue();
							else if (event.getMember() != null && !event.getMember().hasPermission(permissions)) {
								List<String> nonPresentPerms = new ArrayList();
								for (Permission perm : permissions)
									if (!event.getMember().hasPermission(perm)) nonPresentPerms.add(perm.getName());
								event.getChannel().sendMessage("You need the " + Main.joinCustomChar(", ", nonPresentPerms) + " permissions to use that.").queue();
							} else if (event.getGuild() != null && !event.getGuild().getMember(event.getJDA().getSelfUser()).hasPermission(botPermissions)) {
								List<String> nonPresentPerms = new ArrayList();
								for (Permission perm : permissions)
									if (!event.getMember().hasPermission(perm)) nonPresentPerms.add(perm.getName());
								event.getChannel().sendMessage("I need the " + Main.joinCustomChar(", ", nonPresentPerms) + " permissions to do that.").queue();
							} else {
								CommandEvent cevent = new CommandEvent(event, args, command);
								for (CommandExecutionHook hook : Main.getCommandHooks()) // these are useful for e.g., permissions, blacklists, serverprefixes, logging, etc.
									try {
										hook.run(cevent);
									} catch (SecurityException e) {
										if (e.getMessage() == null || !e.getMessage().isEmpty()) event.getChannel().sendMessage(e.getMessage() == null ? "You are forbidden to use that command." : e.getMessage()).queue();
										return;
									}
								Object obj = null;
								try {
									obj = command.getDeclaringClass().newInstance(); // so commands that aren't static still work.
								} catch (Throwable e) {}
								command.setAccessible(true);
								try {
									command.invoke(obj, cevent);
								} catch (Throwable e) {
									sendStackTrace(e.getCause(), event);
								}
							}
						}
					}
			} catch (Throwable e) {
				sendStackTrace(e, event);
			}
		});
	}

	private static void sendStackTrace(Throwable e, MessageReceivedEvent event) {
		Main.print(LogType.INFO, "Sending stacktrace");
		e.printStackTrace();
		StackTraceElement stElement = null;
		for (StackTraceElement element : e.getStackTrace())
			if (element.getFileName() != null && element.getClassName().startsWith("com.ptsmods.impulse.commands")) stElement = element;
		event.getChannel().sendMessageFormat("A `%s` exception was thrown at line %s in %s while parsing the command%s.%s",
				e.getClass().getName(), stElement.getLineNumber(), stElement.getFileName(), e.getMessage() != null ? String.format(": `%s`", e.getMessage()) : "", Main.devMode() ? "" : String.format("\nMy owner, %s, has been informed.", Main.getOwner().getAsMention())).queue();
		if (!Main.devMode())
			Main.sendPrivateMessage(Main.getOwner(), String.format("A `%s` exception was thrown at line %s in %s while parsing the message `%s`. Stacktrace:\n```java\n%s```",
					e.getClass().getName(), stElement.getLineNumber(), stElement.getFileName(), event.getMessage().getContent(), Main.generateStackTrace(e)));
		Main.print(LogType.INFO, "Stacktrace sent");
	}

}
