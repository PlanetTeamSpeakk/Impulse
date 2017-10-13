package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.utils.Random;

public class CommandEightBall extends Command {

	private static String[] answers = new String[] {
			"It is certain", "It is decidedly so", "Without a doubt", "Yes definitely", "You may rely on it",						  // positive
			"As I see it, yes", "Most likely", "Outlook good", "Yes", "Signs point to yes",					  						  // positive
			"Reply hazy try again", "Ask again later", "Better not tell you now", "Cannot predict now", "Concentrate and ask again",  // neutral
			"Don't count on it", "My reply is no", "My sources say no", "Outlook not so good", "Very doubtful"};					  // negative

	public CommandEightBall() {
		name = "8ball";
		help = "Answers the hardest questions life can give you.";
		arguments = "<question>";
		guildOnly = false;
		ownerCommand = false;
		category = Main.getCategory("General");
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!event.getArgs().endsWith("?") || event.getArgs().length() == 0)
			event.reply("That does not look like a question.");
		else event.reply(answers[Random.randInt(answers.length+1)] + ".");
	}

}
