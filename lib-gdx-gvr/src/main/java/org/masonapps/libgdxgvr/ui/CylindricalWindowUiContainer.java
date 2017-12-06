package org.masonapps.libgdxgvr.ui;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pools;
import com.google.vr.sdk.controller.Controller;

import org.masonapps.libgdxgvr.input.DaydreamButtonEvent;
import org.masonapps.libgdxgvr.input.DaydreamControllerInputListener;
import org.masonapps.libgdxgvr.input.DaydreamTouchEvent;
import org.masonapps.libgdxgvr.input.VrInputProcessor;
import org.masonapps.libgdxgvr.math.CylindricalCoordinate;

/**
 * Created by Bob Mason on 10/4/2017.
 */

public class CylindricalWindowUiContainer extends VrUiContainer implements DaydreamControllerInputListener, WindowVR.OnDragStartedListener {


    private final CylindricalCoordinate offsetCoord = new CylindricalCoordinate();
    private float radius;
    private float height;
    private boolean isDragging = false;
    @Nullable
    private WindowVR focusedWindow = null;

    public CylindricalWindowUiContainer(float radius, float height) {
        this.radius = radius;
        this.height = height;
    }

    public CylindricalWindowUiContainer(float radius, float height, VrInputProcessor... processors) {
        super(processors);
        this.radius = radius;
        this.height = height;
        for (VrInputProcessor processor : this.processors) {
            if (processor instanceof WindowVR) {
                final WindowVR virtualStage = (WindowVR) processor;
                virtualStage.setOnDragStartedListener(CylindricalWindowUiContainer.this);
                snapDragTableToCylinder(virtualStage);
            }
        }
    }

    @Override
    public void recalculateTransform() {
        if (transformable)
            setTransformable(false);
        super.recalculateTransform();
    }

    @Override
    public void addProcessor(VrInputProcessor processor) {
        super.addProcessor(processor);
        if (processor instanceof WindowVR) {
            final WindowVR tableVR = (WindowVR) processor;
            snapDragTableToCylinder(tableVR);
            tableVR.setOnDragStartedListener(CylindricalWindowUiContainer.this);
        }
    }

    protected void snapDragTableToCylinder(WindowVR windowVR) {
        final Vector3 tmp = Pools.obtain(Vector3.class);
        final Vector3 tmp2 = Pools.obtain(Vector3.class);
        final CylindricalCoordinate cylCoord = Pools.obtain(CylindricalCoordinate.class);

        cylCoord.setFromCartesian(windowVR.getPosition());
        cylCoord.radius = radius;
        cylCoord.vertical = Math.max(-height / 2f, Math.min(height / 2f, cylCoord.vertical));
        windowVR.setPosition(cylCoord.toCartesian(tmp));
        windowVR.lookAt(tmp2.set(0, cylCoord.vertical, 0), Vector3.Y);

        Pools.free(cylCoord);
        Pools.free(tmp);
        Pools.free(tmp2);
    }

    @Override
    public boolean performRayTest(Ray ray) {
        if (isDragging && focusedWindow != null) {
            if (!visible) return false;
            if (!Intersector.intersectRayPlane(ray, focusedWindow.getPlane(), hitPoint3D))
                return false;
            
            final Vector3 tmp = Pools.obtain(Vector3.class);
            final CylindricalCoordinate cylCoord = Pools.obtain(CylindricalCoordinate.class);

            cylCoord.setFromCartesian(hitPoint3D);
            cylCoord.theta += offsetCoord.theta;
            cylCoord.vertical += offsetCoord.vertical;
            cylCoord.radius = radius;

            cylCoord.vertical = Math.max(-height / 2f, Math.min(height / 2f, cylCoord.vertical));
            focusedWindow.setPosition(cylCoord.toCartesian(tmp));
            focusedWindow.lookAt(tmp.set(0, focusedWindow.getPosition().y, 0), Vector3.Y);

            Pools.free(cylCoord);
            Pools.free(tmp);
            return true;
        } else
            return super.performRayTest(ray);
    }

    @Override
    public void onDaydreamControllerUpdate(Controller controller, int connectionState) {
    }

    @Override
    public void onControllerButtonEvent(Controller controller, DaydreamButtonEvent event) {
        if (isDragging && event.button == DaydreamButtonEvent.BUTTON_TOUCHPAD && event.action == DaydreamButtonEvent.ACTION_UP) {
            isDragging = false;
            focusedWindow = null;
        }
    }

    @Override
    public void onControllerTouchPadEvent(Controller controller, DaydreamTouchEvent event) {
    }

    @Override
    public void onControllerConnectionStateChange(int connectionState) {
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    @Override
    public void onDragStarted(@NonNull WindowVR windowVR) {
        isDragging = true;
        focusedWindow = windowVR;
        final CylindricalCoordinate cylCoord = Pools.obtain(CylindricalCoordinate.class);
        final CylindricalCoordinate hitCoord = Pools.obtain(CylindricalCoordinate.class);
        cylCoord.setFromCartesian(windowVR.getPosition());
        hitCoord.setFromCartesian(hitPoint3D);
        offsetCoord.radius = radius;
        offsetCoord.theta = cylCoord.theta - hitCoord.theta;
        offsetCoord.vertical = cylCoord.vertical - hitCoord.vertical;
        Pools.free(hitCoord);
        Pools.free(cylCoord);

    }

    @Override
    public void moveToFront(@NonNull WindowVR windowVR) {
        //move to front
        if (processors.size() <= 1) return;
        processors.remove(windowVR);
        processors.add(processors.size() - 1, windowVR);
    }
}
