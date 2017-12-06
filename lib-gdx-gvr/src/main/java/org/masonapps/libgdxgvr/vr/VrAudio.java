package org.masonapps.libgdxgvr.vr;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.google.vr.sdk.audio.GvrAudioEngine;

/**
 * Created by Bob Mason on 10/20/2017.
 */

public class VrAudio implements Audio {

    private GvrAudioEngine gvrAudioEngine;

    public VrAudio(GvrAudioEngine gvrAudioEngine) {
        this.gvrAudioEngine = gvrAudioEngine;
    }

    protected void pause() {
        gvrAudioEngine.pause();
    }

    protected void resume() {
        gvrAudioEngine.resume();
    }

//    protected void dispose(){
//        gvrAudioEngine = null;
//    }

    @Override
    public AudioDevice newAudioDevice(int samplingRate, boolean isMono) {
        throw new UnsupportedOperationException("not supported with GvrAudioEngine");
    }

    @Override
    public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono) {
        throw new UnsupportedOperationException("not supported with GvrAudioEngine");
    }

    @Override
    public Sound newSound(FileHandle fileHandle) {
        throw new UnsupportedOperationException("override VrApplicationAdapter.preloadSoundFiles() and preload assets in the GvrAudioEngine");
    }

    @Override
    public Music newMusic(FileHandle file) {
        throw new UnsupportedOperationException("override VrApplicationAdapter.preloadSoundFiles() or use getGvrAudioEngine().createSoundField(String filename)");
    }

    public GvrAudioEngine getGvrAudioEngine() {
        return gvrAudioEngine;
    }
}
