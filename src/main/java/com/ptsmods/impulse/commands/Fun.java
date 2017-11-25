package com.ptsmods.impulse.commands;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.miscellaneous.CommandEvent;
import com.ptsmods.impulse.miscellaneous.CommandException;
import com.ptsmods.impulse.utils.DataIO;
import com.ptsmods.impulse.utils.Random;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

public class Fun {

	private static Map memes;
	private static final String[] ears = {"q%sp", "ʢ%sʡ", "⸮%s?", "ʕ%sʔ", "ᖗ%sᖘ", "ᕦ%sᕥ", "ᕦ(%s)ᕥ", "ᕙ(%s)ᕗ", "ᘳ%sᘰ", "ᕮ%sᕭ", "ᕳ%sᕲ", "(%s)", "[%s]", "¯\\_%s_/¯", "୧%s୨", "୨%s୧", "⤜(%s)⤏", "☞%s☞", "ᑫ%sᑷ", "ᑴ%sᑷ", "ヽ(%s)ﾉ", "\\(%s)/", "乁(%s)ㄏ", "└[%s]┘", "(づ%s)づ", "(ง%s)ง", "|%s|"};
	private static final String[] eyes = {"⌐■%s■", " ͠°%s °", "⇀%s↼", "´• %s •`", "´%s`", "`%s´", "ó%sò", "ò%só", ">%s<", "Ƹ̵̡ %sƷ", "ᗒ%sᗕ", "⪧%s⪦", "⪦%s⪧", "⪩%s⪨", "⪨%s⪩", "⪰%s⪯", "⫑%s⫒", "⨴%s⨵", "⩿%s⪀", "⩾%s⩽", "⩺%s⩹", "⩹%s⩺", "◥▶%s◀◤", "≋%s≋", "૦ઁ%s૦ઁ", "  ͯ%s  ͯ", "  ̿%s  ̿", "  ͌%s  ͌", "ළ%sළ", "◉%s◉", "☉%s☉", "・%s・", "▰%s▰", "ᵔ%sᵔ", "□%s□", "☼%s☼", "*%s*", "⚆%s⚆", "⊜%s⊜", ">%s>", "❍%s❍", "￣%s￣", "─%s─", "✿%s✿", "•%s•", "T%sT", "^%s^", "ⱺ%sⱺ", "@%s@", "ȍ%sȍ", "x%sx", "-%s-", "$%s$", "Ȍ%sȌ", "ʘ%sʘ", "Ꝋ%sꝊ", "๏%s๏", "■%s■", "◕%s◕", "◔%s◔", "✧%s✧", "♥%s♥", " ͡°%s ͡°", "¬%s¬", " º %s º ", "⍜%s⍜", "⍤%s⍤", "ᴗ%sᴗ", "ಠ%sಠ", "σ%sσ"};
	private static final String[] mouth = {"v", "ᴥ", "ᗝ", "Ѡ", "ᗜ", "Ꮂ", "ヮ", "╭͜ʖ╮", " ͟ل͜", " ͜ʖ", " ͟ʖ", " ʖ̯", "ω", "³", " ε ", "﹏", "ل͜", "╭╮", "‿‿", "▾", "‸", "Д", "∀", "!", "人", ".", "ロ", "_", "෴", "ѽ", "ഌ", "⏏", "ツ", "益"};
	private static final String[] positions = {
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
	private static final String[] waysToKill = {
			"{KILLER} shoves a double barreled shotgun into {VICTIM}'s mouth and squeezes the trigger of the gun, causing {VICTIM}'s head to horrifically explode like a ripe pimple, splattering the young person's brain matter, gore, and bone fragments all over the walls and painting it a crimson red.",
			"Screaming in sheer terror and agony, {VICTIM} is horrifically dragged into the darkness by unseen forces, leaving nothing but bloody fingernails and a trail of scratch marks in the ground from which the young person had attempted to halt the dragging process.",
			"{KILLER} takes a machette and starts hacking away on {VICTIM}, chopping {VICTIM} into dozens of pieces.",
			"{KILLER} pours acid over {VICTIM}. *\"Well don't you look pretty right now?\"*",
			"{VICTIM} screams in terror as a giant creature with huge muscular arms grab {VICTIM}'s head; {VICTIM}'s screams of terror are cut off as the creature tears off the head with a sickening crunching sound. {VICTIM}'s spinal cord, which is still attached to the dismembered head, is used by the creature as a makeshift sword to slice a perfect asymmetrical line down {VICTIM}'s body, causing the organs to spill out as the two halves fall to their respective sides.",
			"{KILLER} grabs {VICTIM}'s head and tears it off with superhuman speed and efficiency. Using {VICTIM}'s head as a makeshift basketball, {KILLER} expertly slams dunk it into the basketball hoop, much to the applause of the audience watching the gruesome scene.",
			"{KILLER} uses a shiv to horrifically stab {VICTIM} multiple times in the chest and throat, causing {VICTIM} to gurgle up blood as the young person horrifically dies.",
			"{VICTIM} screams as Pyramid Head lifts Sarcen up using his superhuman strength. Before {VICTIM} can even utter a scream of terror, Pyramid Head uses his superhuman strength to horrifically tear {VICTIM} into two halves; {VICTIM} stares at the monstrosity in shock and disbelief as {VICTIM} gurgles up blood, the upper body organs spilling out of the dismembered torso, before the eyes roll backward into the skull.",
			"{VICTIM} steps on a land mine and is horrifically blown to multiple pieces as the device explodes, {VICTIM}'s entrails and gore flying up and splattering all around as if someone had thrown a watermelon onto the ground from the top of a multiple story building.",
	"{VICTIM} is killed instantly as the top half of his head is blown off by a Red Army sniper armed with a Mosin Nagant, {VICTIM}'s brains splattering everywhere in a horrific fashion."};

	static {
		try {
			memes = DataIO.loadJsonOrDefault("data/fun/memes.json", Map.class, new HashMap());
		} catch (IOException e) {
			throw new RuntimeException("An unknown error occurred while loading the data file.", e);
		}
		if (!memes.containsKey("global"))
			memes.put("global", Lists.newArrayList(
					"https://cdn.impulsebot.com/F7LEB4bHAF.png",
					"https://cdn.impulsebot.com/rhgduDZReD.jpg",
					"https://cdn.impulsebot.com/dlfCtrt9Bb.jpg",
					"https://cdn.impulsebot.com/Wute8pN5E3.jpg",
					"https://cdn.impulsebot.com/l65LMtCr6T.jpg",
					"https://cdn.impulsebot.com/R51VxlMVEf.jpg",
					"https://cdn.impulsebot.com/JII9WB66cw.png",
					"https://cdn.impulsebot.com/vRl9iLYlB6.jpg",
					"https://cdn.impulsebot.com/d5xuj18iTn.png",
					"https://cdn.impulsebot.com/HREswfxIX8.jpg",
					"https://cdn.impulsebot.com/BaFZzoHj1A.jpg",
					"https://cdn.impulsebot.com/nbtDNX4g6G.png",
					"https://cdn.impulsebot.com/rPoaYlL4rX.jpg", // thanks to Gesty#6002 and nev#3618 for providing me these memes.
					"https://cdn.impulsebot.com/VPSo40OS6s.jpg",
					"https://cdn.impulsebot.com/B8PfBlv94t.jpg",
					"https://cdn.impulsebot.com/VXyXT6jF2X.jpg",
					"https://cdn.impulsebot.com/dnHi8uLLVS.jpg",
					"https://cdn.impulsebot.com/Uf3sKmtSFm.jpg",
					"https://cdn.impulsebot.com/ErK0A2Lzon.jpg",
					"https://cdn.impulsebot.com/fBc70g8P8f.jpg",
					"https://cdn.impulsebot.com/FZxkY7IFrK.jpg",
					"https://cdn.impulsebot.com/hrEL5sI9IO.jpg",
					"https://cdn.impulsebot.com/QaDelCA6H2.jpg",
					"https://cdn.impulsebot.com/gE7YXgsJ1o.jpg",
					"https://cdn.impulsebot.com/4ibUyMf4Vj.jpg",
					"https://cdn.impulsebot.com/ydLdYQFG0n.png",
					"https://cdn.impulsebot.com/BGPlGqnsZM.jpg",
					"https://cdn.impulsebot.com/PELnYKuHqW.jpg",
					"https://cdn.impulsebot.com/uRy86TLG0s.jpg",
					"https://cdn.impulsebot.com/rHAzFxP8gR.jpg",
					"https://cdn.impulsebot.com/h4cQ25WlKY.jpg",
					"https://cdn.impulsebot.com/I4LTYkGepC.jpg",
					"https://cdn.impulsebot.com/iFem7ZJBRp.jpg",
					"https://cdn.impulsebot.com/Y70af1roQA.jpg",
					"https://cdn.impulsebot.com/yXPkaNbPuc.jpg",
					"https://cdn.impulsebot.com/D8FPZHVW2G.jpg",
					"https://cdn.impulsebot.com/wSxlddLBGK.jpg"));
		try {
			DataIO.saveJson(memes, "data/fun/memes.json");
		} catch (IOException e) {
			throw new RuntimeException("An unknown error occurred while saving the data file.", e);
		}
	}

	@Command(category = "Fun", help = "Generates a random lenny face.", name = "lenny")
	public static void lenny(CommandEvent event) {
		event.reply(String.format(Random.choice(ears), String.format(Random.choice(eyes), Random.choice(mouth))));
	}

	@Command(category = "Fun", help = "Tells you the size of someone's penis.\n\nThis is 100% accurate.", name = "penis")
	public static void penis(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			User user = Main.getUserFromInput(event.getMessage());
			if (user == null) event.reply("The given user could not be found.");
			else {
				Random.seed(user.getId());
				event.reply("Size: 8" + Main.multiplyString("=", Random.randInt(30)) + "D");
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Fun", help = "Play that old game called Battleship with the bot!", name = "battleship", cooldown = 300)
	public static void battleship(CommandEvent event) {
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
		boolean first = Random.choice(false, true);
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

	@Command(category = "Fun", help = "Kill someone in a creative way.", name = "kill", arguments = "<user>")
	public static void kill(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			if (Main.getUserFromInput(event.getMessage()) == null) event.reply("That user could not be found.");
			else if (Main.getUserFromInput(event.getMessage()).getId().equals(event.getSelfMember().getUser().getId())) event.reply("You think that's funny?");
			else event.reply(Random.choice(waysToKill).replaceAll("\\{KILLER\\}", event.getAuthor().getAsMention()).replaceAll("\\{VICTIM\\}", Main.getUserFromInput(event.getMessage()).getAsMention()));
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Fun", help = "Memes are the only thing that keep PlanetTeamSpeak alive.", name = "meme")
	public static void meme(CommandEvent event) {
		List<String> serverMemes = (List) memes.get("global");
		if (memes.containsKey(event.getGuild().getId())) serverMemes.addAll((List) memes.get(event.getGuild().getId()));
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(new Color(Random.randInt(256*256*256)));
		embed.setImage(Random.choice(serverMemes));
		event.reply(embed.build());
	}

	@Command(category = "Fun", help = "Adds a meme to the list of global memes.\n\nThe given URL can either be an i.imgur.com link or a cdn.impulsebot.com link.", name = "massaddmeme", arguments = "<Imgur or Impulse CDN link>", ownerCommand = true)
	public static void massAddMeme(CommandEvent event) throws CommandException {
		Pattern imgur = Pattern.compile("(?i)(http|https)+(://)+(i\\.imgur\\.com/)+(([a-z]|\\d){7})+(\\.)+(PNG|JPG|GIFV|GIF|MOV|MP4)+");
		Pattern impulseCDN = Pattern.compile("(?i)(http|https)+(://)+(cdn\\.impulsebot\\.com/)+(([a-z]|\\d){10})+(\\.)+(PNG|JPG|GIF|MOV|MP4)+");
		if (!event.getArgs().isEmpty() && imgur.matcher(event.getArgs()).matches() || impulseCDN.matcher(event.getArgs()).matches()) {
			((List) memes.get("global")).add(event.getArgs());
			try {
				DataIO.saveJson(memes, "data/fun/memes.json");
			} catch (IOException e) {
				throw new CommandException("An unknown error occurred while saving the data file.", e);
			}
			event.reply("The meme has been added.");
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Fun", help = "Adds a meme to the list of memes in this server.\n\nThe given URL can either be an i.imgur.com link or a cdn.impulsebot.com link.", name = "addmeme", arguments = "<Imgur or Impulse CDN link>", guildOnly = true, cooldown = 10)
	public static void addMeme(CommandEvent event) throws CommandException {
		Pattern imgur = Pattern.compile("(?i)(http|https)+(://)+(i\\.imgur\\.com/)+(([a-z]|\\d){7})+(\\.)+(PNG|JPG|GIFV|GIF|MOV|MP4)+");
		Pattern impulseCDN = Pattern.compile("(?i)(http|https)+(://)+(cdn\\.impulsebot\\.com/)+(([a-z]|\\d){10})+(\\.)+(PNG|JPG|GIF|MOV|MP4)+");
		if (!event.getArgs().isEmpty() && imgur.matcher(event.getArgs()).matches() || impulseCDN.matcher(event.getArgs()).matches()) {
			if (!memes.containsKey(event.getGuild().getId())) memes.put(event.getGuild().getId(), Lists.newArrayList(event.getArgs()));
			else ((List) memes.get(event.getGuild().getId())).add(event.getArgs());
			try {
				DataIO.saveJson(memes, "data/fun/memes.json");
			} catch (IOException e) {
				throw new CommandException("An unknown error occurred while saving the data file.", e);
			}
			event.reply("The meme has been added.");
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Fun", help = "Deletes a meme from the list of memes in this server.\n\nThe given URL can either be an i.imgur.com link or a cdn.impulsebot.com link.", name = "delmeme", arguments = "<Imgur or Impulse CDN link>", guildOnly = true, userPermissions = {Permission.ADMINISTRATOR})
	public static void delMeme(CommandEvent event) throws CommandException {
		Pattern imgur = Pattern.compile("(?i)(http|https)+(://)+(i\\.imgur\\.com/)+(([a-z]|\\d){7})+(\\.)+(PNG|JPG|GIFV|GIF|MOV|MP4)+");
		Pattern impulseCDN = Pattern.compile("(?i)(http|https)+(://)+(cdn\\.impulsebot\\.com/)+(([a-z]|\\d){10})+(\\.)+(PNG|JPG|GIF|MOV|MP4)+");
		if (!event.getArgs().isEmpty() && imgur.matcher(event.getArgs()).matches() && impulseCDN.matcher(event.getArgs()).matches()) {
			if (!memes.containsKey(event.getGuild().getId())) event.reply("This server has no custom memes.");
			else if (!((List) memes.get(event.getGuild().getId())).contains(event.getArgs())) event.reply("That meme could not be found, to list all memes type %slistmemes");
			else {
				try {
					DataIO.saveJson(memes, "data/fun/memes.json");
				} catch (IOException e) {
					throw new CommandException("An unknown error occurred while saving the data file.", e);
				}
				event.reply("The meme has been deleted.");
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Fun", help = "Turns all characters into regional indicators.", name = "indicator")
	public static void indicator(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			String output = "";
			for (Character ch : event.getArgs().toLowerCase().toCharArray())
				if (ch.charValue() >= 'a' && ch.charValue() <='z')
					output += ":regional_indicator_" + ch + ":";
				else if (ch.charValue() >= '0' && ch.charValue() <= '9')
					switch (ch.charValue()) {
					case '0': {output += ":zero:";  break;}
					case '1': {output += ":one:";   break;}
					case '2': {output += ":two:";   break;}
					case '3': {output += ":three:"; break;}
					case '4': {output += ":four:";  break;}
					case '5': {output += ":five:";  break;}
					case '6': {output += ":six:";   break;}
					case '7': {output += ":seven:"; break;}
					case '8': {output += ":eight:"; break;}
					case '9': {output += ":nine:";  break;}
					default: break;
					}
				else output += ch;
			event.reply(output);
		} else Main.sendCommandHelp(event);
	}

}
