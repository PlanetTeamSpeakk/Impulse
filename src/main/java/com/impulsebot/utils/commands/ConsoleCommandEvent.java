package com.impulsebot.utils.commands;

import java.io.File;
import java.lang.reflect.Method;

import com.impulsebot.commands.Main;

import javafx.application.Platform;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
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

public class ConsoleCommandEvent extends CommandEvent {

	private final TextFlow	textFlow;
	private final Message	message;

	public ConsoleCommandEvent(TextFlow textFlow, String args, Method command) {
		super(args, command);
		this.textFlow = textFlow;
		message = new MessageBuilder().append(Main.getPrefix(null)).append(Main.getFullName(command)).append(' ').append(args).build();
	}

	@Override
	public void reply(String string, Object... args) {
		Platform.runLater(() -> {
			if (!Main.isShuttingDown()) Platform.runLater(() -> {
				final String string0 = String.format(string, args);
				new Object() {
					int		current		= 0;
					char	currentch	= 0;
					char	lastch		= 0;
					String	currents	= "";

					char eat() {
						try {
							lastch = currentch;
							currentch = string0.charAt(current++);
						} catch (StringIndexOutOfBoundsException e) {
							currentch = (char) -1;
						}
						return currentch;
					}

					void puke() {
						current--;
						currentch = lastch;
					}

					void writeCurrent(Font font) {
						if (!currents.isEmpty()) {
							Text text = new Text(currents);
							text.setFont(font);
							text.setStyle("-fx-fill: BLACK");
							textFlow.getChildren().add(text);
						}
					}

					void write() {
						currents = "[" + Main.getFormattedTime() + " '" + Main.getFullName(getCommand()) + (argsEmpty() ? "" : " " + getArgs()) + "'] ";
						writeCurrent(Font.font("Monospaced", 13));
						currents = "";
						if (!string0.contains("*")) {
							currents = string0;
							writeCurrent(Font.font("Monospaced", 13));
						} else {
							boolean isFirst = true;
							int asterisks = 0;
							while (true) {
								eat();
								if (currentch == (char) -1) {
									writeCurrent(Font.font("Monospaced", 13));
									break;
								} else if (currentch != '*')
									currents += currentch;
								else {
									if (isFirst && !currents.isEmpty()) {
										writeCurrent(Font.font("Monospaced", 13));
										currents = "";
									}
									asterisks += 1;
									while (eat() == '*')
										asterisks += 1;
									puke();
									if (isFirst)
										isFirst = false;
									else {
										asterisks /= 2;
										writeCurrent(asterisks >= 3 ? Font.font("Monospaced", FontWeight.BOLD, FontPosture.ITALIC, 13) : asterisks == 2 ? Font.font("Monospaced", FontWeight.BOLD, 13) : Font.font("Monospaced", FontPosture.ITALIC, 13));
										asterisks = 0;
										isFirst = true;
										currents = "";
									}
								}
							}
						}
						textFlow.getChildren().add(new Text(System.lineSeparator()));
					}
				}.write();
			});
		});
	}

	@Override
	public void reply(MessageEmbed message) {
		return;
	}

	@Override
	public void reply(File file, String message, Object... args) {
		reply(message, args);
	}

	@Override
	public void replyInDM(String message, Object... args) {
		Main.sendPrivateMessage(getAuthor(), args != null && args.length > 0 ? String.format(message, args) : message);
	}

	@Override
	public void sendMessage(MessageChannel channel, String message) {
		channel.sendMessage(message).queue();
	}

	@Override
	public void sendMessage(MessageChannel channel, Message message) {
		channel.sendMessage(message).queue();
	}

	@Override
	public void sendMessage(MessageChannel channel, MessageEmbed message) {
		channel.sendMessage(message).queue();
	}

	@Override
	public void sendFile(MessageChannel channel, File file, String message, Object... args) {
		channel.sendFile(file, new MessageBuilder().append(String.format(message, args)).build()).queue();
	}

	@Override
	public boolean isOwner() {
		return true;
	}

	@Override
	public Message getMessage() {
		return message;
	}

	@Override
	public JDA getJDA() {
		return Main.getJDA();
	}

	@Override
	public Member getMember() {
		return null;
	}

	@Override
	public User getAuthor() {
		return Main.getOwner();
	}

	@Override
	public TextChannel getTextChannel() {
		return null;
	}

	@Override
	public MessageChannel getChannel() {
		return getPrivateChannel();
	}

	@Override
	public Group getGroup() {
		return null;
	}

	@Override
	public Guild getGuild() {
		return null;
	}

	@Override
	public PrivateChannel getPrivateChannel() {
		return Main.getOwner().openPrivateChannel().complete();
	}

	@Override
	public long getResponseNumber() {
		return -1;
	}

	@Override
	public boolean isFromType(ChannelType type) {
		return type == ChannelType.PRIVATE;
	}

}
