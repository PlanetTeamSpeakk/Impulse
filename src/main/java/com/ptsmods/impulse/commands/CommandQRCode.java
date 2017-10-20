package com.ptsmods.impulse.commands;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.utils.Downloader;
import com.ptsmods.impulse.utils.Random;

import net.dv8tion.jda.core.MessageBuilder;

public class CommandQRCode extends Command {

	public CommandQRCode() {
		setName("qrcode")
		.setHelp("Creates a QR code from text.")
		.setArguments("<text>")
		.setGuildOnly(false)
		.setOwnerCommand(false)
		.setCategory(Main.getCategory("General"));
	}

	@Override
	protected void execute(CommandEvent event) {
		if (!event.getArgs().isEmpty()) {
			Map<String, String> data;
			try {
				int rng = Random.randInt(10000);
				data = Downloader.downloadFile("https://api.qrserver.com/v1/create-qr-code/?size=1000x1000&data=" + Main.percentEncode(event.getArgs()), "data/general/" + rng + ".png");
				event.getChannel().sendFile(new File(data.get("fileLocation")), new MessageBuilder().append("Here you go:").build()).complete();
				new File(data.get("fileLocation")).delete();
			} catch (IOException e) {
				event.reply("An unknown error occured while creating the QR code, please try again.");
				e.printStackTrace();
			}
		} else Main.sendCommandHelp(event, this);
	}

}
