package com.ptsmods.impulse.miscellaneous;

import java.io.File;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;

public class SilentCommandEvent extends CommandEvent {

	/**
	 * Any messages sent using a 'reply', 'sendFile', or 'sendMessage' method won't through.
	 * Used to invoke other commands without it replying any messages.
	 * @param parent The actual {@link com.ptsmods.impulse.miscellaneous.CommandEvent CommandEvent} that has to be made silent.
	 */
	public SilentCommandEvent(CommandEvent parent) {
		super(parent.getEvent(), parent.getOriginalArgs(), parent.getCommand());
	}

	@Override
	public void reply(File file, String message) { }

	@Override
	public void reply(Message message, Object... args) { }

	@Override
	public void reply(String message, Object... args) { }

	@Deprecated
	@Override
	public void replyFormatted(String message, Object... args) { }

	@Override
	public void replyInDM(String message, Object... args) { }

	@Override
	public void sendFile(MessageChannel channel, File file, String message) { }

	@Override
	public void sendFile(TextChannel channel, File file, String message) { }

	@Override
	public void sendMessage(MessageChannel channel, Message message) { }

	@Override
	public void sendMessage(MessageChannel channel, MessageEmbed message) { }

	@Override
	public void sendMessage(MessageChannel channel, String message) { }

	@Override
	public void sendMessage(TextChannel channel, Message message) { }

	@Override
	public void sendMessage(TextChannel channel, MessageEmbed message) { }

	@Override
	public void sendMessage(TextChannel channel, String message) { }
}
