package org.masonapps.libgdxgvr.bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionWorld;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.PerformanceCounter;

import org.masonapps.libgdxgvr.gfx.Entity;
import org.masonapps.libgdxgvr.gfx.World;

/**
 * Created by Bob on 8/10/2015.
 */
public class BulletWorld extends World {

    public final btCollisionConfiguration collisionConfiguration;
    public final btCollisionDispatcher dispatcher;
    public final btBroadphaseInterface broadphase;
    public final btConstraintSolver solver;
    public final btCollisionWorld collisionWorld;
    public final Vector3 gravity;
    protected final ObjectMap<String, Constructor<BulletEntity>> bulletConstructors = new ObjectMap<>();
    public DebugDrawer debugDrawer = null;
    public boolean renderMeshes = true;
    public PerformanceCounter performanceCounter;
    public int maxSubSteps = 3;
    public float fixedTimeStep = 1f / 60f;

    public BulletWorld(final btCollisionConfiguration collisionConfiguration, final btCollisionDispatcher dispatcher, final btBroadphaseInterface broadphase, final btConstraintSolver solver, final btCollisionWorld collisionWorld, final Vector3 gravity) {
        this.collisionConfiguration = collisionConfiguration;
        this.dispatcher = dispatcher;
        this.broadphase = broadphase;
        this.solver = solver;
        this.collisionWorld = collisionWorld;
        if (collisionWorld instanceof btDynamicsWorld)
            ((btDynamicsWorld) collisionWorld).setGravity(gravity);
        this.gravity = gravity;
    }

    public BulletWorld(final btCollisionConfiguration collisionConfiguration, final btCollisionDispatcher dispatcher, final btBroadphaseInterface broadphase, final btConstraintSolver solver, final btCollisionWorld collisionWorld) {
        this(collisionConfiguration, dispatcher, broadphase, solver, collisionWorld, new Vector3(0f, -10f, 0f));
    }

    public BulletWorld(Vector3 gravity) {
        collisionConfiguration = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfiguration);
        broadphase = new btDbvtBroadphase();
        solver = new btSequentialImpulseConstraintSolver();
        collisionWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        ((btDynamicsWorld) collisionWorld).setGravity(gravity);
        this.gravity = gravity;
    }

    public BulletWorld() {
        this(new Vector3(0f, -10f, 0f));
    }
    
    public BulletEntity add(BulletEntity entity) {
        super.add(entity);
        if (entity.body != null) {
            if (entity.body instanceof btRigidBody) {
                ((btDiscreteDynamicsWorld) collisionWorld).addRigidBody((btRigidBody) entity.body);
            } else {
                collisionWorld.addCollisionObject(entity.body);
            }
            entity.body.setUserValue(entities.size - 1);
        }
        return entity;
    }

    public BulletEntity add(BulletEntity entity, short group, short mask) {
        super.add(entity);
        if (entity.body != null) {
            if (entity.body instanceof btRigidBody) {
                ((btDiscreteDynamicsWorld) collisionWorld).addRigidBody((btRigidBody) entity.body, group, mask);
            } else {
                collisionWorld.addCollisionObject(entity.body, group, mask);
            }
            entity.body.setUserValue(entities.size - 1);
        }
        return entity;
    }

    public BulletEntity add(String type, Vector3 position, short group, short mask) {
        final BulletEntity entity = bulletConstructors.get(type).construct(position.x, position.y, position.z);
        add(entity, group, mask);
        return entity;
    }

    public BulletEntity add(String type, Matrix4 transform, short group, short mask) {
        final BulletEntity entity = bulletConstructors.get(type).construct(transform);
        add(entity, group, mask);
        return entity;
    }

    public BulletEntity add(String type, float x, float y, float z, short group, short mask) {
        final BulletEntity entity = bulletConstructors.get(type).construct(x, y, z);
        add(entity, group, mask);
        return entity;
    }

    @Override
    public void update() {
        if (performanceCounter != null) {
            performanceCounter.tick();
            performanceCounter.start();
        }
        if (collisionWorld instanceof btDynamicsWorld) {
            ((btDynamicsWorld) collisionWorld).stepSimulation(Gdx.graphics.getDeltaTime(), maxSubSteps, fixedTimeStep);
        }
        if (performanceCounter != null) performanceCounter.stop();
    }

    @Override
    public void render(ModelBatch batch, Environment lights, Iterable<Entity> entities) {
        if (renderMeshes) super.render(batch, lights, entities);
        if (debugDrawer != null && debugDrawer.getDebugMode() > 0) {
            batch.flush();
            debugDrawer.begin(batch.getCamera());
            collisionWorld.debugDrawWorld();
            debugDrawer.end();
        }
    }

    @Override
    public void dispose() {
        clearEntities();
        super.dispose();
        collisionWorld.dispose();
        if (solver != null) solver.dispose();
        if (broadphase != null) broadphase.dispose();
        if (dispatcher != null) dispatcher.dispose();
        if (collisionConfiguration != null) collisionConfiguration.dispose();
    }

    public void clearEntities() {
        for (int i = 0; i < entities.size; i++) {
            final Entity e = entities.get(i);
            if (e instanceof BulletEntity) {
                btCollisionObject body = ((BulletEntity)e).body;
                if (body != null) {
                    if (body instanceof btRigidBody)
                        ((btDynamicsWorld) collisionWorld).removeRigidBody((btRigidBody) body);
                    else
                        collisionWorld.removeCollisionObject(body);
                }
            }
            e.dispose();
        }
        entities.clear();
    }

    public int getDebugMode() {
        return debugDrawer == null ? 0 : debugDrawer.getDebugMode();
    }

    public void setDebugMode(final int mode) {
        if (mode == btIDebugDraw.DebugDrawModes.DBG_NoDebug && debugDrawer == null) return;
        if (debugDrawer == null) collisionWorld.setDebugDrawer(debugDrawer = new DebugDrawer());
        debugDrawer.setDebugMode(mode);
    }
}
