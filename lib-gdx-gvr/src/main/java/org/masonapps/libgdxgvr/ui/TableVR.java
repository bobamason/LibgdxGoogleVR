package org.masonapps.libgdxgvr.ui;

import android.util.Log;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/**
 * Created by Bob on 3/31/2017.
 */

public class TableVR extends VirtualStage {

    protected final Table table;

    public TableVR(Batch batch, int tableWidth, int tableHeight) {
        this(batch, null, tableWidth, tableHeight);
    }

    public TableVR(Batch batch, Skin skin, int tableWidth, int tableHeight) {
        super(batch, tableWidth, tableHeight);
        table = new Table(skin);
        table.setFillParent(true);
        addActor(table);
        setActivationMovement(0);
    }

    @Override
    public void setSize(int virtualPixelWidth, int virtualPixelHeight) {
        super.setSize(virtualPixelWidth, virtualPixelHeight);
        if (table != null)
            table.invalidate();
    }

    public Table getTable() {
        return table;
    }

    public void resizeToFitTable() {
        table.setFillParent(false);
        table.layout();
        final int w = Math.round(table.getPrefWidth());
        final int h = Math.round(table.getPrefHeight());
        Log.d(TableVR.class.getSimpleName(), "size: " + w + " x " + h);
        table.setFillParent(true);
        setSize(w, h);
    }

    @Override
    public void setTouchable(boolean touchable) {
        super.setTouchable(touchable);
        table.setTouchable(touchable ? Touchable.enabled : Touchable.childrenOnly);
    }
}
