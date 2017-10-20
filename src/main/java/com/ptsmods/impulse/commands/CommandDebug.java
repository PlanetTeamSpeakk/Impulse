package com.ptsmods.impulse.commands;

import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;

import net.dv8tion.jda.core.entities.ChannelType;

public class CommandDebug extends Command {

	private static final ScriptEngine engine = new ScriptEngineManager(null).getEngineByName("nashorn");

	public CommandDebug() {
		name = "debug";
		help = "Evaluates code.";
		arguments = "<code>";
		guildOnly = false;
		ownerCommand = true;
		category = Main.getCategory("Owner");
		try {
			engine.eval("var imports = new JavaImporter(" +
					"java.io," +
					"java.lang," +
					"java.util," +
					"Packages.net.dv8tion.jda.core," +
					"Packages.net.dv8tion.jda.core.entities," +
					"Packages.net.dv8tion.jda.core.entities.impl," +
					"Packages.net.dv8tion.jda.core.managers," +
					"Packages.net.dv8tion.jda.core.managers.impl," +
					"Packages.net.dv8tion.jda.core.utils," +
					"Packages.com.ptsmods.impulse," +
					"Packages.com.ptsmods.impulse.commands," +
					"Packages.com.ptsmods.impulse.miscellaneous," +
					"Packages.com.ptsmods.impulse.utils," +
					"Packages.com.jagrosh.jdautilities);" +
					"var Main = com.ptsmods.impulse.Main;");
		} catch (ScriptException e) {
			throw new RuntimeException("An error occured while setting up the imports for the evaluator.", e);
		}
	}

	@Override
	protected void execute(CommandEvent event) {
		try {
			engine.put("event", event);
			engine.put("message", event.getMessage());
			engine.put("channel", event.getChannel());
			engine.put("args", event.getArgs());
			engine.put("jda", event.getJDA());
			engine.put("client", event.getClient());
			engine.put("author", event.getAuthor());
			if (event.isFromType(ChannelType.TEXT)) {
				engine.put("guild", event.getGuild());
				engine.put("member", event.getMember());
			}
			Object out;
			try {
				out = engine.eval(String.format("(function() {with (imports) {return %s;}})();", event.getArgs()));
			} catch (Throwable e) {
				out = engine.eval(String.format("(function() {with (imports) {%s;}})();", event.getArgs()));
			}
			out = out == null ? "null" : out.toString();
			List<String> messages = new ArrayList<>();
			while (out.toString().length() > 1990) {
				messages.add(out.toString().substring(0, 1990).trim());
				out = out.toString().substring(1990, out.toString().length());
			}
			messages.add(out.toString());
			event.reply("Input:```javascript\n" + event.getArgs() + "```\nOutput:```javascript\n" + messages.get(0) + "```");
			messages.remove(0);
			for (String message : messages) event.reply("```javascript\n" + message + "```");
		} catch (Throwable e1) {
			event.reply("```javascript\n" + e1.getMessage() + "```");
		}
	}

}