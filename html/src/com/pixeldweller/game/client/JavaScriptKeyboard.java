package com.pixeldweller.game.client;

import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.pixeldweller.game.clientcontroller.IKeyboardInvoker;

public class JavaScriptKeyboard extends JavaScriptObject {

	private static TextField lastTxtField;

	protected JavaScriptKeyboard() {
	}

	public static final void openKeyboard(TextField txtField) {
		try {
			JavaScriptKeyboard.lastTxtField = txtField;
			JavaScriptKeyboard.openPrompt();
			GWT.log("Got it!", null);
		} catch (Exception e) {
			GWT.log("JSNI method badExample() threw an exception:", e);
		}
	}

	public static final native void openPrompt() /*-{
		var instance = this;	 		
		instance.@com.pixeldweller.game.client.JavaScriptKeyboard::onInput(Ljava/lang/String;)(prompt());	
	}-*/;

	public final void onInput(String txt) {
		if (lastTxtField != null) {
			lastTxtField.setText(txt);
		}
	}

}
