package org.masonapps.libgdxgvr.gfx;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;

/**
 * Created by Bob on 3/8/2017.
 */

public class PhongShaderProvider extends DefaultShaderProvider {

    public PhongShaderProvider() {
        super();
    }

    @Override
    protected Shader createShader(Renderable renderable) {
        return new PhongShader(renderable, config);
    }
}
