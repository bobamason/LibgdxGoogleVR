package org.masonapps.libgdxgvr.input;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;

import org.masonapps.libgdxgvr.GdxVr;

/**
 * Created by Bob on 12/28/2016.
 */

public class VrCursor {

    private static final String UNIFORM_CAMERATRANSFORM = "u_projTrans";
    private static final String UNIFORM_WORLDTRANSFORM = "u_worldTrans";
    private static final String UNIFORM_COLOR = "u_color";
    private static final String UNIFORM_ACTIVATION = "u_activation";
    private static final String UNIFORM_MIN_RADIUS = "u_minRadius";
    private static final String UNIFORM_MAX_RADIUS = "u_maxRadius";
    private static String vertexShader = null;
    private static String fragmentShader = null;
    public final Quaternion rotation = new Quaternion();
    public final Vector3 position = new Vector3();
    private final Mesh mesh;
    private Matrix4 transform = new Matrix4();
    private ShaderProgram shader;
    private float deactivatedDiameter = 0.05f;
    private float activatedDiameter = 0.125f;
    private float activation = 0f;
    private Color color = Color.WHITE;
    private boolean visible = true;

    public VrCursor() {
        mesh = new Mesh(true, 4, 6, VertexAttribute.Position(), VertexAttribute.TexCoords(0));
        mesh.setVertices(new float[]{
                -1, -1, 0, 0, 1,
                1, -1, 0, 1, 1,
                1, 1, 0, 1, 0,
                -1, 1, 0, 0, 0
        });
        mesh.setIndices(new short[]{0, 1, 2, 2, 3, 0});
        shader = new ShaderProgram(getVertexShader(), getFragmentShader());
    }

    public static String getVertexShader() {
        if (vertexShader == null)
            vertexShader = GdxVr.files.internal("cursor.vertex.glsl").readString();
        return vertexShader;
    }

    public static String getFragmentShader() {
        if (fragmentShader == null)
            fragmentShader = GdxVr.files.internal("cursor.fragment.glsl").readString();
        return fragmentShader;
    }

    public void render(Camera camera) {
        if (!visible) return;
        shader.begin();
        shader.setUniformMatrix(UNIFORM_CAMERATRANSFORM, camera.combined);
        transform.idt();
        transform.translate(position);
        transform.rotate(rotation);
        shader.setUniformMatrix(UNIFORM_WORLDTRANSFORM, transform);
        shader.setUniformf(UNIFORM_COLOR, color);
        shader.setUniformf(UNIFORM_MIN_RADIUS, deactivatedDiameter * 0.5f);
        shader.setUniformf(UNIFORM_MAX_RADIUS, activatedDiameter * 0.5f);
        shader.setUniformf(UNIFORM_ACTIVATION, activation);
        mesh.render(shader, GL20.GL_TRIANGLES);
        shader.end();
    }

    public float getActivatedDiameter() {
        return activatedDiameter;
    }

    public void setActivatedDiameter(float activatedDiameter) {
        this.activatedDiameter = activatedDiameter;
    }

    public float getDeactivatedDiameter() {
        return deactivatedDiameter;
    }

    public void setDeactivatedDiameter(float deactivatedDiameter) {
        this.deactivatedDiameter = deactivatedDiameter;
    }

    public float getActivation() {
        return activation;
    }

    /**
     * 0.0 deactivated to 1.0 fully activated
     *
     * @param activation
     */
    public void setActivation(float activation) {
        this.activation = activation;
    }

    public void lookAtTarget(Vector3 target, Vector3 up) {
        final Vector3 temp = Pools.obtain(Vector3.class);
        lookAt(temp.set(target).sub(this.position), up);
        Pools.free(temp);
    }

    public void lookAt(Vector3 direction, Vector3 up) {
        final Vector3 temp = Pools.obtain(Vector3.class);
        final Vector3 temp2 = Pools.obtain(Vector3.class);
        temp.set(up).crs(direction).nor();
        temp2.set(direction).crs(temp).nor();
        rotation.setFromAxes(temp.x, temp2.x, direction.x, temp.y, temp2.y, direction.y, temp.z, temp2.z, direction.z);
        Pools.free(temp);
        Pools.free(temp2);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color.set(color);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
