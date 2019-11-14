package com.pixeldweller.game.clientcontroller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by brentaureli on 10/23/15.
 */
public class ArrowController {
    Viewport viewport;
    public static Stage stage;
    boolean upPressed, downPressed, leftPressed, rightPressed;
    OrthographicCamera cam;

    public ArrowController(){
        cam = new OrthographicCamera();
        viewport = new FitViewport(400, 400, cam);
        stage = new Stage(viewport, SimpleInputClient.batch);

        stage.addListener(new InputListener(){

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                switch(keycode){
                    case Input.Keys.UP:
                        upPressed = true;
                        break;
                    case Input.Keys.DOWN:
                        downPressed = true;
                        break;
                    case Input.Keys.LEFT:
                        leftPressed = true;
                        break;
                    case Input.Keys.RIGHT:
                        rightPressed = true;
                        break;
                }
                return true;
            }

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                switch(keycode){
                    case Input.Keys.UP:
                        upPressed = false;
                        break;
                    case Input.Keys.DOWN:
                        downPressed = false;
                        break;
                    case Input.Keys.LEFT:
                        leftPressed = false;
                        break;
                    case Input.Keys.RIGHT:
                        rightPressed = false;
                        break;
                }
                return true;
            }
        });
        
        Table table = new Table();
        table.center().bottom();

        final Image upImg = new Image(new Texture("up.png"));
        upImg.setSize(75, 75);
        upImg.addListener(new CustomInputListener(upImg, "up"));       

        final Image downImg = new Image(new Texture("down.png"));
        downImg.setSize(75, 75);
        downImg.addListener(new CustomInputListener(downImg, "down"));       


        final Image rightImg = new Image(new Texture("right.png"));
        rightImg.setSize(75, 75);
        rightImg.addListener(new CustomInputListener(rightImg, "right"));       


        final Image leftImg = new Image(new Texture("left.png"));
        leftImg.setSize(75, 75);
        leftImg.addListener(new CustomInputListener(leftImg, "left"));       


        table.add();
        table.add(upImg).size(upImg.getWidth(), upImg.getHeight());
        table.add();
        table.row().pad(5, 5, 5, 5);
        table.add(leftImg).size(leftImg.getWidth(), leftImg.getHeight());
        table.add();
        table.add(rightImg).size(rightImg.getWidth(), rightImg.getHeight());
        table.row().padBottom(5);
        table.add();
        table.add(downImg).size(downImg.getWidth(), downImg.getHeight());
        table.add();

        table.moveBy(Gdx.graphics.getWidth()/2, 0);
        stage.addActor(table);
    }

    public void draw(){
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

    public void resize(int width, int height){
        viewport.update(width, height);
    }
}