package org.masonapps.libgdxgvr.gfx;

import android.support.annotation.CallSuper;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.google.vr.sdk.audio.GvrAudioEngine;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import org.masonapps.libgdxgvr.GdxVr;
import org.masonapps.libgdxgvr.input.DaydreamControllerInputListener;
import org.masonapps.libgdxgvr.vr.VrCamera;

/**
 * Created by Bob on 12/22/2016.
 */
public abstract class VrScreen implements Disposable, DaydreamControllerInputListener {

    protected VrGame game;

    public VrScreen(VrGame game) {
        this.game = game;
    }

    public abstract void resume();

    public abstract void pause();

    public void onDrawFrame(HeadTransform headTransform, Eye leftEye, Eye rightEye) {
    }

    @CallSuper
    public void show() {
        GdxVr.input.addDaydreamControllerListener(this);
    }

    @CallSuper
    public void hide() {
        GdxVr.input.removeDaydreamControllerListener(this);
    }

    public void update() {
    }

    public void onCardboardTrigger() {
    }

    public void onNewFrame(HeadTransform headTransform) {
    }

    public void onDrawEye(Eye eye) {
    }

    public void render(Camera camera, int whichEye) {
    }

    public Vector3 getForwardVector() {
        return game.getForwardVector();
    }

    public Vector3 getUpVector() {
        return game.getUpVector();
    }

    public Vector3 getRightVector() {
        return game.getRightVector();
    }

    public Vector3 getHeadTranslation() {
        return game.getHeadTranslation();
    }

    public Quaternion getHeadQuaternion() {
        return game.getHeadQuaternion();
    }

    public Matrix4 getHeadMatrix() {
        return game.getHeadMatrix();
    }

    public VrCamera getVrCamera() {
        return game.getVrCamera();
    }

    public GvrAudioEngine getGvrAudioEngine() {
        return game.getGvrAudioEngine();
    }

    protected void doneLoading(AssetManager assets) {}

    public void renderAfterCursor(Camera camera) {
    }

    public void onFinishFrame(Viewport viewport) {
    }
}
