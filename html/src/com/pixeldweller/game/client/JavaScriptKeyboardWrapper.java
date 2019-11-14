package com.pixeldweller.game.client;

import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.pixeldweller.game.clientcontroller.IKeyboardInvoker;

public class JavaScriptKeyboardWrapper implements IKeyboardInvoker{

	@Override
	public void openKeyboard(TextField txtField) {
		JavaScriptKeyboard.openKeyboard(txtField);
	}

}
