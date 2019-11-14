package com.pixeldweller.game.network;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.pixeldweller.game.ColorCode;
import com.pixeldweller.game.GetTogetherGameHub;

public class ClientData {

	private boolean connected;
	private String username;
	private ColorCode colorCode;

	private float x, y;
	private float velX, velY;
	private float targetX, targetY;
	private float touchpadX, touchpadY;

	private boolean upPressed, downPressed, leftPressed, rightPressed;

	private boolean customKeyPressed;
	private String customKeyString;

	private Integer winCount;
	private Integer banterLevel;

	public ClientData(String username, int colorCodeId) {
		this.username = username;
		this.colorCode = ColorCode.getColor(colorCodeId % 8);

		x = Gdx.graphics.getWidth() / 4;
		y = Gdx.graphics.getHeight() / 4;

		Vector2 vec = new Vector2(10f, 0f);
		vec.rotate(MathUtils.random(360));

		velX = vec.x;
		velY = vec.y;

		connected = true;
		GetTogetherGameHub.speakFromDict("greet", null, this);

		banterLevel = 0;
		winCount = 0;
	}

	public void intepretMessage(String networkMsg) {
		String[] token = networkMsg.split("##");

		if (token[0].equals("-") || token[0].equals("+")) {
			boolean pressed = token[0].equals("+");
			String buttonName = token[1];

			if (buttonName.equals("up")) {
				upPressed = pressed;
			} else if (buttonName.equals("down")) {
				downPressed = pressed;
			} else if (buttonName.equals("left")) {
				leftPressed = pressed;
			} else if (buttonName.equals("right")) {
				rightPressed = pressed;
			} else {
				customKeyString = buttonName;
				customKeyPressed = pressed;
			}
		} else {				
			float deltaX = Float.parseFloat(token[0]);
			float deltaY = Float.parseFloat(token[1]);

			touchpadX = Float.parseFloat(token[0])*0.01f;
			touchpadY = Float.parseFloat(token[1])*0.01f;
			
			addVelX(deltaX*0.005f);
			addVelY(deltaY*0.005f);
		}
	}

	public void reset(float x, float y) {
		this.x = x;
		this.y = y;

		upPressed = false;
		downPressed = false;
		leftPressed = false;
		rightPressed = false;

		customKeyPressed = false;
		customKeyString = null;
	}

	public ColorCode getColorCode() {
		return colorCode;
	}

	public void setColorCode(ColorCode colorCode) {
		this.colorCode = colorCode;
	}

	public Integer getWinCount() {
		return winCount;
	}

	public void setWinCount(Integer winCount) {
		this.winCount = winCount;
	}

	public Integer getBanterLevel() {
		return banterLevel;
	}

	public void setBanterLevel(Integer banterLevel) {
		this.banterLevel = banterLevel;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public boolean isUpPressed() {
		return upPressed;
	}

	public void setUpPressed(boolean upPressed) {
		this.upPressed = upPressed;
	}

	public boolean isDownPressed() {
		return downPressed;
	}

	public void setDownPressed(boolean downPressed) {
		this.downPressed = downPressed;
	}

	public boolean isLeftPressed() {
		return leftPressed;
	}

	public void setLeftPressed(boolean leftPressed) {
		this.leftPressed = leftPressed;
	}

	public boolean isRightPressed() {
		return rightPressed;
	}

	public void setRightPressed(boolean rightPressed) {
		this.rightPressed = rightPressed;
	}

	public boolean isCustomKeyPressed() {
		return customKeyPressed;
	}

	public void setCustomKeyPressed(boolean customKeyPressed) {
		this.customKeyPressed = customKeyPressed;
	}

	public String getCustomKeyString() {
		return customKeyString;
	}

	public void setCustomKeyString(String customKeyString) {
		this.customKeyString = customKeyString;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String name) {
		username = name;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
		if (connected == false) {
			GetTogetherGameHub.speakFromDict("disconnect", null, this);
		} else {
			GetTogetherGameHub.speakFromDict("reconnect", null, this);
		}
	}

	public float getVelX() {
		return velX;
	}

	public void setVelX(float velX) {
		this.velX = velX;
	}

	public float getVelY() {
		return velY;
	}

	public void setVelY(float velY) {
		this.velY = velY;
	}

	public void addVelY(float x) {
		velY += x;
	}

	public void addVelX(float x) {
		velX += x;
	}

	public void mulVelY(float x) {
		velY *= x;
	}

	public void mulVelX(float x) {
		velX *= x;
	}
	
	public void sendMessageBack(String msg){
		// PLS OVERRIDE
	}

	public float getTouchpadX() {
		return touchpadX;
	}

	public void setTouchpadX(float touchpadX) {
		this.touchpadX = touchpadX;
	}

	public float getTouchpadY() {
		return touchpadY;
	}

	public void setTouchpadY(float touchpadY) {
		this.touchpadY = touchpadY;
	}

}