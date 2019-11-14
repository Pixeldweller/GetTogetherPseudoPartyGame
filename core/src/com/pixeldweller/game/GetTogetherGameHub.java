package com.pixeldweller.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.pixeldweller.game.clientcontroller.ControllerType;
import com.pixeldweller.game.minigames.AvoidTheTrain;
import com.pixeldweller.game.minigames.CurveFeverPrototype;
import com.pixeldweller.game.minigames.Killr;
import com.pixeldweller.game.minigames.MiniGame;
import com.pixeldweller.game.minigames.Pong;
import com.pixeldweller.game.network.ClientData;
import com.pixeldweller.game.network.IGameServer;
import com.pixeldweller.game.util.ISpeaker;
import com.pixeldweller.game.util.SpeakerDict;

public class GetTogetherGameHub extends Game {

	public static IGameServer gameServer;
	private static ISpeaker speaker;
	private static SpeakerDict dict;
	private static String dialog;
	private static List<String> dialogQueue;

	private Exception lastException;

	public static SpriteBatch batch;
	public static ShapeRenderer shapeRenderer;
	private Texture background, label;
	public static Texture playerCursor;
	public static Skin skin;
	private OrthographicCamera cam;
	private static Stage stage;

	private float rotation = 1f;
	private List<MiniGame> miniGames;
	public static boolean RESET;

	private static float transitionValue = 0.01f;
	private static Screen nextScreen = null;

	public GetTogetherGameHub(IGameServer gameServer, ISpeaker speaker) {
		GetTogetherGameHub.gameServer = gameServer;
		GetTogetherGameHub.speaker = speaker;
		dialogQueue = new ArrayList<String>();
	}

	@Override
	public void create() {
		cam = new OrthographicCamera(1680 / 2, 1050 / 2);
		skin = new Skin(Gdx.files.internal("comicui/comic-ui.json"));
		gameServer.startServer();
		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
		background = new Texture("vortex.png");
		label = new Texture("label.png");
		playerCursor = new Texture("cursor.png");
		stage = new Stage();
		Gdx.input.setInputProcessor(new InputMultiplexer(stage));

		speaker.init("kevin16");

		miniGames = new ArrayList<MiniGame>();
		miniGames.add(new Pong());
		//miniGames.add(new CurveFeverPrototype());
		//miniGames.add(new Killr());
		//miniGames.add(new AvoidTheTrain());

		Group games = new Group();

		for (int i = 0; i < miniGames.size(); i++) {
			Table gameButton = miniGames.get(i).getGameButton();
			gameButton.moveBy(Gdx.graphics.getWidth() / 4 + 150 * (i / 3),
					-label.getHeight() * 2 - 20 - 100 * (i - 3 * (i / 3)));

			games.addActor(gameButton);
		}
		games.addAction(Actions.sequence(Actions.fadeOut(0f),
				Actions.fadeIn(2f, Interpolation.fade)));

		stage.addActor(games);

		dict = new SpeakerDict();
		
		final List<String> greeting = SpeakerDict.retrieveMessageForGameDesc("start");
		Thread t = new Thread(){
			public void run() {
				for(String text : greeting){
					//speakForGame(text);
				}
			};
		};
		t.start();
//		gameServer.getClientsData().add(
//				new ClientData("Dummy1", ColorCode.RED.getCode()));
//		gameServer.getClientsData().add(
//				new ClientData("Dummy2", ColorCode.RED.getCode()));
	}

	@Override
	public void render() {
		try {

			Gdx.gl.glClearColor(0, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

			if (nextScreen != null) {
				transitionValue *= 1.05f;
				if (transitionValue > 5f) {
					setScreen(nextScreen);
					nextScreen = null;
				}
			}
			if (transitionValue > 0.01f) {
				Gdx.gl.glEnable(GL20.GL_BLEND);
				Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,
						GL20.GL_ONE_MINUS_SRC_ALPHA);
				batch.begin();
				rotation += 1f + 1f / transitionValue * 1.5f;
				Color c = batch.getColor();
				batch.setColor(c.r, c.g, c.b, 1);
				batch.draw(background, getMaxX() / 2 - 400f * transitionValue,
						getMaxY() / 2 - 400f * transitionValue,
						400f * transitionValue, 400f * transitionValue,
						800f * transitionValue, 800f * transitionValue, 1f, 1f,
						rotation, 0, 0, 800, 800, false, false);

				batch.setColor(c.r, c.g, c.b, transitionValue - 0.1f);
				batch.draw(label, getMaxX() / 2 - label.getWidth() / 2,
						getMaxY() - label.getHeight());
				batch.end();
				if (screen == null) {
					stage.act();
					stage.draw();
				}

				Gdx.gl.glDisable(GL20.GL_BLEND);
			}

			if (screen == null) {

				clientUpdate();

				if (transitionValue < 1f) {
					transitionValue *= 1.1f;
				} else if (nextScreen == null) {
					transitionValue = 1f;
				}

				if (Gdx.input.justTouched()) {
					// speakFromDict("banter", gameServer.getClientsData(),
					// gameServer.getClientsData().get(0));
					// nextScreen = new CurveFeverPrototype();
				}

				Gdx.gl.glEnable(GL20.GL_BLEND);
				Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA,
						GL20.GL_ONE_MINUS_SRC_ALPHA);
				shapeRenderer.setProjectionMatrix(cam.combined);
				shapeRenderer.begin(ShapeType.Filled);
				for (ClientData client : gameServer.getClientsData()) {

					Color color = client.getColorCode().getColor();

					if (!client.isConnected()) {
						color.a = 0.2f;
					} else {
						color.a = 1f;
					}
					shapeRenderer.setColor(color);
					// shapeRenderer.ellipse(client.getX() - 10, client.getY() -
					// 10,
					// 20, 20);
					shapeRenderer.setColor(Color.WHITE);
					shapeRenderer.ellipse(client.getX(), client.getY(), 1, 1);

				}
				shapeRenderer.end();
				Gdx.gl.glDisable(GL20.GL_BLEND);

				batch.setProjectionMatrix(cam.combined);
				batch.begin();
				for (ClientData client : gameServer.getClientsData()) {
					BitmapFont font = skin.getFont("font");

					font.draw(batch, client.getUsername(), client.getX(),
							client.getY() - 10f);

					Color c = client.getColorCode().getColor();
					batch.setColor(c.r - 0.1f, c.g - 0.1f, c.b - 0.1f, c.a);
					batch.draw(playerCursor,
							client.getX() - playerCursor.getWidth() / 2,
							client.getY() - playerCursor.getHeight() / 2);
					batch.setColor(Color.WHITE);

				}
				BitmapFont font = skin.getFont("font");
				if (dialog != null) {
					font.draw(batch, dialog, 12f, 15f);
				}
				batch.end();
			} else {
				super.render();

				if (transitionValue > 0.01f) {
					transitionValue *= 0.95f;
				}

				if (RESET || Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
					RESET = false;
					screen = null;
					transitionValue = 0.01f;
					gameServer.sendToAll("controller:"
							+ ControllerType.ANALOGSTICK.ordinal());
				}

				batch.setProjectionMatrix(cam.combined);
				batch.begin();
				BitmapFont font = skin.getFont("font");
				if (dialog != null) {
					font.draw(batch, dialog, 12f, 15f);
				}
				batch.end();
			}
		} catch (Exception e) {
			if (lastException != null
					&& lastException.getClass().equals(e.getClass())) {
				System.err.print(".");
			} else {
				e.printStackTrace();
				System.out.println();
			}
			lastException = e;

			if (batch.isDrawing()) {
				batch.end();
			}
			if (shapeRenderer.isDrawing()) {
				shapeRenderer.end();
			}
		}
	}

	public static void clientUpdate() {
		for (ClientData client : gameServer.getClientsData()) {

			float accX = 0;
			float accY = 0;

			if (client.isUpPressed()) {
				accY += 0.2f;
			}
			if (client.isDownPressed()) {
				accY -= 0.2f;
			}
			if (client.isRightPressed()) {
				accX += 0.2f;
			}
			if (client.isLeftPressed()) {
				accX -= 0.2f;
			}
			client.addVelX(accX);
			client.addVelY(accY);

			if (nextScreen != null) {
				float xdiff = getMaxX() / 2 - client.getX();
				float ydiff = getMaxY() / 2 - client.getY();

				client.setVelX(xdiff * 0.1f);
				client.setVelY(ydiff * 0.1f);
			} else if (transitionValue == 0.01f) {
				Vector2 vec = new Vector2(6f, 0f);
				vec.rotate(MathUtils.random(360));

				client.addVelX(vec.x);
				client.addVelY(vec.y);
			}

			float x = client.getX() + client.getVelX();
			float y = client.getY() + client.getVelY();

			if (x > getMaxX() + 15) {
				x = -15;
			}

			if (x < -15) {
				x = getMaxX() + 15;
			}

			if (y > getMaxY() + 15) {
				y = -15;
			}

			if (y < -15) {
				y = getMaxY() + 15;
			}

			client.setX(x);
			client.setY(y);

			if (accX == 0) {
				client.mulVelX(0.95f);
			}
			if (accY == 0) {
				client.mulVelY(0.95f);
			}
		}
	}

	@Override
	public void resize(int width, int height) {
		cam = new OrthographicCamera(width / 2, height / 2);
		cam.translate(getMaxX() / 2, getMaxY() / 2);
		cam.update();
	}

	public static float getMaxY() {
		return Gdx.graphics.getHeight() / 2;
	}

	public static float getMaxX() {
		return Gdx.graphics.getWidth() / 2;
	}

	public static void forceScreenChange(MiniGame miniGame) {
		if (miniGame == null) {
			RESET = true;
		} else {
			gameServer.sendToAll("controller:"
					+ miniGame.getControllerType().ordinal());
			nextScreen = miniGame;
		}

	}

	public static void speak(String toSpeak) {
		speaker.doSpeak(toSpeak);
	}

	public static void speakForGame(final String text) {
		while(dialog != null){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		dialog = text;
		speaker.doSpeak(text);
		dialog = null;
	}
	public static void speakFromDict(final String key,
			final List<ClientData> clients, final ClientData receiver) {
		speakFromDict(key, clients, receiver, null, null);
	}

	public static void speakFromDict(final String key,
			final List<ClientData> clients, final ClientData receiver, final String replaceString, final String replacement) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				String retrieveMessageForClient = dict.retrieveMessageForClient(key, clients,
						receiver);
				if(replaceString != null){
					retrieveMessageForClient = retrieveMessageForClient.replaceAll(replaceString, replacement);
				}
				if (dialog != null) {
					dialogQueue.add(retrieveMessageForClient);
				} else {
					dialog = retrieveMessageForClient;
					speaker.doSpeak(dialog);
					while (!dialogQueue.isEmpty()) {
						for (String msg : new ArrayList<String>(dialogQueue)) {
							dialogQueue.remove(msg);
							dialog = msg;
							speak(msg);
						}
					}
					dialog = null;
				}
			}
		};
		thread.start();
	}

	@Override
	public void dispose() {
		speaker.terminate();
	}

	private Table buildGameButton() {
		Window window = new Window("", GetTogetherGameHub.skin);
		Label functionLabel = new Label("Roast Me", GetTogetherGameHub.skin);
		window.pack();
		window.setPosition(Gdx.graphics.getWidth() / 2 - window.getWidth() / 2,
				Gdx.graphics.getHeight() - window.getHeight());
		return window;
	}
}
