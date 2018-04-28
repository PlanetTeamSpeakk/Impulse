package com.ptsmods.impulse.utils;

import java.util.HashMap;
import java.util.Map;

import com.ptsmods.impulse.miscellaneous.CommandException;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

public class Cleverbot {

	private static final AIConfiguration	configuration	= new AIConfiguration("709b133deafa4a9d9a1c7ce2a25651fe");
	private static final AIDataService		ai				= new AIDataService(configuration);
	private static Map<String, Cleverbot>	bots			= new HashMap();
	private final String					key;

	private Cleverbot(String key) {
		this.key = key;
	}

	public static Cleverbot newBot() {
		return getBot(Random.INSTANCE.genKey(128));
	}

	public static Cleverbot getBot(String key) {
		Cleverbot bot = bots.getOrDefault(key, new Cleverbot(key));
		bots.put(key, bot);
		return bot;
	}

	public String askQuestion(String question) throws CommandException {
		AIResponse response;
		try {
			response = ai.request(new AIRequest(question));
		} catch (AIServiceException e) {
			throw new CommandException(e);
		}
		String resp = response.getResult().getFulfillment().getSpeech().replaceAll("\\\\n ", "\n").replaceAll("\\\\n", "\n");
		// if (resp.isEmpty()) {
		// JSONObject body = new JSONObject()
		// .put("user", "Ozy7HCw70XV8Oq8s")
		// .put("key", "7u1jhrKerMIucToGfEgrN7ixMt61hWC6");
		// Request.Builder builder = new
		// Request.Builder().post(RequestBody.create(Requester.MEDIA_TYPE_JSON,
		// body.toString()));
		// final Map data = Main.newHashMap(new String[] {"success", "exception"}, new
		// Object[] {true, null});
		// ((JDAImpl)
		// Main.getShards().get(0)).getHttpClientBuilder().build().newCall(builder.build()).enqueue(new
		// Callback() {
		//
		// @Override
		// public void onFailure(Call arg0, IOException arg1) {
		// data.put("success", false);
		// data.put("exception", arg1);
		// }
		//
		// @Override
		// public void onResponse(Call arg0, Response arg1) throws IOException {}
		//
		// });
		// final Map data1 = Main.newHashMap(new String[] {"success", "exception"}, new
		// Object[] {true, null});
		// ((JDAImpl)
		// Main.getShards().get(0)).getHttpClientBuilder().build().newCall(builder.build()).enqueue(new
		// Callback() {
		//
		// @Override
		// public void onFailure(Call arg0, IOException arg1) {
		// data1.put("success", false);
		// data1.put("exception", arg1);
		// }
		//
		// @Override
		// public void onResponse(Call arg0, Response arg1) throws IOException {}
		//
		// });
		// } Maybe someday, I am too lazy now.
		return resp;
	}

	public String getKey() {
		return key;
	}

}
