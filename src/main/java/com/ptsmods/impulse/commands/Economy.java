package com.ptsmods.impulse.commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.miscellaneous.CommandEvent;
import com.ptsmods.impulse.miscellaneous.CommandException;
import com.ptsmods.impulse.miscellaneous.Subcommand;
import com.ptsmods.impulse.utils.DataIO;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;

public class Economy {

	private static Map bank;
	private static Map settings;
	private static Map cooldowns = new HashMap();

	static {
		try {
			settings = DataIO.loadJson("data/economy/bank.json", Map.class);
			settings = settings == null ? new HashMap() : settings;
		} catch (IOException e) {
			RuntimeException e1 = new RuntimeException("An unknown error occurred while loading the data file.");
			e1.setStackTrace(e.getStackTrace());
			throw e1;
		}
		try {
			loadBank();
		} catch (IOException e) {
			throw new RuntimeException("Well, shit.", e);
		}
	}

	@Command(category = "Economy", help = "Manage your bank account.", name = "bank", guildOnly = true)
	public static void bank(CommandEvent event) throws CommandException {
		Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Register an account at the Impulse bank.", name = "register", parent = "com.ptsmods.impulse.commands.Economy.bank", guildOnly = true)
	public static void bankRegister(CommandEvent event) throws IOException {
		try {
			register(event.getMember());
		} catch (UserAlreadyHasABankAccountException e) {
			event.reply("You already have an account at the Impulse Bank.");
			return;
		}
		event.reply("Successfully opened an account at the Impulse Bank.");
	}

	@Subcommand(help = "Set, add or remove credits from your or someone elses bank account.\n\nExamples:\n\t[p]bank set 6900 @PlanetTeamSpeak\n\t[p]bank set +6900 @PlanetTeamSpeak\n\t[p]bank set -6900 @PlanetTeamSpeak.", name = "set", parent = "com.ptsmods.impulse.commands.Economy.bank", arguments = "<amount> [user]", guildOnly = true, userPermissions = {Permission.ADMINISTRATOR})
	public static void bankSet(CommandEvent event) throws CommandException, IOException {
		if (!event.getArgs().isEmpty()) {
			Member member = event.getMember();
			if (event.getArgs().split(" ").length > 1)
				if (!event.getMessage().getMentionedUsers().isEmpty()) member = event.getGuild().getMember(event.getMessage().getMentionedUsers().get(0));
				else {
					String username = Main.join(Main.removeArg(event.getArgs().split(" "), 0));
					List<Member> members = event.getGuild().getMembersByName(username, true);
					if (members.isEmpty()) {
						event.reply("A user by that name could not be found.");
						return;
					} else member = members.get(0);
				}
			String arg0 = event.getArgs().split(" ")[0];
			if (!hasAccount(member)) event.reply(member.getUser().getId().equals(event.getAuthor().getId()) ?
					"You do not have a bank account, you can register one with %sbank register." :
						"That user does not have a bank account, they can register one with %sbank register.",
						Main.getPrefix(event.getGuild()));
			else if (!Main.isInteger(arg0) && !Main.isInteger(arg0.substring(1)) && !arg0.startsWith("+") && !arg0.startsWith("-")) Main.sendCommandHelp(event);
			else {
				int oldBalance = getBalance(member);
				if (arg0.startsWith("+")) addBalance(member, Integer.parseInt(arg0.substring(1)));
				else if (arg0.startsWith("-")) removeBalance(member, Integer.parseInt(arg0.substring(1)));
				else setBalance(member, Integer.parseInt(arg0));
				int newBalance = getBalance(member);
				event.reply(member.getUser().getId().equals(event.getAuthor().getId()) ? String.format("Your balance has been set from **%s** to **%s**.", oldBalance, newBalance) : String.format("%s's balance has been set from **%s** to **%s**.", member.getAsMention(), oldBalance, newBalance));
			}
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Tells you your or someone else's balance.", name = "balance", parent = "com.ptsmods.impulse.commands.Economy.bank", arguments = "[user]", guildOnly = true)
	public static void bankBalance(CommandEvent event) throws CommandException {
		Member member = event.getMember();
		if (!event.getArgs().isEmpty()) {
			member = Main.getMemberFromInput(event.getMessage());
			if (member == null) {
				event.reply("The given user could not be found.");
				return;
			}
		}
		if (!hasAccount(member))
			event.reply(member.getUser().getId().equals(event.getAuthor().getId()) ? "You do not have a bank account." : "The given user does not have a bank account.");
		else event.reply(member.getUser().getId().equals(event.getAuthor().getId()) ? String.format("You currently have **%s** credits.", getBalance(member)) : String.format("%s currently has **%s** credits.", member.getEffectiveName(), getBalance(member)));
	}

	@Subcommand(help = "Transfer balance from your account to another account.", name = "transfer", parent = "com.ptsmods.impulse.commands.Economy.bank", arguments = "<amount> <user>", guildOnly = true)
	public static void bankTransfer(CommandEvent event) throws Exception {
		if (!event.getArgs().isEmpty() && event.getArgs().split(" ").length > 1) {
			Member member = null;
			if (!event.getMessage().getMentionedUsers().isEmpty()) member = event.getGuild().getMember(event.getMessage().getMentionedUsers().get(0));
			else {
				String username = Main.join(Main.removeArg(event.getArgs().split(" "), 0));
				List<Member> members = event.getGuild().getMembersByName(username, true);
				if (members.isEmpty()) {
					event.reply("A user by that name could not be found.");
					return;
				} else member = members.get(0);
			}
			String arg0 = event.getArgs().split(" ")[0];
			if (!hasAccount(event.getMember())) event.reply("You do not have a bank account, you can register one with %sbank register.", Main.getPrefix(event.getGuild()));
			else if (!hasAccount(member)) event.reply("That user does not have a bank account, they can register one with %sbank register.", Main.getPrefix(event.getGuild()));
			else if (!Main.isInteger(arg0)) Main.sendCommandHelp(event);
			else {
				int oldBalanceAuthor = getBalance(event.getMember());
				int oldBalanceMember = getBalance(member);
				try {
					transfer(event.getMember(), member, Integer.parseInt(arg0));
				} catch (UserDoesNotHaveEnoughBalanceException e) {
					event.reply("You do not have enough credits to do that.");
					return;
				}
				int newBalanceAuthor = getBalance(event.getMember());
				int newBalanceMember = getBalance(member);
				event.reply("Successfully transferred **%s** credits from %s to %s, %s had **%s** credits and now has **%s** credits and %s had **%s** credits and now has **%s** credits.",
						Integer.parseInt(arg0),
						event.getAuthor().getAsMention(),
						member.getAsMention(),
						event.getAuthor().getAsMention(),
						oldBalanceAuthor,
						newBalanceAuthor,
						member.getAsMention(),
						oldBalanceMember,
						newBalanceMember);
			}
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Resets the balance of everyone in this server.", name = "reset", parent = "com.ptsmods.impulse.commands.Economy.bank", guildOnly = true, userPermissions = {Permission.ADMINISTRATOR})
	public static void bankReset(CommandEvent event) throws IOException {
		if (bank.containsKey(event.getGuild().getId())) {
			event.reply("Are you sure you want to unregister %s users? (yes/no)", ((Map) bank.get(event.getGuild().getId())).size());
			Message response = Main.waitForInput(event.getMember(), event.getChannel(), 15000, event.getMessage().getCreationTime().toEpochSecond());
			if (response == null) event.reply("No response gotten, guess not.");
			else if (!response.getContent().startsWith("ye")) event.reply("Kk, then not.");
			else {
				int registeredUsers = ((Map) bank.get(event.getGuild().getId())).size();
				bank.remove(event.getGuild().getId());
				saveBank();
				event.reply("Successfully unregistered %s users.", registeredUsers);
			}
		} else event.reply("No one in this server has a bank account yet.");
	}

	@Command(category = "Economy", help = "Manage economy bank.", name = "economyset", userPermissions = {Permission.ADMINISTRATOR}, guildOnly = true)
	public static void economySet(CommandEvent event) throws CommandException {
		Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Set the amount of credits a user should get when using the payday command.", name = "paydaycredits", parent = "com.ptsmods.impulse.commands.Economy.economySet", arguments = "<value>", userPermissions = {Permission.ADMINISTRATOR}, guildOnly = true)
	public static void economySetPaydayCredits(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty() && Main.isInteger(event.getArgs().split(" ")[0])) {
			if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"paydayCredits", "paydayCooldown", "slotCooldown", "russianRouletteCooldown"}, new Integer[] {Integer.parseInt(event.getArgs().split(" ")[0]), 3600, 300, 300}));
			else ((Map) settings.get(event.getGuild().getId())).put("paydayCredits", Integer.parseInt(event.getArgs().split(" ")[0]));
			saveSettings();
			event.reply("This server's amount you get when using payday has been set to " + Integer.parseInt(event.getArgs().split(" ")[0]) + ".");
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Set the amount of seconds a user should wait before using the payday command.", name = "paydaycooldown", parent = "com.ptsmods.impulse.commands.Economy.economySet", arguments = "<value>", userPermissions = {Permission.ADMINISTRATOR}, guildOnly = true)
	public static void economySetPaydayCooldown(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty() && Main.isInteger(event.getArgs().split(" ")[0])) {
			if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"paydayCredits", "paydayCooldown", "slotCooldown", "russianRouletteCooldown"}, new Integer[] {360, Integer.parseInt(event.getArgs().split(" ")[0]), 300, 300}));
			else ((Map) settings.get(event.getGuild().getId())).put("paydayCooldown", Integer.parseInt(event.getArgs().split(" ")[0]));
			saveSettings();
			event.reply("This server's payday cooldown has been set to " + Integer.parseInt(event.getArgs().split(" ")[0]) + ".");
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Set the amount of seconds a user should wait before using the slot command.", name = "slotcooldown", parent = "com.ptsmods.impulse.commands.Economy.economySet", arguments = "<value>", userPermissions = {Permission.ADMINISTRATOR}, guildOnly = true)
	public static void economySetSlotCooldown(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty() && Main.isInteger(event.getArgs().split(" ")[0])) {
			if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"paydayCredits", "paydayCooldown", "slotCooldown", "russianRouletteCooldown"}, new Integer[] {360, 3600, Integer.parseInt(event.getArgs().split(" ")[0]), 300}));
			else ((Map) settings.get(event.getGuild().getId())).put("slotCooldown", Integer.parseInt(event.getArgs().split(" ")[0]));
			saveSettings();
			event.reply("This server's slot cooldown has been set to " + Integer.parseInt(event.getArgs().split(" ")[0]) + ".");
		} else Main.sendCommandHelp(event);
	}

	@Subcommand(help = "Set the amount of credits a user should get when using the payday command.", name = "paydaycredits", parent = "com.ptsmods.impulse.commands.Economy.economySet", arguments = "<value>", userPermissions = {Permission.ADMINISTRATOR}, guildOnly = true)
	public static void economySetRussianRouletteCooldown(CommandEvent event) throws CommandException {
		if (!event.getArgs().isEmpty() && Main.isInteger(event.getArgs().split(" ")[0])) {
			if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"paydayCredits", "paydayCooldown", "slotCooldown", "russianRouletteCooldown"}, new Integer[] {360, 3600, 300, Integer.parseInt(event.getArgs().split(" ")[0])}));
			else ((Map) settings.get(event.getGuild().getId())).put("russianRouletteCooldown", Integer.parseInt(event.getArgs().split(" ")[0]));
			saveSettings();
			event.reply("This server's Russian Roulette cooldown has been set to " + Integer.parseInt(event.getArgs().split(" ")[0]) + ".");
		} else Main.sendCommandHelp(event);
	}

	@Command(category = "Economy", help = "Free moneyzz!", name = "payday", guildOnly = true)
	public static void payday(CommandEvent event) throws IOException {
		if (hasAccount(event.getMember())) {
			if (!settings.containsKey(event.getGuild().getId())) settings.put(event.getGuild().getId(), Main.newHashMap(new String[] {"paydayCredits", "paydayCooldown", "slotCooldown", "russianRouletteCooldown"}, new Integer[] {360, 3600, 300, 300}));
			if (!cooldowns.containsKey(event.getGuild().getId())) cooldowns.put(event.getGuild().getId(), new HashMap());
			Long cooldown = (Long) ((Map) cooldowns.get(event.getGuild().getId())).get(event.getAuthor().getId());
			if (cooldown == null || System.currentTimeMillis()-cooldown >= Main.getIntFromPossibleDouble(((Map) settings.get(event.getGuild().getId())).get("paydayCooldown")) * 1000) {
				addBalance(event.getMember(), Main.getIntFromPossibleDouble(((Map) settings.get(event.getGuild().getId())).get("paydayCredits")));
				event.reply("It's payday :smile:, you have been given %s credits.", Main.getIntFromPossibleDouble(((Map) settings.get(event.getGuild().getId())).get("paydayCredits")));
				((Map) cooldowns.get(event.getGuild().getId())).put(event.getAuthor().getId(), System.currentTimeMillis());
			} else event.reply("You cannot do that yet, you have to wait for another " + Main.formatMillis(Main.getIntFromPossibleDouble(((Map) settings.get(event.getGuild().getId())).get("paydayCooldown")) * 1000 - (System.currentTimeMillis()-cooldown)) + ".");
		} else event.reply("You do not have a bank account, you can make one with %sbank register.", Main.getPrefix(event.getGuild()));
	}

	public static boolean hasAccount(Member member) {
		if (!bank.containsKey(member.getGuild().getId()) || !((Map) bank.get(member.getGuild().getId())).containsKey(member.getUser().getId())) return false;
		else return true;
	}

	public static int getBalance(Member member) throws UserHasNoBankAccountException {
		if (!hasAccount(member)) throw new UserHasNoBankAccountException();
		else return Main.getIntFromPossibleDouble(((Map) bank.get(member.getGuild().getId())).get(member.getUser().getId()));
	}

	public static void setBalance(Member member, int balance) throws IOException {
		if (!hasAccount(member)) throw new UserHasNoBankAccountException();
		((Map) bank.get(member.getGuild().getId())).put(member.getUser().getId(), balance);
		saveBank();
	}

	public static void addBalance(Member member, int balance) throws IOException {
		setBalance(member, getBalance(member) + balance);
	}

	public static void removeBalance(Member member, int balance) throws IOException {
		setBalance(member, getBalance(member) - balance);
	}

	public static boolean hasEnoughBalance(Member member, int balance) throws UserHasNoBankAccountException {
		return getBalance(member) >= balance;
	}

	public static void register(Member member) throws IOException, UserAlreadyHasABankAccountException {
		if (!bank.containsKey(member.getGuild().getId())) bank.put(member.getGuild().getId(), new HashMap());
		if (((Map) bank.get(member.getGuild().getId())).keySet().contains(member.getUser().getId())) throw new UserAlreadyHasABankAccountException();
		else {
			((Map) bank.get(member.getGuild().getId())).put(member.getUser().getId(), 0);
			saveBank();
		}
	}

	public static void transfer(Member from, Member to, int balance) throws Exception {
		if (!hasAccount(from) || !hasAccount(to)) throw new UserHasNoBankAccountException();
		else if (getBalance(from) < balance) throw new UserDoesNotHaveEnoughBalanceException();
		else if (!from.getGuild().getId().equals(to.getGuild().getId())) throw new Exception("Users are not in the same guild.");
		else {
			((Map) bank.get(from.getGuild().getId())).put(from.getUser().getId(), getBalance(from) - balance);
			((Map) bank.get(from.getGuild().getId())).put(to.getUser().getId(), getBalance(to) + balance);
			saveBank();
		}
	}

	private static void loadBank() throws IOException {
		try {
			bank = DataIO.loadJson("data/economy/bank.json", Map.class);
			bank = bank == null ? new HashMap() : bank;
		} catch (IOException e) {
			throw new IOException("An unknown error occurred while loading the data file.", e);
		}
	}

	private static void saveBank() throws IOException {
		try {
			DataIO.saveJson(bank, "data/economy/bank.json");
		} catch (IOException e) {
			throw new IOException("An unknown error occurred while saving the data file.", e);
		}
	}

	private static void saveSettings() throws CommandException {
		try {
			DataIO.saveJson(settings, "data/economy/settings.json");
		} catch (IOException e) {
			throw new CommandException("An unknown error occurred while saving the data file.", e);
		}
	}

	public static class UserHasNoBankAccountException extends RuntimeException { // doesn't have to be catched.
		private static final long serialVersionUID = -1632246256790292835L;

		public UserHasNoBankAccountException() { super(); }
		public UserHasNoBankAccountException(String message) { super(message); }
		public UserHasNoBankAccountException(Throwable cause) { super(cause); }
		public UserHasNoBankAccountException(String message, Throwable cause) { super(message, cause); }

	}

	public static class UserAlreadyHasABankAccountException extends Exception { // must be catched.
		private static final long serialVersionUID = 8832996165900822543L;

		public UserAlreadyHasABankAccountException() { super(); }
		public UserAlreadyHasABankAccountException(String message) { super(message); }
		public UserAlreadyHasABankAccountException(Throwable cause) { super(cause); }
		public UserAlreadyHasABankAccountException(String message, Throwable cause) { super(message, cause); }

	}

	public static class UserDoesNotHaveEnoughBalanceException extends Exception {
		private static final long serialVersionUID = 4539371480275587350L;

		public UserDoesNotHaveEnoughBalanceException() { super(); }
		public UserDoesNotHaveEnoughBalanceException(String message) { super(message); }
		public UserDoesNotHaveEnoughBalanceException(Throwable cause) { super(cause); }
		public UserDoesNotHaveEnoughBalanceException(String message, Throwable cause) { super(message, cause); }

	}

}
