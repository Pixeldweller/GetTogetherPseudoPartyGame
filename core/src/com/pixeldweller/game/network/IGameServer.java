package com.pixeldweller.game.network;

import java.util.List;


public interface IGameServer {

	public abstract void startServer();

	public abstract boolean isListening();

	public abstract void sendToAll(String text);

	public abstract boolean sendToClient(int ID, String text);

	public abstract int getClientCount();

	public abstract void dropAllClients();

	public abstract void stop();

	public abstract String getIP();
	
	public abstract List<ClientData> getClientsData();

}