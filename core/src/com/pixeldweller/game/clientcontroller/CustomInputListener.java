package com.pixeldweller.game.clientcontroller;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.pixeldweller.game.network.WebSocketInterface;

public class CustomInputListener extends InputListener {

	private static WebSocketInterface network;

	private Widget component;
	private String definiton;

	public CustomInputListener(Widget component, String definiton) {
		this.component = component;
		this.definiton = definiton;
	}

	public static void setWebSocketInterface(WebSocketInterface socketInterface){
		network = socketInterface;
	}
	
	@Override
	public boolean touchDown(InputEvent event, float x, float y, int pointer,
			int button) {
		component.setColor(Color.RED);
		System.out.println("send touchDOWN: "+definiton);
		// Send to Server touch down
		network.sendMsg("+##"+definiton);		
		return true;
	}

	@Override
	public void touchUp(InputEvent event, float x, float y, int pointer,
			int button) {
		System.out.println("send touchUP: "+definiton);
		// Send to Server touch up
		network.sendMsg("-##"+definiton);		
		component.setColor(Color.WHITE);
	}
}
