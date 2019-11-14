package com.pixeldweller.game.util;

public interface ISpeaker {
	
	public void init(String voiceName);
	public void doSpeak(String speakText);
	public void terminate();
}
