package org.masonapps.libgdxgvr.ui;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

/**
 * Created by Bob Mason on 10/4/2017.
 */

public class WindowVR extends VirtualStage {
    @Nullable
    private OnDragStartedListener listener = null;
    private Table titleTable;
    private Label titleLabel;
    private float titleBarHeight;

    public WindowVR(Batch batch, int virtualPixelWidth, int virtualPixelHeight, WindowVrStyle windowStyle) {
        this(batch, virtualPixelWidth, virtualPixelHeight, "", windowStyle);
    }

    public WindowVR(Batch batch, int virtualPixelWidth, int virtualPixelHeight, String title, WindowVrStyle windowStyle) {
        super(batch, virtualPixelWidth, virtualPixelHeight);
        init(windowStyle);
        setTitle(title);
    }

    private void init(WindowVrStyle windowStyle) {
        setBackground(windowStyle.background);
        titleTable = new Table();
        titleTable.setBackground(windowStyle.titleBackground);
        titleTable.setTouchable(Touchable.enabled);
        titleLabel = new Label("", new Label.LabelStyle(windowStyle.titleFont, windowStyle.fontColor));
        titleLabel.setEllipsis(true);
        titleTable.add(titleLabel).pad(8f).expandX().fillX().minWidth(0);
        titleTable.setFillParent(false);
        titleTable.layout();

        titleBarHeight = titleLabel.getPrefHeight();
        titleTable.setSize(getWidth(), titleBarHeight);
        titleTable.setPosition(0, getHeight(), Align.bottomLeft);
        titleTable.addListener(new InputListener() {

            @SuppressWarnings("ConstantConditions")
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (listener != null)
                    listener.onDragStarted(WindowVR.this);
                return true;
            }
        });
        addActor(titleTable);
    }

    @Override
    public void recalculateTransform() {
        super.recalculateTransform();
        bounds.set(0, 0, getViewport().getCamera().viewportWidth * pixelSizeWorld * scale.x, (getViewport().getCamera().viewportHeight + titleBarHeight) * pixelSizeWorld * scale.y);
        titleTable.setWidth(getWidth());
        titleTable.setPosition(0, getHeight(), Align.bottomLeft);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public float getTitleBarHeight() {
        return titleBarHeight;
    }

    @Override
    protected void drawBackground(Batch batch) {
        if (background != null)
            background.draw(batch, 0, 0, getWidth(), getHeight() + titleBarHeight);
    }

    protected void setOnDragStartedListener(@Nullable OnDragStartedListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (listener != null)
            listener.moveToFront(this);
        return super.touchDown(screenX, screenY, pointer, button);
    }

    protected interface OnDragStartedListener {
        void onDragStarted(@NonNull WindowVR windowVR);

        void moveToFront(@NonNull WindowVR windowVR);
    }

    public static class WindowVrStyle {
        @Nullable
        public Drawable background = null;
        @Nullable
        public Drawable titleBackground = null;

        public BitmapFont titleFont;
        public Color fontColor = new Color(Color.WHITE);

        public WindowVrStyle(@Nullable Drawable background, @Nullable Drawable titleBackground, @NonNull BitmapFont titleFont, Color fontColor) {
            this.background = background;
            this.titleBackground = titleBackground;
            this.titleFont = titleFont;
            if (fontColor != null)
                this.fontColor.set(fontColor);
        }
    }
}
