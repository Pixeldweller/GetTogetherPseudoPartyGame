package com.pixeldweller.game.clientcontroller;

import java.text.DecimalFormat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.pixeldweller.game.ColorCode;

/**
 * Created by brentaureli on 10/23/15.
 */
public class AnalogStickController {
	Viewport viewport;
	public static Stage stage;
	private static Skin touchpadSkin;
	boolean upPressed, downPressed, leftPressed, rightPressed;
	OrthographicCamera cam;

	float offsetX, offsetY;
	float lastX, lastY;
	private Touchpad touchpad;

	public AnalogStickController() {
		cam = new OrthographicCamera();
		viewport = new FitViewport(400, 400, cam);
		stage = new Stage(viewport, SimpleInputClient.batch);
		touchpadSkin = createSkinWithUIColor(Color.WHITE);
		touchpad = new Touchpad(3f, touchpadSkin, "default");
		touchpad.moveBy(Gdx.graphics.getWidth() / 2 - touchpad.getWidth() / 2,
				0);
		stage.addActor(touchpad);
	}

	public void refreshColor() {
		touchpad.setColor(ColorCode.getColor(
				SimpleInputClient.networkInterface.getId()).getColor());
	}

	public Skin createSkinWithUIColor(Color c) {
		float[] colorcode = { c.r, c.g, c.b };
		Skin skin = new Skin();
		skin.add("color",
				new Color(colorcode[0], colorcode[1], colorcode[2], 1),
				Color.class);
		skin.add("selected", new Color(colorcode[0], colorcode[1],
				colorcode[2], 1), Color.class);
		skin.add("text",
				new Color(colorcode[0], colorcode[1], colorcode[2], 1),
				Color.class);
		skin.add("text-selected", new Color(colorcode[0], colorcode[1],
				colorcode[2], 1), Color.class);

		FileHandle fileHandle = Gdx.files.internal("neonui/neon-ui.json");
		FileHandle atlasFile = fileHandle.sibling("neon-ui.atlas");
		if (atlasFile.exists()) {
			skin.addRegions(new TextureAtlas(atlasFile));
		}
		skin.load(fileHandle);
		return skin;
	}

	public void draw() {
		if(Gdx.input.isTouched()){
			float deltaX = touchpad.getKnobPercentX();			
			float deltaY = touchpad.getKnobPercentY();
			
			// Workaround fuer 1/100 = 1
			if(deltaX > 0.99f){
				deltaX-=0.02;
			} else if ( deltaX < -0.99){
				deltaX+=0.02;
			}
			if(deltaY > 0.99f){
				deltaY-=0.02;
			}else if ( deltaY < -0.99){
				deltaY+=0.02;
			}			
			String x = (deltaX+"000").substring(0,deltaX < 0 ? 5 :4);
			String y = (deltaY+"000").substring(0,deltaY < 0 ? 5 :4);			
			System.out.println("send x: "+x+"| y:"+y);	
			SimpleInputClient.networkInterface.sendMsg(x.replace("0.", "")+"##"+y.replace("0.", ""));	
		}
		
		stage.act();
		stage.draw();
	}

	public boolean isUpPressed() {
		return upPressed;
	}

	public boolean isDownPressed() {
		return downPressed;
	}

	public boolean isLeftPressed() {
		return leftPressed;
	}

	public boolean isRightPressed() {
		return rightPressed;
	}

	public void resize(int width, int height) {
		viewport.update(width, height);
	}
}