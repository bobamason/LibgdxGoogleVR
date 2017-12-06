package org.masonapps.libgdxgvr.gfx;

import android.support.annotation.CallSuper;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.controller.Controller;

import org.masonapps.libgdxgvr.GdxVr;
import org.masonapps.libgdxgvr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgvr.input.DaydreamControllerInputListener;
import org.masonapps.libgdxgvr.input.DaydreamTouchEvent;

/**
 * Created by Bob on 10/9/2016.
 */

public abstract class VrWorldScreen extends VrScreen implements DaydreamControllerInputListener {
    protected Environment environment;
    protected World world;
    private Array<Disposable> disposables = new Array<>();
    private Color backgroundColor = Color.BLACK.cpy();

    public VrWorldScreen(VrGame game) {
        super(game);
        environment = createEnvironment();
        final Array<BaseLight> lights = new Array<>();
        addLights(lights);
        environment.add(lights);
        world = createWorld();
    }

    protected Environment createEnvironment() {
        final Environment environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, Color.DARK_GRAY));
        return environment;
    }

    protected void addLights(Array<BaseLight> lights) {
        final DirectionalLight light = new DirectionalLight();
        light.set(Color.DARK_GRAY, 0.0f, -1.0f, 0.0f);
        lights.add(light);
    }

    protected World createWorld() {
        return new World();
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        super.onNewFrame(headTransform);
        Gdx.gl.glClearColor(backgroundColor.r, backgroundColor.g, backgroundColor.b, backgroundColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    @CallSuper
    public void update() {
        world.update();
    }

    @Override
    @CallSuper
    public void render(Camera camera, int whichEye) {
        getModelBatch().begin(camera);
        world.render(getModelBatch(), environment);
        getModelBatch().end();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    public void runOnGLThread(Runnable runnable) {
        GdxVr.app.postRunnable(runnable);
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {

    }

    @Override
    public void onControllerButtonEvent(Controller controller, DaydreamButtonEvent event) {

    }

    @Override
    public void onControllerTouchPadEvent(Controller controller, DaydreamTouchEvent event) {

    }

    @Override
    public void onControllerConnectionStateChange(int connectionState) {

    }

    @Override
    @CallSuper
    public void dispose() {
        if (disposables != null) {
            for (Disposable d : disposables) {
                try {
                    if (d != null)
                        d.dispose();
                } catch (Exception e) {
                    Log.e(VrWorldScreen.class.getSimpleName(), e.getMessage());
                }
            }
            disposables.clear();
        }
        if (world != null)
            world.dispose();
        world = null;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public ModelBatch getModelBatch() {
        return game.getModelBatch();
    }

    public Array<Disposable> getDisposables() {
        return disposables;
    }

    public World getWorld() {
        return world;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor.set(backgroundColor);
    }

    public void setBackgroundColor(float r, float g, float b, float a) {
        this.backgroundColor.set(r, g, b, a);
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int rgba) {
        this.backgroundColor.set(rgba);
    }

    public void manageDisposable(Disposable... disposables) {
        for (Disposable d : disposables) {
            this.disposables.add(d);
        }
    }

    public void manageDisposable(Disposable disposable) {
        this.disposables.add(disposable);
    }

    public Ray getControllerRay() {
        return game.getControllerRay();
    }
}
