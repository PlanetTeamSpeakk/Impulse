package com.ptsmods.impulse.miscellaneous;

import java.io.File;
import java.lang.reflect.Method;

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

	private final MessageReceivedEvent event;
	private final Method command;
	private String args;

	public CommandEvent(MessageReceivedEvent event, String args, Method command) {
		this.event = event;
		this.args = args;
		this.command = command;
	}

	public CommandEvent setArgs(String args) {
		this.args = args;
		return this;
	}

	public String getArgs() {
		return args;
	}

	public MessageReceivedEvent getEvent() {
		return event;
	}

	public void reply(String message) {
		sendMessage(event.getChannel(), message);
	}

	public void reply(Message message) {
		sendMessage(event.getChannel(), message);
	}

	public void reply(MessageEmbed message) {
		sendMessage(event.getChannel(), message);
	}

	public void replyFormatted(String message, Object... args) {
		reply(String.format(message, args));
	}

	public void reply(File file, String message) {
		sendFile(event.getChannel(), file, message);
	}

	public void replyInDM(String message) {
		Main.sendPrivateMessage(event.getAuthor(), message);
	}

	public void sendMessage(TextChannel channel, String message) {
		channel.sendMessage(message).queue();
	}

	public void sendMessage(MessageChannel channel, String message) {
		channel.sendMessage(message).queue();
	}

	public void sendMessage(TextChannel channel, Message message) {
		channel.sendMessage(message).queue();
	}

	public void sendMessage(MessageChannel channel, Message message) {
		channel.sendMessage(message).queue();
	}

	public void sendMessage(TextChannel channel, MessageEmbed message) {
		channel.sendMessage(message).queue();
	}

	public void sendMessage(MessageChannel channel, MessageEmbed message) {
		channel.sendMessage(message).queue();
	}

	public void sendFile(TextChannel channel, File file, String message) {
		channel.sendFile(file, new MessageBuilder().append(message).build()).queue();
	}

	public void sendFile(MessageChannel channel, File file, String message) {
		channel.sendFile(file, new MessageBuilder().append(message).build()).queue();
	}

	public boolean isOwner() {
		return event.getAuthor().getId().equals(Main.getOwner().getId());
	}

	public boolean isCoOwner() { // I dunno if I am ever gonna add this.
		return isOwner();
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
		return getGuild() == null ? null : getGuild().getMember(getJDA().getSelfUser());
	}

	public Method getCommand() {
		return command;
	}

}
