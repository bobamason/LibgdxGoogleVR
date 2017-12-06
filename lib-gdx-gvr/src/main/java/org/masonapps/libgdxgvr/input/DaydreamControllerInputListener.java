package org.masonapps.libgdxgvr.input;

import com.google.vr.sdk.controller.Controller;

/**
 * Created by Bob on 1/9/2017.
 */

public interface DaydreamControllerInputListener {

    void onDaydreamControllerUpdate(Controller controller, int connectionState);

    void onControllerButtonEvent(Controller controller, DaydreamButtonEvent event);

    void onControllerTouchPadEvent(Controller controller, DaydreamTouchEvent event);

    void onControllerConnectionStateChange(int connectionState);
}
