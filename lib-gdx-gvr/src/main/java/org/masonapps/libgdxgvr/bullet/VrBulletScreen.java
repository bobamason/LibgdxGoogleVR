package org.masonapps.libgdxgvr.bullet;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;
import com.badlogic.gdx.physics.bullet.collision.btShapeHull;

import org.masonapps.libgdxgvr.gfx.VrGame;
import org.masonapps.libgdxgvr.gfx.VrWorldScreen;
import org.masonapps.libgdxgvr.gfx.World;

/**
 * Created by Bob on 8/11/2015.
 */
public abstract class VrBulletScreen extends VrWorldScreen {
    
    private static final Vector3 tempV = new Vector3();
    private static boolean initialized = false;

    public VrBulletScreen(VrGame game) {
        super(game);
        initializeBullet();
    }

    private static void initializeBullet() {
        if (!initialized) {
            Bullet.init();
            initialized = true;
        }
    }

    public static btConvexHullShape createConvexHullShape(Model model, boolean optimize) {
        final Mesh mesh = model.meshes.get(0);
        final btConvexHullShape shape = new btConvexHullShape(mesh.getVerticesBuffer(), mesh.getNumVertices(), mesh.getVertexSize());
        if (!optimize) return shape;
        // now optimize the shape
        final btShapeHull hull = new btShapeHull(shape);
        hull.buildHull(shape.getMargin());
        final btConvexHullShape result = new btConvexHullShape(hull);
        // delete the temporary shape
        shape.dispose();
        hull.dispose();
        return result;
    }

    public static btConvexHullShape createConvexHullShape(Mesh mesh, boolean optimize) {
        final btConvexHullShape shape = new btConvexHullShape(mesh.getVerticesBuffer(), mesh.getNumVertices(), mesh.getVertexSize());
        if (!optimize) return shape;
        // now optimize the shape
        final btShapeHull hull = new btShapeHull(shape);
        hull.buildHull(shape.getMargin());
        final btConvexHullShape result = new btConvexHullShape(hull);
        // delete the temporary shape
        shape.dispose();
        hull.dispose();
        return result;
    }

    @Override
    public World createWorld() {
        return new BulletWorld();
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void hide() {
        super.hide();
    }

    @Override
    public void onCardboardTrigger() {

    }

    @Override
    public void render(Camera camera, int whichEye) {
        super.render(camera, whichEye);
    }

    public BulletWorld getBulletWorld() {
        return (BulletWorld) world;
    }
}
