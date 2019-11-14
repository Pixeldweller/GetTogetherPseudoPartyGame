package com.pixeldweller.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.pixeldweller.game.GetTogetherGameHub;

public class DesktopServerLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();	
		config.width = 1920;
		config.height = 1080;//-400;		
		
		
		new LwjglApplication(new GetTogetherGameHub(new GameWebSocketServer(9090), new SpeechUtils()),config);
	}
}
