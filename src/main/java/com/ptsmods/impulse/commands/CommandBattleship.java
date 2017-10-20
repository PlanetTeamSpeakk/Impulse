package com.ptsmods.impulse.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.utils.Random;

import net.dv8tion.jda.core.entities.Message;

public class CommandBattleship extends Command {

	private static final String[] positions = new String[] {
			"A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "A10",
			"B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "B10",
			"C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "C10",
			"D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9", "D10",
			"E1", "E2", "E3", "E4", "E5", "E6", "E7", "E8", "E9", "E10",
			"F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10",
			"G1", "G2", "G3", "G4", "G5", "G6", "G7", "G8", "G9", "G10",
			"H1", "H2", "H3", "H4", "H5", "H6", "H7", "H8", "H9", "H10",
			"I1", "I2", "I3", "I4", "I5", "I6", "I7", "I8", "I9", "I10",
			"J1", "J2", "J3", "J4", "J5", "J6", "J7", "J8", "J9", "J10"
	};

	public CommandBattleship() {
		name = "battleship";
		help = "Play that old game called battleship with the bot!";
		arguments = "";
		guildOnly = false;
		ownerCommand = false;
		cooldown = 300;
		category = Main.getCategory("Fun");
	}

	@Override
	protected void execute(CommandEvent event) {
		Message status = event.getChannel().sendMessage("Generating my sea...").complete();
		List<String> botSea = new ArrayList<>();
		for (int x : Main.range(10)) {
			String position = positions[Random.randInt(positions.length)];
			while (botSea.contains(position)) position = positions[Random.randInt(positions.length)];
			botSea.add(position);
		}
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {}
		status.editMessage("Generating your sea...").complete();
		List<String> userSea = new ArrayList<>();
		for (int x : Main.range(10)) {
			String position = positions[Random.randInt(positions.length)];
			while (userSea.contains(position)) position = positions[Random.randInt(positions.length)];
			userSea.add(position);
		}
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {}
		String board = "A1 A2 A3 A4 A5 A6 A7 A8 A9 A10\n"
				+ "B1 B2 B3 B4 B5 B6 B7 B8 B9 B10\n"
				+ "C1 C2 C3 C4 C5 C6 C7 C8 C9 C10\n"
				+ "D1 D2 D3 D4 D5 D6 D7 D8 D9 D10\n"
				+ "E1 E2 E3 E4 E5 E6 E7 E8 E9 E10\n"
				+ "F1 F2 F3 F4 F5 F6 F7 F8 F9 F10\n"
				+ "G1 G2 G3 G4 G5 G6 G7 G8 G9 G10\n"
				+ "H1 H2 H3 H4 H5 H6 H7 H8 H9 H10\n"
				+ "I1 I2 I3 I4 I5 I6 I7 I8 I9 I10\n"
				+ "J1 J2 J3 J4 J5 J6 J7 J8 J9 J10";
		status.delete().complete();
		Message boardMsg = event.getChannel().sendMessage("```fix\n" + board + "```").complete();
		boolean first = new boolean[] {false, true}[Random.randInt(2)];
		if (first) {
			status = event.getChannel().sendMessage("I'll go first.").complete();
			while (true) {
				String bomb = positions[Random.randInt(positions.length)];
				if (userSea.contains(bomb)) {
					userSea.remove(bomb);
					if (userSea.size() > 0) {
						status.editMessage(String.format("I've hit one of your ships! I hit %s, you have **%s** left (%s ships). Your turn.", bomb, Main.joinCustomChar("**, **", userSea.toArray(new String[0])), userSea.size())).complete();
						try {
							Thread.sleep(1500);
						} catch (InterruptedException e) {}
					} else {
						status.editMessage(String.format("I've hit your last ship! I hit %s, so I win. I had **%s** left (%s ships)", bomb, Main.join(botSea.toArray(new String[0])), botSea.size())).complete();
						return;
					}
				} else {
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {}
					status.editMessage("I missed, your turn.").complete();
				}
				Message bombm = Main.waitForInput(event.getMember(), event.getChannel(), 30000, event.getMessage().getCreationTime().getSecond());
				if (bombm == null || bombm.getContent().toUpperCase().equals("STOP")) {
					status.editMessage(String.format("K then, I'll stop. I had **%s** left (%s ships).", Main.joinCustomChar("**, **", botSea.toArray(new String[0])), botSea.size())).complete();
					try {
						bombm.delete().complete();
					} catch (Throwable e) {}
					return;
				} else if (!Arrays.asList(positions).contains(bombm.getContent().toUpperCase()) || bombm.getContent().toUpperCase().equals("HELP")) {
					String bombExample = positions[Random.randInt(positions.length)];
					event.reply(String.format("In this game you have to bomb the bot's ship, " +
							"you can do this by saying %s for example, this will bomb %s." +
							" If there's a ship there that ship will be destroyed, if there's no ship there the bomb will explode in the water." +
							" So when it's your turn you'll just have to say A1, B5, G9, it goes all the way up to J10." +
							" And just so I have to code less, it's now the bot's turn.", bombExample, bombExample));
				} else if (!board.contains(bombm.getContent().toUpperCase())) {
					status.editMessage("You already picked that one, my turn now.").complete();
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {}
				} else if (botSea.contains(bombm.getContent().toUpperCase())) {
					botSea.remove(bombm.getContent().toUpperCase());
					if (botSea.size() > 0) {
						status.editMessage(String.format("You've hit one of my ships! I have %s ships left.", botSea.size())).complete();
						if (!bombm.getContent().endsWith("10"))
							board = board.replace(bombm.getContent().toUpperCase() + " ", "XX ");
						else board = board.replace(bombm.getContent().toUpperCase(), "XX");
						boardMsg.editMessage("```fix\n" + board + "```").complete();
						try {
							Thread.sleep(1500);
						} catch (InterruptedException e) {}
					} else {
						status.editMessage("You've hit my last ship! You win!").queue();
						return;
					}
				} else {
					status.editMessage("You missed, my turn.").complete();
					if (!bombm.getContent().endsWith("10"))
						board = board.replace(bombm.getContent().toUpperCase() + " ", "XX ");
					else board = board.replace(bombm.getContent().toUpperCase(), "XX");
					boardMsg.editMessage("```fix\n" + board + "```").complete();
					try {
						bombm.delete().complete();
					} catch (Throwable e) {}
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {}
				}
			}
		} else {
			status = event.getChannel().sendMessage("You go first.").complete();
			while (true) {
				Message bombm = Main.waitForInput(event.getMember(), event.getChannel(), 30000, event.getMessage().getCreationTime().getSecond());
				if (bombm == null || bombm.getContent().toUpperCase().equals("STOP")) {
					status.editMessage(String.format("K then, I'll stop. I had **%s** left (%s ships).", Main.joinCustomChar("**, **", botSea.toArray(new String[0])), botSea.size())).complete();
					try {
						bombm.delete();
					} catch (Throwable e) {}
					return;
				} else if (!Arrays.asList(positions).contains(bombm.getContent().toUpperCase()) || bombm.getContent().toUpperCase().equals("HELP")) {
					String example = positions[Random.randInt(positions.length)];
					event.reply(String.format("In this game you have to bomb the bot's ship, " +
							"you can do this by saying {0} for example, this will bomb {0}." +
							" If there's a ship there that ship will be destroyed, if there's no ship there the bomb will explode in the water." +
							" So when it's your turn you'll just have to say A1, B5, G9, it goes all the way up to J10." +
							" And just so I have to code less, it's now the bot's turn.", example));
				} else if (!board.contains(bombm.getContent().toUpperCase())) {
					status.editMessage("You already picked that one, my turn now.").complete();
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {}
				} else if (botSea.contains(bombm.getContent().toUpperCase())) {
					botSea.remove(bombm.getContent().toUpperCase());
					if (botSea.size() > 0) {
						status.editMessage(String.format("You've hit one of my ships! I have %s ships left.", botSea.size())).complete();
						if (!bombm.getContent().toUpperCase().endsWith("10"))
							board = board.replace(bombm.getContent().toUpperCase() + " ", "XX ");
						else board = board.replace(bombm.getContent().toUpperCase(), "XX");
						try {
							Thread.sleep(1500);
						} catch (InterruptedException e) {}
					} else {
						status.editMessage("You've hit my last ship! You win!").complete();
						return;
					}
				} else {
					status.editMessage("You missed, my turn.").complete();
					if (!bombm.getContent().endsWith("10"))
						board = board.replace(bombm.getContent().toUpperCase() + " ", "XX ");
					else board = board.replace(bombm.getContent().toUpperCase(), "XX");
					boardMsg.editMessage("```fix\n" + board + "```").complete();
					try {
						bombm.delete().complete();
					} catch (Throwable e) {}
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {}
				}
				String bomb = positions[Random.randInt(2)];
				if (userSea.contains(bomb)) {
					userSea.remove(bomb);
					if (userSea.size() > 0) {
						status.editMessage(String.format("I've hit one of your ships! I hit %s, you have **%s** left (%s ships). Your turn.", bomb, Main.joinCustomChar("**, **", userSea.toArray(new String[0])), userSea.size())).complete();
						try {
							Thread.sleep(1500);
						} catch (InterruptedException e) {}
					} else {
						status.editMessage(String.format("I've hit your last ship! I hit %s, so I win. I had **%s** left (%s ships)", bomb, Main.joinCustomChar("**, **", botSea.toArray(new String[0])), botSea.size())).complete();
						return;
					}
				} else {
					status.editMessage("I missed, your turn.").complete();
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {}
				}
			}
		}
	}

}
