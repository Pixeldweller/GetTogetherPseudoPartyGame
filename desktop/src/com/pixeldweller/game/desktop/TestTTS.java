package com.pixeldweller.game.desktop;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class TestTTS {
    VoiceManager freettsVM;
    Voice freettsVoice;

    public TestTTS(String words) {
        // Most important part!
    	//D:\USER\Desktop\TritonRepo\mbrola_test\text to speech mbrola\mbrola
    	
    	//ATTETION
    	System.setProperty("mbrola.base", "D:/USER/Desktop/TritonRepo/mbrola_test/text to speech mbrola/mbrola");       
        freettsVM = VoiceManager.getInstance();
        
        Voice[] voices = freettsVM.getVoices();

        // Simply change to MBROLA voice
        freettsVoice = freettsVM.getVoice("mbrola_us1");

        // Allocate your chosen voice
        freettsVoice.allocate();
        sayWords(words);
    }

    public void sayWords(String words) {
        // Make her speak!
        freettsVoice.speak(words);
    }

    public static void main(String [] args) {
        new TestTTS("This is weird, why AM I Stuttering?");
    }
}