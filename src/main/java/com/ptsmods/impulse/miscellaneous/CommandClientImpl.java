package com.ptsmods.impulse.miscellaneous;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import com.jagrosh.jdautilities.commandclient.AnnotatedModuleCompiler;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.Main.LogType;
import com.ptsmods.impulse.utils.Config;
import com.ptsmods.impulse.utils.DataIO;

import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.impl.MessageImpl;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandClientImpl extends com.jagrosh.jdautilities.commandclient.impl.CommandClientImpl {

	private final String altprefix;
	private final Game game;
	private OnlineStatus status;
	private String carbonKey;
	private String botsKey;
	private String botsOrgKey;
	private boolean useHelp;
	private Function<CommandEvent, String> helpFunction;
	private ScheduledExecutorService executor;
	private int linkedCacheSize;
	private AnnotatedModuleCompiler compiler;
	private static List<String> prefixedMessages = new ArrayList<>();

	public CommandClientImpl(String ownerId, String[] coOwnerIds, String prefix, String altprefix, Game game,
			OnlineStatus status, String serverInvite, String success, String warning, String error, String carbonKey,
			String botsKey, String botsOrgKey, ArrayList<Command> commands, boolean useHelp,
			Function<CommandEvent, String> helpFunction, String helpWord, ScheduledExecutorService executor,
			int linkedCacheSize, AnnotatedModuleCompiler compiler) {
		super(ownerId, coOwnerIds, prefix, altprefix, game, status, serverInvite, success, warning, error, carbonKey, botsKey,
				botsOrgKey, commands, useHelp, helpFunction, helpWord, executor, linkedCacheSize, compiler);
		this.altprefix = altprefix;
		this.game = game;
		this.status = status;
		this.carbonKey = carbonKey;
		this.botsKey = botsKey;
		this.botsOrgKey = botsOrgKey;
		this.useHelp = useHelp;
		this.helpFunction = helpFunction;
		this.executor = executor;
		this.linkedCacheSize = linkedCacheSize;
		this.compiler = compiler;
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		try {
			Main.runAsynchronously(new Runnable() {@Override public void run() {System.gc();}});
			String ownerId = getOwnerId();
			String[] coOwnerIds = getCoOwnerIds();
			String prefix = getPrefix();
			String altprefix = this.altprefix;
			Game game = this.game;
			OnlineStatus status = this.status;
			String invite = getServerInvite();
			String success = getSuccess();
			String warning = getWarning();
			String error = getError();
			String carbonKey = this.carbonKey;
			String botsKey = this.botsKey;
			String botsOrgKey = this.botsOrgKey;
			ArrayList<Command> commands = (ArrayList<Command>) getCommands();
			Boolean useHelp = this.useHelp;
			Function<CommandEvent, String> helpFunction = this.helpFunction;
			String helpWord = getHelpWord();
			ScheduledExecutorService executor = this.executor;
			Integer linkedCacheSize = this.linkedCacheSize;
			AnnotatedModuleCompiler compiler = this.compiler;
			if (Main.devMode() && !event.getAuthor().getId().equals(Config.getValue("ownerId")) && event.getMessage().getContent().startsWith(prefix)) {
				event.getChannel().sendMessage("Developer mode is turned on, so only my owner can use commands.").complete();
				return;
			}
			try {
				Map prefixes = DataIO.loadJson("data/mod/settings.json", Map.class);
				prefixes = prefixes == null ? new HashMap<>() : prefixes;
				String serverPrefix = Main.getClient().getPrefix();
				try {
					if (prefixes.containsKey(event.getGuild().getId())) serverPrefix = (String) ((Map) prefixes.get(event.getGuild().getId())).get("serverPrefix");
				} catch (NullPointerException e) { }
				if (event.getMessage().getContent().startsWith(Main.getClient().getPrefix()) && !serverPrefix.equals(Main.getClient().getPrefix()) && !prefixedMessages.contains(event.getMessage().getId())) return;
				if (event.getMessage().getContent().startsWith(serverPrefix) && !serverPrefix.equals(Main.getClient().getPrefix())) {
					prefixedMessages.add(event.getMessageId());
					onMessageReceived(
							new MessageReceivedEvent(
									event.getJDA(),
									event.getResponseNumber(),
									new MessageImpl(
											event.getMessageIdLong(),
											event.getChannel(),
											event.getMessage().isWebhookMessage(),
											event.getMessage().getType())
									.setContent(Main.getClient().getPrefix() + event.getMessage().getRawContent().substring(serverPrefix.length()))
									.setAuthor(event.getAuthor())
									.setAttachments(event.getMessage().getAttachments())
									.setEditedTime(event.getMessage().getEditedTime())
									.setEmbeds(event.getMessage().getEmbeds())
									.setMentionedChannels(event.getMessage().getMentionedChannels())
									.setMentionedRoles(event.getMessage().getMentionedRoles())
									.setMentionedUsers(event.getMessage().getMentionedUsers())
									.setMentionsEveryone(event.getMessage().mentionsEveryone())
									.setPinned(event.getMessage().isPinned())
									.setReactions(event.getMessage().getReactions())
									.setTime(event.getMessage().getCreationTime())
									.setTTS(event.getMessage().isTTS())
									)
							);
					return;
				}
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			try {
				Map blacklist = DataIO.loadJson("data/mod/blacklist.json", Map.class);
				blacklist = blacklist == null ? new HashMap() : blacklist;
				if (event.getGuild() != null && blacklist.containsKey(event.getGuild().getId()) && ((List) blacklist.get(event.getGuild().getId())).contains(event.getAuthor().getId())) return;
				else if (blacklist.containsKey("global") && ((List) blacklist.get("global")).contains(event.getAuthor().getId())) return;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if (event.getMessage().getContent().startsWith(prefix) && Main.getCommandNames().contains(event.getMessage().getContent().split(" ")[0].substring(prefix.length())))
				Main.print(LogType.INFO, String.format("%s#%s used the '%s' command in %s.",
						event.getAuthor().getName(),
						event.getAuthor().getDiscriminator(),
						event.getMessage().getContent().substring(prefix.length()).replaceAll("\n", " "),
						event.getGuild() == null ? "private messages" : event.getGuild().getName()));
			Main.executeCommand(() -> {
				try {
					new com.jagrosh.jdautilities.commandclient.impl.CommandClientImpl(ownerId, coOwnerIds, prefix, altprefix, game,
							status, invite, success, warning, error, carbonKey, botsKey, botsOrgKey, commands,
							useHelp, helpFunction, helpWord, executor, linkedCacheSize, compiler).onMessageReceived(event);
				} catch (Throwable e) {
					e.printStackTrace();
					StackTraceElement stElement = null;
					for (StackTraceElement element : e.getStackTrace())
						if (element.getFileName() != null && element.getClassName().startsWith("com.ptsmods.impulse.commands") && element.getFileName().startsWith("Command")) stElement = element;
					event.getChannel().sendMessage(String.format("A `%s` exception was thrown at line %s in %s while parsing the command%s.%s",
							e.getClass().getName(), stElement.getLineNumber(), stElement.getFileName(), e.getMessage() != null ? String.format(": `%s`", e.getMessage()) : "", Main.devMode() ? "" : String.format("\nMy owner, %s, has been informed.", Main.getOwner().getAsMention()))).complete();
					if (!Main.devMode()) {
						String stackTrace = String.format("%s: %s\n\t", e.getClass().toString(), e.getMessage());
						for (StackTraceElement element : e.getStackTrace())
							stackTrace += String.format("at %s.%s(%s)\n\t", element.getClassName(), element.getMethodName(), element.getFileName() != null ? element.getFileName() + ":" + element.getLineNumber() : "Unknown Source");
						while (e.getCause() != null) {
							e = e.getCause();
							stackTrace += String.format("Caused by %s: %s\n\t", e.getClass().getName(), e.getMessage());
							for (StackTraceElement element : e.getStackTrace())
								stackTrace += String.format("at %s.%s(%s)\n\t", element.getClassName(), element.getMethodName(), element.getFileName() != null ? element.getFileName() + ":" + element.getLineNumber() : "Unknown Source");
						}
						Main.sendPrivateMessage(Main.getOwner(), String.format("A `%s` exception was thrown at line %s in %s while parsing the message `%s`. Stacktrace:\n```java\n%s```",
								e.getClass().getName(), stElement.getLineNumber(), stElement.getFileName(), event.getMessage().getContent(), stackTrace.trim()));
						event.getMessage().addReaction(Main.getClient().getError()).complete();
					}
				}
			});
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
