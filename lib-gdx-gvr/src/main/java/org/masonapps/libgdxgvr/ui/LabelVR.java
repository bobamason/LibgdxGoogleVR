package org.masonapps.libgdxgvr.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.StringBuilder;

/**
 * Created by Bob on 3/31/2017.
 */

public class LabelVR extends VirtualStage {

    protected final Label label;

    public LabelVR(CharSequence text, Batch batch, Skin skin) {
        this(text, batch, skin.get(Label.LabelStyle.class));
    }

    public LabelVR(CharSequence text, Batch batch, Skin skin, String fontName, Color color) {
        this(text, batch, new Label.LabelStyle(skin.getFont(fontName), color));
    }

    public LabelVR(CharSequence text, Batch batch, Skin skin, String fontName, String colorName) {
        this(text, batch, new Label.LabelStyle(skin.getFont(fontName), skin.getColor(colorName)));
    }

    public LabelVR(CharSequence text, Batch batch, Skin skin, String styleName) {
        this(text, batch, skin.get(styleName, Label.LabelStyle.class));
    }
    
    public LabelVR(CharSequence text, Batch batch, Label.LabelStyle labelStyle) {
        super(batch, 100, 100);
        setTouchable(false);
        label = new Label(text, labelStyle);
        addActor(label);
        getViewport().update((int) label.getWidth(), (int) label.getHeight(), false);
        invalidate();
    }

    public Label getLabel() {
        return label;
    }

    public Label.LabelStyle getStyle () {
        return label.getStyle();
    }

    public void setStyle(Label.LabelStyle style) {
        label.setStyle(style);
    }

    public StringBuilder getText () {
        return label.getText();
    }

    public void setText(CharSequence newText) {
        label.setText(newText);
    }

    public GlyphLayout getGlyphLayout () {
        return label.getGlyphLayout();
    }

    public int getLabelAlign () {
        return label.getLabelAlign();
    }

    public int getLineAlign () {
        return label.getLineAlign();
    }
    
    public boolean textEquals (CharSequence other) {
        return label.textEquals(other);
    }
    public void setWrap (boolean wrap) {
        label.setWrap(wrap);
    }
    
    public void setAlignment (int alignment) {
        label.setAlignment(alignment);
    }
    public void setAlignment (int labelAlign, int lineAlign) {
        label.setAlignment(labelAlign, lineAlign);
    }
    
    public void setEllipsis (String ellipsis) {
        label.setEllipsis(ellipsis);
    }
    
    public void setEllipsis (boolean ellipsis) {
        label.setEllipsis(ellipsis);
    }
}
