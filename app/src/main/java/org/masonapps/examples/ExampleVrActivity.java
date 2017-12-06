package org.masonapps.examples;

import android.os.Bundle;

import com.badlogic.gdx.graphics.Camera;
import com.google.vr.sdk.audio.GvrAudioEngine;

import org.masonapps.libgdxgvr.vr.VrActivity;
import org.masonapps.libgdxgvr.vr.VrApplicationAdapter;

public class ExampleVrActivity extends VrActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize(new BasicExampleAdapter());
    }

    private class BasicExampleAdapter extends VrApplicationAdapter {

        @Override
        public void create() {

        }
        
        @Override
        public void preloadSoundFiles(GvrAudioEngine gvrAudioEngine) {
            
        }

        @Override
        public void update() {

        }

        @Override
        public void render(Camera camera, int whichEye) {

        }
    }
}
