package org.masonapps.libgdxgvr.ui;

import android.support.annotation.Nullable;
import android.util.Log;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pools;

import org.masonapps.libgdxgvr.input.VrInputProcessor;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Bob on 3/15/2017.
 */

public class VrUiContainer implements VrInputProcessor, Disposable {
    protected final ArrayList<VrInputProcessor> processors;
    protected final Vector3 position = new Vector3();
    protected final Quaternion rotation = new Quaternion();
    protected final Quaternion rotator = new Quaternion();
    protected final Matrix4 transform = new Matrix4();
    protected final Matrix4 globalTransform = new Matrix4();
    protected final Matrix4 invTransform = new Matrix4();
    protected final Ray transformedRay = new Ray();
    protected boolean isCursorOver = false;
    protected Vector2 hitPoint2DPixels = new Vector2();
    protected Vector3 hitPoint3D = new Vector3();
    protected boolean updated = false;
    protected boolean transformable = false;
    @Nullable
    protected VrInputProcessor focusedProcessor;
    protected boolean visible = true;

    public VrUiContainer() {
        processors = new ArrayList<>();
    }

    public VrUiContainer(VrInputProcessor... processors) {
        this();
        Collections.addAll(this.processors, processors);
    }

    public void setRotationX(float angle) {
        rotation.set(Vector3.X, angle);
        invalidate();
    }

    public void setRotationY(float angle) {
        rotation.set(Vector3.Y, angle);
        invalidate();
    }

    public void setRotationZ(float angle) {
        rotation.set(Vector3.Z, angle);
        invalidate();
    }

    public void rotateX(float angle) {
        rotator.set(Vector3.X, angle);
        rotation.mul(rotator);
        invalidate();
    }

    public void rotateY(float angle) {
        rotator.set(Vector3.Y, angle);
        rotation.mul(rotator);
        invalidate();
    }

    public void rotateZ(float angle) {
        rotator.set(Vector3.Z, angle);
        rotation.mul(rotator);
        invalidate();
    }

    public void setRotation(float yaw, float pitch, float roll) {
        rotation.setEulerAngles(yaw, pitch, roll);
        invalidate();
    }

    public void invalidate() {
        updated = false;
        transformable = true;
    }

    public void setRotation(Vector3 dir, Vector3 up) {
        final Vector3 tmp = Pools.obtain(Vector3.class);
        final Vector3 tmp2 = Pools.obtain(Vector3.class);
        tmp.set(up).crs(dir).nor();
        tmp2.set(dir).crs(tmp).nor();
        rotation.setFromAxes(tmp.x, tmp2.x, dir.x, tmp.y, tmp2.y, dir.y, tmp.z, tmp2.z, dir.z);
        invalidate();
        Pools.free(tmp);
        Pools.free(tmp2);
    }

    public void lookAt(Vector3 position, Vector3 up) {
        final Vector3 dir = Pools.obtain(Vector3.class);
        dir.set(position).sub(this.position).nor();
        setRotation(dir, up);
        Pools.free(dir);
    }

    public Quaternion getRotation() {
        return rotation;
    }

    public void setRotation(Quaternion q) {
        rotation.set(q);
        invalidate();
    }

    public void translateX(float units) {
        this.position.x += units;
        invalidate();
    }

    public float getX() {
        return this.position.x;
    }

    public void setX(float x) {
        this.position.x = x;
        invalidate();
    }

    public void translateY(float units) {
        this.position.y += units;
        invalidate();
    }

    public float getY() {
        return this.position.y;
    }

    public void setY(float y) {
        this.position.y = y;
        invalidate();
    }

    public void translateZ(float units) {
        this.position.z += units;
        invalidate();
    }

    public float getZ() {
        return this.position.z;
    }

    public void setZ(float z) {
        this.position.z = z;
        invalidate();
    }

    public void translate(float x, float y, float z) {
        this.position.add(x, y, z);
        invalidate();
    }

    public void translate(Vector3 trans) {
        this.position.add(trans);
        invalidate();
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        invalidate();
    }

    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(Vector3 pos) {
        this.position.set(pos);
        invalidate();
    }

    public void recalculateTransform() {
        if (transformable) {
            transform.idt().set(position, rotation);
            try {
                invTransform.set(transform).inv();
            } catch (Exception e) {
                Log.e(VrUiContainer.class.getName(), e.getLocalizedMessage());
            }
        }
        updated = true;
    }

    @Override
    public boolean performRayTest(Ray ray) {
        if (!visible) return false;
        if (!updated && transformable) recalculateTransform();
        VrInputProcessor newFocusedProcessor = null;
        isCursorOver = false;
        if (transformable)
            transformedRay.set(ray).mul(invTransform);
        else
            transformedRay.set(ray);
        for (int i = processors.size() - 1; i >= 0; i--) {
            final VrInputProcessor inputProcessor = processors.get(i);
            if (inputProcessor.performRayTest(transformedRay)) {
                isCursorOver = true;

                if (inputProcessor.getHitPoint2D() != null)
                    hitPoint2DPixels.set(inputProcessor.getHitPoint2D());

                if (inputProcessor.getHitPoint3D() != null) {
                    if (transformable)
                        hitPoint3D.set(inputProcessor.getHitPoint3D()).mul(transform);
                    else
                        hitPoint3D.set(inputProcessor.getHitPoint3D());
                }

                newFocusedProcessor = inputProcessor;
                break;
            }
        }
        if (focusedProcessor != null && focusedProcessor != newFocusedProcessor) {
            if (focusedProcessor.getHitPoint2D() != null)
                focusedProcessor.touchUp(Math.round(focusedProcessor.getHitPoint2D().x), Math.round(focusedProcessor.getHitPoint2D().y), 0, 0);
            else
                focusedProcessor.touchUp(0, 0, 0, 0);

            if (focusedProcessor instanceof VirtualStage)
                ((VirtualStage) focusedProcessor).isCursorOver = false;
            else if (focusedProcessor instanceof VrUiContainer)
                ((VrUiContainer) focusedProcessor).isCursorOver = false;
        }

        focusedProcessor = newFocusedProcessor;
        return isCursorOver;
    }

    public void act() {
        if (!visible) return;
        if (!updated) recalculateTransform();
        for (VrInputProcessor processor : processors) {
            if (processor instanceof VirtualStage)
                ((VirtualStage) processor).act();
            if (processor instanceof VrUiContainer)
                ((VrUiContainer) processor).act();
        }
    }

    public void draw(Camera camera) {
        draw(camera, null);
    }

    public void draw(Camera camera, @Nullable Matrix4 parentTransform) {
        if (!visible) return;
        if (!updated) recalculateTransform();
        globalTransform.set(transform);
        if (parentTransform != null)
            globalTransform.mulLeft(parentTransform);
        for (VrInputProcessor processor : processors) {
            if (processor instanceof VirtualStage)
                ((VirtualStage) processor).draw(camera, globalTransform);
            if (processor instanceof VrUiContainer)
                ((VrUiContainer) processor).draw(camera, globalTransform);
        }
    }

    @Override
    public Vector2 getHitPoint2D() {
        return hitPoint2DPixels;
    }

    @Override
    public Vector3 getHitPoint3D() {
        return hitPoint3D;
    }

    @Override
    public boolean isCursorOver() {
        return isCursorOver && visible;
    }

    public void addProcessor(VrInputProcessor processor) {
        processors.add(processor);
    }

    public void removeProcessor(VirtualStage stage) {
        processors.remove(stage);
    }

    public void clearProcessors() {
        processors.clear();
    }

    @Override
    public boolean keyDown(int keycode) {
        return focusedProcessor != null && focusedProcessor.keyDown(keycode);
    }

    @Override
    public boolean keyUp(int keycode) {
        return focusedProcessor != null && focusedProcessor.keyUp(keycode);
    }

    @Override
    public boolean keyTyped(char character) {
        return focusedProcessor != null && focusedProcessor.keyTyped(character);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return focusedProcessor != null && focusedProcessor.touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return focusedProcessor != null && focusedProcessor.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return focusedProcessor != null && focusedProcessor.touchDragged(screenX, screenY, pointer);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return focusedProcessor != null && focusedProcessor.mouseMoved(screenX, screenY);
    }

    @Override
    public boolean scrolled(int amount) {
        return focusedProcessor != null && focusedProcessor.scrolled(amount);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isTransformable() {
        return transformable;
    }

    public void setTransformable(boolean transformable) {
        this.transformable = transformable;
    }

    @Override
    public void dispose() {
        for (VrInputProcessor processor : processors) {
            if (processor instanceof Disposable)
                ((Disposable) processor).dispose();
        }
        clearProcessors();
    }

    public void setAlpha(float alpha) {
        for (VrInputProcessor processor : processors) {
            if (processor instanceof VirtualStage)
                ((VirtualStage) processor).setAlpha(alpha);
            if (processor instanceof VrUiContainer)
                ((VrUiContainer) processor).setAlpha(alpha);
        }
    }
}
