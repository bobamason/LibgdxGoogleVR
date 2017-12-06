package org.masonapps.libgdxgvr.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

import org.masonapps.libgdxgvr.GdxVr;

/**
 * Created by Bob on 3/31/2017.
 */

public class LoadingSpinnerVR extends VirtualStage {

    protected final Image loadingSpinner;
    /**
     * degrees per second
     */
    private float speed = 360f;

    public LoadingSpinnerVR(Batch batch, Drawable loadingSpinnerDrawable) {
        super(batch, 100, 100);
        loadingSpinner = new Image(loadingSpinnerDrawable);
        addActor(loadingSpinner);
        setTouchable(false);
        loadingSpinner.setOrigin(Align.center);
        setSize((int) loadingSpinner.getWidth(), (int) loadingSpinner.getHeight());
        loadingSpinner.setPosition(getWidth() / 2f, getHeight() / 2f, Align.center);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (isVisible())
            loadingSpinner.rotateBy(-speed * GdxVr.graphics.getDeltaTime());
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
