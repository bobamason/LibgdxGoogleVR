package org.masonapps.libgdxgvr.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Created by Bob on 3/31/2017.
 */

public class ImageButtonVR extends VirtualStage {

    protected final ImageButton imageButton;

    public ImageButtonVR(Batch batch, Skin skin) {
        this(batch, skin.get(ImageButton.ImageButtonStyle.class));
    }

    public ImageButtonVR(Batch batch, Skin skin, String styleName) {
        this(batch, skin.get(styleName, ImageButton.ImageButtonStyle.class));
    }

    public ImageButtonVR(Batch batch, Drawable imageUp) {
        this(batch, new ImageButton.ImageButtonStyle(null, null, null, imageUp, null, null));
    }

    public ImageButtonVR(Batch batch, Drawable imageUp, Drawable imageDown) {
        this(batch, new ImageButton.ImageButtonStyle(null, null, null, imageUp, imageDown, null));
    }

    public ImageButtonVR(Batch batch, Drawable imageUp, Drawable imageDown, Drawable imageChecked) {
        this(batch, new ImageButton.ImageButtonStyle(null, null, null, imageUp, imageDown, imageChecked));
    }

    public ImageButtonVR(Batch batch, ImageButton.ImageButtonStyle imageButtonStyle) {
        super(batch, 100, 100);
        imageButton = new ImageButton(imageButtonStyle);
        addActor(imageButton);
        getViewport().update((int) imageButton.getWidth(), (int) imageButton.getHeight(), false);
        invalidate();
    }

    public ImageButton getImageButton() {
        return imageButton;
    }

    public ImageButton.ImageButtonStyle getStyle () {
        return imageButton.getStyle();
    }

    public void setStyle(Button.ButtonStyle style) {
        imageButton.setStyle(style);
    }
    
    public Image getImage () {
        return  imageButton.getImage();
    }

    public Cell getImageCell () {
        return imageButton.getImageCell();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public boolean addListener(EventListener listener) {
        return imageButton.addListener(listener);
    }
}
