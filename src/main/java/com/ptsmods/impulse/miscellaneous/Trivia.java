package com.ptsmods.impulse.miscellaneous;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.ptsmods.impulse.utils.Downloader;
import com.ptsmods.impulse.utils.Random;
import com.ptsmods.impulse.utils.Zipper;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class Trivia {

	private Trivia(String category, List<TriviaQuestion> questions) {
		this.category = category;
		this.questions = Collections.unmodifiableList(questions);
		questionsAmount = questions.size();
	}

	private final int questionsAmount;
	private final String category;
	private final List<TriviaQuestion> questions;
	private List<TriviaQuestion> passedQuestions = new ArrayList();
	private static List<Trivia> trivias = new ArrayList();

	static {
		if (!new File("data/fun/trivias/").isDirectory()) {
			new File("data/fun/trivias/").mkdirs();
			try {
				Downloader.downloadFile("https://github.com/Cog-Creators/Red-DiscordBot/archive/develop.zip", "data/tmp/red.zip");
				Zipper.unzip("data/tmp/red.zip", "data/tmp/red");
				for (File file : new File("data/tmp/red/Red-DiscordBot-develop/data/trivia/").listFiles()) {
					Files.copy(file.toPath(), Paths.get("data/fun/trivias/" + file.getPath().substring(file.getPath().indexOf("data" + File.separator + "trivia" + File.separator) + 12, file.getPath().length())));
					file.delete();
				}
				new File("data/tmp/red/").delete();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("An error occurred while extracting the default trivia questions from Red.");
			}
		}
		for (File file : new File("data/fun/trivias/").listFiles()) {
			List<TriviaQuestion> questions = new ArrayList();
			try {
				for (String line : Files.readAllLines(file.toPath()))
					questions.add(new TriviaQuestion(line.split("`")[0], Lists.newArrayList(Main.removeArg(line.split("`"), 0))));
			} catch (MalformedInputException e) {
				try {
					for (String line : Files.readAllLines(file.toPath(), Charset.forName("CP1252"))) // reading as ANSI since some trivias from Red are encoded in ANSI.
						questions.add(new TriviaQuestion(line.split("`")[0], Lists.newArrayList(Main.removeArg(line.split("`"), 0))));
				} catch (IOException e1) {
					e1.printStackTrace();
					continue;
				}
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			trivias.add(new Trivia(file.getName().split("\\.")[0], questions));
		}
	}

	public static List<String> getCategories() {
		List<String> categories = new ArrayList();
		for (Trivia trivia : trivias)
			categories.add(trivia.getCategory());
		return categories;
	}

	public static boolean isValidCategory(String category) {
		for (Trivia trivia : trivias)
			if (trivia.getCategory().equalsIgnoreCase(category)) return true;
		return false;
	}

	public static Trivia getInstance(String category) {
		for (Trivia trivia : trivias)
			if (trivia.getCategory().equalsIgnoreCase(category)) return trivia.clone();
		return null;
	}

	@Override
	protected Trivia clone() {
		return new Trivia(getCategory(), getQuestions());
	}

	public TriviaQuestion getRandomQuestion() {
		if (passedQuestions.size() == questionsAmount) return null;
		TriviaQuestion question = Random.choice(questions);
		while (passedQuestions.contains(question))
			question = Random.choice(questions);
		return question;
	}

	public boolean hasMoreQuestions() {
		return passedQuestions.size() != questionsAmount;
	}

	public TriviaResult start(MessageChannel channel, Guild guild) {
		channel.sendMessage("Trivia starting, you can always say 'stop trivia' to stop.").queue();
		Map<User, Integer> appendees = new HashMap();
		int counter = 0;
		while (hasMoreQuestions()) {
			TriviaQuestion question = getRandomQuestion();
			if (question == null) return new TriviaResult(appendees, false); // just in case.
			counter += 1;
			channel.sendMessageFormat("**Question %s of %s**\n%s", counter, getQuestions().size(), question.getQuestion()).complete();
			int wrongAnswers = 0;
			Message response = Main.waitForInput(channel, 10000);
			if (response == null) return new TriviaResult(appendees, true);
			else if (response.getContent().equalsIgnoreCase("stop trivia")) return new TriviaResult(appendees, false);
			while (!isCorrect(question, response.getContent())) {
				wrongAnswers += 1;
				response = Main.waitForInput(channel, 10000);
				if (response == null || wrongAnswers == 5) {
					channel.sendMessageFormat("It was **%s**, of course.", question.getAnswers().get(0)).queue();
					break;
				}
				else if (response.getContent().equalsIgnoreCase("stop trivia")) return new TriviaResult(appendees, false);
			}
			if (response != null) {
				User appendee = response.getAuthor();
				appendees.put(appendee, appendees.getOrDefault(appendee, 1));
				channel.sendMessageFormat("You got it, %s! **+1** to you. (%s total points)", guild == null ? appendee.getName() : guild.getMember(appendee).getEffectiveName(), appendees.get(appendee)).queue();
			}
		}
		return new TriviaResult(appendees, false);
	}

	public String getCategory() {
		return category;
	}

	public List<TriviaQuestion> getQuestions() {
		return questions;
	}

	public boolean isCorrect(TriviaQuestion question, String answer) {
		for (String answer1 : question.getAnswers())
			if (answer1.equalsIgnoreCase(answer)) return true;
		return false;
	}

	public static class TriviaQuestion {

		private TriviaQuestion(String question, List<String> answers) {
			this.question = question;
			this.answers = Collections.unmodifiableList(answers);
		}

		private final String question;
		private final List<String> answers;

		public String getQuestion() {
			return question;
		}

		public List<String> getAnswers() {
			return answers;
		}

	}

	public static class TriviaResult {

		private final Map<User, Integer> appendees;
		private final boolean stoppedCuzNoResponse;

		private TriviaResult(Map<User, Integer> appendees, boolean stoppedCuzNoResponse) {
			this.appendees = Main.sortByValue(appendees);
			this.stoppedCuzNoResponse = stoppedCuzNoResponse;
		}

		@Override
		public String toString() {
			String output = (stoppedCuzNoResponse ? "No response gotten, guess I'll stop.\n" : "") + "**Trivia results**\n";
			int counter = 1;
			for (User user : appendees.keySet()) {
				output += String.format("**%s.** %s - %s\n", counter, Main.str(user), appendees.get(user));
				counter += 1;
			}
			return output;
		}

	}

}
