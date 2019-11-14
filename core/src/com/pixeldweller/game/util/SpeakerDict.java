package com.pixeldweller.game.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.pixeldweller.game.GetTogetherGameHub;
import com.pixeldweller.game.network.ClientData;

public class SpeakerDict {

	private static HashMap<String, List<String>> actualDict;
	private static HashMap<String, List<String>> gameDescDict;

	private static int silly_counter = 0;

	public SpeakerDict() {
		loadScript();
	}

	public void debug() {
		FileHandle handle = Gdx.files.internal("testScript.txt");
		String text = handle.readString();
		String wordsArray[] = text.split("\\r?\\n");
		for (String word : wordsArray) {
			if (word.equals("")) {
				continue;
			}
			String[] tokens = word.split("#");
			GetTogetherGameHub.speak(tokens[1].replace("@player",
					"The Legend 27")); // DEBUG SCRIPT
		}
	}

	private void loadScript() {
		FileHandle handle = Gdx.files.internal("speechPrototypeScript.txt");
		String text = handle.readString();
		String wordsArray[] = text.split("\\r?\\n");
		actualDict = new HashMap<String, List<String>>();
		for (String word : wordsArray) {
			if (word.equals("")) {
				continue;
			}
			String[] tokens = word.split("#");
			//
			List<String> keywordList = actualDict.get(tokens[0]);

			if (keywordList == null) {
				keywordList = new ArrayList<String>();
				if (tokens.length == 1) {
					System.out.println(word);
				}
				keywordList.add(tokens[1]);
				actualDict.put(tokens[0], keywordList);
			} else {
				keywordList.add(tokens[1]);
			}
			// debug(); // DEBUG SCRIPT
		}

		for (List<String> compositeList : actualDict.values()) {
			Collections.shuffle(compositeList);
		}

		handle = Gdx.files.internal("gameDesc.txt");
		text = handle.readString();
		wordsArray = text.split("\\r?\\n");
		gameDescDict = new HashMap<String, List<String>>();
		for (String word : wordsArray) {
			if (word.equals("")) {
				continue;
			}
			String[] tokens = word.split("#");
			//
			List<String> keywordList = gameDescDict.get(tokens[0]);

			if (keywordList == null) {
				keywordList = new ArrayList<String>();
				if (tokens.length == 1) {
					System.out.println(word);
				}

				for (String substring : tokens[1].split(";")) {
					keywordList.add(substring);
				}
				gameDescDict.put(tokens[0], keywordList);
			} else {
				for (String substring : tokens[1].split(";")) {
					keywordList.add(substring);
				}
			}
			// debug(); // DEBUG SCRIPT
		}

		for (List<String> compositeList : actualDict.values()) {
			Collections.shuffle(compositeList);
		}
	}

	public static List<String> retrieveMessageForGameDesc(String key) {
		return gameDescDict.get(key);
	}

	public static synchronized String retrieveMessageForClient(String key,
			List<ClientData> clients, ClientData receiver) {

		List<String> keywordList = actualDict.get(key);
		String msg = keywordList.get(silly_counter % keywordList.size());

		try {

			if (key.equals("banter")) {
				sortClientsByBanterScore(clients);
				receiver = clients.get(0);
			}

			if (msg.contains("@player0")) {
				sortClientsByWins(clients);
				msg = msg.replaceAll("@player0", clients.get(0).getUsername()
						.toString());
				msg = msg.replaceAll("@player1", clients.get(1).getUsername()
						.toString());

				int winDiff = clients.get(0).getWinCount()
						- clients.get(1).getWinCount();
				msg = msg.replaceAll("@winDiff", winDiff + "");
			}

			if (msg.contains("@playerX")) {
				sortClientsByWins(clients);
				msg = msg.replaceAll("@playerX", clients
						.get(clients.size() - 1).getUsername());
			}

			if (msg.contains("@player")) {
				msg = msg.replaceAll("@player", receiver.getUsername());
			}

			if (msg.contains("@winCount")) {
				msg = msg.replaceAll("@winCount", receiver.getWinCount()
						.toString());
			}

			if (msg.contains("rename:")) {
				String[] exec = msg.split("rename:");
				msg = exec[0];
				receiver.setUsername(exec[1]);
			}
		} catch (Exception e) {

		}

		silly_counter += 1;
		return msg;
	}

	private static void sortClientsByWins(List<ClientData> clients) {
		clients.sort(new Comparator<ClientData>() {

			@Override
			public int compare(ClientData c0, ClientData c1) {
				return c0.getWinCount().compareTo(c1.getWinCount());
			}
		});
	}

	private static void sortClientsByBanterScore(List<ClientData> clients) {
		clients.sort(new Comparator<ClientData>() {

			@Override
			public int compare(ClientData c0, ClientData c1) {
				return c0.getBanterLevel().compareTo(c1.getBanterLevel());
			}
		});
	}

}
