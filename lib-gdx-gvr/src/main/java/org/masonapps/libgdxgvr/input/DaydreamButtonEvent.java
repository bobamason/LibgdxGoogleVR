package org.masonapps.libgdxgvr.input;

import com.badlogic.gdx.utils.Pool;

/**
 * Created by Bob on 1/10/2017.
 */

public class DaydreamButtonEvent implements Pool.Poolable {
    public static final int ACTION_DOWN = 0;
    public static final int ACTION_UP = 1;
    public static final int BUTTON_TOUCHPAD = 2;
    public static final int BUTTON_APP = 3;
    public static final int BUTTON_VOLUME_UP = 4;
    public static final int BUTTON_VOLUME_DOWN = 5;

    public int action;
    public int button;

    @Override
    public void reset() {
        action = -1;
        button = -1;
    }

    @Override
    public String toString() {
        String str = "action: ";
        switch (action) {
            case ACTION_DOWN:
                str += "DOWN";
                break;
            case ACTION_UP:
                str += "UP";
                break;
            default:
                str += "NONE";
        }
        str += ", button: ";
        switch (button) {
            case BUTTON_APP:
                str += "APP";
                break;
            case BUTTON_TOUCHPAD:
                str += "TOUCHPAD";
                break;
            case BUTTON_VOLUME_DOWN:
                str += "VOLUME DOWN";
                break;
            case BUTTON_VOLUME_UP:
                str += "VOLUME UP";
                break;
            default:
                str += "NONE";
        }
        return str;
    }
}
