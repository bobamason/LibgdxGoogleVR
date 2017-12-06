package org.masonapps.libgdxgvr.math;

import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;

/**
 * Created by Bob on 8/25/2017.
 */

public class PlaneUtils {

    public static Vector2 toSubSpace(Plane plane, Vector3 point) {
        return toSubSpace(plane, point, new Vector2());
    }

    public static Vector2 toSubSpace(Plane plane, Vector3 point, Vector2 out) {
        final Vector3 u = Pools.obtain(Vector3.class);
        final Vector3 v = Pools.obtain(Vector3.class);
        
        u.set(Vector3.Y).crs(plane.normal).nor();
        v.set(plane.normal).crs(u).nor();
        out.x = point.dot(u);
        out.y = point.dot(v);

        Pools.free(u);
        Pools.free(v);
        return out;
    }

    public static Vector3 toSpace(Plane plane, Vector2 point) {
        return toSpace(plane, point, new Vector3());
    }

    public static Vector3 toSpace(Plane plane, Vector2 point, Vector3 out) {
        final Vector3 u = Pools.obtain(Vector3.class);
        final Vector3 v = Pools.obtain(Vector3.class);
        
        u.set(Vector3.Y).crs(plane.normal).nor();
        v.set(plane.normal).crs(u).nor();
        final float a = point.x;
        final float b = point.y;
        final float c = -plane.d;
        out.x = a * u.x + b * v.x + c * plane.normal.x;
        out.y = a * u.y + b * v.y + c * plane.normal.y;
        out.z = a * u.z + b * v.z + c * plane.normal.z;

        Pools.free(u);
        Pools.free(v);
        return out;
    }
}
