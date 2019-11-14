package com.pixeldweller.game.minigames;

import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.pixeldweller.game.GetTogetherGameHub;
import com.pixeldweller.game.clientcontroller.ControllerType;
import com.pixeldweller.game.network.ClientData;
import com.pixeldweller.game.util.SpeakerDict;

public abstract class MiniGame implements Screen {

	private static final int tile_y = 85;
	private static final int tile_x = 61;
	// For Hub
	private String minigameName;
	private Table gameTable;
	private Label gameLabel;

	private boolean explained = true;

	private boolean readyState, startState;
	private Texture getReadyTexture;
	private float animationKey;

	private int minimumPlayerCount;
	private float selectionTime = 3f;
	private ControllerType controllerType;
	private String gameDescription = "- Sorry, I did found any description to this game yet.";
	private float standardGameCountdown = 1000 * 15; // Standard value 30 Sec.
	private float gameCountdown;
	private Texture countdownTexture;
	private Animation<TextureRegion> countdownAnimation;

	public float getStandardGameCountdown() {
		return standardGameCountdown;
	}

	public void setStandardGameCountdown(float gameCountdown) {
		this.standardGameCountdown = gameCountdown;
	}

	public MiniGame(String name, int minimumPlayerCount,
			ControllerType controllerType) {
		minigameName = name;
		this.minimumPlayerCount = minimumPlayerCount;
		this.setControllerType(controllerType);
		gameTable = buildGameButton();

	}

	@Override
	public void show() {
		readyState = false;
		startState = false;
		gameCountdown = standardGameCountdown;
		Collections.shuffle(GetTogetherGameHub.gameServer.getClientsData());
		getReadyTexture = new Texture(Gdx.files.internal("getready.png"));

		// Lade Icon
		countdownTexture = new Texture(Gdx.files.internal("numbers.png"));
		TextureRegion[][] tmp = TextureRegion.split(countdownTexture, tile_x,
				tile_y);
		TextureRegion[] animFrames = new TextureRegion[9];
		int index = 0;
		for (int i = 0; i < 9; i++) {
			animFrames[index++] = tmp[0][i];

		}
		countdownAnimation = new Animation<TextureRegion>(1f, animFrames);
		countdownAnimation.setPlayMode(PlayMode.LOOP);
		animationKey = 0f;

		Thread gameDesc = new Thread() {
			public void run() {
				if (!explained) {

					List<String> gamedesc = SpeakerDict
							.retrieveMessageForGameDesc(minigameName);
					for (String desc : gamedesc) {
						GetTogetherGameHub.speakForGame(desc);
					}

					List<ClientData> clientsData = GetTogetherGameHub.gameServer
							.getClientsData();
					int size = clientsData.size();
					if (size % 2 == 1) {
						// UNEVEN
						gamedesc = SpeakerDict
								.retrieveMessageForGameDesc(minigameName
										+ "Extra");
						for (String desc : gamedesc) {
							if (desc != null) {
								GetTogetherGameHub.speakForGame(desc
										.replaceAll("@player",
												clientsData.get(size - 1)
														.getUsername()));
							}
						}
					}

					
					explained = true;
				}else {

					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				startGame();
			};
		};
		gameDesc.start();
	}

	@Override
	public void render(float delta) {
		if (!hasStarted()) {
			if (GetTogetherGameHub.batch.getColor().a == 1f) {
				GetTogetherGameHub.batch.begin();
				GetTogetherGameHub.batch.draw(
						getReadyTexture,
						GetTogetherGameHub.getMaxX() / 2
								- getReadyTexture.getWidth() / 4,
						GetTogetherGameHub.getMaxY() / 2
								- getReadyTexture.getHeight() / 4,
						getReadyTexture.getWidth() / 2,
						getReadyTexture.getHeight() / 2);
				GetTogetherGameHub.batch.end();
			}
		} else {
			if (gameCountdown < 1000 * 10) {
				GetTogetherGameHub.batch.begin();
				TextureRegion keyFrame = countdownAnimation
						.getKeyFrame(9f - (animationKey += Gdx.graphics
								.getDeltaTime()) % 10f);
				GetTogetherGameHub.batch.draw(keyFrame,
						GetTogetherGameHub.getMaxX() / 2 - tile_x / 4,
						GetTogetherGameHub.getMaxY() / 2 - tile_y / 4,
						tile_x / 2, tile_y / 2);
				GetTogetherGameHub.batch.end();
			}
		}

	}

	private Table buildGameButton() {
		Window window = new Window("", GetTogetherGameHub.skin);
		gameLabel = new Label(minigameName, GetTogetherGameHub.skin);
		gameLabel.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				GetTogetherGameHub.forceScreenChange(MiniGame.this);
				GetTogetherGameHub.speakFromDict("gamestart", null, null,
						"@gamename", minigameName);
			}
		});
		window.add(gameLabel).pad(10, 0, 10, 0).row();

		window.pack();
		window.setPosition(Gdx.graphics.getWidth() / 2 - window.getWidth() / 2,
				Gdx.graphics.getHeight() - window.getHeight());
		return window;
	}

	public void gameWon(List<ClientData> winners) {
		if (winners.isEmpty()) {
			GetTogetherGameHub.speakFromDict("nowin", null, null);
		}
	}

	public String getName() {
		return minigameName;
	}

	public Table getGameButton() {
		return gameTable;
	}

	public int getMinimumPlayerCount() {
		return minimumPlayerCount;
	}

	public float getSelectionTime() {
		return selectionTime;
	}

	public void setSelectionTime(float selectionTime) {
		this.selectionTime = selectionTime;
	}

	public String getGameDescription() {
		return gameDescription;
	}

	public void setGameDescription(String gameDescription) {
		this.gameDescription = gameDescription;
	}

	public ControllerType getControllerType() {
		return controllerType;
	}

	public void setControllerType(ControllerType controllerType) {
		this.controllerType = controllerType;
	}

	public boolean isReadyState() {
		return readyState;
	}

	public void setReadyState(boolean readyState) {
		this.readyState = readyState;
	}

	public boolean hasStarted() {
		return startState;
	}

	public void startGame() {
		startState = true;
		Thread gameTimeout = new Thread() {
			public void run() {
				while (gameCountdown > 0) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					gameCountdown -= 1000;
					if (gameCountdown == 10000) {
						GetTogetherGameHub.speakFromDict("time", null, null);
					}
				}

				endGame();

			}

		};
		gameTimeout.start();
	}

	public void endGame() {
		GetTogetherGameHub.RESET = true;
	};

}
