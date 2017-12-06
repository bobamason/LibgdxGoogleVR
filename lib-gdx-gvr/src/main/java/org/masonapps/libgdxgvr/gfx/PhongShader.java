package org.masonapps.libgdxgvr.gfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;

import org.masonapps.libgdxgvr.vr.VrGraphics;

/**
 * Created by Bob on 3/8/2017.
 */

public class PhongShader extends DefaultShader {

    public final static Setter ambientLightSetter = new LocalSetter() {
        @Override
        public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
            if (combinedAttributes.has(ColorAttribute.AmbientLight)) {
                final Color color = ((ColorAttribute) combinedAttributes.get(ColorAttribute.AmbientLight)).color;
                shader.set(inputID, color.r, color.g, color.b);
            }
            VrGraphics.checkGlError("set ambient light");
        }
    };

    public final static Setter ambientColorSetter = new LocalSetter() {
        @Override
        public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
            if (combinedAttributes.has(ColorAttribute.Ambient))
                shader.set(inputID, ((ColorAttribute) combinedAttributes.get(ColorAttribute.Ambient)).color);
            VrGraphics.checkGlError("set ambient color");
        }
    };
    private final static Uniform ambientLight = new Uniform("u_ambientLight", ColorAttribute.AmbientLight);
    private final static Uniform ambientColor = new Uniform("u_ambientColor", ColorAttribute.Ambient);
    private static String phongVertexShader = null;
    private static String phongFragmentShader = null;
    private final int u_ambientLight;
    private final int u_ambientColor;

    public PhongShader(Renderable renderable) {
        this(renderable, new Config());
    }

    public PhongShader(Renderable renderable, Config config) {
        super(renderable, config, createPrefix(renderable, config) + createAmbientPrefix(renderable), getPhongVertexShader(), getPhongFragmentShader());
        u_ambientLight = register(ambientLight, ambientLightSetter);
        u_ambientColor = register(ambientColor, ambientColorSetter);
    }

    private static String createAmbientPrefix(Renderable renderable) {
        final Attributes attributes = combineAttributes(renderable);
        String prefix = "\n";
        final long attributesMask = attributes.getMask();
        if (renderable.environment != null) {
            if ((attributesMask & ColorAttribute.AmbientLight) == ColorAttribute.AmbientLight)
                prefix += "#define ambientLightFlag\n";
            if ((attributesMask & ColorAttribute.Ambient) == ColorAttribute.Ambient)
                prefix += "#define ambientColorFlag\n";
        }
        return prefix;
    }

    private static Attributes combineAttributes(final Renderable renderable) {
        final Attributes attributes = new Attributes();
        if (renderable.environment != null) attributes.set(renderable.environment);
        if (renderable.material != null) attributes.set(renderable.material);
        return attributes;
    }

    public static String getPhongVertexShader() {
        if (phongVertexShader == null)
            phongVertexShader = Gdx.files.internal("phong.vertex.glsl").readString();
        return phongVertexShader;
    }

    public static String getPhongFragmentShader() {
        if (phongFragmentShader == null)
            phongFragmentShader = Gdx.files.internal("phong.fragment.glsl").readString();
        return phongFragmentShader;
    }

}
