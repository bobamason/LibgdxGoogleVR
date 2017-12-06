package org.masonapps.libgdxgvr.gfx;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Created by Bob on 8/10/2015.
 */
public class World implements Disposable {
    public final Array<Entity> entities = new Array<>();
    protected final ObjectMap<String, Constructor<Entity>> constructors = new ObjectMap<>();
    private final Array<Model> models = new Array<>();

    public void addConstructor(final String name, final Constructor<Entity> constructor) {
        constructors.put(name, constructor);
        if (constructor.model != null && !models.contains(constructor.model, true))
            models.add(constructor.model);
    }

    public Constructor<Entity> getConstructor(final String name) {
        return constructors.get(name);
    }

    public Entity add(final Entity entity) {
        entities.add(entity);
        return entity;
    }

    public Entity add(final String type, final float x, final float y, final float z) {
        final Entity entity = constructors.get(type).construct(x, y, z);
        return add(entity);
    }

    public Entity add(final String type, final Matrix4 transform) {
        final Entity entity = constructors.get(type).construct(transform);
        return add(entity);
    }

    public Entity add(final String type, final Vector3 position) {
        final Entity entity = constructors.get(type).construct(position.x, position.y, position.z);
        return add(entity);
    }

    public void render(final ModelBatch batch, final Environment lights) {
        render(batch, lights, entities);
    }

    public void render(final ModelBatch batch, final Environment lights, final Iterable<Entity> entities) {
        for (final Entity e : entities) {
            render(batch, lights, e);
        }
    }

    public void render(final ModelBatch batch, final Environment lights, final Entity entity) {
        if (!entity.updated) entity.recalculateTransform();
        if (entity.isInCameraFrustum(batch.getCamera())) {
            if (entity.isLightingEnabled()) {
                if (entity.shader != null)
                    batch.render(entity.modelInstance, lights, entity.shader);
                else
                    batch.render(entity.modelInstance, lights);
            } else {
                if (entity.shader != null)
                    batch.render(entity.modelInstance, entity.shader);
                else
                    batch.render(entity.modelInstance);
            }
        }
    }

    public void update() {

    }

    @Override
    public void dispose() {
        for (int i = 0; i < entities.size; i++) {
            entities.get(i).dispose();
        }
        entities.clear();
        for (Constructor<Entity> constructor : constructors.values()) {
            constructor.dispose();
        }
        constructors.clear();
        models.clear();
    }

    public void remove(Entity entity) {
        entities.removeValue(entity, true);
    }

    public static abstract class Constructor<T extends Entity> implements Disposable {
        public final Model model;

        public Constructor(Model model) {
            this.model = model;
        }

        public abstract T construct(final float x, final float y, final float z);

        public abstract T construct(final Matrix4 transform);
    }
}
