package com.pixeldweller.game.desktop;

import java.io.File;

import com.pixeldweller.game.util.ISpeaker;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

/**
 * 
 * @author Manindar
 */
public class SpeechUtils implements ISpeaker {

	VoiceManager freettsVM;
	Voice freettsVoice;

	public void init(String voiceName) {
		// Most important part!
		// D:\USER\Desktop\TritonRepo\mbrola_test\text to speech mbrola\mbrolas
		String absolutePath = new File("mbrola").getAbsolutePath();
		//absolutePath = "D:/Programme/#Dev/eclipse_gettogether/GitTogether/GetTogetherPseudoPartyGame/desktop/mbrola";
		absolutePath = absolutePath.replaceAll("\\\\", "/");
		System.setProperty("mbrola.base", absolutePath);
		freettsVM = VoiceManager.getInstance();

		Voice[] voices = freettsVM.getVoices();

		// Simply change to MBROLA voice
		freettsVoice = freettsVM.getVoice(voiceName);

		freettsVoice.setPitch(130);
		freettsVoice.setRate(115);
		// Allocate your chosen voice
		freettsVoice.allocate();
	}

	public void terminate() {
		freettsVoice.deallocate();
	}

	public void doSpeak(String speakText) {
		try {
			freettsVoice.speak(speakText);
		} catch (Exception e) {

		}
	}

	public static void main(String[] args) throws Exception {
		SpeechUtils su = new SpeechUtils();
		su.init("kevin16");
		// su.init("kevin");
		// su.init("mbrola_us1");
		// su.init("mbrola_us2");
		su.init("mbrola_us1");
		// high quality
		su.doSpeak("Hello fellow players... this is just like JackBoxParty");
		su.terminate();
	}
}