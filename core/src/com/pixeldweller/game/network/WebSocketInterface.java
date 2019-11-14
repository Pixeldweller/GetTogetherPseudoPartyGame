package com.pixeldweller.game.network;


public interface WebSocketInterface {
	
	public void connectClient (String ip);
	
	public boolean sendMsg(String msg);

	public boolean isConnected();
	
	public int getId();
	
	public void close();
	
	public void onCloseEvent();
	
	public void serverResponse(String response);
}
