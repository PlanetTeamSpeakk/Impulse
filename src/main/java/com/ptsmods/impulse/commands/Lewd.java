package com.ptsmods.impulse.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.ptsmods.impulse.miscellaneous.Command;
import com.ptsmods.impulse.miscellaneous.CommandEvent;
import com.ptsmods.impulse.miscellaneous.CommandException;
import com.ptsmods.impulse.utils.Random;

public class Lewd {

	public static final int oboobsLewds;
	public static final int obuttsLewds;

	static {
		try {
			oboobsLewds = Main.getIntFromPossibleDouble(((Map) new Gson().fromJson(Main.getHTML("http://api.oboobs.ru/boobs/count"), List.class).get(0)).get("count"))-1;
			obuttsLewds = Main.getIntFromPossibleDouble(((Map) new Gson().fromJson(Main.getHTML("http://api.obutts.ru/butts/count"), List.class).get(0)).get("count"))-1;
		} catch (JsonSyntaxException | IOException e) {
			throw new RuntimeException("An unknown error occurred while getting the amount of lewds available on obutts and oboobs.", e);
		}
	}

	@Command(category = "Lewd", help = "Shows you a random image from Yandere.", name = "yandere")
	public static void yandere(CommandEvent event) throws CommandException {
		if (event.getTextChannel().isNSFW()) {
			Document yandere;
			try {
				yandere = Jsoup.connect("https://yande.re/post/random").get();
			} catch (IOException e) {
				throw new CommandException("An unknown error occurred while getting your lewd pics.", e);
			}
			event.reply(yandere.getElementById("highres").attr("href"));
		} else event.reply("You can only see lewd pics if you turn on NSFW for this channel in its settings.");
	}

	@Command(category = "Lewd", help = "Shows you a random image from Konachan.", name = "konachan")
	public static void konachan(CommandEvent event) throws CommandException {
		if (event.getTextChannel().isNSFW()) {
			Document konachan;
			try {
				konachan = Jsoup.connect("https://konachan.com/post/random").get();
			} catch (IOException e) {
				throw new CommandException("An unknown error occurred while getting your lewd pics.", e);
			}
			event.reply("https:" + konachan.getElementById("highres").attr("href"));
		} else event.reply("You can only see lewd pics if you turn on NSFW for this channel in its settings.");
	}

	@Command(category = "Lewd", help = "Shows you a random image from e621.", name = "e621")
	public static void e621(CommandEvent event) throws CommandException {
		if (event.getTextChannel().isNSFW()) {
			Document e621;
			try {
				e621 = Jsoup.connect("https://e621.net/post/random").get();
			} catch (IOException e) {
				throw new CommandException("An unknown error occurred while getting your lewd pics.", e);
			}
			event.reply(e621.getElementById("highres").attr("href"));
		} else event.reply("You can only see lewd pics if you turn on NSFW for this channel in its settings.");
	}

	@Command(category = "Lewd", help = "Shows you a random image or one matching your tags from Rule34.", name = "rule34", arguments = "[tags]")
	public static void rule34(CommandEvent event) throws CommandException {
		if (event.getTextChannel().isNSFW()) {
			List<String> urls = new ArrayList();
			if (!event.argsEmpty()) {
				String query = "http://rule34.xxx/index.php?page=post&s=list&tags=" + event.getArgs().replaceAll(" ", "+");
				Document rule34 = null;
				try {
					rule34 = Jsoup.connect(query).get();
				} catch (IOException e) {
					event.reply("An unknown error occurred, I'm sorry, no lewds for you.");
					return;
				}
				Elements elements = rule34.getElementsByTag("a");
				for (int i : Main.range(elements.size()))
					try {
						if (elements.get(i).id().startsWith("p")) urls.add(elements.get(i).attr("href"));
					} catch (Throwable e) {
						continue;
					}
			} else urls.add("index.php?page=post&s=random");
			if (urls.isEmpty()) event.reply("No results found.");
			else {
				Document rule34 = null;
				try {
					rule34 = Jsoup.connect("http://rule34.xxx/" + Random.choice(urls)).get();
				} catch (IOException e) {
					event.reply("An unknown error occurred while getting your lewds.");
					return;
				}
				event.reply("http:" + rule34.getElementById("image").attr("src"));
			}
		} else event.reply("You can only see lewd pics if you turn on NSFW for this channel in its settings.");
	}

	@Command(category = "Lewd", help = "Shows you a random image from Danbooru.", name = "danbooru")
	public static void danbooru(CommandEvent event) throws CommandException {
		if (event.getTextChannel().isNSFW()) {
			Document danbooru;
			try {
				danbooru = Jsoup.connect("http://danbooru.donmai.us/posts/random").get();
			} catch (IOException e) {
				throw new CommandException("An unknown error occurred while getting your lewd pics.", e);
			}
			event.reply("http://danbooru.donmai.us" + danbooru.getElementById("image").attr("src"));
		} else event.reply("You can only see lewd pics if you turn on NSFW for this channel in its settings.");
	}

	@Command(category = "Lewd", help = "Shows you a random image from Gelbooru.", name = "gelbooru")
	public static void gelbooru(CommandEvent event) throws CommandException {
		if (event.getTextChannel().isNSFW()) {
			Document gelbooru;
			try {
				gelbooru = Jsoup.connect("http://www.gelbooru.com/index.php?page=post&s=random").get();
			} catch (IOException e) {
				throw new CommandException("An unknown error occurred while getting your lewd pics.", e);
			}
			event.reply(gelbooru.getElementById("image").attr("src"));
		} else event.reply("You can only see lewd pics if you turn on NSFW for this channel in its settings.");
	}

	@Command(category = "Lewd", help = "Shows you a random image from TBib.", name = "tbib")
	public static void tbib(CommandEvent event) throws CommandException {
		if (event.getTextChannel().isNSFW()) {
			Document tbib;
			try {
				tbib = Jsoup.connect("http://www.tbib.org/index.php?page=post&s=random").get();
			} catch (IOException e) {
				throw new CommandException("An unknown error occurred while getting your lewd pics.", e);
			}
			event.reply("http:" + tbib.getElementById("image").attr("src"));
		} else event.reply("You can only see lewd pics if you turn on NSFW for this channel in its settings.");
	}

	@Command(category = "Lewd", help = "Shows you a random image from Xbooru.", name = "xbooru")
	public static void xbooru(CommandEvent event) throws CommandException {
		if (event.getTextChannel().isNSFW()) {
			Document xbooru;
			try {
				xbooru = Jsoup.connect("http://xbooru.com/index.php?page=post&s=random").get();
			} catch (IOException e) {
				throw new CommandException("An unknown error occurred while getting your lewd pics.", e);
			}
			event.reply(xbooru.getElementById("image").attr("src"));
		} else event.reply("You can only see lewd pics if you turn on NSFW for this channel in its settings.");
	}

	@Command(category = "Lewd", help = "Shows you a random image from Furrybooru.", name = "furrybooru")
	public static void furrybooru(CommandEvent event) throws CommandException {
		if (event.getTextChannel().isNSFW()) {
			Document furrybooru;
			try {
				furrybooru = Jsoup.connect("http://furry.booru.org/index.php?page=post&s=random").get();
			} catch (IOException e) {
				throw new CommandException("An unknown error occurred while getting your lewd pics.", e);
			}
			event.reply(furrybooru.getElementById("image").attr("src"));
		} else event.reply("You can only see lewd pics if you turn on NSFW for this channel in its settings.");
	}

	@Command(category = "Lewd", help = "Shows you a random image from Drunkenpumken.", name = "drunkenpumken")
	public static void drunkenpumken(CommandEvent event) throws CommandException {
		if (event.getTextChannel().isNSFW()) {
			Document drunkenpumken;
			try {
				drunkenpumken = Jsoup.connect("http://drunkenpumken.booru.org/index.php?page=post&s=random").get();
			} catch (IOException e) {
				throw new CommandException("An unknown error occurred while getting your lewd pics.", e);
			}
			event.reply(drunkenpumken.getElementById("image").attr("src"));
		} else event.reply("You can only see lewd pics if you turn on NSFW for this channel in its settings.");
	}

	@Command(category = "Lewd", help = "Shows you a random image from Lolibooru.", name = "lolibooru")
	public static void lolibooru(CommandEvent event) throws CommandException {
		if (event.getTextChannel().isNSFW()) {
			Document lolibooru;
			try {
				lolibooru = Jsoup.connect("https://lolibooru.moe/post/random/").get();
			} catch (IOException e) {
				throw new CommandException("An unknown error occurred while getting your lewd pics.", e);
			}
			event.reply(lolibooru.getElementById("image").attr("src").replaceAll(" ", "%20"));
		} else event.reply("You can only see lewd pics if you turn on NSFW for this channel in its settings.");
	}

	@Command(category = "Lewd", help = "Shows you a random image from Oboobs.", name = "boobs")
	public static void boobs(CommandEvent event) throws CommandException {
		if (event.getTextChannel().isNSFW()) {
			Map boobs;
			try {
				boobs = (Map) new Gson().fromJson(Main.getHTML("http://api.oboobs.ru/boobs/" + Random.randInt(oboobsLewds)), List.class).get(0);
			} catch (IOException e) {
				throw new CommandException("An unknown error occurred while getting your lewd pics.", e);
			}
			event.reply("**Model**: %s\n**Author**: %s\n**ID**: %s\n**Rank**: %s\nhttp://media.oboobs.ru/" + boobs.get("preview"),
					boobs.get("model") == null || boobs.get("model").toString().isEmpty() ? "Unknown" : boobs.get("model"),
							boobs.get("author") == null || boobs.get("author").toString().isEmpty() ? "Unknown" : boobs.get("author"),
									Main.getIntFromPossibleDouble(boobs.get("id")),
									Main.getIntFromPossibleDouble(boobs.get("rank")));
		} else event.reply("You can only see lewd pics if you turn on NSFW for this channel in its settings.");
	}

	@Command(category = "Lewd", help = "Shows you a random image from Obutts.", name = "ass")
	public static void ass(CommandEvent event) throws CommandException {
		if (event.getTextChannel().isNSFW()) {
			Map ass;
			try {
				ass = (Map) new Gson().fromJson(Main.getHTML("http://api.obutts.ru/butts/" + Random.randInt(obuttsLewds)), List.class).get(0);
			} catch (IOException e) {
				throw new CommandException("An unknown error occurred while getting your lewd pics.", e);
			}
			event.reply("**Model**: %s\n**Author**: %s\n**ID**: %s\n**Rank**: %s\nhttp://media.obutts.ru/" + ass.get("preview"),
					ass.get("model") == null || ass.get("model").toString().isEmpty() ? "Unknown" : ass.get("model"),
							ass.get("author") == null || ass.get("author").toString().isEmpty() ? "Unknown" : ass.get("author"),
									Main.getIntFromPossibleDouble(ass.get("id")),
									Main.getIntFromPossibleDouble(ass.get("rank")));
		} else event.reply("You can only see lewd pics if you turn on NSFW for this channel in its settings.");
	}

}
