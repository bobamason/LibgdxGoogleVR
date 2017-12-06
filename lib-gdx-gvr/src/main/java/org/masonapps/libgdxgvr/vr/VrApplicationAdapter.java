package org.masonapps.libgdxgvr.vr;

import android.support.annotation.CallSuper;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;
import com.google.vr.sdk.audio.GvrAudioEngine;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import org.masonapps.libgdxgvr.GdxVr;

/**
 * Created by Bob on 12/15/2016.
 */

public abstract class VrApplicationAdapter implements ApplicationListener {
    protected VrCamera vrCamera;

    public VrApplicationAdapter() {
        vrCamera = new VrCamera();
    }

    @Override
    public void create() {
        
    }

    public abstract void preloadSoundFiles(GvrAudioEngine gvrAudioEngine);

    /**
     * called once at the beginning of each frame
     */
    public abstract void update();

    /**
     * called for each eye
     * @param camera
     * @param whichEye
     */
    public abstract void render(Camera camera, int whichEye);

    @Override
    public void resume() {

    }

    @Override
    public void pause() {
        
    }

    @CallSuper
    public void onDrawFrame(HeadTransform headTransform, Eye leftEye, Eye rightEye) {
        onNewFrame(headTransform);
        updateGvrAudioEngine();
        if (leftEye != null)
            onDrawEye(leftEye);
        if (rightEye != null)
            onDrawEye(rightEye);
    }

    protected void updateGvrAudioEngine() {
        final GvrAudioEngine gvrAudioEngine = getGvrAudioEngine();
        final Vector3 translation = getHeadTranslation();
        final VrCamera cam = getVrCamera();
        final Quaternion cameraRotation = Pools.obtain(Quaternion.class);
        final Quaternion rotation = Pools.obtain(Quaternion.class);
        final Vector3 tmp = Pools.obtain(Vector3.class);
        final Vector3 tmp2 = Pools.obtain(Vector3.class);

        gvrAudioEngine.setHeadPosition(translation.x + cam.position.x, translation.y + cam.position.y, translation.z + cam.position.z);
        final Vector3 dir = cam.direction;
        final Vector3 up = cam.up;
        tmp.set(up).crs(dir).nor();
        tmp2.set(dir).crs(tmp).nor();
        cameraRotation.setFromAxes(tmp.x, tmp2.x, dir.x, tmp.y, tmp2.y, dir.y, tmp.z, tmp2.z, dir.z);
        rotation.set(getHeadQuaternion()).mulLeft(cameraRotation);
        gvrAudioEngine.setHeadRotation(rotation.x, rotation.y, rotation.z, rotation.w);
        gvrAudioEngine.update();

        Pools.free(tmp);
        Pools.free(tmp2);
        Pools.free(rotation);
        Pools.free(cameraRotation);
    }

    @CallSuper
    public void onNewFrame(HeadTransform headTransform) {
        update();
    }

    @CallSuper
    public void onDrawEye(Eye eye) {
        final Viewport viewport = eye.getViewport();
        GdxVr.gl.glViewport(viewport.x, viewport.y, viewport.width, viewport.height);
        vrCamera.onDrawEye(eye);
        render(vrCamera, eye.getType());
    }

    public void onCardboardTrigger() {
    }

    @Override
    @CallSuper
    public void render() {
        throw new UnsupportedOperationException("render() is not supported, call onNewFrame() and onDrawEye() instead");
    }

    @Override
    @CallSuper
    public void resize(int width, int height) {
        throw new UnsupportedOperationException("resize() is not supported");
    }

    @Override
    @CallSuper
    public void dispose() {
    }

    public Vector3 getForwardVector() {
        return GdxVr.graphics.getForward();
    }

    public Vector3 getUpVector() {
        return GdxVr.graphics.getUp();
    }

    public Vector3 getRightVector() {
        return GdxVr.graphics.getRight();
    }

    public Vector3 getHeadTranslation() {
        return GdxVr.graphics.getHeadTranslation();
    }

    public Quaternion getHeadQuaternion() {
        return GdxVr.graphics.getHeadQuaternion();
    }

    public Matrix4 getHeadMatrix() {
        return GdxVr.graphics.getHeadMatrix();
    }

    public VrCamera getVrCamera() {
        return vrCamera;
    }

    public void onFinishFrame(Viewport viewport) {

    }

    public GvrAudioEngine getGvrAudioEngine() {
        return GdxVr.audio.getGvrAudioEngine();
    }
}
