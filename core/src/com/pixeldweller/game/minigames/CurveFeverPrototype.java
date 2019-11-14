package com.pixeldweller.game.minigames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.pixeldweller.game.GetTogetherGameHub;
import com.pixeldweller.game.clientcontroller.ControllerType;
import com.pixeldweller.game.network.ClientData;
import com.pixeldweller.game.util.PixmapHelper;
import com.sun.javafx.geom.Vec2f;
import com.sun.prism.GraphicsPipeline.ShaderType;

public class CurveFeverPrototype extends MiniGame{

	private PixmapHelper pixmapHelper;

	public CurveFeverPrototype() {
		super("Curve Fever Prototype", 2, ControllerType.ARROWS);		
	}

	@Override
	public void show() {
		Texture t = new Texture(Gdx.files.internal("canvas.png"));
		pixmapHelper = new PixmapHelper(new Pixmap(Gdx.files.internal("canvas.png")),new Sprite(t),t);
		pixmapHelper.setFileHandle(Gdx.files.internal("canvas.png"));
	}

	@Override
	public void render(float delta) {		
		GetTogetherGameHub.shapeRenderer.begin(ShapeType.Filled);
		GetTogetherGameHub.shapeRenderer.setColor(Color.WHITE);
		GetTogetherGameHub.shapeRenderer.rect(0, 0, 1600,1000);
		GetTogetherGameHub.shapeRenderer.end();
		
		GetTogetherGameHub.batch.begin();
		GetTogetherGameHub.batch.setColor(Color.WHITE);
		GetTogetherGameHub.batch.draw(pixmapHelper.texture, 0, 0);		
		GetTogetherGameHub.batch.end();
		
		GetTogetherGameHub.shapeRenderer.begin(ShapeType.Filled);
		for(ClientData client: GetTogetherGameHub.gameServer.getClientsData()){
			Vector2 pos = new Vector2(client.getX(),client.getVelY());
			pixmapHelper.project(pos, client.getX(),client.getVelY());
			pixmapHelper.eraseCircle(pos.x, pos.y, 35f, 0.9f);
			pixmapHelper.update();
			GetTogetherGameHub.shapeRenderer.rect(client.getX(), client.getY(), 2,2);
		}
		
		GetTogetherGameHub.shapeRenderer.end();
	
		GetTogetherGameHub.clientUpdate();
		
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
