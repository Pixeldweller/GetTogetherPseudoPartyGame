package com.pixeldweller.game.desktop;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.pixeldweller.game.network.ClientData;
import com.pixeldweller.game.network.IGameServer;

public class GameWebSocketServer implements IGameServer {
		
	private int port;
	private WebSocketServer webSocket;
	private boolean isReady;
	
	private List<Connection> clientSockets;	
	private List<Connection> inactiveClientSockets;
	private List<ClientData> clientData;
	
	private int clientCount; // Number of clients connected to our server
	private int newClientId; // To create a unique Id for each client (never
							// decreased)

	// For Bidirectional Communication mode
	public GameWebSocketServer(int port) {
		this.port = port;
		isReady = false;
		clientCount = 0;
		newClientId = 0;
		clientSockets = new ArrayList<Connection>();
		inactiveClientSockets = new ArrayList<Connection>();
		clientData = new ArrayList<ClientData>();
		
		System.out.println("Server IP: " + this.getIP());

	}

	/* (non-Javadoc)
	 * @see com.pixeldweller.game.desktop.IGameServer#startServer()
	 */
	@Override
	public void startServer() {
		// Here we must create the server and all the behavior for the messages
		// received by the clients
		webSocket = new WebSocketServer(new InetSocketAddress(port)) {

			@Override
			public void onOpen(WebSocket arg0, ClientHandshake arg1) {
				System.out.println("Handshake :" + arg1.toString());
			}

			@Override
			public void onMessage(WebSocket arg0, String message) {

				System.out.println("Server receives:  " + message + " "
						+ arg0.getRemoteSocketAddress());

				if (message.startsWith("GET_ID")) {
					// SERVER SEND THE CLIENT ID AND REGISTER A NEW CONNECTION
					acceptNewClient(arg0, message);
				} else {
					String[] token = message.split("###");
					for (Connection c : clientSockets) {						
						if ((c.getID()+"").equals(token[0])) {
							c.getClientData().intepretMessage(token[1]);
						}
					}
				}				
			}

			private void acceptNewClient(final WebSocket socket, String message) {
				String[] split = message.split("###");
				Connection establishedConnection = null;
				
				for(Connection inactive : inactiveClientSockets){
					if(inactive.getClientData().getUsername().equalsIgnoreCase(split[1])){
						establishedConnection = inactive;
					}
				}
				
				if(establishedConnection == null){
					ClientData newClientData = new ClientData(split[1],newClientId){
						@Override
						public void sendMessageBack(String msg) {
							socket.send(msg);
							System.out.println("Server send to: " + newClientId + " " + msg);
						}
					};
					clientData.add(newClientData);
					establishedConnection = new Connection(socket, newClientId, newClientData);
					clientSockets.add(establishedConnection);
					newClientId++;
				} else {					
					inactiveClientSockets.remove(establishedConnection);
					establishedConnection.getClientData().setConnected(true);
					clientSockets.add(establishedConnection);
				}

				socket.send("SEND_ID " + establishedConnection.getID());
				System.out.println("Server sent SEND_ID " + establishedConnection.getID());				
				clientCount++;
			}

			@Override
			public void onError(WebSocket arg0, Exception arg1) {
				// TODO Auto-generated method stub
				System.out.println("Server Error " + arg1);
			}

			@Override
			public void onClose(WebSocket arg0, int arg1, String arg2,
					boolean arg3) {

				closeConnection(arg0);
			}
		};

		webSocket.start(); // Start Server functionality
		isReady = true;
		System.out.println("Server started and ready.");
	}

	/* (non-Javadoc)
	 * @see com.pixeldweller.game.desktop.IGameServer#isListening()
	 */
	@Override
	public boolean isListening() {
		return isReady;
	}

	/* (non-Javadoc)
	 * @see com.pixeldweller.game.desktop.IGameServer#sendToAll(java.lang.String)
	 */
	@Override
	public void sendToAll(String text) {
		synchronized (clientSockets) {
			for (Connection c : clientSockets) {
				if (c.getWS().isOpen())
					c.getWS().send(text);
				System.out.println("Server send to all:" + c.getWS().isOpen()
						+ "  " + text);
				// Only we must send the message if the WS is Open.
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.pixeldweller.game.desktop.IGameServer#sendToClient(int, java.lang.String)
	 */
	@Override
	public boolean sendToClient(int ID, String text) // not tested already
	{
		for (Connection c : clientSockets) {
			if (c.getID() == ID) {
				c.getWS().send(text);
				System.out.println("Server send to: " + ID + " " + text);
				return true;
			}
		}
		return false;
	}

	private boolean closeConnection(WebSocket ws) {
		int i = 0, clientToDelete = 0, clientID = 0;
		boolean found = false;

		if (clientCount == 0)
			return found; // 0 Clients

		synchronized (clientSockets) {
			for (Connection c : clientSockets) {
				if (!found && (c.getWS().hashCode() == ws.hashCode())) // There
																		// are
																		// the
																		// same
																		// WebSocket
				{
					clientToDelete = i; // We can't delete the connection here,
										// ConcurrentException!
					clientID = c.getID();
					found = true;
				}
				i++;
			}
		}

		if (found) {
			Connection inactive = clientSockets.remove(clientToDelete);
			inactive.getClientData().setConnected(false);
			inactiveClientSockets.add(inactive);
			clientCount--;
			System.out.println("Client " + clientID + " disconnected. "
					+ clientCount + " clients connected.");

		}
		return found;
	}

	/* (non-Javadoc)
	 * @see com.pixeldweller.game.desktop.IGameServer#nClients()
	 */
	@Override
	public int getClientCount() {
		return clientCount;
	}

	/* (non-Javadoc)
	 * @see com.pixeldweller.game.desktop.IGameServer#dropAllClients()
	 */
	@Override
	public void dropAllClients() {
		this.sendToAll("CLOSE_WS");
	}

	/* (non-Javadoc)
	 * @see com.pixeldweller.game.desktop.IGameServer#stop()
	 */
	@Override
	public void stop() {
		dropAllClients();

		try {
			webSocket.stop();
			System.out.println("Server Stopped.");
		} catch (IOException e) {			
			e.printStackTrace();
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}

		isReady = false;
	}

	/* (non-Javadoc)
	 * @see com.pixeldweller.game.desktop.IGameServer#getIP()
	 */
	@Override
	public String getIP() {
		InetAddress thisIp;
		try {
			thisIp = InetAddress.getLocalHost();

			return (thisIp.getHostAddress().toString());
		} catch (UnknownHostException e) {		
			e.printStackTrace();
		}
		return ("127.0.0.1");
	}

	@Override
	public synchronized List<ClientData> getClientsData() {	
		return clientData;
	}
}

class Connection {
	private WebSocket ws;
	private int clientID;
	private ClientData clientData;	

	public Connection(WebSocket ws, int ID, ClientData clientData) {
		this.ws = ws;
		this.clientID = ID;
		this.clientData = clientData;		
	}	

	public ClientData getClientData() {
		return clientData;
	}

	public int getID() {
		return clientID;
	}
	
	public WebSocket getWS() {
		return ws;
	}		
}