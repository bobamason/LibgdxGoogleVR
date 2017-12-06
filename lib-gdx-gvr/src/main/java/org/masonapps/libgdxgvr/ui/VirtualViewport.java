package org.masonapps.libgdxgvr.ui;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.viewport.Viewport;

import org.masonapps.libgdxgvr.GdxVr;

/**
 * Created by Bob on 9/13/2017.
 */

public class VirtualViewport extends Viewport {


    private final Vector3 tmp = new Vector3();
    private Ray ray = new Ray();

    public VirtualViewport() {
        setCamera(new OrthographicCamera());
    }

    @Override
    public void apply(boolean ignored) {
        getCamera().viewportWidth = getWorldWidth();
        getCamera().viewportHeight = getWorldHeight();
        getCamera().position.set(0, 0, 0);
        getCamera().update();
    }

    @Override
    public void update(int screenWidth, int screenHeight, boolean centerCamera) {
        setScreenBounds(0, 0, screenWidth, screenHeight);
        setWorldSize(screenWidth, screenHeight);
        apply(centerCamera);
    }

    @Override
    public Vector2 unproject(Vector2 screenCoords) {
        return screenCoords;
    }

    @Override
    public Vector2 project(Vector2 worldCoords) {
        return worldCoords;
    }

    @Override
    public Vector3 unproject(Vector3 screenCoords) {
        return screenCoords;
    }

    @Override
    public Vector3 project(Vector3 worldCoords) {
        return worldCoords;
    }

    @Override
    public Ray getPickRay(float screenX, float screenY) {
        return ray.set(screenX, screenY, 0, 0, 0, 1);
    }

    @Override
    public void calculateScissors(Matrix4 batchTransform, Rectangle area, Rectangle scissor) {
        scissor.set(0, 0, GdxVr.graphics.getWidth(), GdxVr.graphics.getHeight());
    }

    @Override
    public Vector2 toScreenCoordinates(Vector2 worldCoords, Matrix4 transformMatrix) {
        tmp.set(worldCoords.x, worldCoords.y, 0);
        tmp.mul(transformMatrix);
        tmp.y = getScreenHeight() - tmp.y;
        worldCoords.x = tmp.x;
        worldCoords.y = tmp.y;
        return worldCoords;
    }

    @Override
    public int getLeftGutterWidth() {
        return 0;
    }

    @Override
    public int getRightGutterX() {
        return 0;
    }

    @Override
    public int getRightGutterWidth() {
        return 0;
    }

    @Override
    public int getBottomGutterHeight() {
        return 0;
    }

    @Override
    public int getTopGutterY() {
        return 0;
    }

    @Override
    public int getTopGutterHeight() {
        return 0;
    }
}
