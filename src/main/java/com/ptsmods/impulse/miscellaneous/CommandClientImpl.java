package com.ptsmods.impulse.miscellaneous;

import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import com.jagrosh.jdautilities.commandclient.AnnotatedModuleCompiler;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.utils.Config;

import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
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
		new Thread(new Runnable() {@Override public void run() {System.gc();}}).start();
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
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					new com.jagrosh.jdautilities.commandclient.impl.CommandClientImpl(ownerId, coOwnerIds, prefix, altprefix, game,
							status, invite, success, warning, error, carbonKey, botsKey, botsOrgKey, commands,
							useHelp, helpFunction, helpWord, executor, linkedCacheSize, compiler).onMessageReceived(event);
				} catch (Throwable e) {
					e.printStackTrace();
					StackTraceElement stElement = null;
					for (StackTraceElement element : e.getStackTrace())
						if (element.getFileName() != null && element.getClassName().startsWith("com.ptsmods.impulse.commands") && element.getFileName().startsWith("Command")) stElement = element;
					event.getChannel().sendMessage("A `" + e.getClass().getName() + "` exception was thrown at line " + stElement.getLineNumber() + " in " + stElement.getFileName() + " while parsing the command" + (e.getMessage() != null ? ": `" + e.getMessage() + "`" : "") + "." +
							(Main.devMode() ? "" : "\nMy owner, " + event.getJDA().getUserById(Config.getValue("ownerId")).getAsMention() + ", has been informed.")).complete();
					if (!Main.devMode()) {
						String stackTrace = e.getClass().toString() + ": " + e.getMessage() + "\n\t";
						for (StackTraceElement element : e.getStackTrace())
							stackTrace += "at " + element.getClassName() + "." + element.getMethodName() + "(" + (element.getFileName() != null ? element.getFileName() + ":" + element.getLineNumber() : "Unknown Source") + ")\n\t";
						while (e.getCause() != null) {
							e = e.getCause();
							stackTrace += "Caused by " + e.getClass().getName() + ": " + e.getMessage() + "\n\t";
							for (StackTraceElement element : e.getStackTrace())
								stackTrace += "at " + element.getClassName() + "." + element.getMethodName() + "(" + (element.getFileName() != null ? element.getFileName() + ":" + element.getLineNumber() : "Unknown Source") + ")\n\t";
						}
						Main.sendPrivateMessage(event.getJDA().getUserById(Config.getValue("ownerId")), "A `" + e.getClass().getName() + "` exception was thrown at line " + stElement.getLineNumber() + " in " + stElement.getFileName() +
								" while parsing the message `" + event.getMessage().getContent() + "`. Stacktrace:\n```java\n" + stackTrace.trim() + "```");
						event.getMessage().addReaction(Main.getClient().getError()).complete();
					}
				}
			}
		}).start();
	}

}
