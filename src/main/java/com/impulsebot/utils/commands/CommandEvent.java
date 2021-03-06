package com.impulsebot.utils.commands;

import java.io.File;
import java.lang.reflect.Method;

import com.impulsebot.miscellaneous.Main;

import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class CommandEvent {

	private final MessageReceivedEvent	event;
	private final Method				command;
	private final String				argsOriginal;
	private String						args;
	private Message						response;

	CommandEvent(String args, Method command) {
		this(null, args, command);
	}

	public CommandEvent(MessageReceivedEvent event, String args, Method command) {
		this.event = event;
		this.args = args;
		argsOriginal = args;
		this.command = command;
	}

	public CommandEvent setArgs(String args) {
		this.args = args;
		return this;
	}

	public String getArgs() {
		return args;
	}

	public String getOriginalArgs() {
		return argsOriginal;
	}

	public boolean argsEmpty() {
		return args.isEmpty();
	}

	public MessageReceivedEvent getEvent() {
		return event;
	}

	public void reply(String message, Object... args) {
		message = args == null || args.length == 0 ? message : String.format(message, args);
		String msg = "";
		boolean inBox = message.indexOf("```") != message.lastIndexOf("```") && message.contains("```");
		String boxLang = "";
		try {
			boxLang = message.substring(message.indexOf("```") + 3);
		} catch (StringIndexOutOfBoundsException e) {
		}
		boxLang = boxLang.split("\n")[0];
		for (char ch : message.toCharArray()) {
			msg += ch;
			if (msg.length() == 1997) {
				if (inBox) msg += "```";
				sendMessage(event.getChannel(), msg);
				msg = inBox ? "```" + boxLang + "\n" : "";
			}
		}
		sendMessage(event.getChannel(), msg.replaceAll("@everyone", "@\u200Beveryone").replaceAll("@here", "@\u200Bhere"));
	}

	public void reply(Message message, Object... args) {
		sendMessage(event.getChannel(), args == null || args.length == 0 ? message : new MessageBuilder().append(String.format(message.getContentRaw(), args)).build());
	}

	public void reply(MessageEmbed message) {
		sendMessage(event.getChannel(), message);
	}

	public void reply(File file, String message, Object... args) {
		sendFile(event.getChannel(), file, message, args);
	}

	public void replyInDM(String message, Object... args) {
		Main.sendPrivateMessage(event.getAuthor(), args != null && args.length > 0 ? String.format(message, args) : message);
	}

	public void sendMessage(MessageChannel channel, String message) {
		try {
			channel.sendMessage(message).queue();
		} catch (NullPointerException e) {
			Main.fixJDAEntity(getGuild());
			channel.sendMessage(message).queue();
		}
	}

	public void sendMessage(MessageChannel channel, Message message) {
		try {
			channel.sendMessage(message).queue();
		} catch (NullPointerException e) {
			Main.fixJDAEntity(getGuild());
			channel.sendMessage(message).queue();
		}
	}

	public void sendMessage(MessageChannel channel, MessageEmbed message) {
		try {
			channel.sendMessage(message).queue();
		} catch (NullPointerException e) {
			Main.fixJDAEntity(getGuild());
			channel.sendMessage(message).queue();
		}
	}

	public void sendFile(MessageChannel channel, File file, String message, Object... args) {
		channel.sendFile(file, new MessageBuilder().append(String.format(message, args)).build()).queue();
	}

	public boolean isOwner() {
		return event.getAuthor().getId().equals(Main.getOwner().getId());
	}

	public boolean isCoOwner() {
		return Main.isCoOwner(getAuthor());
	}

	public boolean hasOwnerPerms() {
		return isOwner() || isCoOwner();
	}

	public Message getMessage() {
		return event.getMessage();
	}

	public JDA getJDA() {
		return event.getJDA();
	}

	public Member getMember() {
		return event.getMember();
	}

	public User getAuthor() {
		return event.getAuthor();
	}

	public TextChannel getTextChannel() {
		return event.getTextChannel();
	}

	public MessageChannel getChannel() {
		return event.getChannel();
	}

	public Group getGroup() {
		return event.getGroup();
	}

	public Guild getGuild() {
		return event.getGuild();
	}

	public PrivateChannel getPrivateChannel() {
		return event.getPrivateChannel();
	}

	public long getResponseNumber() {
		return event.getResponseNumber();
	}

	public boolean isFromType(ChannelType type) {
		return event.isFromType(type);
	}

	public Member getSelfMember() {
		return getGuild() == null ? null : getGuild().getSelfMember();
	}

	public Method getCommand() {
		return command;
	}

	public Message waitForInput(int timeoutMillis) {
		return response = Main.waitForInput(getAuthor(), getChannel(), timeoutMillis);
	}

	public Message getResponse() {
		return response;
	}

	public Message sendCommandHelp() {
		return Main.sendCommandHelp(this);
	}

}
