package com.pixeldweller.game.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.google.gwt.core.client.GWT;
import com.pixeldweller.game.GetTogetherGameHub;
import com.pixeldweller.game.clientcontroller.IKeyboardInvoker;
import com.pixeldweller.game.clientcontroller.SimpleInputClient;

public class HtmlLauncher extends GwtApplication {

	@Override
	public void log(String tag, String message) {
		if (getLogLevel() >= LOG_INFO) {
			consoleLog(tag + ": " + message);
		}
	}

	@Override
	public void log(String tag, String message, Throwable exception) {
		if (getLogLevel() >= LOG_INFO) {
			consoleLog(tag + ": " + message + "\n" + exception.getMessage());
		}
	}

	@Override
	public GwtApplicationConfiguration getConfig() {
		return new GwtApplicationConfiguration(400, 500);
	}

	@Override
	public ApplicationListener createApplicationListener() {

		SimpleInputClient.keyboardInvoker = new JavaScriptKeyboardWrapper();
		SimpleInputClient simpleInputClient = new SimpleInputClient(
				new GWTClient("127.0.0.1", 9090));
		return simpleInputClient;
	}

	
}