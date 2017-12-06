package org.masonapps.libgdxgvr.input;

import android.support.annotation.Nullable;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

/**
 * Created by Bob on 1/9/2017.
 */

public interface VrInputProcessor extends InputProcessor {

    /**
     * @return test if ray hits whatever 2D or 3D shape
     */
    boolean performRayTest(Ray ray);

    /**
     * @return the stored result of the ray test to avoid repeating the expensive math operations
     */
    boolean isCursorOver();

    /**
     * @return 2D point on a plane for Scene2d ui elements like buttons, return null if this is not applicable
     */
    @Nullable
    Vector2 getHitPoint2D();

    /**
     * @return 3D hit point for the cursor to be drawn at, return null if this is not applicable
     */
    @Nullable
    Vector3 getHitPoint3D();


    /**
     * @return a hint to be displayed under the cursor, return null if this is not applicable
     */
//    @Nullable
//    String getToolTipHint();
}
