package org.masonapps.libgdxgvr.input;

import com.badlogic.gdx.utils.Pool;

/**
 * Created by Bob on 1/10/2017.
 */

public class DaydreamTouchEvent implements Pool.Poolable {
    public static final int ACTION_DOWN = 0;
    public static final int ACTION_MOVE = 1;
    public static final int ACTION_UP = 2;

    public int action = -1;
    public float x = 0f;
    public float y = 0f;

    @Override
    public void reset() {
        action = -1;
        x = 0f;
        y = 0f;
    }
}
