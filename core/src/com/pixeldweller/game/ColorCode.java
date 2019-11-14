package com.pixeldweller.game;

import com.badlogic.gdx.graphics.Color;

public enum ColorCode {
	RED(1, Color.RED), ORANGE(2,Color.ORANGE), YELLOW(3, Color.YELLOW), OLIVE(4,Color.OLIVE),GREEN(5, Color.GREEN), CYAN(6, Color.CYAN), DARKBLUE(7, Color.BLUE), PURPLE(8, Color.PURPLE), PINK(9, Color.PINK);
	
	private int code;
	private Color color;
	
	ColorCode(int code, Color color){
		this.code = code;
		this.color = color;		
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
	
	
	public static ColorCode getColor(int code){
		if(code == -1){
			return RED;
		}
		return values()[code];
	}
	
	
}
