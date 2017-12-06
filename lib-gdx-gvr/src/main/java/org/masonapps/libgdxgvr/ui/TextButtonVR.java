package org.masonapps.libgdxgvr.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

/**
 * Created by Bob on 3/31/2017.
 */

public class TextButtonVR extends VirtualStage {

    protected final TextButton textButton;

    public TextButtonVR(Batch batch, String text, Skin skin) {
        this(batch, text, skin.get(TextButton.TextButtonStyle.class));
    }
    
    public TextButtonVR(Batch batch, String text, Skin skin, String styleName) {
        this(batch, text, skin.get(styleName, TextButton.TextButtonStyle.class));
    }
    
    public TextButtonVR(Batch batch, String text, TextButton.TextButtonStyle style) {
        super(batch, 100, 100);
        setTouchable(true);
        textButton = new TextButton(text, style);
        addActor(textButton);
        getViewport().update((int) textButton.getWidth(), (int) textButton.getHeight(), false);
        invalidate();
    }

    public TextButton getTextButton() {
        return textButton;
    }

    public TextButton.TextButtonStyle getStyle () {
        return textButton.getStyle();
    }

    public void setStyle(Button.ButtonStyle style) {
        textButton.setStyle(style);
    }

    public Label getLabel () {
        return textButton.getLabel();
    }

    public Cell getLabelCell () {
        return textButton.getLabelCell();
    }

    public CharSequence getText () {
        return textButton.getText();
    }

    public void setText(String text) {
        textButton.setText(text);
    }

    @Override
    public boolean addListener(EventListener listener) {
        return textButton.addListener(listener);
    }
}
