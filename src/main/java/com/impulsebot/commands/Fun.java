package com.impulsebot.commands;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.google.common.collect.Lists;
import com.impulsebot.miscellaneous.Main;
import com.impulsebot.miscellaneous.SubscribeEvent;
import com.impulsebot.miscellaneous.Trivia;
import com.impulsebot.miscellaneous.Trivia.TriviaResult;
import com.impulsebot.utils.AtomicObject;
import com.impulsebot.utils.DataIO;
import com.impulsebot.utils.ImageManipulator;
import com.impulsebot.utils.LaughingMao;
import com.impulsebot.utils.Random;
import com.impulsebot.utils.commands.Command;
import com.impulsebot.utils.commands.CommandEvent;
import com.impulsebot.utils.commands.CommandException;
import com.impulsebot.utils.commands.Subcommand;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Fun {

	private static Map												memes;
	private static Map<String, Trivia>								triviaSessions	= new HashMap();
	private static final String[]									ears			= {"q%sp", "ʢ%sʡ", "⸮%s?", "ʕ%sʔ", "ᖗ%sᖘ", "ᕦ%sᕥ", "ᕦ(%s)ᕥ", "ᕙ(%s)ᕗ", "ᘳ%sᘰ", "ᕮ%sᕭ", "ᕳ%sᕲ", "(%s)", "[%s]", "¯\\_%s_/¯", "୧%s୨", "୨%s୧", "⤜(%s)⤏", "☞%s☞", "ᑫ%sᑷ", "ᑴ%sᑷ", "ヽ(%s)ﾉ", "\\(%s)/", "乁(%s)ㄏ", "└[%s]┘", "(づ%s)づ", "(ง%s)ง", "|%s|"};
	private static final String[]									eyes			= {"⌐■%s■", " ͠°%s °", "⇀%s↼", "´• %s •`", "´%s`", "`%s´", "ó%sò", "ò%só", ">%s<", "Ƹ̵̡ %sƷ", "ᗒ%sᗕ", "⪧%s⪦", "⪦%s⪧", "⪩%s⪨", "⪨%s⪩", "⪰%s⪯", "⫑%s⫒", "⨴%s⨵", "⩿%s⪀", "⩾%s⩽", "⩺%s⩹", "⩹%s⩺", "◥▶%s◀◤", "≋%s≋", "૦ઁ%s૦ઁ", "  ͯ%s  ͯ", "  ̿%s  ̿", "  ͌%s  ͌", "ළ%sළ", "◉%s◉", "☉%s☉", "・%s・", "▰%s▰", "ᵔ%sᵔ", "□%s□", "☼%s☼", "*%s*", "⚆%s⚆", "⊜%s⊜", ">%s>", "❍%s❍", "￣%s￣", "─%s─", "✿%s✿", "•%s•", "T%sT", "^%s^", "ⱺ%sⱺ", "@%s@", "ȍ%sȍ", "x%sx", "-%s-", "$%s$", "Ȍ%sȌ", "ʘ%sʘ", "Ꝋ%sꝊ", "๏%s๏", "■%s■", "◕%s◕", "◔%s◔", "✧%s✧", "♥%s♥", " ͡°%s ͡°", "¬%s¬", " º %s º ", "⍜%s⍜", "⍤%s⍤", "ᴗ%sᴗ", "ಠ%sಠ", "σ%sσ"};
	private static final String[]									mouth			= {"v", "ᴥ", "ᗝ", "Ѡ", "ᗜ", "Ꮂ", "ヮ", "╭͜ʖ╮", " ͟ل͜", " ͜ʖ", " ͟ʖ", " ʖ̯", "ω", "³", " ε ", "﹏", "ل͜", "╭╮", "‿‿", "▾", "‸", "Д", "∀", "!", "人", ".", "ロ", "_", "෴", "ѽ", "ഌ", "⏏", "ツ", "益"};
	private static final String[]									positions		= {"A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9", "A10", "B1", "B2", "B3", "B4", "B5", "B6", "B7", "B8", "B9", "B10", "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "C10", "D1", "D2", "D3", "D4", "D5", "D6", "D7", "D8", "D9", "D10", "E1", "E2", "E3", "E4", "E5", "E6", "E7", "E8", "E9", "E10", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "G1", "G2", "G3", "G4", "G5", "G6", "G7", "G8", "G9", "G10", "H1", "H2", "H3", "H4", "H5", "H6", "H7", "H8", "H9", "H10", "I1", "I2", "I3", "I4", "I5", "I6", "I7", "I8", "I9", "I10", "J1", "J2", "J3", "J4", "J5", "J6", "J7", "J8", "J9", "J10"};
	private static final String[]									waysToKill		= {"{KILLER} shoves a double barreled shotgun into {VICTIM}'s mouth and squeezes the trigger of the gun, causing {VICTIM}'s head to horrifically explode like a ripe pimple, splattering the young person's brain matter, gore, and bone fragments all over the walls and painting it a crimson red.", "Screaming in sheer terror and agony, {VICTIM} is horrifically dragged into the darkness by unseen forces, leaving nothing but bloody fingernails and a trail of scratch marks in the ground from which the young person had attempted to halt the dragging process.", "{KILLER} takes a machette and starts hacking away on {VICTIM}, chopping {VICTIM} into dozens of pieces.", "{KILLER} pours acid over {VICTIM}. *\"Well don't you look pretty right now?\"*", "{VICTIM} screams in terror as a giant creature with huge muscular arms grab {VICTIM}'s head; {VICTIM}'s screams of terror are cut off as the creature tears off the head with a sickening crunching sound. {VICTIM}'s spinal cord, which is still attached to the dismembered head, is used by the creature as a makeshift sword to slice a perfect asymmetrical line down {VICTIM}'s body, causing the organs to spill out as the two halves fall to their respective sides.", "{KILLER} grabs {VICTIM}'s head and tears it off with superhuman speed and efficiency. Using {VICTIM}'s head as a makeshift basketball, {KILLER} expertly slams dunk it into the basketball hoop, much to the applause of the audience watching the gruesome scene.", "{KILLER} uses a shiv to horrifically stab {VICTIM} multiple times in the chest and throat, causing {VICTIM} to gurgle up blood as the young person horrifically dies.", "{VICTIM} screams as Pyramid Head lifts Sarcen up using his superhuman strength. Before {VICTIM} can even utter a scream of terror, Pyramid Head uses his superhuman strength to horrifically tear {VICTIM} into two halves; {VICTIM} stares at the monstrosity in shock and disbelief as {VICTIM} gurgles up blood, the upper body organs spilling out of the dismembered torso, before the eyes roll backward into the skull.", "{VICTIM} steps on a land mine and is horrifically blown to multiple pieces as the device explodes, {VICTIM}'s entrails and gore flying up and splattering all around as if someone had thrown a watermelon onto the ground from the top of a multiple story building.", "{VICTIM} is killed instantly as the top half of his head is blown off by a Red Army sniper armed with a Mosin Nagant, {VICTIM}'s brains splattering everywhere in a horrific fashion."};
	private static final Map<String, Integer>						times			= Main.newHashMap(new String[] {"minute", "hour", "day", "week", "month"}, new Integer[] {60, 3600, 86400, 604800, 181440000});
	public static final Map<String, Map<String, Object>>			plants			= Main.newHashMap(new String[] {
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																/* === VEGETABLES === */
			"carrots", "potatoes", "pumpkin", "tomatoes", "cabbage", "cucumber", "onions", "broccoli", "lettuce", "spinach", "eggplant", "cauliflower", "peas", "maize", "radish", "garlic", "celery", "kale", "sprout", "bell pepper", "asparagus", "turnip", "bean", "leek", "zucchini", "artichoke", "chilli pepper", "red cabbage",
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																/* === FRUITS === */
			"apple tree", "orange tree", "banana tree", "grapevine", "strawberry bush", "pear tree", "pineapple", "cherry tree", "lemon tree", "peach tree", "mango tree", "berry tree", "watermelon", "grapefruit tree", "kiwi tree", "papaya tree", "pomegranate tree", "fig tree", "avocado tree", "apricot tree", "blackberry bush", "cranberry bush", "cantaloupe bush", "palm tree", "passion fruit bush", "olive tree", "raspberry bush", "lime tree"},
			new Map[] {
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																/* === VEGETABLES === */
					Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {15 * times.get("minute"), "carrot", 250, 65}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {15 * times.get("minute"), "potato", 250, 65}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {times.get("hour"), "pumpkin", 1000, 250}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {30 * times.get("minute"), "tomato", 500, 125}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {30 * times.get("minute"), "cabbage", 500, 125}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {45 * times.get("minute"), "cucumber", 750, 375}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {30 * times.get("minute"), "onion", 500, 125}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"},																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																		// value = seconds / 3.6
							new Object[] {times.get("hour"), "broccoli", 1000, 250}),																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																											// price = value / 4
					Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {45 * times.get("minute"), "lettuce", 750, 375}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {45 * times.get("minute"), "spinach leaf", 750, 375}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {times.get("hour"), "eggplant", 1000, 250}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {times.get("hour"), "cauliflower", 1000, 250}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {15 * times.get("minute"), "pea", 250, 65}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {times.get("hour"), "corn", 1000, 250}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {30 * times.get("minute"), "radish", 500, 125}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {45 * times.get("minute"), "garlic", 750, 375}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {15 * times.get("minute"), "celery stalk", 250, 65}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {30 * times.get("minute"), "kale stalk", 500, 125}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {30 * times.get("minute"), "sprout", 500, 125}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {45 * times.get("minute"), "bell pepper", 750, 375}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {times.get("hour"), "asparagus", 1000, 250}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {45 * times.get("minute"), "turnip", 750, 375}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {15 * times.get("minute"), "bean", 250, 65}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {30 * times.get("minute"), "leek", 500, 125}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {45 * times.get("minute"), "zucchini", 750, 375}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {30 * times.get("minute"), "chilli pepper", 500, 125}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {times.get("hour"), "red cabbage", 1000, 500}),
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																/* === FRUITS === */
					Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {3 * times.get("hour"), "apple", 3000, 750}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {3 * times.get("hour"), "orange", 3000, 750}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {3 * times.get("hour"), "banana", 3000, 750}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {2 * times.get("hour"), "grape", 2000, 500}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {30 * times.get("minute"), "strawberry", 500, 125}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {3 * times.get("hour"), "pear", 3000, 750}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {times.get("hour"), "pineapple", 1000, 250}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {3 * times.get("hour"), "cherry", 3000, 750}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {3 * times.get("hour"), "lemon", 3000, 750}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {3 * times.get("hour"), "peach", 3000, 750}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {3 * times.get("hour"), "mango", 3000, 750}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {3 * times.get("hour"), "berry", 3000, 750}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {times.get("hour"), "watermelon", 1000, 250}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {3 * times.get("hour"), "grapefruit", 3000, 750}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {3 * times.get("hour"), "kiwi", 3000, 750}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {3 * times.get("hour"), "papaya", 3000, 750}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {3 * times.get("hour"), "pomegranate", 3000, 750}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {3 * times.get("hour"), "fig", 3000, 750}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {3 * times.get("hour"), "avocado", 3000, 750}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {3 * times.get("hour"), "apricot", 3000, 750}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {2 * times.get("hour"), "blackberry", 2000, 500}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {2 * times.get("hour"), "cranberry", 2000, 500}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {2 * times.get("hour"), "cantaloupe", 2000, 500}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {6 * times.get("hour"), "coconut", 6000, 1500}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {2 * times.get("hour"), "passion fruit", 3000, 750}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {3 * times.get("hour"), "olive", 3000, 750}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {2 * times.get("hour"), "raspberry", 3000, 750}), Main.newHashMap(new String[] {"growthtime", "item", "value", "price"}, new Object[] {3 * times.get("hour"), "lime", 3000, 750})});
	private static Map<String, Map<String, Map<String, Object>>>	garden;
	private static List<String>										sadMaos;
	private static Map<String, Integer>								easterEgg		= new HashMap();
	private static List<String>										gameSessions	= new ArrayList();

	static {
		try {
			memes = DataIO.loadJsonOrDefault("data/fun/memes.json", Map.class, new HashMap());
		} catch (IOException e) {
			throw new RuntimeException("An unknown error occurred while loading the data file.", e);
		}
		if (!memes.containsKey("global")) {
			memes.put("global", Lists.newArrayList("https://cdn.impulsebot.com/F7LEB4bHAF.png", "https://cdn.impulsebot.com/rhgduDZReD.jpg", "https://cdn.impulsebot.com/dlfCtrt9Bb.jpg", "https://cdn.impulsebot.com/Wute8pN5E3.jpg", "https://cdn.impulsebot.com/l65LMtCr6T.jpg", "https://cdn.impulsebot.com/R51VxlMVEf.jpg", "https://cdn.impulsebot.com/JII9WB66cw.png", "https://cdn.impulsebot.com/vRl9iLYlB6.jpg", "https://cdn.impulsebot.com/d5xuj18iTn.png", "https://cdn.impulsebot.com/HREswfxIX8.jpg", "https://cdn.impulsebot.com/BaFZzoHj1A.jpg", "https://cdn.impulsebot.com/nbtDNX4g6G.png", "https://cdn.impulsebot.com/rPoaYlL4rX.jpg", // thanks to Gesty#6002 and nev#3618 for providing me these memes.
					"https://cdn.impulsebot.com/VPSo40OS6s.jpg", // almost all jpg, though, smh.
					"https://cdn.impulsebot.com/B8PfBlv94t.jpg", "https://cdn.impulsebot.com/VXyXT6jF2X.jpg", "https://cdn.impulsebot.com/dnHi8uLLVS.jpg", "https://cdn.impulsebot.com/Uf3sKmtSFm.jpg", "https://cdn.impulsebot.com/ErK0A2Lzon.jpg", "https://cdn.impulsebot.com/fBc70g8P8f.jpg", "https://cdn.impulsebot.com/FZxkY7IFrK.jpg", "https://cdn.impulsebot.com/hrEL5sI9IO.jpg", "https://cdn.impulsebot.com/QaDelCA6H2.jpg", "https://cdn.impulsebot.com/gE7YXgsJ1o.jpg", "https://cdn.impulsebot.com/4ibUyMf4Vj.jpg", "https://cdn.impulsebot.com/ydLdYQFG0n.png", "https://cdn.impulsebot.com/BGPlGqnsZM.jpg", "https://cdn.impulsebot.com/PELnYKuHqW.jpg", "https://cdn.impulsebot.com/uRy86TLG0s.jpg", "https://cdn.impulsebot.com/rHAzFxP8gR.jpg", "https://cdn.impulsebot.com/h4cQ25WlKY.jpg", "https://cdn.impulsebot.com/I4LTYkGepC.jpg", "https://cdn.impulsebot.com/iFem7ZJBRp.jpg", "https://cdn.impulsebot.com/Y70af1roQA.jpg", "https://cdn.impulsebot.com/yXPkaNbPuc.jpg", "https://cdn.impulsebot.com/D8FPZHVW2G.jpg", "https://cdn.impulsebot.com/wSxlddLBGK.jpg"));
			try {
				DataIO.saveJson(memes, "data/fun/memes.json");
			} catch (IOException e) {
				throw new RuntimeException("An unknown error occurred while saving the data file.", e);
			}
		}
		try {
			garden = DataIO.loadJsonOrDefault("data/fun/garden.json", Map.class, new HashMap());
		} catch (IOException e) {
			throw new RuntimeException("An unknown error occurred while loading the garden.", e);
		}
		try {
			sadMaos = DataIO.loadJsonOrDefault("data/fun/sadmaos.json", List.class, new ArrayList());
		} catch (IOException e) {
			throw new RuntimeException("An unknown error occurred while loading the sad maos.", e);
		}
	}

	@Command(category = "Fun", help = "Generates a Random.INSTANCE lenny face.", name = "lenny")
	public static void lenny(CommandEvent event) {
		event.reply(String.format(Random.INSTANCE.choice(ears), String.format(Random.INSTANCE.choice(eyes), Random.INSTANCE.choice(mouth))));
	}

	@Command(category = "Fun", help = "Tells you the size of someone's penis.\n\nThis is 100% accurate.", name = "penis", arguments = "[user]")
	public static void penis(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			User user = Main.getUserFromInput(event.getMessage());
			if (user == null)
				event.reply("The given user could not be found.");
			else {
				Random.INSTANCE.seed(user.getId());
				event.reply("Size: 8" + Main.multiplyString("=", Random.INSTANCE.randInt(30)) + "D");
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Fun", help = "Play that old game called Battleship with the bot!", name = "battleship", cooldown = 300, guildOnly = true)
	public static void battleship(CommandEvent event) {
		if (gameSessions.contains(event.getAuthor().getId())) {
			event.reply("You are already playing a game, please finish that one first.");
			return;
		}
		gameSessions.add(event.getAuthor().getId());
		Message status = event.getChannel().sendMessage("Generating my sea...").complete();
		List<String> botSea = new ArrayList<>();
		for (int x : Main.range(10)) {
			String position = positions[Random.INSTANCE.randInt(positions.length)];
			while (botSea.contains(position))
				position = positions[Random.INSTANCE.randInt(positions.length)];
			botSea.add(position);
		}
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
		}
		status.editMessage("Generating your sea...").complete();
		List<String> userSea = new ArrayList<>();
		for (int x : Main.range(10)) {
			String position = positions[Random.INSTANCE.randInt(positions.length)];
			while (userSea.contains(position))
				position = positions[Random.INSTANCE.randInt(positions.length)];
			userSea.add(position);
		}
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
		}
		String board = "A1 A2 A3 A4 A5 A6 A7 A8 A9 A10\n" + "B1 B2 B3 B4 B5 B6 B7 B8 B9 B10\n" + "C1 C2 C3 C4 C5 C6 C7 C8 C9 C10\n" + "D1 D2 D3 D4 D5 D6 D7 D8 D9 D10\n" + "E1 E2 E3 E4 E5 E6 E7 E8 E9 E10\n" + "F1 F2 F3 F4 F5 F6 F7 F8 F9 F10\n" + "G1 G2 G3 G4 G5 G6 G7 G8 G9 G10\n" + "H1 H2 H3 H4 H5 H6 H7 H8 H9 H10\n" + "I1 I2 I3 I4 I5 I6 I7 I8 I9 I10\n" + "J1 J2 J3 J4 J5 J6 J7 J8 J9 J10";
		status.delete().complete();
		Message boardMsg = event.getChannel().sendMessage("```fix\n" + board + "```").complete();
		boolean first = Random.INSTANCE.choice(false, true);
		if (first) {
			status = event.getChannel().sendMessage("I'll go first.").complete();
			while (true) {
				String bomb = positions[Random.INSTANCE.randInt(positions.length)];
				if (userSea.contains(bomb)) {
					userSea.remove(bomb);
					if (userSea.size() > 0) {
						status.editMessage(String.format("I've hit one of your ships! I hit %s, you have **%s** left (%s ships). Your turn.", bomb, Main.joinCustomChar("**, **", userSea.toArray(new String[0])), userSea.size())).complete();
						try {
							Thread.sleep(1500);
						} catch (InterruptedException e) {
						}
					} else {
						status.editMessage(String.format("I've hit your last ship! I hit %s, so I win. I had **%s** left (%s ships)", bomb, Main.join(botSea.toArray(new String[0])), botSea.size())).complete();
						return;
					}
				} else {
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
					}
					status.editMessage("I missed, your turn.").complete();
				}
				Message bombm = Main.waitForInput(event.getMember(), event.getChannel(), 30000);
				if (bombm == null || bombm.getContentDisplay().toUpperCase().equals("STOP")) {
					status.editMessage(String.format("K then, I'll stop. I had **%s** left (%s ships).", Main.joinCustomChar("**, **", botSea.toArray(new String[0])), botSea.size())).complete();
					try {
						bombm.delete().complete();
					} catch (Throwable e) {
					}
					gameSessions.remove(event.getAuthor().getId());
					return;
				} else if (!Arrays.asList(positions).contains(bombm.getContentDisplay().toUpperCase()) || bombm.getContentDisplay().toUpperCase().equals("HELP")) {
					String bombExample = positions[Random.INSTANCE.randInt(positions.length)];
					event.reply(String.format("In this game you have to bomb the bot's ship, " + "you can do this by saying %s for example, this will bomb %s." + " If there's a ship there that ship will be destroyed, if there's no ship there the bomb will explode in the water." + " So when it's your turn you'll just have to say A1, B5, G9, it goes all the way up to J10." + " And just so I have to code less, it's now the bot's turn.", bombExample, bombExample));
				} else if (!board.contains(bombm.getContentDisplay().toUpperCase())) {
					status.editMessage("You already picked that one, my turn now.").complete();
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
					}
				} else if (botSea.contains(bombm.getContentDisplay().toUpperCase())) {
					botSea.remove(bombm.getContentDisplay().toUpperCase());
					if (botSea.size() > 0) {
						status.editMessage(String.format("You've hit one of my ships! I have %s ships left.", botSea.size())).complete();
						if (!bombm.getContentDisplay().endsWith("10"))
							board = board.replace(bombm.getContentDisplay().toUpperCase() + " ", "XX ");
						else board = board.replace(bombm.getContentDisplay().toUpperCase(), "XX");
						boardMsg.editMessage("```fix\n" + board + "```").complete();
						try {
							Thread.sleep(1500);
						} catch (InterruptedException e) {
						}
					} else {
						status.editMessage("You've hit my last ship! You win!").queue();
						gameSessions.remove(event.getAuthor().getId());
						return;
					}
				} else {
					status.editMessage("You missed, my turn.").complete();
					if (!bombm.getContentDisplay().endsWith("10"))
						board = board.replace(bombm.getContentDisplay().toUpperCase() + " ", "XX ");
					else board = board.replace(bombm.getContentDisplay().toUpperCase(), "XX");
					boardMsg.editMessage("```fix\n" + board + "```").complete();
					try {
						bombm.delete().complete();
					} catch (Throwable e) {
					}
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
					}
				}
			}
		} else {
			status = event.getChannel().sendMessage("You go first.").complete();
			while (true) {
				Message bombm = Main.waitForInput(event.getMember(), event.getChannel(), 30000);
				if (bombm == null || bombm.getContentDisplay().toUpperCase().equals("STOP")) {
					status.editMessage(String.format("K then, I'll stop. I had **%s** left (%s ships).", Main.joinCustomChar("**, **", botSea.toArray(new String[0])), botSea.size())).complete();
					try {
						bombm.delete();
					} catch (Throwable e) {
					}
					gameSessions.remove(event.getAuthor().getId());
					return;
				} else if (!Arrays.asList(positions).contains(bombm.getContentDisplay().toUpperCase()) || bombm.getContentDisplay().toUpperCase().equals("HELP")) {
					String example = positions[Random.INSTANCE.randInt(positions.length)];
					event.reply(String.format("In this game you have to bomb the bot's ship, " + "you can do this by saying {0} for example, this will bomb {0}." + " If there's a ship there that ship will be destroyed, if there's no ship there the bomb will explode in the water." + " So when it's your turn you'll just have to say A1, B5, G9, it goes all the way up to J10." + " And just so I have to code less, it's now the bot's turn.", example));
				} else if (!board.contains(bombm.getContentDisplay().toUpperCase())) {
					status.editMessage("You already picked that one, my turn now.").complete();
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
					}
				} else if (botSea.contains(bombm.getContentDisplay().toUpperCase())) {
					botSea.remove(bombm.getContentDisplay().toUpperCase());
					if (botSea.size() > 0) {
						status.editMessage(String.format("You've hit one of my ships! I have %s ships left.", botSea.size())).complete();
						if (!bombm.getContentDisplay().toUpperCase().endsWith("10"))
							board = board.replace(bombm.getContentDisplay().toUpperCase() + " ", "XX ");
						else board = board.replace(bombm.getContentDisplay().toUpperCase(), "XX");
						try {
							Thread.sleep(1500);
						} catch (InterruptedException e) {
						}
					} else {
						status.editMessage("You've hit my last ship! You win!").complete();
						gameSessions.remove(event.getAuthor().getId());
						return;
					}
				} else {
					status.editMessage("You missed, my turn.").complete();
					if (!bombm.getContentDisplay().endsWith("10"))
						board = board.replace(bombm.getContentDisplay().toUpperCase() + " ", "XX ");
					else board = board.replace(bombm.getContentDisplay().toUpperCase(), "XX");
					boardMsg.editMessage("```fix\n" + board + "```").complete();
					try {
						bombm.delete().complete();
					} catch (Throwable e) {
					}
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
					}
				}
				String bomb = positions[Random.INSTANCE.randInt(2)];
				if (userSea.contains(bomb)) {
					userSea.remove(bomb);
					if (userSea.size() > 0) {
						status.editMessage(String.format("I've hit one of your ships! I hit %s, you have **%s** left (%s ships). Your turn.", bomb, Main.joinCustomChar("**, **", userSea.toArray(new String[0])), userSea.size())).complete();
						try {
							Thread.sleep(1500);
						} catch (InterruptedException e) {
						}
					} else {
						status.editMessage(String.format("I've hit your last ship! I hit %s, so I win. I had **%s** left (%s ships)", bomb, Main.joinCustomChar("**, **", botSea.toArray(new String[0])), botSea.size())).complete();
						gameSessions.remove(event.getAuthor().getId());
						return;
					}
				} else {
					status.editMessage("I missed, your turn.").complete();
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	@Command(category = "Fun", help = "Kill someone in a creative way.", name = "kill", arguments = "<user>")
	public static void kill(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			if (Main.getUserFromInput(event.getMessage()) == null)
				event.reply("That user could not be found.");
			else if (Main.getUserFromInput(event.getMessage()).getId().equals(event.getSelfMember().getUser().getId()))
				event.reply("You think that's funny?");
			else event.reply(Random.INSTANCE.choice(waysToKill).replaceAll("\\{KILLER\\}", event.getAuthor().getAsMention()).replaceAll("\\{VICTIM\\}", Main.getUserFromInput(event.getMessage()).getAsMention()));
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Fun", help = "Memes are the only thing that keep PlanetTeamSpeak alive.", name = "meme")
	public static void meme(CommandEvent event) {
		List<String> serverMemes = (List) memes.get("global");
		if (event.getGuild() != null && memes.containsKey(event.getGuild().getId())) serverMemes.addAll((List) memes.get(event.getGuild().getId()));
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(new Color(Random.INSTANCE.randInt(256 * 256 * 256)));
		embed.setImage(Random.INSTANCE.choice(serverMemes));
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
			if (!memes.containsKey(event.getGuild().getId()))
				memes.put(event.getGuild().getId(), Lists.newArrayList(event.getArgs()));
			else((List) memes.get(event.getGuild().getId())).add(event.getArgs());
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
			if (!memes.containsKey(event.getGuild().getId()))
				event.reply("This server has no custom memes.");
			else if (!((List) memes.get(event.getGuild().getId())).contains(event.getArgs()))
				event.reply("That meme could not be found, to list all memes type %slistmemes");
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
				if (ch.charValue() >= 'a' && ch.charValue() <= 'z')
					output += ":regional_indicator_" + ch + ":";
				else switch (ch.charValue()) {
				case '0': {
					output += ":zero:";
					break;
				}
				case '1': {
					output += ":one:";
					break;
				}
				case '2': {
					output += ":two:";
					break;
				}
				case '3': {
					output += ":three:";
					break;
				}
				case '4': {
					output += ":four:";
					break;
				}
				case '5': {
					output += ":five:";
					break;
				}
				case '6': {
					output += ":six:";
					break;
				}
				case '7': {
					output += ":seven:";
					break;
				}
				case '8': {
					output += ":eight:";
					break;
				}
				case '9': {
					output += ":nine:";
					break;
				}
				default: {
					output += ch;
					break;
				}
				}
			event.reply(output);
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Fun", help = "Plant or harvest crops.", name = "garden")
	public static void garden(CommandEvent event) {
		Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Plant a plant.", name = "plant", parent = "com.impulsebot.commands.Fun.garden", guildOnly = true, arguments = "<plant>")
	public static void gardenPlant(CommandEvent event) throws CommandException {
		if (!event.argsEmpty()) {
			event.setArgs(event.getArgs().toLowerCase());
			if (!plants.containsKey(event.getArgs()))
				event.reply("That plant could not be found, to list all plants type %sgarden plants.", Main.getPrefix(event.getGuild()));
			else if (!Economy.hasAccount(event.getMember()))
				event.reply("You do not have a bank account, you can make one with %sbank register.", Main.getPrefix(event.getGuild()));
			else if (!Economy.hasEnoughBalance(event.getMember(), (int) plants.get(event.getArgs()).get("price")))
				event.reply("You do not have enough money to plant that, you have **%s credits**, but you need **%s credits**.", Economy.getBalance(event.getMember()), (int) plants.get(event.getArgs()).get("price"));
			else if (garden.containsKey(event.getGuild().getId()) && garden.get(event.getGuild().getId()).containsKey(event.getAuthor().getId()) && garden.get(event.getGuild().getId()).get(event.getAuthor().getId()).containsKey(event.getArgs()))
				event.reply("You have already planted that plant.");
			else {
				event.reply("Are you sure you want to plant **a %s** for **%s credits**? It will take %s to grow. (yes/no)", event.getArgs(), (int) plants.get(event.getArgs()).get("price"), Main.formatMillis((int) plants.get(event.getArgs()).get("growthtime") * 1000));
				Message response = Main.waitForInput(event.getMember(), event.getChannel(), 15000);
				if (response == null || !response.getContentDisplay().startsWith("y"))
					event.reply("Kk, then not.");
				else {
					Economy.subtractBalance(event.getMember(), (int) plants.get(event.getArgs()).get("price"));
					if (!garden.containsKey(event.getGuild().getId())) garden.put(event.getGuild().getId(), new HashMap());
					if (!garden.get(event.getGuild().getId()).containsKey(event.getAuthor().getId())) garden.get(event.getGuild().getId()).put(event.getAuthor().getId(), Main.newHashMap(new String[] {"items"}, new Map[] {new HashMap()}));
					garden.get(event.getGuild().getId()).get(event.getAuthor().getId()).put(event.getArgs(), (double) System.currentTimeMillis());
					try {
						DataIO.saveJson(garden, "data/fun/garden.json");
					} catch (IOException e) {
						throw new CommandException("An unknown error occurred while saving the data file.", e);
					}
					event.reply("Successfully planted **a %s**, it will take %s to grow, you can always view it's statistics with **%sgarden info %s**.", event.getArgs(), Main.formatMillis((int) plants.get(event.getArgs()).get("growthtime") * 1000), Main.getPrefix(event.getGuild()), event.getArgs());
				}
			}
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Harvest a plant.", name = "harvest", parent = "com.impulsebot.commands.Fun.garden", guildOnly = true, arguments = "<plant>")
	public static void gardenHarvest(CommandEvent event) throws CommandException {
		if (!event.argsEmpty()) {
			event.setArgs(event.getArgs().toLowerCase());
			if (!plants.containsKey(event.getArgs()))
				event.reply("That plant could not be found, to list all plants type %sgarden plants.", Main.getPrefix(event.getGuild()));
			else if (!garden.containsKey(event.getGuild().getId()))
				event.reply("No one in this server has planted any plants yet.");
			else if (!garden.get(event.getGuild().getId()).containsKey(event.getAuthor().getId()))
				event.reply("You have not planted any plants yet.");
			else if (!garden.get(event.getGuild().getId()).get(event.getAuthor().getId()).containsKey(event.getArgs()))
				event.reply("You have not planted that plant.");
			else if (Double.parseDouble(garden.get(event.getGuild().getId()).get(event.getAuthor().getId()).get(event.getArgs()).toString()) + (int) plants.get(event.getArgs()).get("growthtime") * 1000 >= System.currentTimeMillis())
				event.reply("You cannot harvest that plant yet, it still has to grow for another %s.", Main.formatMillis((long) Double.parseDouble(garden.get(event.getGuild().getId()).get(event.getAuthor().getId()).get(event.getArgs()).toString()) + (int) plants.get(event.getArgs()).get("growthtime") * 1000 - System.currentTimeMillis()));
			else {
				((Map) garden.get(event.getGuild().getId()).get(event.getAuthor().getId()).get("items")).put(plants.get(event.getArgs()).get("item"), Main.getIntFromPossibleDouble(((Map) garden.get(event.getGuild().getId()).get(event.getAuthor().getId()).get("items")).getOrDefault(plants.get(event.getArgs()).get("item"), 0)) + 1);
				garden.get(event.getGuild().getId()).get(event.getAuthor().getId()).remove(event.getArgs());
				try {
					DataIO.saveJson(garden, "data/fun/garden.json");
				} catch (IOException e) {
					throw new CommandException("An unknown error occurred while saving the data file.", e);
				}
				int amount = Integer.parseInt(((Map) garden.get(event.getGuild().getId()).get(event.getAuthor().getId()).get("items")).get(plants.get(event.getArgs()).get("item")).toString().split("\\.")[0]);
				event.reply("Successfully harvested your **%s** you got **1 %s** which brings you to a total of **%s %s**, you can sell them with %sgarden sell.", event.getArgs(), plants.get(event.getArgs()).get("item"), amount, amount == 1 ? "" : Main.plural(plants.get(event.getArgs()).get("item").toString()), Main.getPrefix(event.getGuild()));
			}
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Get information about a plant.", name = "info", parent = "com.impulsebot.commands.Fun.garden", arguments = "<plant>")
	public static void gardenInfo(CommandEvent event) {
		if (!event.argsEmpty()) {
			event.setArgs(event.getArgs().toLowerCase());
			if (!plants.containsKey(event.getArgs()))
				event.reply("That plant could not be found, to list all plants type %sgarden plants.", Main.getPrefix(event.getGuild()));
			else {
				EmbedBuilder builder = new EmbedBuilder().setColor(Color.GREEN).setTitle(Main.pascalCase(event.getArgs())).addField("Name", Main.pascalCase(event.getArgs()), true).addField("Item", plants.get(event.getArgs()).get("item").toString(), true).addField("Growthtime", Main.formatMillis(Long.parseLong(plants.get(event.getArgs()).get("growthtime").toString()) * 1000), true);
				if (event.getGuild() != null && garden.containsKey(event.getGuild().getId()) && garden.get(event.getGuild().getId()).containsKey(event.getAuthor().getId()) && garden.get(event.getGuild().getId()).get(event.getAuthor().getId()).containsKey(event.getArgs())) {
					long timeLeft = Long.parseLong(garden.get(event.getGuild().getId()).get(event.getAuthor().getId()).get(event.getArgs()).toString().split("\\.")[0]) - System.currentTimeMillis();
					timeLeft = timeLeft < 0 ? 0 : timeLeft;
					builder.addField("Growthtime left", Main.formatMillis(timeLeft), true);
				}
				event.reply(builder.build());
			}
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Get all available and planted plants.", name = "plants", parent = "com.impulsebot.commands.Fun.garden", cooldown = 5)
	public static void gardenPlants(CommandEvent event) {
		EmbedBuilder builder = new EmbedBuilder().setColor(Color.GREEN).setTitle("Available plants").setDescription("Formulas:\n\tvalue = seconds / 3.6\n\tprice = value / 4");
		for (String plant : Main.sort(new ArrayList<>(plants.keySet())))
			builder.addField(Main.pascalCase(plant), Main.formatMillis(Long.parseLong(plants.get(plant).get("growthtime").toString()) * 1000), true);
		event.reply(builder.build());
		if (event.getGuild() != null && garden.containsKey(event.getGuild().getId()) && garden.get(event.getGuild().getId()).containsKey(event.getAuthor().getId())) {
			builder.clearFields();
			builder.setTitle("Your plants");
			for (String plant : garden.get(event.getGuild().getId()).get(event.getAuthor().getId()).keySet())
				if (!plant.equalsIgnoreCase("items")) {
					long timeLeft = 0;
					timeLeft = (int) plants.get(plant).get("growthtime") * 1000 - (System.currentTimeMillis() - (long) Double.parseDouble(garden.get(event.getGuild().getId()).get(event.getAuthor().getId()).get(plant).toString()));
					timeLeft = timeLeft < 0 ? 0 : timeLeft;
					builder.addField(Main.pascalCase(plant), Main.formatMillis(timeLeft), true);
				}
			if (builder.getFields().size() > 0) event.reply(builder.build());
		}
	}

	@Subcommand(help = "Shows you all your items.", name = "items", parent = "com.impulsebot.commands.Fun.garden", guildOnly = true)
	public static void gardenItems(CommandEvent event) {
		if (garden.containsKey(event.getGuild().getId())) {
			if (garden.get(event.getGuild().getId()).containsKey(event.getAuthor().getId())) {
				if (!((Map) garden.get(event.getGuild().getId()).get(event.getAuthor().getId()).get("items")).isEmpty()) {
					EmbedBuilder builder = new EmbedBuilder().setColor(Color.GREEN).setTitle("Your items");
					for (String item : ((Map<String, Object>) garden.get(event.getGuild().getId()).get(event.getAuthor().getId()).get("items")).keySet())
						builder.addField(Main.pascalCase(item), "" + Main.getIntFromPossibleDouble(((Map<String, Object>) garden.get(event.getGuild().getId()).get(event.getAuthor().getId()).get("items")).get(item)), true);
					event.reply(builder.build());
				} else event.reply("You have no items.");
			} else event.reply("You haven't planted anything yet.");
		} else event.reply("No one in this server has planted anything yet.");
	}

	@Subcommand(help = "Sell all your earned items and convert them into money.", name = "sell", parent = "com.impulsebot.commands.Fun.garden", guildOnly = true)
	public static void gardenSell(CommandEvent event) throws CommandException {
		if (Economy.hasAccount(event.getMember())) {
			if (garden.containsKey(event.getGuild().getId())) {
				if (garden.get(event.getGuild().getId()).containsKey(event.getAuthor().getId())) {
					Map items = (Map) garden.get(event.getGuild().getId()).get(event.getAuthor().getId()).get("items");
					Map items1 = new HashMap(items); // avoiding ConcurrentModificationExceptions.
					if (!items.isEmpty()) {
						int value = 0;
						for (Object item : items.keySet())
							for (Map plant : plants.values())
								if (plant.get("item").toString().equals(item.toString())) {
									value += (int) plant.get("value");
									items1.remove(item);
									break;
								}
						garden.get(event.getGuild().getId()).get(event.getAuthor().getId()).put("items", items1);
						try {
							DataIO.saveJson(garden, "data/fun/garden.json");
						} catch (IOException e) {
							throw new CommandException("An unknown error occurred while saving the garden file.", e);
						}
						Economy.addBalance(event.getMember(), value);
						event.reply("Successfully sold all your items, **%s credits** have been added to your account.", value);
					} else event.reply("You do not have any items.");
				} else event.reply("You have not touched the garden yet.");
			} else event.reply("No one in this server has touched the garden yet.");
		} else event.reply("You do not have a bank account, you can make one with %sbank register.", Main.getPrefix(event.getGuild()));
	}

	@Command(category = "Fun", help = "Make Mao either happy, or sad.", name = "sadmao", guildOnly = true, userPermissions = {Permission.MESSAGE_MANAGE})
	public static void sadMao(CommandEvent event) throws CommandException {
		if (!sadMaos.contains(event.getGuild().getId())) {
			sadMaos.add(event.getGuild().getId());
			try {
				DataIO.saveJson(sadMaos, "data/fun/sadmaos.json");
			} catch (IOException e) {
				throw new CommandException("Mao seems too powerful, he cannot be deactivated :O.", e);
			}
			event.reply("You've made Mao sad, now he's too sad to laugh when someone says ayy.");
		} else {
			sadMaos.remove(event.getGuild().getId());
			try {
				DataIO.saveJson(sadMaos, "data/fun/sadmaos.json");
			} catch (IOException e) {
				throw new CommandException("Mao is too depressed to ever be happy again, he cannot be activated. :(", e);
			}
			event.reply("You've made Mao happy, now he's no longer too sad to laugh when someone says ayy.");
		}
	}

	@Command(category = "Fun", help = "There's no such thing.", name = "easteregg", hidden = true)
	public static void easterEgg(CommandEvent event) {
		if (!easterEgg.containsKey(event.getAuthor().getId())) easterEgg.put(event.getAuthor().getId(), 1);
		String output = "";
		switch (easterEgg.get(event.getAuthor().getId())) {
		case 1: {
			output = "Easteregg?";
			break;
		}
		case 2: {
			output = "Never heard of it.";
			break;
		}
		case 3: {
			output = "Stop that.";
			break;
		}
		case 4: {
			output = "There's nothing here.";
			break;
		}
		case 5: {
			output = "Nothing to see here!";
			break;
		}
		case 6: {
			output = "You stop that right now!";
			break;
		}
		case 7: {
			output = "I'll have a talk with your mom if you don't stop it right now!";
			break;
		}
		case 8: {
			output = "Ok, that's it! I'm calling your mom!";
			easterEgg.put(event.getAuthor().getId(), 0);
			Main.sendPrivateMessage(Main.getOwner(), event.getAuthor().getAsMention() + " has gotten to easter egg #8, they're bullying me. :(");
			break;
		}
		}
		easterEgg.put(event.getAuthor().getId(), easterEgg.get(event.getAuthor().getId()) + 1);
		event.reply(output);
	}

	@Command(category = "General", help = "Spin the revolver's chamber and then *PANG*. \nThe given bet will be removed from your balance if you lose or doubled if you win, if the given bet is 0 or not given there's nothing to lose.", name = "russianroulette", cooldown = 30, arguments = "[bet] [bullets]")
	public static void russianRoulette(CommandEvent event) throws IOException {
		if (!event.getArgs().isEmpty() && Main.isInteger(event.getArgs().split(" ")[0]) && Integer.parseInt(event.getArgs().split(" ")[0]) != 0) {
			int bet = Integer.parseInt(event.getArgs().split(" ")[0]);
			if (!Economy.hasAccount(event.getMember()))
				event.reply("You cannot bet without having a bank account, you can make one with %sbank register.", Main.getPrefix(event.getGuild()));
			else if (!Economy.hasEnoughBalance(event.getMember(), bet))
				event.reply("You do not have enough balance to bid that high.");
			else {
				int bullets = Random.INSTANCE.randInt(1, 5);
				if (event.getArgs().split(" ").length > 1 && Main.isInteger(event.getArgs().split(" ")[1]) && Integer.parseInt(event.getArgs().split(" ")[1]) > 0 && Integer.parseInt(event.getArgs().split(" ")[1]) < 6) bullets = Integer.parseInt(event.getArgs().split(" ")[1]);
				if (bet > 0) {
					Map<Integer, Integer> maxBets = Main.newHashMap(new Integer[] {1, 2, 3, 4, 5}, new Integer[] {500, 1500, 4500, 13500, 40500});
					if (bet > maxBets.get(bullets)) {
						event.reply("You can only bid a maximum of %s credits with %s bullet%s.", maxBets.get(bullets), bullets, bullets == 1 ? "" : "s");
						return;
					}
				}
				boolean shot = rrPlay(event, bullets, event.getChannel().sendMessageFormat("You've made a bet for %s credits that you'll survive with %s bullets in the barrel. You load up %s bullets and start spinning the barrel...", bet, bullets, bullets).complete());
				int oldBalance = Economy.getBalance(event.getMember());
				if (shot)
					Economy.subtractBalance(event.getMember(), bet);
				else Economy.addBalance(event.getMember(), bet);
				event.reply("%s credits have been %s %s your bank account, old balance: **%s** credits, new balance: **%s** credits.", bet, shot ? "removed" : "added", shot ? "from" : "to", oldBalance, Economy.getBalance(event.getMember()));
			}
		} else {
			int bullets = Random.INSTANCE.randInt(1, 5);
			if (event.getArgs().split(" ").length > 1 && Main.isInteger(event.getArgs().split(" ")[1]) && Integer.parseInt(event.getArgs().split(" ")[1]) > 0 && Integer.parseInt(event.getArgs().split(" ")[1]) < 6) bullets = Integer.parseInt(event.getArgs().split(" ")[1]);
			rrPlay(event, bullets, null);
		}
	}

	@Command(category = "Fun", help = "Play trivia.", name = "trivia")
	public static void trivia(CommandEvent event) {
		Main.sendCommandHelp(event);
	}

	@Subcommand(help = "List all trivias available.", name = "list", parent = "com.impulsebot.commands.Fun.trivia")
	public static void triviaList(CommandEvent event) {
		event.reply("You can currently play the following trivias:\n" + Main.joinNiceString(Trivia.getCategories()));
	}

	@Subcommand(help = "Start a trivia of your choice.", name = "start", parent = "com.impulsebot.commands.Fun.trivia")
	public static void triviaStart(CommandEvent event) {
		if (!event.argsEmpty()) {
			if (!Trivia.isValidCategory(event.getArgs()))
				event.reply("The given category could not be found, you can get a list of possible categories using %strivia list.", Main.getPrefix(event.getGuild()));
			else {
				TriviaResult result = Trivia.getInstance(event.getArgs()).start(event.getChannel(), event.getGuild());
				event.reply(String.valueOf(result));
			}
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Fun", help = "Create an Impact meme.\n\nImage and toptext are mandatory, bottomtext and size are optional\nExample:\n\t[p]impactmeme <https://target.scene7.com/is/image/Target/red_bg84112-170313_1489428814121?wid=1110&qlt=100&fmt=png>;one does not simply;get rid of impact", name = "impactmeme", arguments = "<image>;<toptext>;[bottomtext];[size]")
	public static void impactMeme(CommandEvent event) throws CommandException {
		if (!event.argsEmpty() && event.getArgs().split(";").length > 1) {
			String image = event.getArgs().split(";")[0];
			String topText = event.getArgs().split(";")[1];
			String bottomText = event.getArgs().split(";").length > 2 ? event.getArgs().split(";")[2] : "";
			float size = event.getArgs().split(";").length > 3 && Main.isFloat(event.getArgs().split(";")[3]) ? Float.parseFloat(event.getArgs().split(";")[3]) : 48F;
			String fileLocation = "data/tmp/" + Random.INSTANCE.randInt() + ".png";
			try {
				BufferedImage imageOut = ImageManipulator.toBufferedImage(ImageManipulator.impactMeme(ImageIO.read(new URL(image).openStream()), topText, bottomText, size), true);
				ImageIO.write(imageOut, "png", new File(fileLocation));
				imageOut.flush();
			} catch (IOException e) {
				if (e.getMessage() != null && e.getMessage().startsWith("Server returned HTTP response code:")) {
					event.reply("The given image could not be downloaded because it returned a **%s** error, you could try downloading it and uploading it to <https://cdn.impulsebot.com> and use that URL instead.", e.getMessage().substring(36, 39));
					return;
				} else if (e.getMessage() != null && e.getMessage().toLowerCase().startsWith("no protocol")) {
					event.reply("Could not find a protocol in the given URL, make sure it starts with http://, https://, ftp://, etc...");
					return;
				}
				throw new CommandException("An unknown error occurred while drawing the top- and bottomtext.", e);
			} catch (NullPointerException e) {
				e.printStackTrace();
				event.reply("The given image is not supported by the ImageIO Java library.");
				return;
			}
			try {
				event.getChannel().sendFile(new File(fileLocation)).complete();
			} catch (IllegalArgumentException e) {
				event.reply("The output image was too large, please try again with a smaller image.");
			}
			new File(fileLocation).delete();
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Fun", help = "***A S C E N D E D***", name = "ascended")
	public static void ascended(CommandEvent event) {
		if (!event.argsEmpty()) {
			String ascended = "***";
			for (char ch : event.getArgs().toCharArray())
				if (ch == '*')
					ascended += "\\* ";
				else ascended += "" + ch + ' ';
			event.reply(ascended.trim() + "***");
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Fun", help = "InSeRt ReTaRdEd QuOtE hErE.", name = "rarted")
	public static void rarted(CommandEvent event) {
		if (!event.argsEmpty()) {
			String rarted = "";
			boolean currentUp = false;
			for (char ch : event.getArgs().toCharArray())
				rarted += (currentUp = ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' ? !currentUp : currentUp) ? "" + Character.toUpperCase(ch) : "" + ch;
			event.reply(rarted.trim());
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Fun", help = "Play four-in-a-row with the bot!", name = "4row", botPermissions = {Permission.MESSAGE_MANAGE}, guildOnly = true)
	public static void fourRow(CommandEvent event) {
		try {
			if (gameSessions.contains(event.getAuthor().getId())) {
				event.reply("You are already playing a game, please finish that one first before starting a new one.");
				return;
			}
			gameSessions.add(event.getAuthor().getId());
		// @formatter:off
		int[][] board = {
				{
					0, 0, 0, 0, 0, 0
				},
				{
					0, 0, 0, 0, 0, 0
				},
				{
					0, 0, 0, 0, 0, 0
				},
				{
					0, 0, 0, 0, 0, 0
				},
				{
					0, 0, 0, 0, 0, 0
				},
				{
					0, 0, 0, 0, 0, 0
				},
				{
					0, 0, 0, 0, 0, 0
				},
		}; // 7 wide, 6 high. 7 columns of 6 spaces per column.
		// -1 indicates bot's space, 0 indicates not used, 1 indicates user's space.
		// @formatter:on
			AtomicObject<String> boardString = new AtomicObject("");
			AtomicObject<Message> boardMsg = new AtomicObject();
			class FourRowHelper {
				public boolean canPutCoin(int column) {
					if (column > 6) column = 6;
					for (int i = 5; i >= 0; i--)
						if (board[column][i] == 0) return true;
					return false;
				}

				public int putCoin(int column, boolean bot) {
					if (column > 6) column = 7;
					column -= 1;
					for (int i = 5; i >= 0; i--)
						if (board[column][i] == 0) {
							board[column][i] = bot ? -1 : 1;
							break;
						}
					generateBoardString();
					for (int c = 0; c <= 6; c++)
						for (int r = 0; r <= 5; r++) { // checking if anyone won.
							if (r + 3 < 6 && board[c][r] != 0 && board[c][r + 1] == board[c][r] && board[c][r + 2] == board[c][r] && board[c][r + 3] == board[c][r]) return board[c][r]; // checking vertical
							if (c + 3 < 7 && board[c][r] != 0 && board[c + 1][r] == board[c][r] && board[c + 2][r] == board[c][r] && board[c + 3][r] == board[c][r]) return board[c][r]; // checking horizontal
							if (c - 3 > 0 && r - 3 > 0 && board[c][r] != 0 && board[c - 1][r - 1] == board[c][r] && board[c - 2][r - 2] == board[c][r] && board[c - 3][r - 3] == board[c][r]) return board[c][r]; // checking diagonal-up
							if (r + 3 < 6 && c + 3 < 7 && board[c][r] != 0 && board[c + 1][r + 1] == board[c][r] && board[c + 2][r + 2] == board[c][r] && board[c + 3][r + 3] == board[c][r]) return board[c][r]; // checking diagonal-down
						}
					return 0;
				}

				public void generateBoardString() {
					int[][] boardRows = new int[6][7];
					for (int i = 0; i < 6; i++)
						for (int x = 0; x < 7; x++)
							boardRows[i][x] = board[x][i]; // converting a column-based board to a row-based board.
					boardString.set("");
					for (int[] row : boardRows) {
						for (int column : row)
							boardString.set(boardString.get() + (column == 0 ? ":o:" : column == 1 ? ":grimacing:" : ":robot:"));
						boardString.set(boardString.get() + "\n");
					}
					boardString.set(boardString.get().trim());
					if (boardMsg.get() == null)
						boardMsg.set(event.getChannel().sendMessage(boardString.get()).complete());
					else boardMsg.get().editMessage(boardString.get()).queue();
				}

				public int pickColumn() {
					int column = -1;
					for (int c = 0; c <= 6; c++)
						for (int r = 0; r <= 5; r++) { // ai system, so the player won't win too easily.
							if (r + 2 < 6 && board[c][r] != 0 && board[c][r + 1] == board[c][r] && canWin(c, r, 0) && Random.INSTANCE.choice(true, false)) column = c; // checking vertical
							if (c + 2 < 7 && board[c][r] != 0 && board[c + 1][r] == board[c][r] && canWin(c, r, 1)) column = board[c + 2][r] != 0 && c + 3 < 7 ? c + 3 : c + 2; // checking horizontal left to right
							if (c - 2 >= 0 && board[c][r] != 0 && board[c - 1][r] == board[c][r] && canWin(c, r, 2)) column = board[c - 2][r] != 0 && c - 3 >= 0 ? c - 3 : c - 2; // checking horizontal right to left
							if (c + 2 < 7 && r - 2 >= 0 && board[c][r] != 0 && board[c + 1][r - 1] == board[c][r] && canWin(c, r, 3)) column = board[c + 2][r - 2] != 0 && c + 3 < 7 && r - 3 >= 0 ? c + 3 : c - 2; // checking diagonal-up
							if (r + 2 < 6 && c + 2 >= 0 && board[c][r] != 0 && board[c + 1][r + 1] == board[c][r] && canWin(c, r, 4)) column = board[c + 2][r + 2] != 0 && c + 3 < 7 && r + 3 < 6 ? c + 3 : c + 2; // checking diagonal-down
						}
					if (column < 0 || column > 6) column = Random.INSTANCE.randInt(0, 6);
					while (!canPutCoin(column))
						column = Random.INSTANCE.randInt(0, 6);
					return column + 1;
				}

				private boolean canWin(int c, int r, int w) {
					switch (w) {
					case 0:
						return r - 1 >= 0 && board[c][r - 1] == 0 || r - 2 >= 0 && board[c][r - 2] == 0;
					case 1:
						return c + 2 < 7 && board[c + 2][r] == 0 || c + 3 < 7 && board[c + 3][r] == 0;
					case 2:
						return c - 2 >= 0 && board[c - 2][r] == 0 || c - 3 >= 0 && board[c - 3][r] == 0;
					case 3:
						return c + 2 < 7 && r - 2 >= 0 && board[c + 2][r - 2] == 0 || c + 3 < 7 && r - 3 >= 0 && board[c + 3][r - 3] == 0;
					case 4:
						return c + 2 < 7 && r + 2 < 6 && board[c + 2][r + 2] == 0 || c + 3 < 7 && r + 3 < 6 && board[c + 3][r + 3] == 0;
					default:
						return false;
					}
				}

				public boolean isBoardFull() {
					for (int c = 0; c < 7; c++)
						for (int r = 0; r < 6; r++)
							if (board[c][r] == 0) return false;
					return true;
				}

			}
			FourRowHelper helper = new FourRowHelper();
			Message status = null;
			helper.generateBoardString();
			if (Random.INSTANCE.choice(true, false)) {
				status = event.getChannel().sendMessage("I'll start.").complete();
				Main.sleep(5000);
				helper.putCoin(Random.INSTANCE.randInt(1, 7), true);
				status.editMessage("Your turn, type a number ranging from 1 to 7 to select a column.").complete();
			} else status = event.getChannel().sendMessage("Your turn, type a number ranging from 1 to 7 to select a column.").complete();
			Message response = null;
			while (true) {
				response = Main.waitForInput(event.getAuthor(), event.getChannel(), 15000);
				if (response == null || response.getContentDisplay().equalsIgnoreCase("stop")) {
					event.reply("Alright, quitting.");
					gameSessions.remove(event.getAuthor().getId());
					return;
				} else {
					int column = -1;
					while (!Main.isInteger(response.getContentDisplay()) || (column = Integer.parseInt(response.getContentDisplay())) < 0 || column > 7 || !helper.canPutCoin(column)) {
						status.editMessage("The response gotten wasn't an integer ranging from 1 to 7 or that column is full, please try again.").complete();
						response.delete().queue();
						response = Main.waitForInput(event.getAuthor(), event.getChannel(), 15000);
						if (response == null || response.getContentDisplay().equalsIgnoreCase("stop")) {
							event.reply("Alright, quitting.");
							gameSessions.remove(event.getAuthor().getId());
							return;
						}
					}
					response.delete().queue();
					int winner = helper.putCoin(column, false);
					if (winner == -1) {
						status.editMessage("I won!").complete();
						gameSessions.remove(event.getAuthor().getId());
						return;
					} else if (winner == 1) {
						status.editMessage("You won, gg!").complete();
						gameSessions.remove(event.getAuthor().getId());
						return;
					} else {
						status.editMessage("My turn!").complete();
						Main.sleep(5000);
						winner = helper.putCoin(helper.pickColumn(), true);
						if (winner == -1) {
							status.editMessage("I won!").complete();
							gameSessions.remove(event.getAuthor().getId());
							return;
						} else if (winner == 1) {
							status.editMessage("You won, gg!").complete();
							gameSessions.remove(event.getAuthor().getId());
							return;
						} else if (helper.isBoardFull()) {
							status.editMessage("Board is full, but nobody won. Maybe next time!").complete();
							return;
						} else status.editMessage("Your turn, type a number ranging from 1 to 7 to select a column.").complete();
					}
				}
			}
		} catch (Exception e) {
			// This should never happen, but if the command errors the user is most likely
			// still in the gameSessions List and won't be able to play games until restart.
			gameSessions.remove(event.getAuthor().getId());
			throw e;
		}
	}

	private static boolean rrPlay(CommandEvent event, int bullets, Message message) {
		if (message == null) message = event.getChannel().sendMessage("You load up " + bullets + " bullets and start spinning the barrel...").complete();
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
		}
		message.editMessage("You put the gun against your head and work up the courage to pull the trigger.").queue();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
		boolean shot = false;
		Boolean[] choices = new Boolean[6];
		for (int x : Main.range(bullets))
			choices[x] = true;
		for (int x = 0; x < choices.length; x++)
			if (choices[x] == null) choices[x] = false;
		shot = Random.INSTANCE.choice(choices);
		if (shot)
			message.editMessage("*PANG* sadly, one of the bullets hit you and now you're dead.").queue();
		else message.editMessage("*click* nothing happened, you've survived!").queue();
		return shot;
	}

	@SubscribeEvent
	public static void onMessageReceived(MessageReceivedEvent event) {
		if (event.getMessage().getContentDisplay().toLowerCase().contains("ayy") && (event.getGuild() == null || !sadMaos.contains(event.getGuild().getId()) && !event.getGuild().getName().toLowerCase().contains("bots"))) {
			LaughingMao.sendLMao(event.getChannel(), null);
			LaughingMao.addLmao(event.getMessage());
		}
	}

}
