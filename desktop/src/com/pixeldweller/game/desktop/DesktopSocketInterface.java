package com.pixeldweller.game.desktop;

import java.net.URI;
import java.net.URISyntaxException;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import com.pixeldweller.game.clientcontroller.SimpleInputClient;
import com.pixeldweller.game.network.WebSocketInterface;
//This is the Standard WebSocket Implementation

public class DesktopSocketInterface implements WebSocketInterface
{	
	private int port;
	private WebSocketClient wsc; //Websocket
	private boolean connected;
	
	//For the Client side only
	private int myID;
	
	
	//For Bidirectional Communication mode
	public DesktopSocketInterface (String ip, int port)
	{
		this.port = port;
		connected = false;
		//this.connectClient(ip);
		myID = -1;		
	}
	
	public void connectClient (String ip)
	{
		ip = SimpleInputClient.ip;
		
		if (!ip.isEmpty())
		{
			//Websocket implementation
			URI url = null; //URI (url address of the server)
			try {
				url = new URI("wss://"+ ip +":"+ port); //We create the URI of the server. Use a port upper than 1024 on Android and Linux!
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} 

			//We select the standard implementation of WebSocket
			Draft standard = new Draft_17(); 

			wsc = new WebSocketClient( url, standard) {

				@Override
				public void onOpen( ServerHandshake handshake ) {
					connected = true;
					requestID();
				}
				
				@Override
				public void onMessage( String message ) {
					
					//Low level control of Messages received from server
					//SERVER CLOSES MY WS CONNECTION.
					if (message.equals("CLOSE_WS")) 
					{
						this.close(); 
					}

					//SERVER SEND MY CLIENT ID.
					else if (message.startsWith("SEND_ID"))
					{
						String [] values = message.split("\\s+"); //splitter with the " " separator
						myID = Integer.valueOf(values[1]);	
						SimpleInputClient.loggedIn();
					}
					//High level Message, send to the ClientMSG class
					else
					{
						serverResponse(message);
					}	
				}

				@Override
				public void onError( Exception ex ) {
					System.out.println("WSClient Error.");
					if(this.getReadyState() == WebSocket.READY_STATE_CLOSED){
						connected = false;
						onCloseEvent();
					}
				}
				
				@Override
				public void onClose( int code, String reason, boolean remote ) {
					connected = false;
					System.out.println("Disconnected: "+reason);
					onCloseEvent();
				}
			};
			wsc.connect(); //And we create the connection between client and server
		}
	}

	private void requestID()
	{
		sendMsg("GET_ID###"+SimpleInputClient.username);
	}
	
	public boolean sendMsg(String msg)
	{
		if (connected)
		{
			if(myID != -1){
				wsc.send(myID+"###"+msg);
			} else {
				wsc.send(msg);
			}
			return true;
		}
		else return false;
	}

	public boolean isConnected()
	{
		return connected;
	}

	public int getId()
	{
		return myID;
	}
	
	public void close()
	{
		wsc.close();
		connected = false;
		onCloseEvent();
	}

	@Override
	public void onCloseEvent() {
		SimpleInputClient.disconnect();
		myID = -1;
	}

	/**
	 * OVERRIDE
	 */
	@Override
	public void serverResponse(String response) {
		SimpleInputClient.onServerResponse(response);
	}
	

}
