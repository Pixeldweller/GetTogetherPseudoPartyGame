package com.pixeldweller.game.client;

import net.zschech.gwt.websockets.client.CloseHandler;
import net.zschech.gwt.websockets.client.ErrorHandler;
import net.zschech.gwt.websockets.client.MessageEvent;
import net.zschech.gwt.websockets.client.MessageHandler;
import net.zschech.gwt.websockets.client.OpenHandler;
import net.zschech.gwt.websockets.client.WebSocket;

import com.google.gwt.core.client.JavaScriptException;
import com.pixeldweller.game.clientcontroller.SimpleInputClient;
import com.pixeldweller.game.network.WebSocketInterface;

public class GWTClient implements WebSocketInterface {

	// private static int DEFAULT_SERVER_PORT = 80;
	private int port;
	private WebSocket wsc; // Websocket to connect to a remote server
	private boolean connected;

	// For the Client side only
	private int myID;

	// For Bidirectional Communication mode
	public GWTClient(String ip, int port) {
		this.port = port;
		this.connected = false;
		// this.connectClient(ip);
		myID = -1;

	}

	public void connectClient(String ip) {
		ip = SimpleInputClient.ip;

		if (!ip.isEmpty()) {
			// We create the URI in String format
			String url = null; // URI (url address of the server)
			url = new String("ws://" + ip + ":" + port);

			try {
				wsc = WebSocket.create(url); // "ws://echo.websocket.org" //For
												// the echo testing server

				// Handler methods override the original methods for the
				// webSocket functionality
				wsc.setOnOpen(new OpenHandler() {
					@Override
					public void onOpen(WebSocket webSocket) {
						connected = true;
						requestID();
					}
				});

				wsc.setOnMessage(new MessageHandler() {
					@Override
					public void onMessage(WebSocket webSocket,
							MessageEvent event) {
						String message = event.getData(); // Different
															// implementation
															// respect WSClient

						// Low level control of Messages received from server
						// SERVER CLOSES MY WS CONNECTION.
						if (message.equals("CLOSE_WS")) {
							wsc.close();
						}

						// SERVER SEND MY CLIENT ID.
						else if (message.startsWith("SEND_ID")) {
							String[] values = message.split("\\s+"); // splitter
																		// with
																		// the
																		// " "
																		// separator
							myID = Integer.valueOf(values[1]);
							SimpleInputClient.loggedIn();
						}
						// High level Message, send to the ClientMSG class
						else {
							serverResponse(message);
						}
					}
				});

				wsc.setOnError(new ErrorHandler() {
					@Override
					public void onError(WebSocket webSocket) {
						System.out.println("GWTClient Error.");
						if (webSocket.getReadyState() == WebSocket.CLOSED) {
							connected = false;
							onCloseEvent();
						}
					}
				});

				wsc.setOnClose(new CloseHandler() {
					@Override
					public void onClose(WebSocket webSocket) {
						connected = false;
						onCloseEvent();
					}
				});
			} catch (JavaScriptException e) {
			}
		}
	}

	private void requestID() {
		sendMsg("GET_ID###" + SimpleInputClient.username);
	}

	public boolean sendMsg(String msg) {
		if (connected) {
			if (myID != -1) {
				wsc.send(myID + "###" + msg);
			} else {
				wsc.send(msg);
			}
			return true;
		} else
			return false;
	}

	public boolean isConnected() {
		return connected;
	}

	public int getId() {
		return myID;
	}

	public void close() {
		wsc.close();
		connected = false;
		onCloseEvent();
	}

	public void onCloseEvent() {
		SimpleInputClient.disconnect();
		myID = -1;
	}

	public void serverResponse(String response) {
		SimpleInputClient.onServerResponse(response);
	}
}