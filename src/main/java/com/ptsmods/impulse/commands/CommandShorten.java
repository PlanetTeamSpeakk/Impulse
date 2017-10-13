package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.Main.LogType;

import net.dv8tion.jda.core.entities.Message;
import net.swisstech.bitly.BitlyClient;
import net.swisstech.bitly.model.Response;
import net.swisstech.bitly.model.v3.ShortenResponse;

public class CommandShorten extends Command {

	public CommandShorten() {
		name = "shorten";
		help = "Shortens a url.";
		arguments = "<url>";
		guildOnly = false;
		ownerCommand = false;
		category = Main.getCategory("General");
	}

	@Override
	protected void execute(CommandEvent event) {
		if (event.getArgs().length() != 0) {
			Message msg = event.getChannel().sendMessage("Shortening your URL, please wait...").complete();
			Response<ShortenResponse> resp = new BitlyClient("dd800abec74d5b12906b754c630cdf1451aea9e0").shorten().setLongUrl(event.getArgs()).call();
			Main.print(LogType.INFO, resp.status_txt, resp.status_code, resp == null, msg == null);
			if (resp.status_txt.equals("INVALID_URI")) msg.editMessage("The given URL was invalid, according to bit.ly.").complete();
			else msg.editMessage("Here you go: <" + resp.data.url + ">").complete();
		}
	}

}
