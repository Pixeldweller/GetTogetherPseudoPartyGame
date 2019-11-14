package com.pixeldweller.game.clientcontroller;

import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pixeldweller.game.ColorCode;
import com.pixeldweller.game.network.WebSocketInterface;

public class SimpleInputClient extends ApplicationAdapter {

	public static WebSocketInterface networkInterface;
	public static SpriteBatch batch, labelBatch;

	public static IKeyboardInvoker keyboardInvoker;

	public static InputMultiplexer inputMultiplexer;
	public static boolean loggedIn;
	public static String username;
	public static String ip;

	private static Stage loginStage;

	private World world;
	private Skin skin;
	private OrthographicCamera cam;
	private Viewport viewport;
	private Box2DDebugRenderer b2dr;
	private static ArrowController arrowController;
	private static AnalogStickController analogController;
	private Texture label, playerCursor;
	private static ControllerType currentControllerType;
	private static Color player_color;
	private static TextField usernameTxtField;

	public SimpleInputClient(WebSocketInterface networkInterface) {
		SimpleInputClient.networkInterface = networkInterface;
	}

	public static void disconnect() {
		loggedIn = false;
		inputMultiplexer.clear();
		inputMultiplexer.addProcessor(loginStage);
	}

	public static void loggedIn() {
		loggedIn = true;
		inputMultiplexer.clear();
		analogController.refreshColor();
		inputMultiplexer.addProcessor(AnalogStickController.stage);
		currentControllerType = ControllerType.ANALOGSTICK;
		player_color = ColorCode.getColor(networkInterface.getId()).getColor();
	}

	public static void changeControllerType(ControllerType type) {
		inputMultiplexer.clear();
		switch (type) {
		case ARROWS:
			inputMultiplexer.addProcessor(ArrowController.stage);
			break;
		case ANALOGSTICK:
			inputMultiplexer.addProcessor(AnalogStickController.stage);
			break;
		default:
			break;
		}
		currentControllerType = type;
	}

	@Override
	public void create() {
		skin = new Skin(Gdx.files.internal("comicui/comic-ui.json"));
		cam = new OrthographicCamera();
		inputMultiplexer = new InputMultiplexer();
		Gdx.input.setInputProcessor(inputMultiplexer);

		label = new Texture(Gdx.files.internal("label.png"));
		playerCursor = new Texture(Gdx.files.internal("cursor.png"));
		viewport = new FitViewport(400, 500, cam);
		batch = new SpriteBatch();
		labelBatch = new SpriteBatch();
		world = new World(new Vector2(0, -10), true);
		b2dr = new Box2DDebugRenderer();
		arrowController = new ArrowController();
		analogController = new AnalogStickController();
		CustomInputListener.setWebSocketInterface(networkInterface);

		createLoginStage();
		// loggedIn = true;
	}

	private void createLoginStage() {
		loginStage = new Stage(viewport);
		inputMultiplexer.addProcessor(loginStage);

		if (keyboardInvoker != null) {
			// keyboardInvoker.openKeyboard();
		}

		usernameTxtField = new TextField("", skin);
		usernameTxtField.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (keyboardInvoker != null) {
					keyboardInvoker.openKeyboard(usernameTxtField);
				}
			}
		});
		final TextField ipTxtField = new TextField("192.168.178.23", skin);
		ipTxtField.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (keyboardInvoker != null) {
					keyboardInvoker.openKeyboard(ipTxtField);
				}
			}
		});

		Table table = new Table();
		TextButton joinBttn = new TextButton("JOIN", skin);
		table.add(joinBttn);

		joinBttn.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (!networkInterface.isConnected()) {
					ip = ipTxtField.getText();
					username = usernameTxtField.getText();
					if (username == null || username.equals("")) {
						username = "Guest#" + new Random().nextInt(30);
					}
					username = username.replaceAll("([A-Z])", " $1")
							.toLowerCase();
					usernameTxtField.setName(username);
					networkInterface.connectClient("localhost");
				}
			}
		});

		table.row();
		Label label = new Label("NAME YOUR HERO:", skin);
		table.add(label).padTop(15.0f);

		table.row();
		table.add(usernameTxtField).padTop(5.0f);
		table.row();

		table.row();
		table.add(ipTxtField).padTop(5.0f);
		table.row();

		ProgressBar progressBar = new ProgressBar(0.0f, 100.0f, 1.0f, false,
				skin);
		progressBar.setValue(100f);
		table.add(progressBar).padTop(15.0f);

		table.row();
		SelectBox<String> selectBox = new SelectBox<String>(skin, "big");
		selectBox
				.setItems("green bumble-bee", "crimson roach", "banana master");
		// table.add(selectBox).padTop(15.0f);

		table.row();

		table.moveBy(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
		loginStage.addActor(table);
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		viewport.update(width, height);
		arrowController.resize(width, height);
	}

	public void update(float dt) {
		world.step(1 / 60f, 6, 2);
		cam.position.set(viewport.getWorldWidth() / 2,
				viewport.getWorldHeight() / 2, 0);
		cam.update();
	}

	@Override
	public void render() {
		update(Gdx.graphics.getDeltaTime());
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		b2dr.render(world, cam.combined);

		if (loggedIn) {
			switch (currentControllerType) {
			case ARROWS:
				arrowController.draw();
				break;
			case ANALOGSTICK:
				analogController.draw();
				break;
			default:
				break;
			}
		} else {
			loginStage.draw();
		}

		labelBatch.begin();
		labelBatch.setColor(Color.WHITE);
		labelBatch.draw(label, 0, Gdx.graphics.getHeight() - label.getHeight(),
				Gdx.graphics.getWidth(), label.getHeight());
		if (loggedIn && username != null) {
			BitmapFont font = skin.getFont("font");
			font.draw(labelBatch, "Playing as " + username, 35f,
					Gdx.graphics.getHeight() - label.getHeight() - 10f);
			labelBatch.setColor(player_color);
			labelBatch.draw(playerCursor, 10,
					Gdx.graphics.getHeight() - label.getHeight() - 25f, 20f,
					20f);
		}
		labelBatch.end();
	}

	public static void onServerResponse(String msg){
		if(msg.startsWith("rename")){
			username = msg.split(":")[1];
			usernameTxtField.setText(username);
		}
		
		if(msg.startsWith("controller")){			
			String typeString = msg.split(":")[1];
			int slot = Integer.parseInt(typeString);
			changeControllerType(ControllerType.values()[slot]);
		}
	}
}
