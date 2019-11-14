package com.pixeldweller.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.pixeldweller.game.clientcontroller.SimpleInputClient;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		LwjglApplicationConfiguration configController = new LwjglApplicationConfiguration();
		
		config.width = 1680;
		config.height = 1050;
		configController.width = 400;
		configController.height = 500;
		
		//new LwjglApplication(new GetTogetherGame(), config);
		//new LwjglApplication(new Main(), config);
		//new LwjglApplication(new Physics4(), config);
		new LwjglApplication(new SimpleInputClient(new DesktopSocketInterface("localhost", 9090)), configController);
	}
}
