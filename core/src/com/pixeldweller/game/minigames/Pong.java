package com.pixeldweller.game.minigames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.compression.CRC;
import com.pixeldweller.game.GetTogetherGameHub;
import com.pixeldweller.game.clientcontroller.ControllerType;
import com.pixeldweller.game.network.ClientData;
import com.pixeldweller.game.util.SpeakerDict;

public class Pong extends MiniGame {

	private World world;
	private HashMap<ClientData, Paddle> paddleMap;

	final short PHYSICS_ENTITY = 0x1; // 0001
	final short WORLD_ENTITY = 0x1 << 1; // 0010 or 0x2 in hex
	final short BALL_ENTITY = 0x1 << 2; // 0010 or 0x2 in hex
	private Box2DDebugRenderer debugRenderer;
	private OrthographicCamera camera;
	private List<Ball> balls;

	private int score_right, score_left;

	private class Ball {
		private Body ballBody;

		public Ball(Body body) {
			ballBody = body;
		}

		public Body getBallBody() {
			return ballBody;
		}
	}

	private class Paddle {
		private boolean right;
		private Body paddleBody;
		private boolean middlePaddle;

		public Paddle(Body body, boolean right) {
			paddleBody = body;
			this.right = right;
		}		

		public Body getPaddleBody() {
			return paddleBody;
		}

		public boolean isMiddlePaddle() {
			return middlePaddle;
		}

		public void setMiddlePaddle(boolean middlePaddle) {
			this.middlePaddle = middlePaddle;
		}
	}

	public Pong() {
		super("Weird Pong", 2, ControllerType.ANALOGSTICK);
		setStandardGameCountdown(1000 * 60);
	}

	@Override
	public void startGame() {
		super.startGame();
		createBall();
	}

	@Override
	public void show() {		
		super.show();		
		score_left = 0;
		score_right = 0;
		camera = new OrthographicCamera();
		world = new World(new Vector2(0, -10f), true);
		debugRenderer = new Box2DDebugRenderer();
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		// We are going to use 1 to 1 dimensions. Meaning 1 in physics engine is
		// 1 pixel
		// Set our body to the same position as our sprite
		// Now define the dimensions of the physics shape
		PolygonShape shape = new PolygonShape();
		// We are a box, so this makes sense, no?
		// Basically set the physics polygon to a box with the same dimensions
		// as our sprite
		shape.setAsBox(5f, 25f);

		paddleMap = new HashMap<ClientData, Paddle>();

		boolean right = true;

		for (ClientData client : GetTogetherGameHub.gameServer.getClientsData()) {
			if (client.isConnected()) {
				if (right) {
					bodyDef.position.set(Gdx.graphics.getWidth() / 4 + 325f,
							client.getY() - 7.5f + 100);
					right = false;
				} else {
					bodyDef.position.set(Gdx.graphics.getWidth() / 4 - 325f,
							client.getY() - 7.5f + 100);
					right = true;
				}

				// Create a body in the world using our definition
				if (GetTogetherGameHub.gameServer.getClientsData().size() % 2 != 0
						&& client == GetTogetherGameHub.gameServer
								.getClientsData().get(
										GetTogetherGameHub.gameServer
												.getClientsData().size() - 1)) {
					continue;
				}

				Body client_body = world.createBody(bodyDef);

				FixtureDef fixtureDef = new FixtureDef();
				fixtureDef.shape = shape;
				fixtureDef.density = 1f;
				fixtureDef.restitution = 1f;
				fixtureDef.filter.categoryBits = PHYSICS_ENTITY;
				fixtureDef.filter.maskBits = PHYSICS_ENTITY | WORLD_ENTITY
						| BALL_ENTITY;

				client_body.createFixture(fixtureDef);

				paddleMap.put(client, new Paddle(client_body, !right));
			}
		}

		if (GetTogetherGameHub.gameServer.getClientsData().size() % 2 != 0) {
			ClientData clientData = GetTogetherGameHub.gameServer
					.getClientsData().get(
							GetTogetherGameHub.gameServer.getClientsData()
									.size() - 1);
			bodyDef.position.set(Gdx.graphics.getWidth() / 4,
					Gdx.graphics.getHeight() / 4);
			shape.setAsBox(2f, 10f);

			Body client_body = world.createBody(bodyDef);

			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = shape;
			fixtureDef.density = 1f;
			fixtureDef.restitution = 1f;
			fixtureDef.filter.categoryBits = PHYSICS_ENTITY;
			fixtureDef.filter.maskBits = PHYSICS_ENTITY | WORLD_ENTITY
					| BALL_ENTITY;

			client_body.setGravityScale(0f);

			client_body.createFixture(fixtureDef);
			Paddle paddle = new Paddle(client_body, right);
			paddle.setMiddlePaddle(true);
			paddleMap.put(clientData, paddle);
		}

		// Ball Array
		balls = new ArrayList<Pong.Ball>();

		// Ground
		bodyDef.type = BodyType.StaticBody;
		bodyDef.position.set(0, 0);

		ChainShape groundShape = new ChainShape();
		groundShape.createChain(new Vector2[] {
				new Vector2(Gdx.graphics.getWidth() / 4, 0),
				new Vector2(0, 0),
				new Vector2(0, Gdx.graphics.getHeight() / 2),
				new Vector2(Gdx.graphics.getWidth() / 2, Gdx.graphics
						.getHeight() / 2),
				new Vector2(Gdx.graphics.getWidth() / 2, 0),
				new Vector2(Gdx.graphics.getWidth() / 4, 0),
				new Vector2(Gdx.graphics.getWidth() / 4, Gdx.graphics
						.getHeight() / 2) });
		// groundShape.createChain(new Vector2[]{new Vector2(-50,0),new
		// Vector2(50,0)});

		FixtureDef groundDef = new FixtureDef();
		groundDef.shape = groundShape;
		groundDef.friction = 0.1f;
		groundDef.restitution = .3f;
		groundDef.filter.categoryBits = WORLD_ENTITY;

		world.createBody(bodyDef).createFixture(groundDef);

		shape.dispose();
	}

	private void createBall() {
		CircleShape circle = new CircleShape();
		circle.setRadius(5f);
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		FixtureDef fixDef = new FixtureDef();
		fixDef.shape = circle;
		fixDef.density = 0.5f;
		fixDef.friction = 0.01f;
		fixDef.restitution = .75f;
		fixDef.filter.categoryBits = BALL_ENTITY;
		fixDef.filter.maskBits = PHYSICS_ENTITY;

		Body ball;
		ball = world.createBody(bodyDef);
		ball.createFixture(fixDef);
		ball.setGravityScale(0f);
		ball.setLinearVelocity(MathUtils.random(-30, 20) * 10 + 1000,
				MathUtils.random(-1000, 1000));
		ball.setFixedRotation(true);
		ball.setTransform(Gdx.graphics.getWidth() / 4,
				MathUtils.random(10, (Gdx.graphics.getHeight() / 2) - 10), 0);
		balls.add(new Ball(ball));
	}

	@Override
	public void render(float delta) {
		world.step(1 / 60f, 10, 10);

		// ball.applyForceToCenter(5000f, 0f, true);
		for (int i = 0; i < balls.size(); i++) {
			Ball ball = balls.get(i);
			if (ball.getBallBody().getPosition().x < 0
					|| ball.getBallBody().getPosition().x > Gdx.graphics
							.getWidth() / 2) {
				if (ball.getBallBody().getPosition().x < 0) {
					score_right++;
				}
				if (ball.getBallBody().getPosition().x > Gdx.graphics
						.getWidth() / 2) {
					score_left++;
				}

				ball.getBallBody().setTransform(Gdx.graphics.getWidth() / 4,
						Gdx.graphics.getHeight() / 4, 0);

				if (MathUtils.random(1) == 0) {
					ball.getBallBody().setLinearVelocity(
							MathUtils.random(500, 2000),
							MathUtils.random(-1000, 1000));
				} else {
					ball.getBallBody().setLinearVelocity(
							-MathUtils.random(500, 2000),
							MathUtils.random(-1000, 1000));
				}

				if (balls.size() < 10) {
					createBall();
				}

			}

			if (ball.getBallBody().getPosition().y < 2) {
				Vector2 position = ball.getBallBody().getPosition();
				float angle = ball.getBallBody().getAngle();
				Vector2 linearVelocity = ball.getBallBody().getLinearVelocity();
				ball.getBallBody().setTransform(position.x, 2, angle);
				ball.getBallBody().setLinearVelocity(linearVelocity.x,
						-linearVelocity.y);
			}
			if (ball.getBallBody().getPosition().y > Gdx.graphics.getHeight() / 2 - 2) {
				Vector2 position = ball.getBallBody().getPosition();
				Vector2 linearVelocity = ball.getBallBody().getLinearVelocity();
				float angle = ball.getBallBody().getAngle();
				ball.getBallBody().setTransform(position.x,
						Gdx.graphics.getHeight() / 2 - 2, angle);
				ball.getBallBody().setLinearVelocity(linearVelocity.x,
						-linearVelocity.y);
			}

			Vector2 linearVelocity = ball.getBallBody().getLinearVelocity();
			ball.getBallBody().setLinearVelocity(linearVelocity.x * 1.05f,
					linearVelocity.y * 1.05f);
		}
		debugRenderer.render(world,
				GetTogetherGameHub.batch.getProjectionMatrix());
		GetTogetherGameHub.shapeRenderer.begin(ShapeType.Filled);
		for (ClientData client : paddleMap.keySet()) {
			GetTogetherGameHub.shapeRenderer.setColor(client.getColorCode()
					.getColor());

			Paddle paddle = paddleMap.get(client);
			Body body = paddle.getPaddleBody();
			updateToControlls(client, paddle);
			Vector2 position = body.getPosition();
			float width = 5f;
			float height = 25f;
			if (paddle.isMiddlePaddle()) {
				width = 3f;
				height = 11f;
			}
			GetTogetherGameHub.shapeRenderer.rect(position.x - width / 2,
					position.y - height / 2, width / 2, height / 2, width,
					height, 2.1f, 2.1f,
					MathUtils.radiansToDegrees * body.getAngle());
		}

		GetTogetherGameHub.shapeRenderer.end();

		GetTogetherGameHub.batch.begin();
		for (ClientData client : paddleMap.keySet()) {
			GetTogetherGameHub.batch.setColor(client.getColorCode().getColor());

			GetTogetherGameHub.batch.setColor(Color.WHITE);
		}

		BitmapFont font = GetTogetherGameHub.skin.getFont("font");
		font.draw(GetTogetherGameHub.batch, "Team Left: " + score_left, 0f,
				Gdx.graphics.getHeight() / 2 - 15f);
		font.draw(GetTogetherGameHub.batch, "Team Right: " + score_right,
				Gdx.graphics.getWidth() / 4 + 15f,
				Gdx.graphics.getHeight() / 2 - 15f);

		GetTogetherGameHub.batch.end();

		GetTogetherGameHub.shapeRenderer.begin(ShapeType.Filled);

		GetTogetherGameHub.shapeRenderer.end();

		GetTogetherGameHub.clientUpdate();
		super.render(delta);
	}

	private void updateToControlls(ClientData client, Paddle paddle) {
		Vector2 linearVelocity = paddle.getPaddleBody().getLinearVelocity();
		if (paddle.isMiddlePaddle()) {
			paddle.getPaddleBody().setLinearVelocity(linearVelocity.x,
					linearVelocity.y + client.getVelY() * 300f);
		} else {
			paddle.getPaddleBody().setLinearVelocity(
					linearVelocity.x + client.getVelX() * 300f,
					linearVelocity.y + client.getVelY() * 300f);
		}

		client.mulVelX(0.95f);
		client.mulVelY(0.95f);
		// body.applyForceToCenter(client.getTouchpadX() * 5000f,
		// client.getTouchpadY() * 5000f, true);
	}

	@Override
	public void endGame() {
		boolean rightWin = false;
		boolean leftWin = false;
		if (score_left > score_right) {
			String winText = SpeakerDict.retrieveMessageForClient("win", null, null).replaceAll("@player", "Team Left");
			GetTogetherGameHub.speakForGame(winText);
		} else if (score_right > score_left) {
			String winText = SpeakerDict.retrieveMessageForClient("win", null, null).replaceAll("@player", "Team Right");
			GetTogetherGameHub.speakForGame(winText);
		} else if (score_right == 0 && score_left == 0) {
			GetTogetherGameHub.speakFromDict("nowin", null, null);
		} else {
			GetTogetherGameHub.speakFromDict("nowin", null, null);
		}

		for (Paddle paddle : paddleMap.values()) {
			if (!paddle.middlePaddle && paddle.right && rightWin) {
				
			}
			if (!paddle.middlePaddle && !paddle.right&& leftWin) {

			}
		}

		super.endGame();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
