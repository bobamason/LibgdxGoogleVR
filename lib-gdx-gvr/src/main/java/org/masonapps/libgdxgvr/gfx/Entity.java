package org.masonapps.libgdxgvr.gfx;

import android.support.annotation.Nullable;
import android.util.Log;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pools;

/**
 * Created by Bob on 8/10/2015.
 */
public class Entity implements Disposable {
    protected final Vector3 position = new Vector3();
    protected final Quaternion rotation = new Quaternion();
    protected final Matrix4 inverseTransform = new Matrix4();
    protected final Vector3 scale = new Vector3(1f, 1f, 1f);
    private final Vector3 dimensions = new Vector3();
    private final Vector3 center = new Vector3();
    private final float radius;
    public ModelInstance modelInstance;
    @Nullable
    protected BaseShader shader = null;
    protected boolean updated = false;
    private BoundingBox bounds = new BoundingBox();
    private boolean visible = true;
    private boolean lightingEnabled = true;

    public Entity(ModelInstance modelInstance) {
        this.modelInstance = modelInstance;
        setTransform(modelInstance.transform);
        bounds.inf();
        for (Node node : modelInstance.nodes) {
            node.extendBoundingBox(bounds, false);
        }
        bounds.getDimensions(dimensions);
        bounds.getCenter(center);
        radius = dimensions.len() / 2f;
    }

    public Entity(ModelInstance modelInstance, BoundingBox bounds) {
        this.modelInstance = modelInstance;
        setTransform(modelInstance.transform);
        this.bounds.set(bounds);
        this.bounds.getDimensions(dimensions);
        this.bounds.getCenter(center);
        radius = dimensions.len() / 2f;
    }

    public boolean isInCameraFrustum(Camera camera) {
        if (!visible) return false;
        if (!updated) recalculateTransform();
        final Vector3 tmp = Pools.obtain(Vector3.class);
        final boolean inFrustum = camera.frustum.sphereInFrustum(tmp.set(position).add(center), radius * Math.max(scale.x, Math.max(scale.y, scale.z)));
        Pools.free(tmp);
        return inFrustum;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isLightingEnabled() {
        return lightingEnabled;
    }

    public void setLightingEnabled(boolean lightingEnabled) {
        this.lightingEnabled = lightingEnabled;
    }

    @Nullable
    public BaseShader getShader() {
        return shader;
    }

    public void setShader(@Nullable BaseShader shader) {
        this.shader = shader;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public float getRadius() {
        return radius;
    }

    public Vector3 getCenter() {
        return center;
    }

    public Vector3 getDimensions() {
        return dimensions;
    }

    @Override
    public void dispose() {
        if (shader != null)
            shader.dispose();
        shader = null;
    }

    public boolean intersectsRayBoundsFast(Ray ray) {
        if (!updated) recalculateTransform();
        final Ray tmpRay = Pools.obtain(Ray.class);

        tmpRay.set(ray).mul(inverseTransform);
        final boolean intersectRayBoundsFast = Intersector.intersectRayBoundsFast(tmpRay, bounds);

        Pools.free(tmpRay);
        return intersectRayBoundsFast;
    }

    public boolean intersectsRayBounds(Ray ray, @Nullable Vector3 hitPoint) {
        if (!updated) recalculateTransform();
        final Ray tmpRay = Pools.obtain(Ray.class);

        tmpRay.set(ray).mul(inverseTransform);
        final boolean intersectRayBounds = Intersector.intersectRayBounds(tmpRay, bounds, hitPoint);
        if (intersectRayBounds && hitPoint != null) hitPoint.mul(modelInstance.transform);

        Pools.free(tmpRay);
        return intersectRayBounds;
    }

    public boolean intersectsRaySphere(Ray ray, @Nullable Vector3 hitPoint) {
        if (!updated) recalculateTransform();
        final Ray tmpRay = Pools.obtain(Ray.class).set(ray);
        final Vector3 tmp = Pools.obtain(Vector3.class);
//        tmpRay.mul(inverseTransform);
//        tmp.set(center).add(position);
        tmp.set(position);
        final boolean intersectRaySphere = Intersector.intersectRaySphere(tmpRay, tmp, radius * Math.min(scale.x, Math.min(scale.y, scale.z)), hitPoint);
//        if (intersectRaySphere && hitPoint != null) hitPoint.mul(modelInstance.transform);

//        Pools.free(tmpRay);
        Pools.free(tmp);
        return intersectRaySphere;
    }

    public Matrix4 getTransform(Matrix4 out) {
        return out.set(modelInstance.transform);
    }

    public Entity setScale(float x, float y, float z) {
        scale.set(x, y, z);
        invalidate();
        return this;
    }

    public Entity setScale(float scale) {
        this.scale.set(scale, scale, scale);
        invalidate();
        return this;
    }

    public Entity scaleX(float x) {
        scale.x *= x;
        invalidate();
        return this;
    }

    public Entity scaleY(float y) {
        scale.y *= y;
        invalidate();
        return this;
    }

    public Entity scaleZ(float z) {
        scale.z *= z;
        invalidate();
        return this;
    }

    public Entity scale(float s) {
        scale.scl(s, s, s);
        invalidate();
        return this;
    }

    public Entity scale(float x, float y, float z) {
        scale.scl(x, y, z);
        invalidate();
        return this;
    }

    public float getScaleX() {
        return this.scale.x;
    }

    public Entity setScaleX(float x) {
        scale.x = x;
        invalidate();
        return this;
    }

    public float getScaleY() {
        return this.scale.y;
    }

    public Entity setScaleY(float y) {
        scale.y = y;
        invalidate();
        return this;
    }

    public float getScaleZ() {
        return this.scale.z;
    }

    public Entity setScaleZ(float z) {
        scale.z = z;
        invalidate();
        return this;
    }

    public Entity setRotationX(float angle) {
        rotation.set(Vector3.X, angle);
        invalidate();
        return this;
    }

    public Entity setRotationY(float angle) {
        rotation.set(Vector3.Y, angle);
        invalidate();
        return this;
    }

    public Entity setRotationZ(float angle) {
        rotation.set(Vector3.Z, angle);
        invalidate();
        return this;
    }

    public Entity rotateX(float angle) {
        final Quaternion rotator = Pools.obtain(Quaternion.class);
        rotator.set(Vector3.X, angle);
        rotation.mul(rotator);
        Pools.free(rotator);
        invalidate();
        return this;
    }

    public Entity rotateY(float angle) {
        final Quaternion rotator = Pools.obtain(Quaternion.class);
        rotator.set(Vector3.Y, angle);
        rotation.mul(rotator);
        Pools.free(rotator);
        invalidate();
        return this;
    }

    public Entity rotateZ(float angle) {
        final Quaternion rotator = Pools.obtain(Quaternion.class);
        rotator.set(Vector3.Z, angle);
        rotation.mul(rotator);
        Pools.free(rotator);
        invalidate();
        return this;
    }

    public Entity setRotation(float yaw, float pitch, float roll) {
        rotation.setEulerAngles(yaw, pitch, roll);
        invalidate();
        return this;
    }

    public Entity setRotation(Vector3 dir, Vector3 up) {
        final Vector3 tmp = Pools.obtain(Vector3.class);
        final Vector3 tmp2 = Pools.obtain(Vector3.class);
        tmp.set(up).crs(dir).nor();
        tmp2.set(dir).crs(tmp).nor();
        rotation.setFromAxes(tmp.x, tmp2.x, dir.x, tmp.y, tmp2.y, dir.y, tmp.z, tmp2.z, dir.z);
        invalidate();
        Pools.free(tmp);
        Pools.free(tmp2);
        return this;
    }

    public Entity lookAt(Vector3 position, Vector3 up) {
        final Vector3 dir = Pools.obtain(Vector3.class);
        dir.set(position).sub(this.position).nor();
        setRotation(dir, up);
        Pools.free(dir);
        return this;
    }

    public Quaternion getRotation() {
        return rotation;
    }

    public Entity setRotation(Quaternion q) {
        rotation.set(q);
        invalidate();
        return this;
    }

    public Entity translateX(float units) {
        this.position.x += units;
        invalidate();
        return this;
    }

    public float getX() {
        return this.position.x;
    }

    public Entity setX(float x) {
        this.position.x = x;
        invalidate();
        return this;
    }

    public Entity translateY(float units) {
        this.position.y += units;
        invalidate();
        return this;
    }

    public float getY() {
        return this.position.y;
    }

    public Entity setY(float y) {
        this.position.y = y;
        invalidate();
        return this;
    }

    public Entity translateZ(float units) {
        this.position.z += units;
        invalidate();
        return this;
    }

    public float getZ() {
        return this.position.z;
    }

    public Entity setZ(float z) {
        this.position.z = z;
        invalidate();
        return this;
    }

    public Entity translate(float x, float y, float z) {
        this.position.add(x, y, z);
        invalidate();
        return this;
    }

    public Entity translate(Vector3 trans) {
        this.position.add(trans);
        invalidate();
        return this;
    }

    public Entity setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        invalidate();
        return this;
    }

    public Vector3 getPosition() {
        return position;
    }

    public Entity setPosition(Vector3 pos) {
        this.position.set(pos);
        invalidate();
        return this;
    }

    public void invalidate() {
        updated = false;
    }

    public void recalculateTransform() {
        modelInstance.transform.set(position, rotation, scale);
        try {
            inverseTransform.set(modelInstance.transform).inv();
        } catch (Exception e) {
            inverseTransform.idt();
            Log.e(Entity.class.getName(), e.getLocalizedMessage());
        }
        updated = true;
    }

    public Entity setTransform(Matrix4 transform) {
        modelInstance.transform.set(transform);
        modelInstance.transform.getTranslation(position);
        modelInstance.transform.getRotation(rotation);
        modelInstance.transform.getScale(scale);
        try {
            inverseTransform.set(modelInstance.transform).inv();
        } catch (Exception e) {
            e.printStackTrace();
        }
        updated = true;
        return this;
    }

    public Matrix4 getInverseTransform(Matrix4 out) {
        return out.set(inverseTransform);
    }

    public static class EntityConstructor extends World.Constructor<Entity> {

        public EntityConstructor(Model model) {
            super(model);
        }

        @Override
        public Entity construct(float x, float y, float z) {
            return new Entity(new ModelInstance(model, x, y, z));
        }

        @Override
        public Entity construct(Matrix4 transform) {
            return new Entity(new ModelInstance(model, transform));
        }

        @Override
        public void dispose() {
            try {
                model.dispose();
            } catch (Exception ignored) {
            }
        }
    }
}
